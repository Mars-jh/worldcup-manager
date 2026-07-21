package com.worldcup.service;

import com.worldcup.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据初始化器 - 应用启动时自动填充：
 * 1. 3个默认用户 (admin/operator/viewer)
 * 2. 32支世界杯参赛球队（分配到8个小组）
 * 3. 每支球队23名球员（随机生成）
 * 4. 自动生成小组赛赛程（48场）
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final GroupStageService groupStageService;
    private final Random random = new Random(42); // 固定种子，保证每次启动数据一致

    public DataInitializer(UserService userService, TeamService teamService,
                           PlayerService playerService, MatchService matchService,
                           GroupStageService groupStageService) {
        this.userService = userService;
        this.teamService = teamService;
        this.playerService = playerService;
        this.matchService = matchService;
        this.groupStageService = groupStageService;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> 正在初始化世界杯数据...");
        initUsers();
        initTeams();
        initPlayers();
        initGroupSchedule();
        System.out.println(">>> 数据初始化完成！共 " +
                teamService.findAll().size() + " 支球队, " +
                playerService.findAll().size() + " 名球员, " +
                matchService.findAll().size() + " 场比赛");
    }

    private void initUsers() {
        userService.register("admin", "admin123", "admin@worldcup.com", Role.ADMIN);
        userService.register("operator", "123456", "operator@worldcup.com", Role.OPERATOR);
        userService.register("viewer", "viewer123", "viewer@worldcup.com", Role.VIEWER);
    }

    private void initTeams() {
        // 32支球队数据：{名称, 三字母代码, 大洲, 小组, 国旗emoji, 世界排名}
        Object[][] teamData = {
            // A组
            {"卡塔尔", "QAT", Continent.ASIA, "A", "\uD83C\uDDF6\uD83C\uDDE6", 35},
            {"厄瓜多尔", "ECU", Continent.SOUTH_AMERICA, "A", "\uD83C\uDDEA\uD83C\uDDE8", 44},
            {"塞内加尔", "SEN", Continent.AFRICA, "A", "\uD83C\uDDF8\uD83C\uDDF3", 18},
            {"荷兰", "NED", Continent.EUROPE, "A", "\uD83C\uDDF3\uD83C\uDDF1", 8},
            // B组
            {"英格兰", "ENG", Continent.EUROPE, "B", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F", 5},
            {"伊朗", "IRN", Continent.ASIA, "B", "\uD83C\uDDEE\uD83C\uDDF7", 20},
            {"美国", "USA", Continent.NORTH_AMERICA, "B", "\uD83C\uDDFA\uD83C\uDDF8", 16},
            {"威尔士", "WAL", Continent.EUROPE, "B", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC77\uDB40\uDC6C\uDB40\uDC73\uDB40\uDC7F", 19},
            // C组
            {"阿根廷", "ARG", Continent.SOUTH_AMERICA, "C", "\uD83C\uDDE6\uD83C\uDDF7", 3},
            {"沙特", "KSA", Continent.ASIA, "C", "\uD83C\uDDF8\uD83C\uDDE6", 51},
            {"墨西哥", "MEX", Continent.NORTH_AMERICA, "C", "\uD83C\uDDF2\uD83C\uDDFD", 13},
            {"波兰", "POL", Continent.EUROPE, "C", "\uD83C\uDDF5\uD83C\uDDF1", 26},
            // D组
            {"法国", "FRA", Continent.EUROPE, "D", "\uD83C\uDDEB\uD83C\uDDF7", 4},
            {"澳大利亚", "AUS", Continent.OCEANIA, "D", "\uD83C\uDDE6\uD83C\uDDFA", 38},
            {"丹麦", "DEN", Continent.EUROPE, "D", "\uD83C\uDDE9\uD83C\uDDF0", 10},
            {"突尼斯", "TUN", Continent.AFRICA, "D", "\uD83C\uDDF9\uD83C\uDDF3", 30},
            // E组
            {"西班牙", "ESP", Continent.EUROPE, "E", "\uD83C\uDDEA\uD83C\uDDF8", 7},
            {"哥斯达黎加", "CRC", Continent.NORTH_AMERICA, "E", "\uD83C\uDDE8\uD83C\uDDF7", 31},
            {"德国", "GER", Continent.EUROPE, "E", "\uD83C\uDDE9\uD83C\uDDEA", 11},
            {"日本", "JPN", Continent.ASIA, "E", "\uD83C\uDDEF\uD83C\uDDF5", 24},
            // F组
            {"比利时", "BEL", Continent.EUROPE, "F", "\uD83C\uDDE7\uD83C\uDDEA", 2},
            {"加拿大", "CAN", Continent.NORTH_AMERICA, "F", "\uD83C\uDDE8\uD83C\uDDE6", 41},
            {"摩洛哥", "MAR", Continent.AFRICA, "F", "\uD83C\uDDF2\uD83C\uDDE6", 22},
            {"克罗地亚", "CRO", Continent.EUROPE, "F", "\uD83C\uDDED\uD83C\uDDF7", 12},
            // G组
            {"巴西", "BRA", Continent.SOUTH_AMERICA, "G", "\uD83C\uDDE7\uD83C\uDDF7", 1},
            {"塞尔维亚", "SRB", Continent.EUROPE, "G", "\uD83C\uDDF7\uD83C\uDDF8", 21},
            {"瑞士", "SUI", Continent.EUROPE, "G", "\uD83C\uDDE8\uD83C\uDDED", 15},
            {"喀麦隆", "CMR", Continent.AFRICA, "G", "\uD83C\uDDE8\uD83C\uDDF2", 43},
            // H组
            {"葡萄牙", "POR", Continent.EUROPE, "H", "\uD83C\uDDF5\uD83C\uDDF9", 9},
            {"加纳", "GHA", Continent.AFRICA, "H", "\uD83C\uDDEC\uD83C\uDDED", 61},
            {"乌拉圭", "URU", Continent.SOUTH_AMERICA, "H", "\uD83C\uDDFA\uD83C\uDDFE", 14},
            {"韩国", "KOR", Continent.ASIA, "H", "\uD83C\uDDF0\uD83C\uDDF7", 28},
        };

        for (Object[] td : teamData) {
            Team team = Team.builder()
                    .name((String) td[0])
                    .code((String) td[1])
                    .continent((Continent) td[2])
                    .groupLetter((String) td[3])
                    .flagEmoji((String) td[4])
                    .worldRanking((int) td[5])
                    .coach(generateCoachName((String) td[0]))
                    .build();
            teamService.create(team);
        }
    }

    private void initPlayers() {
        List<Team> teams = teamService.findAll();
        // 随机姓名库（按大洲区分）
        String[][] namesByContinent = {
            // 欧洲姓氏/名字
            {"Liam", "Noah", "Oliver", "James", "Lucas", "Mason", "Ethan", "Leo", "Alex", "Max",
             "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Moore", "Taylor",
             "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Clark", "Lewis", "Walker", "Hall"},
            // 南美洲
            {"Carlos", "Diego", "Luis", "Juan", "Pedro", "Rafael", "Marco", "Sergio", "Pablo", "Andres",
             "Silva", "Santos", "Rodriguez", "Garcia", "Martinez", "Lopez", "Gonzalez", "Perez", "Fernandez", "Torres",
             "Romero", "Diaz", "Cruz", "Morales", "Reyes", "Ortiz", "Gutierrez", "Chavez", "Ramos", "Vargas"},
            // 亚洲
            {"Wei", "Jun", "Ming", "Hao", "Yang", "Lei", "Tao", "Feng", "Yuki", "Ken",
             "Zhang", "Wang", "Li", "Chen", "Liu", "Huang", "Wu", "Zhou", "Xu", "Sun",
             "Kim", "Park", "Tanaka", "Suzuki", "Sato", "Lee", "Choi", "Nakamura", "Yamada", "Zheng"},
            // 非洲
            {"Mohamed", "Ahmed", "Ali", "Omar", "Hassan", "Ibrahim", "Youssef", "Khalid", "Amr", "Tarek",
             "Diallo", "Traore", "Keita", "Coulibaly", "Sissoko", "Diop", "Ndiaye", "Fall", "Ba", "Sow",
             "Mensah", "Owusu", "Agyemang", "Asante", "Boateng", "Adjei", "Appiah", "Sarfo", "Amponsah", "Danso"}
        };

        for (Team team : teams) {
            int nameSet = 0; // 默认欧洲
            switch (team.getContinent()) {
                case SOUTH_AMERICA: nameSet = 1; break;
                case ASIA: nameSet = 2; break;
                case AFRICA: nameSet = 3; break;
                case NORTH_AMERICA: nameSet = random.nextBoolean() ? 0 : 1; break;
                default: nameSet = 0;
            }

            // 生成23人名单：3门将 + 8后卫 + 7中场 + 5前锋
            Position[] positions = new Position[23];
            int idx = 0;
            for (int i = 0; i < 3; i++) positions[idx++] = Position.GK;
            for (int i = 0; i < 8; i++) positions[idx++] = Position.DF;
            for (int i = 0; i < 7; i++) positions[idx++] = Position.MF;
            for (int i = 0; i < 5; i++) positions[idx++] = Position.FW;

            Set<Integer> usedNumbers = new HashSet<>();
            for (int i = 0; i < 23; i++) {
                int number;
                do { number = random.nextInt(99) + 1; } while (usedNumbers.contains(number));
                usedNumbers.add(number);

                String firstName = namesByContinent[nameSet][random.nextInt(10)];
                String lastName = namesByContinent[nameSet][10 + random.nextInt(20)];

                int baseRating = switch (positions[i]) {
                    case GK -> 70 + random.nextInt(20);
                    case DF -> 65 + random.nextInt(25);
                    case MF -> 68 + random.nextInt(25);
                    case FW -> 70 + random.nextInt(25);
                };
                // 明星球员加成（每队1-2个85+的球员）
                if (i < 2) baseRating = Math.max(baseRating, 82 + random.nextInt(15));

                Player player = Player.builder()
                        .name(firstName + " " + lastName)
                        .teamId(team.getId())
                        .position(positions[i])
                        .jerseyNumber(number)
                        .age(20 + random.nextInt(15))
                        .height(170 + random.nextInt(25))
                        .weight(65 + random.nextInt(25))
                        .rating(Math.min(99, baseRating))
                        .goals(0)
                        .assists(0)
                        .build();
                playerService.create(player);
            }
        }
    }

    private void initGroupSchedule() {
        for (String group : groupStageService.getAllGroups()) {
            groupStageService.generateGroupSchedule(group);
        }
    }

    /** 为主教练生成名字 */
    private String generateCoachName(String country) {
        return "Coach_" + country;
    }
}