/**
 * 数据转换工具
 */
import { logger } from './logger'
export class DataTransformer {
  static isSuccessCode(code: any): boolean {
    return code === 0 || code === 200 || code === '0' || code === '200'
  }

  /**
   * 修复中文乱码
   * @param text 可能包含乱码的文本
   * @returns 修复后的文本
   */
  static fixChineseEncoding(text: string): string {
    if (!text || typeof text !== 'string') return text;
    
    // 保存原始文本用于比较
    const originalText = text;
    
    // 检测文本是否已经包含有效中文字符
    const hasValidChinese = /[\u4e00-\u9fa5]/.test(text);
    
    // 检测是否包含乱码特征
    const hasUtf8Mojibake = /[\u0080-\u00FF]{2,}/.test(text);
    
    // 特殊情况处理：直接检测已知的乱码序列
    const isKnownMojibake = /éƒ¨é—¨ç»|é‡‡è´­ç»|â€œ|â€�|â€™|â€˜/.test(text);
    
    // 只有在检测到真正的乱码时才进行修复
    if (!hasUtf8Mojibake && !/[\uFFFD]/.test(text) && !isKnownMojibake) {
      return text;
    }
    
    // 优先使用直接替换已知乱码
    const knownFixes: Record<string, string> = {
      'éƒ¨é—¨ç»ç†': '部门经理',
      'é‡‡è´­ç»ç†': '采购经理',
      'â€œ': '“',
      'â€�': '”',
      'â€™': '’',
      'â€˜': '‘',
      'â€¢': '•',
      'â€“': '–',
      'â€”': '—',
      'â€¦': '…',
      'Â': '', // 移除UTF-8 BOM标记
      'ã': '　', // 全角空格
      'ã': '。',
      'ã': '，',
      'ã': '》',
      'ã': '《',
      'ã': '】',
      'ã': '【',
      'ã': '：',
      'ã': '；',
      'ã': '“',
      'ã': '”',
      'ã': '？',
      'ã': '！',
      'ã': '‘',
      'ã': '’',
      'ã': '…',
      'ã': '—',
      'ã': '～'
    };
    
    let result = text;
    for (const [from, to] of Object.entries(knownFixes)) {
      result = result.replace(new RegExp(from, 'g'), to);
    }
    
    // 如果直接替换后已经包含有效中文，返回结果
    if (/[\u4e00-\u9fa5]/.test(result)) {
      return result;
    }
    
    // 尝试传统的UTF-8乱码修复方法
    try {
      const fixed = decodeURIComponent(escape(text));
      if (/[\u4e00-\u9fa5]/.test(fixed) || /[\u2014-\u2019]/.test(fixed)) {
        return fixed;
      }
    } catch (error) {
      // 修复失败，继续尝试其他方法
    }
    
    // 尝试备选修复方法
    try {
      const fixed = unescape(encodeURIComponent(text));
      if (/[\u4e00-\u9fa5]/.test(fixed) || /[\u2014-\u2019]/.test(fixed)) {
        return fixed;
      }
    } catch (error) {
      // 修复失败，返回直接替换后的结果
    }
    
    // 对直接替换后的结果再次尝试修复
    if (result !== text) {
      try {
        const fixed = decodeURIComponent(escape(result));
        if (/[\u4e00-\u9fa5]/.test(fixed) || /[\u2014-\u2019]/.test(fixed)) {
          return fixed;
        }
      } catch (error) {
        // 修复失败，返回直接替换后的结果
      }
    }
    
    return result;
  }

