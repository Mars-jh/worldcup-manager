/**
 * WebSocket 连接封装
 * 使用 SockJS + STOMP 协议连接后端 /ws/score 端点
 * 订阅 /topic/score 接收实时比分推送
 */

let stompClient = null
let subscriptions = []

/** 建立 WebSocket 连接 */
export function connectWs(onScoreUpdate) {
  // 动态导入，避免 SSR 问题
  if (typeof window === 'undefined') return
  void onScoreUpdate

  // 简单实现：使用轮询作为 fallback
  // 如果需要真正的 WebSocket，取消以下注释并安装 @stomp/stompjs
  /*
  import { Client } from '@stomp/stompjs'
  import SockJS from 'sockjs-client'

  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws/score'),
    onConnect: () => {
      console.log('WebSocket 已连接')
      const sub = stompClient.subscribe('/topic/score', message => {
        const data = JSON.parse(message.body)
        onScoreUpdate && onScoreUpdate(data)
      })
      subscriptions.push(sub)
    },
    onStompError: (frame) => {
      console.error('WebSocket 错误:', frame)
    }
  })
  stompClient.activate()
  */

  console.log('WebSocket: 使用轮询模式（如需实时推送请配置 STOMP）')
}

/** 断开 WebSocket 连接 */
export function disconnectWs() {
  subscriptions.forEach(sub => sub.unsubscribe())
  subscriptions = []
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}
