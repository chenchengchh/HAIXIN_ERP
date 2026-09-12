export interface VideoItem {
  title: string
  author: string
  videoUrl: string
  crawledTime: string
}

export interface Customer {
  id: string
  nickname: string
  commentContent: string
  matchKeyword: string
  createTime: string
  status: 'pending' | 'sent' | 'failed' | 'converted'
}

export interface LogItem {
  time: string
  type: 'info' | 'success' | 'warning' | 'error'
  message: string
}

export interface MessageConfig {
  template: string
  maxCount: number
  interval: [number, number]
}

export interface FilterConfig {
  intentKeywords: string[]
  excludeKeywords: string[]
}

export interface DouyinTask {
  id: string
  name: string
  status: 'running' | 'stopped' | 'completed' | 'error'
  searchKeyword: string
  filterConfig: FilterConfig
  messageConfig: MessageConfig
  stats: {
    videosFound: number
    customersFound: number
    messagesSent: number
  }
  lastRunTime?: string
  logs: LogItem[]
  customers: Customer[] // Runtime data
  videos: VideoItem[] // Runtime data
}