  /**
   * 验证属性名是否合法
   * @param name 属性名
   * @returns 是否合法
   */
  static validatePropertyName(name: string): boolean {
    // 检查是否为字符串
    if (typeof name !== 'string') {
      return false;
    }
    
    // 检查是否为空
    if (!name.trim()) {
      return false;
    }
    
    // 检查是否包含HTML注释
    if (/<!--|-->/.test(name)) {
      console.log(`DataTransformer - 非法属性名，包含HTML注释: "${name}"`);
      return false;
    }
    
    // 检查是否包含非法字符
    if (/[<>"'&=\s]/.test(name)) {
      console.log(`DataTransformer - 非法属性名，包含特殊字符: "${name}"`);
      return false;
    }
    
    // 检查是否是有效的DOM属性名
    const invalidPropNames = ['id', 'class', 'style', 'href', 'src'];
    if (invalidPropNames.includes(name)) {
      // 这些属性需要特殊处理，这里暂时返回true，后续在使用时会进行更严格的验证
      return true;
    }
    
    return true;
  }
  
  /**
   * 清理字符串，确保不包含非法字符
   * @param str 原始字符串
   * @returns 清理后的字符串
   */
  static cleanString(str: string): string {
    if (typeof str !== 'string') {
      return str;
    }
    
    // 移除HTML注释
    let cleaned = str.replace(/<!--[\s\S]*?-->/g, '');
    
    // 移除完整HTML标签（保留独立的 > < & 引号等业务文本字符，Vue插值已自动转义）
    cleaned = cleaned.replace(/<[^>]+>/g, '');
    
    // 移除控制字符
    cleaned = cleaned.replace(/[\x00-\x1F\x7F]/g, '');
    
    // 移除多余空格
    cleaned = cleaned.trim();
    
    return cleaned;
  }
  
  /**
   * 递归修复对象中的中文乱码
   * @param obj 可能包含乱码的对象
   * @returns 修复后的对象
   */
  static recursiveFixEncoding(obj: any): any {
    // 安全检查：直接返回null或undefined
    if (obj === null || obj === undefined) {
      return obj;
    }
    
    // 安全检查：如果是DOM元素，直接返回
    if (typeof window !== 'undefined' && 
        (obj instanceof Element || obj instanceof HTMLElement || obj instanceof Node)) {
      return obj;
    }
    
    // 安全检查：如果是Blob或File对象，直接返回
    if (typeof Blob !== 'undefined' && obj instanceof Blob) {
      return obj;
    }
    
    // 安全检查：如果是File对象，直接返回
    if (typeof File !== 'undefined' && obj instanceof File) {
      return obj;
    }
    
    // 字符串类型处理
    if (typeof obj === 'string') {
      // 全面检测并处理包含HTML注释的字符串
      // 检查是否包含HTML注释标记
      const hasHtmlComment = /<!--|-->/.test(obj);
      
      // 检查是否是HTML注释本身
      const isHtmlComment = /^\s*<!--[\s\S]*-->\s*$/.test(obj);
      
      if (hasHtmlComment || isHtmlComment) {
        console.log(`DataTransformer - 发现包含HTML注释的字符串，返回空字符串: "${obj}"`);
        return ''; // 直接返回空字符串，避免后续处理出错
      }
      
      // 清理字符串，确保不包含非法字符
      const cleaned = this.cleanString(obj);
      
      const fixed = this.fixChineseEncoding(cleaned);
      
      // 再次检查修复后的字符串是否包含HTML注释
      if (fixed.includes('<!--') || fixed.includes('-->')) {
        console.log(`DataTransformer - 修复后的字符串仍包含HTML注释，返回空字符串: "${fixed}"`);
        return '';
      }
      
      // 仅在字符串实际被修改时才输出日志
      if (obj !== fixed) {
        console.log(`DataTransformer - 修复字符串: "${obj}" -> "${fixed}"`);
      }
      return fixed;
    }
    
    // 数组类型处理
    if (Array.isArray(obj)) {
      return obj
        .map(item => this.recursiveFixEncoding(item))
        .filter(item => item !== null && item !== undefined);
    }
    
    // 对象类型处理
    if (typeof obj === 'object') {
      // 处理日期对象
      if (obj instanceof Date) {
        return obj;
      }
      
      // 处理正则表达式对象
      if (obj instanceof RegExp) {
        return obj;
      }
      
      // 处理Map对象
      if (obj instanceof Map) {
        const fixedMap = new Map();
        for (const [key, value] of obj.entries()) {
          // 验证键的合法性
          const keyStr = String(key);
          if (!this.validatePropertyName(keyStr)) {
            console.log(`DataTransformer - 发现非法Map键，跳过: "${key}"`);
            continue;
          }
          
          const fixedValue = this.recursiveFixEncoding(value);
          // 只过滤null和undefined，保留空字符串、空数组和空对象
          if (fixedValue !== null && fixedValue !== undefined) {
            fixedMap.set(key, fixedValue);
          }
        }
        return fixedMap;
      }
      
      // 处理Set对象
      if (obj instanceof Set) {
        const fixedSet = new Set();
        for (const value of obj.values()) {
          const fixedValue = this.recursiveFixEncoding(value);
          // 只过滤null和undefined，保留空字符串、空数组和空对象
          if (fixedValue !== null && fixedValue !== undefined) {
            fixedSet.add(fixedValue);
          }
        }
        return fixedSet;
      }
      
      // 处理普通对象
      const fixedObj: any = {};
      for (const key in obj) {
        // 确保key是合法的字符串，避免"<!--"这样的非法属性名
        if (key === null || key === undefined || typeof key !== 'string') {
          console.log(`DataTransformer - 发现无效属性名，跳过: ${key}`);
          continue;
        }
        
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
          // 额外检查：直接检测属性名是否为HTML注释或包含HTML注释
          if (key === '<!--' || key === '-->' || /<!--|-->/.test(key)) {
            console.log(`DataTransformer - 发现属性名为HTML注释，直接跳过: "${key}"`);
            continue;
          }
          
          // 增强检查：确保属性名不以非法字符开头
          if (/^[^a-zA-Z_]/.test(key)) {
            console.log(`DataTransformer - 发现属性名不以字母或下划线开头，跳过: "${key}"`);
            continue;
          }
          
          // 验证属性名合法性
          if (!this.validatePropertyName(key)) {
            console.log(`DataTransformer - 发现非法属性名，跳过: "${key}"`);
            continue;
          }
          
          try {
            const value = obj[key];
            // 检查值是否为字符串，如果是，先检查是否包含HTML注释
            if (typeof value === 'string') {
              // 直接检查原始字符串值，避免递归调用时多次打印日志
              if (/<!--|-->/.test(value)) {
                console.log(`DataTransformer - 发现值包含HTML注释，跳过属性: "${key}"`);
                continue;
              }
            }
            
            // 递归修复值
            const fixedValue = this.recursiveFixEncoding(value);
            
            // 只添加非null和非undefined值，保留空字符串、空数组和空对象
            if (fixedValue !== null && fixedValue !== undefined) {
              // 对于对象或数组，即使是空的，也应该添加，因为后端返回的数据可能就是空数组或空对象
              fixedObj[key] = fixedValue;
            }
          } catch (error) {
            console.error(`DataTransformer - 处理属性"${key}"时出错:`, error);
            continue; // 跳过出错的属性，继续处理其他属性
          }
        }
      }
      return fixedObj;
    }
    
    return obj;
  }

