import router from '../router'

let isRedirectingToLogin = false

export const redirectToLoginOnce = async (options?: { redirect?: string; replace?: boolean }) => {
  if (isRedirectingToLogin) return
  isRedirectingToLogin = true

  try {
    const current = router.currentRoute?.value
    if (current?.path === '/login') return

    const redirect = options?.redirect || current?.fullPath || '/'
    if (options?.replace === false) {
      await router.push({ path: '/login', query: { redirect } })
      return
    }
    await router.replace({ path: '/login', query: { redirect } })
  } finally {
    window.setTimeout(() => {
      isRedirectingToLogin = false
    }, 1000)
  }
}

