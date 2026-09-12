export const getWsBaseUrl = () => {
  const configured = String(import.meta.env.VITE_WS_BASE_URL || '').trim()
  if (configured) return configured.replace(/\/+$/, '')
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws`
}

export const buildWsUrl = (path: string) => {
  const base = getWsBaseUrl()
  const cleanPath = String(path || '').trim()
  if (!cleanPath) return base
  if (cleanPath.startsWith('/')) return `${base}${cleanPath}`
  return `${base}/${cleanPath}`
}

export const getHttpBaseUrl = () => {
  const configured = String(import.meta.env.VITE_API_BASE_URL || '').trim()
  if (configured) return configured.replace(/\/+$/, '')
  return window.location.origin
}

export const buildHttpUrl = (path: string) => {
  const base = getHttpBaseUrl()
  const cleanPath = String(path || '').trim()
  if (!cleanPath) return base
  if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://')) return cleanPath
  if (cleanPath.startsWith('/')) return `${base}${cleanPath}`
  return `${base}/${cleanPath}`
}