  /**
   * 标准化响应数据
   * @param data 原始响应数据
   * @returns 标准化后的数据
   */
  static normalizeResponse(data: any): any {
    const isAxiosResponseLike =
      typeof data === 'object' &&
      data !== null &&
      'data' in data &&
      'status' in data &&
      'headers' in data &&
      'config' in data

    const raw = isAxiosResponseLike ? (data as any).data : data
    console.log('DataTransformer - 原始响应数据:', raw, '类型:', typeof raw, '是否为数组:', Array.isArray(raw))
    data = raw
    
    // 检查是否为HTML响应或包含HTML注释
    let isHtmlResponse = false;
    
    if (typeof data === 'string') {
      // 字符串类型检查
      isHtmlResponse = data.trim().toLowerCase().startsWith('<!doctype html') || 
                       data.trim().toLowerCase().startsWith('<html') ||
                       data.includes('<!--');
    } else if (typeof data === 'object' && data !== null) {
      // 对象类型检查 - 检查是否包含HTML相关的属性或值
      const objStr = JSON.stringify(data);
      isHtmlResponse = objStr.includes('<!doctype html') || 
                       objStr.includes('<html') ||
                       objStr.includes('<!--');
    }
    
    if (isHtmlResponse) {
      console.log('DataTransformer - 处理HTML响应')
      return {
        code: 500,
        msg: '后端返回HTML响应，可能是服务器错误或路由配置问题',
        data: null
      };
    }
    
    // 统一修复所有类型数据的中文乱码
    let fixedRawData = this.recursiveFixEncoding(data);

    fixedRawData = this.recursiveSnakeToCamelPreserve(fixedRawData);
    
    // 添加数据安全检查，确保返回的数据不会包含无效的属性名
    if (typeof fixedRawData === 'object' && fixedRawData !== null) {
      // 检查对象的属性名是否合法
      for (const key in fixedRawData) {
        if (Object.prototype.hasOwnProperty.call(fixedRawData, key)) {
          // 如果属性名不合法，移除该属性
          if (!this.validatePropertyName(key)) {
            console.warn(`DataTransformer - 移除非法属性名: "${key}"`);
            delete fixedRawData[key];
          }
        }
      }
    }
    
    // 处理后端标准响应格式（包括错误响应，错误响应可能没有data字段）
    if (fixedRawData?.code !== undefined && (fixedRawData?.msg !== undefined || fixedRawData?.message !== undefined)) {
      console.log('DataTransformer - 处理后端标准响应格式')
      
      // 处理后端标准响应格式，code=0或200都表示成功，其他表示失败
      const normalizedCode = this.isSuccessCode(fixedRawData.code) ? 200 : fixedRawData.code
      
      // 检查data字段是否存在且为HTML
      if (fixedRawData.data !== undefined) {
        const isHtmlData = typeof fixedRawData.data === 'string' && 
          (fixedRawData.data.trim().toLowerCase().startsWith('<!doctype html') || 
           fixedRawData.data.trim().toLowerCase().startsWith('<html') ||
           fixedRawData.data.includes('<!--'));
        
        if (isHtmlData) {
          console.log('DataTransformer - 响应data字段为HTML')
          return {
            code: 500,
            msg: '后端返回HTML内容，可能是服务器错误或路由配置问题',
            data: null
          };
        }
      }
      
      // 检查是否为分页数据
      if (fixedRawData.data && (fixedRawData.data.total !== undefined || fixedRawData.data.totalElements !== undefined)) {
        console.log('DataTransformer - 处理分页数据')
        // 确保分页数据的list也被正确修复，支持content字段（Spring Data JPA格式）
        const records = fixedRawData.data.records || fixedRawData.data.list || fixedRawData.data.content || []
        // 再次修复list中的中文乱码，确保万无一失
        const fixedList = this.recursiveFixEncoding(records)
        const normalizedPage = fixedRawData.data.currentPage || fixedRawData.data.page || (fixedRawData.data.number !== undefined ? fixedRawData.data.number + 1 : 1)
        const normalizedSize = fixedRawData.data.pageSize || fixedRawData.data.size || 10
        const normalizedTotal = fixedRawData.data.total || fixedRawData.data.totalElements || 0
        // 处理后端分页数据格式
        const result = {
          code: normalizedCode,
          msg: fixedRawData.msg || fixedRawData.message,
          list: fixedList,
          total: normalizedTotal,
          data: {
            records: fixedList, // 保持records字段，兼容前端代码
            list: fixedList, // 同时保留list字段，兼容其他组件
            total: normalizedTotal,
            page: normalizedPage,
            size: normalizedSize
          }
        }
        console.log('DataTransformer - 分页数据转换结果:', result)
        return result
      } else if (Array.isArray(fixedRawData.data)) {
        console.log('DataTransformer - 处理data为数组的情况')
        // 修复数组中的中文乱码
        const fixedList = this.recursiveFixEncoding(fixedRawData.data)
        // 转换为带有list和records字段的对象，兼容前端组件
        const result = {
          code: normalizedCode,
          msg: fixedRawData.msg || fixedRawData.message,
          list: fixedList,
          total: fixedList.length,
          data: {
            records: fixedList, // 保持records字段，兼容前端代码
            list: fixedList, // 同时保留list字段，兼容其他组件
            total: fixedList.length, // 设置总条数为数组长度
            page: 1, // 默认页码为1
            size: fixedList.length // 默认每页大小为数组长度
          }
        }
        console.log('DataTransformer - data为数组的转换结果:', result)
        return result
      }
      
      // 非分页数据，直接返回（包括错误响应，错误响应可能没有data字段）
      const result = {
        code: normalizedCode,
        msg: fixedRawData.msg || fixedRawData.message,
        data: fixedRawData.data !== undefined ? fixedRawData.data : null
      }
      console.log('DataTransformer - 非分页数据转换结果:', result)
      return result
    }
    
    // 处理不同的响应格式
    if (fixedRawData?.success !== undefined) {
      console.log('DataTransformer - 处理success响应格式')
      // 格式：{ success: boolean, message: string, result: any }
      
      const result = {
        code: fixedRawData.success ? 200 : 500,
        msg: fixedRawData.message || (fixedRawData.success ? '请求成功' : '请求失败'),
        data: fixedRawData.result || fixedRawData.data || null
      }
      console.log('DataTransformer - success响应格式转换结果:', result)
      return result
    } else if (fixedRawData?.ok !== undefined) {
      console.log('DataTransformer - 处理ok响应格式')
      // 格式：{ ok: boolean, message: string, data: any }
      
      const result = {
        code: fixedRawData.ok ? 200 : 500,
        msg: fixedRawData.message || fixedRawData.msg || (fixedRawData.ok ? '请求成功' : '请求失败'),
        data: fixedRawData.data || null
      }
      console.log('DataTransformer - ok响应格式转换结果:', result)
      return result
    } else if (
      fixedRawData &&
      typeof fixedRawData === 'object' &&
      Array.isArray((fixedRawData as any).content) &&
      (((fixedRawData as any).totalElements !== undefined) || ((fixedRawData as any).total !== undefined))
    ) {
      const content = (fixedRawData as any).content || []
      const fixedList = this.recursiveFixEncoding(content)
      const normalizedTotal = (fixedRawData as any).totalElements ?? (fixedRawData as any).total ?? fixedList.length
      const normalizedPage = (fixedRawData as any).page ?? ((fixedRawData as any).number !== undefined ? (fixedRawData as any).number + 1 : 1)
      const normalizedSize = (fixedRawData as any).size ?? (fixedRawData as any).pageSize ?? fixedList.length
      return {
        code: 200,
        msg: '请求成功',
        list: fixedList,
        total: Number(normalizedTotal) || 0,
        data: {
          records: fixedList,
          list: fixedList,
          total: Number(normalizedTotal) || 0,
          page: normalizedPage,
          size: normalizedSize
        }
      }
    } else if (Array.isArray(fixedRawData)) {
      console.log('DataTransformer - 处理直接返回数组格式')
      // 直接返回数组 - 转换为带有list和records字段的对象，兼容前端组件
      
      const result = {
        code: 200,
        msg: '请求成功',
        list: fixedRawData,
        total: fixedRawData.length,
        data: {
          records: fixedRawData, // 保持records字段，兼容前端代码
          list: fixedRawData, // 同时保留list字段，兼容其他组件
          total: fixedRawData.length,
          page: 1,
          size: fixedRawData.length
        }
      }
      console.log('DataTransformer - 直接返回数组格式转换结果:', result)
      return result
    } else if (fixedRawData?.value && Array.isArray(fixedRawData.value)) {
      console.log('DataTransformer - 处理Spring Boot直接返回集合格式')
      // 处理Spring Boot直接返回集合的情况（带有value和Count字段）
      
      const result = {
        code: 200,
        msg: '请求成功',
        data: {
          records: fixedRawData.value, // 保持records字段，兼容前端代码
          list: fixedRawData.value, // 同时保留list字段，兼容其他组件
          total: fixedRawData.Count || fixedRawData.value.length,
          page: 1,
          size: fixedRawData.value.length
        }
      }
      console.log('DataTransformer - Spring Boot直接返回集合格式转换结果:', result)
      return result
    } else {
      console.log('DataTransformer - 处理其他格式')
      // 其他格式，直接封装
      
      // 确保返回的是安全的对象格式
      const safeData = typeof fixedRawData === 'object' && fixedRawData !== null ? fixedRawData : {}
      
      const result = {
        code: 200,
        msg: '请求成功',
        data: safeData
      }
      console.log('DataTransformer - 其他格式转换结果:', result)
      return result
    }
  }

