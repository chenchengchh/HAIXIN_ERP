export function extractArray(result: any): any[] {
  const data = result?.data
  if (Array.isArray(data?.records)) return data.records
  if (Array.isArray(data?.list)) return data.list
  if (Array.isArray(data)) return data
  return []
}

export function extractTotal(result: any): number {
  const total = result?.data?.total ?? result?.total ?? 0
  const n = typeof total === 'number' ? total : Number(total)
  return Number.isFinite(n) ? n : 0
}
