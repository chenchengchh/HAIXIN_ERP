/**
 * API响应处理工具
 * 用于统一处理后端返回的各种响应格式
 */
import { DataTransformer } from './data-transformer'

/**
 * 响应数据格式接口
 */
interface ApiResponse<T = any> {
  code: number;
  msg: string;
  data: T;
}

/**
 * 分页响应数据格式接口
 */
interface PageResponse<T = any> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages?: number;
  list?: T[];
  content?: T[];
}

/**
 * 统一处理API响应
 * @param response API响应数据
 * @returns 标准化的响应结果
 */
export const handleApiResponse = <T = any>(response: any): { success: boolean; data: T | null; message: string } => {
  const responseData = DataTransformer.normalizeResponse(response) as ApiResponse<T>
  if (responseData?.code !== undefined) {
    const message = (responseData as any)?.msg || (responseData as any)?.message || (DataTransformer.isSuccessCode(responseData.code) ? '操作成功' : '操作失败')
    return {
      success: DataTransformer.isSuccessCode(responseData.code),
      data: DataTransformer.isSuccessCode(responseData.code) ? (responseData.data as T) : null,
      message
    }
  }

  return {
    success: true,
    data: responseData as T,
    message: '操作成功'
  }
}

/**
 * 统一处理分页响应
 * @param response API响应数据
 * @returns 标准化的分页结果
 */
export const handlePageResponse = <T = any>(response: any): { success: boolean; list: T[]; total: number; message: string } => {
  const result = handleApiResponse<PageResponse<T>>(response);
  
  if (!result.success) {
    return {
      success: false,
      list: [],
      total: 0,
      message: result.message
    };
  }
  
  const page = DataTransformer.unwrapPage<T>(response)
  return {
    success: true,
    list: page.list,
    total: page.total,
    message: result.message
  }
}

/**
 * 统一处理列表响应
 * @param response API响应数据
 * @returns 标准化的列表结果
 */
export const handleListResponse = <T = any>(response: any): { success: boolean; list: T[]; message: string } => {
  const result = handleApiResponse<T[] | PageResponse<T>>(response);
  
  if (!result.success) {
    return {
      success: false,
      list: [],
      message: result.message
    };
  }
  
  return {
    success: true,
    list: DataTransformer.unwrapList<T>(response),
    message: result.message
  }
}

/**
 * 统一处理单条数据响应
 * @param response API响应数据
 * @returns 标准化的单条数据结果
 */
export const handleSingleResponse = <T = any>(response: any): { success: boolean; data: T | null; message: string } => {
  const result = handleApiResponse<T>(response);
  
  if (!result.success) {
    return {
      success: false,
      data: null,
      message: result.message
    };
  }
  
  return result
}