  /**
   * 标准化分页响应数据
   * @param data 原始响应数据
   * @returns 标准化后的分页数据
   */
  static normalizePaginationResponse(data: any): any {
    // 检查是否为HTML响应或包含HTML注释
    const isHtmlResponse = typeof data === 'string' && 
      (data.trim().toLowerCase().startsWith('<!doctype html') || 
       data.trim().toLowerCase().startsWith('<html') ||
       data.includes('<!--'));
    
    if (isHtmlResponse) {
      console.log('DataTransformer - 分页处理HTML响应')
      return {
        code: 500,
        msg: '后端返回HTML响应，可能是服务器错误或路由配置问题',
        data: {
          list: [],
          total: 0,
          page: 1,
          size: 10
        }
      };
    }
    
    // 处理后端分页数据格式
    if (data?.code !== undefined && data?.message !== undefined && data?.data?.total !== undefined && data?.data?.records !== undefined) {
      // 检查data.data是否为HTML或包含HTML注释
      const isHtmlData = typeof data.data === 'string' && 
        (data.data.trim().toLowerCase().startsWith('<!doctype html') || 
         data.data.trim().toLowerCase().startsWith('<html') ||
         data.data.includes('<!--'));
      
      if (isHtmlData) {
        console.log('DataTransformer - 分页响应data字段为HTML')
        return {
          code: 500,
          msg: '后端返回HTML内容，可能是服务器错误或路由配置问题',
          data: {
            list: [],
            total: 0,
            page: 1,
            size: 10
          }
        };
      }
      
      // 修复中文乱码
      const fixedData = this.recursiveFixEncoding(data.data);
      
      return {
        code: 200,
        msg: data.message,
        data: {
          list: fixedData.records || fixedData.content || [],
          total: fixedData.total || 0,
          page: fixedData.currentPage || fixedData.number || 1,
          size: fixedData.pageSize || fixedData.size || 10
        }
      }
    }
    
    // 处理前端分页数据格式
    if (data?.list !== undefined || data?.data?.list !== undefined) {
      const paginationData = data?.data || data
      
      // 修复中文乱码
      const fixedData = this.recursiveFixEncoding(paginationData);
      
      return {
        code: 200,
        msg: data.msg || '请求成功',
        data: {
          list: fixedData.list || [],
          total: fixedData.total || 0,
          page: fixedData.page || 1,
          size: fixedData.size || 10
        }
      }
    }

    // 如果是数组，转换为分页格式
    if (Array.isArray(data)) {
      // 修复中文乱码
      const fixedData = this.recursiveFixEncoding(data);
      
      return {
        code: 200,
        msg: '请求成功',
        data: {
          list: fixedData,
          total: fixedData.length,
          page: 1,
          size: fixedData.length
        }
      }
    }

    // 处理normalizeResponse返回的数据
    const normalizedData = this.normalizeResponse(data)
    
    // 确保返回的是标准化的分页数据格式
    if (normalizedData?.data?.list === undefined) {
      return {
        code: normalizedData.code || 200,
        msg: normalizedData.msg || '请求成功',
        data: {
          list: [],
          total: 0,
          page: 1,
          size: 10
        }
      }
    }

    return normalizedData
  }

