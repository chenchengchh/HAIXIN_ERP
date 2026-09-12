import type { Plugin } from 'vite'
import { randomUUID } from 'crypto'

const json = (res: any, statusCode: number, body: any) => {
  res.statusCode = statusCode
  res.setHeader('Content-Type', 'application/json; charset=utf-8')
  res.setHeader('Cache-Control', 'no-store')
  res.end(JSON.stringify(body))
}

const ok = (data: any, message = '成功') => ({
  code: 0,
  message,
  data
})

const page = (list: any[] = [], pageNum = 1, size = 10) => ({
  records: list,
  list,
  total: list.length,
  page: pageNum,
  size
})

const parseQuery = (url: string) => {
  const idx = url.indexOf('?')
  if (idx === -1) return new URLSearchParams()
  return new URLSearchParams(url.slice(idx + 1))
}

const getPathname = (url: string) => {
  const q = url.indexOf('?')
  return q === -1 ? url : url.slice(0, q)
}

export function createMockApiPlugin(options?: { enabled?: boolean }): Plugin {
  const enabled = options?.enabled ?? true

  const issuedTokens = new Set<string>()
  const prefixes = ['/api', '/mes', '/erp', '/login', '/logout', '/user']

  return {
    name: 'hxcoe-mock-api',
    apply: 'serve',
    enforce: 'pre',
    configureServer(server) {
      if (!enabled) return

      server.middlewares.use(async (req, res, next) => {
        const url = req.url || '/'
        const pathname = getPathname(url)
        if (!prefixes.some((p) => pathname === p || pathname.startsWith(`${p}/`))) return next()

        if (
          (
            pathname === '/api/login' ||
            pathname === '/api/v1/iam/auth/login' ||
            pathname === '/iam/auth/login' ||
            pathname === '/login'
          ) &&
          req.method === 'POST'
        ) {
          const token = randomUUID()
          issuedTokens.add(token)
          return json(
            res,
            200,
            ok({
              token,
              tokenType: 'Bearer',
              userId: 1,
              username: 'admin',
              employeeId: 1,
              employee: { id: 1, name: '管理员', realName: '管理员' },
              role: 'ADMIN',
              roles: ['ADMIN'],
              permissions: ['*']
            })
          )
        }

        if (pathname === '/api/logout' || pathname === '/logout') {
          return json(res, 200, ok(true))
        }

        if (pathname === '/api/user/info' || pathname === '/user/info') {
          return json(
            res,
            200,
            ok({
              id: 1,
              username: 'admin',
              name: '管理员',
              roles: ['ADMIN'],
              permissions: ['*']
            })
          )
        }

        const query = parseQuery(url)
        const p = Number(query.get('page') || query.get('currentPage') || 1)
        const s = Number(query.get('size') || query.get('pageSize') || 10)

        if (req.method === 'GET') {
          if (pathname.endsWith('/tree') || pathname.includes('/organization')) {
            return json(
              res,
              200,
              ok([
                { id: 1, name: '默认组织', code: 'ORG', children: [{ id: 2, name: '默认部门', code: 'DEPT' }] }
              ])
            )
          }
          return json(res, 200, ok(page([], p, s)))
        }

        if (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH') {
          return json(res, 200, ok(true))
        }

        if (req.method === 'DELETE') {
          return json(res, 200, ok(true))
        }

        return next()
      })
    }
  }
}
