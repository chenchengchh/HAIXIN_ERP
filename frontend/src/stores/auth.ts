import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'
import { DataTransformer } from '../utils/data-transformer'

interface UserInfo {
  id?: string | number
  username: string
  name?: string
  avatar?: string
  roles?: string[]
  [key: string]: any
}

const LOGIN_API_PATH = '/api/v1/iam/auth/login'
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const PERMISSIONS_KEY = 'permissions'

const getStoredValue = (key: string) => localStorage.getItem(key) || sessionStorage.getItem(key)

const clearStorageKey = (key: string) => {
  localStorage.removeItem(key)
  sessionStorage.removeItem(key)
}

const parseStoredJson = <T>(key: string, fallback: T): T => {
  const raw = getStoredValue(key)
  if (!raw) return fallback
  try {
    return JSON.parse(raw) as T
  } catch (error) {
    console.error(`Failed to parse ${key}`, error)
    clearStorageKey(key)
    return fallback
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getStoredValue(TOKEN_KEY))
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const hasPermission = (permission: string) => permissions.value.includes(permission)
  const hasAnyPermission = (required: string[]) => required.some((permission) => hasPermission(permission))

  function getToken() {
    return token.value || getStoredValue(TOKEN_KEY)
  }

  function getStorage(remember: boolean) {
    return remember ? localStorage : sessionStorage
  }

  function setToken(newToken: string | null, remember: boolean = false) {
    token.value = newToken
    if (newToken) {
      const storage = getStorage(remember)
      const otherStorage = remember ? sessionStorage : localStorage
      storage.setItem(TOKEN_KEY, newToken)
      otherStorage.removeItem(TOKEN_KEY)
    } else {
      clearStorageKey(TOKEN_KEY)
    }
  }

  function setUserInfo(info: UserInfo | null, remember: boolean = false) {
    userInfo.value = info
    if (info) {
      const storage = getStorage(remember)
      const otherStorage = remember ? sessionStorage : localStorage
      storage.setItem(USER_INFO_KEY, JSON.stringify(info))
      otherStorage.removeItem(USER_INFO_KEY)
    } else {
      clearStorageKey(USER_INFO_KEY)
    }
  }

  function setPermissions(perms: string[], remember: boolean = false) {
    permissions.value = perms
    if (perms.length > 0) {
      const storage = getStorage(remember)
      const otherStorage = remember ? sessionStorage : localStorage
      storage.setItem(PERMISSIONS_KEY, JSON.stringify(perms))
      otherStorage.removeItem(PERMISSIONS_KEY)
    } else {
      clearStorageKey(PERMISSIONS_KEY)
    }
  }

  function initAuth() {
    const storedToken = getStoredValue(TOKEN_KEY)
    if (storedToken) {
      token.value = storedToken
      userInfo.value = parseStoredJson<UserInfo | null>(USER_INFO_KEY, null)
      permissions.value = parseStoredJson<string[]>(PERMISSIONS_KEY, [])
      return
    }
    userInfo.value = null
    permissions.value = []
  }

  async function login(loginForm: any) {
    try {
      const response = await api.post(LOGIN_API_PATH, {
        username: loginForm.username,
        password: loginForm.password
      })
      
      const normalized = DataTransformer.normalizeResponse(response)
      if (!normalized || !DataTransformer.isSuccessCode(normalized.code)) {
        throw new Error(normalized?.msg || '登录失败')
      }
      const payload = normalized?.data || {}
      const accessToken = payload?.token as string | undefined
      if (!accessToken) {
        throw new Error('登录失败：未获取到Token')
      }

      setToken(accessToken, loginForm.rememberMe)
      setUserInfo(
        {
          id: payload?.userId,
          username: payload?.username || loginForm.username,
          name: payload?.employee?.realName || payload?.employee?.name || payload?.username || loginForm.username,
          roles: payload?.roles || (payload?.role ? [payload.role] : []),
          employeeId: payload?.employeeId,
          employee: payload?.employee
        },
        loginForm.rememberMe
      )
      setPermissions(Array.isArray(payload?.permissions) ? payload.permissions : [], loginForm.rememberMe)

      return response
    } catch (error) {
      throw error
    }
  }

  function logout() {
    setToken(null)
    setUserInfo(null)
    setPermissions([])
  }

  return {
    token,
    userInfo,
    permissions,
    isLoggedIn,
    getToken,
    hasPermission,
    hasAnyPermission,
    setToken,
    setUserInfo,
    setPermissions,
    initAuth,
    login,
    logout
  }
})