  static unwrapData<T = any>(response: any, fallback: T | null = null): T | null {
    const normalized = this.normalizeResponse(response)
    return (normalized?.data ?? fallback) as T | null
  }

  static unwrapList<T = any>(response: any): T[] {
    const normalized = this.normalizeResponse(response)
    const data = normalized?.data

    if (Array.isArray(data)) return data as T[]
    if (Array.isArray(data?.records)) return data.records as T[]
    if (Array.isArray(data?.list)) return data.list as T[]
    if (Array.isArray(data?.content)) return data.content as T[]
    if (Array.isArray(normalized?.list)) return normalized.list as T[]

    // 兼容api包装层已解包为{data: pageObj}后被二次包装的形态（data.data为分页对象）
    const inner = data?.data
    if (inner && typeof inner === 'object' && !Array.isArray(inner)) {
      if (Array.isArray(inner.records)) return inner.records as T[]
      if (Array.isArray(inner.list)) return inner.list as T[]
      if (Array.isArray(inner.content)) return inner.content as T[]
    }

    return []
  }

  static unwrapPage<T = any>(response: any): { list: T[]; records: T[]; total: number; page: number; size: number } {
    const normalized = this.normalizeResponse(response)
    let data = normalized?.data ?? {}
    const list = this.unwrapList<T>(normalized)
    // 兼容api包装层已解包为{data: pageObj}后被二次包装的形态，向下一层取分页元数据
    if (
      data?.total === undefined && data?.totalElements === undefined &&
      data?.data && typeof data.data === 'object' && !Array.isArray(data.data)
    ) {
      data = data.data
    }
    const total = Number(data?.total ?? data?.totalElements ?? normalized?.total ?? list.length) || 0
    const page = Number(data?.page ?? data?.currentPage ?? (data?.number !== undefined ? Number(data.number) + 1 : 1)) || 1
    const size = Number(data?.size ?? data?.pageSize ?? list.length) || 0

    return {
      list,
      records: list,
      total,
      page,
      size
    }
  }

