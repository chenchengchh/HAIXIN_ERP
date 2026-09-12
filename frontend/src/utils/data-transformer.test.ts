import { describe, expect, it } from 'vitest'
import { DataTransformer } from './data-transformer'

describe('DataTransformer.normalizeResponse', () => {
  it('should unwrap axios-like response objects', () => {
    const axiosLike = {
      data: { code: 0, message: '成功', data: { a: 1 } },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {}
    }
    const normalized = DataTransformer.normalizeResponse(axiosLike as any)
    expect(normalized.code).toBe(200)
    expect(normalized.data).toEqual({ a: 1 })
  })
})