  /**
   * 标准化请求数据
   * @param data 原始请求数据
   * @returns 标准化后的请求数据
   */
  static normalizeRequest(data: any): any {
    if (!data) return data

    // 转换日期格式
    const normalizedData = { ...data }
    
    // 遍历对象属性，转换日期格式
    for (const key in normalizedData) {
      if (Object.prototype.hasOwnProperty.call(normalizedData, key)) {
        const value = normalizedData[key]
        
        // 转换日期对象为字符串
        if (value instanceof Date) {
          normalizedData[key] = value.toISOString()
        } else if (typeof value === 'object' && value !== null) {
          // 递归处理嵌套对象
          normalizedData[key] = this.normalizeRequest(value)
        }
      }
    }
    
    return normalizedData
  }

  /**
   * 转换日期格式
   * @param date 日期对象或字符串
   * @param format 目标格式
   * @returns 格式化后的日期字符串
   */
  static formatDate(date: any, format: string = 'YYYY-MM-DD'): string {
    try {
      if (!date) return ''

      let d: Date
      
      // 转换为Date对象
      if (date instanceof Date) {
        d = date
      } else if (typeof date === 'string') {
        d = new Date(date)
      } else {
        return ''
      }

      // 检查日期是否有效
      if (isNaN(d.getTime())) {
        return ''
      }

      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')

      // 替换格式字符串
      return format
        .replace('YYYY', String(year))
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds)
    } catch (error) {
      logger.error('日期格式化错误:', error)
      return ''
    }
  }

  /**
   * 转换数字格式
   * @param number 数字
   * @param precision 小数位数
   * @returns 格式化后的数字
   */
  static formatNumber(number: any, precision: number = 2): string {
    try {
      const num = Number(number)
      if (isNaN(num)) return '0'
      return num.toFixed(precision)
    } catch (error) {
      logger.error('数字格式化错误:', error)
      return '0'
    }
  }

  /**
   * 格式化金额
   * @param amount 金额
   * @param precision 小数位数
   * @returns 格式化后的金额
   */
  static formatMoney(amount: any, precision: number = 2): string {
    try {
      const num = Number(amount)
      if (isNaN(num)) return '0.00'
      return num.toFixed(precision)
    } catch (error) {
      logger.error('金额格式化错误:', error)
      return '0.00'
    }
  }

  /**
   * 转换布尔值为中文
   * @param value 布尔值
   * @returns 中文描述
   */
  static booleanToChinese(value: boolean): string {
    return value ? '是' : '否'
  }

  /**
   * 转换状态码为中文
   * @param status 状态码
   * @param statusMap 状态映射表
   * @returns 中文描述
   */
  static statusToChinese(status: string | number, statusMap: Record<string, string>): string {
    return statusMap[status] || String(status)
  }

  /**
   * 处理空值
   * @param value 原始值
   * @param defaultValue 默认值
   * @returns 处理后的值
   */
  static handleNull(value: any, defaultValue: any = ''): any {
    if (value === null || value === undefined || value === '') {
      return defaultValue
    }
    return value
  }

  /**
   * 将下划线命名转换为小驼峰命名
   * @param str 下划线命名的字符串
   * @returns 小驼峰命名的字符串
   */
  static snakeToCamel(str: string): string {
    if (!str) return str
    return str.replace(/(_\w)/g, (match) => {
      return match[1] ? match[1].toUpperCase() : ''
    })
  }

  /**
   * 递归将对象的所有属性从下划线命名转换为小驼峰命名
   * @param obj 原始对象
   * @returns 转换后的对象
   */
  static recursiveSnakeToCamel(obj: any): any {
    if (obj === null || typeof obj !== 'object') {
      return obj
    }

    if (obj instanceof Date) {
      return new Date(obj.getTime())
    }

    if (Array.isArray(obj)) {
      return obj.map(item => this.recursiveSnakeToCamel(item))
    }

    const transformedObj: any = {}
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        const camelKey = this.snakeToCamel(key)
        transformedObj[camelKey] = this.recursiveSnakeToCamel(obj[key])
      }
    }
    return transformedObj
  }

  static recursiveSnakeToCamelPreserve(obj: any): any {
    if (obj === null || typeof obj !== 'object') {
      return obj
    }

    if (obj instanceof Date) {
      return new Date(obj.getTime())
    }

    if (Array.isArray(obj)) {
      return obj.map(item => this.recursiveSnakeToCamelPreserve(item))
    }

    const transformedObj: any = {}
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        const value = this.recursiveSnakeToCamelPreserve(obj[key])
        transformedObj[key] = value
        const camelKey = this.snakeToCamel(key)
        if (camelKey && camelKey !== key && transformedObj[camelKey] === undefined) {
          transformedObj[camelKey] = value
        }
      }
    }
    return transformedObj
  }

  /**
   * 深拷贝对象
   * @param obj 原始对象
   * @returns 拷贝后的对象
   */
  static deepClone<T>(obj: T): T {
    if (obj === null || typeof obj !== 'object') {
      return obj
    }

    if (obj instanceof Date) {
      return new Date(obj.getTime()) as any
    }

    if (Array.isArray(obj)) {
      return obj.map(item => this.deepClone(item)) as any
    }

    const clonedObj: any = {}
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        clonedObj[key] = this.deepClone(obj[key])
      }
    }

    return clonedObj
  }
}
