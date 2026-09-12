/**
 * 数据校验工具
 */
export class Validator {
  /**
   * 验证是否为必填字段
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static required(value: any, message = '该字段为必填项'): { valid: boolean; message: string } {
    if (value === null || value === undefined || value === '') {
      return { valid: false, message }
    }
    if (Array.isArray(value) && value.length === 0) {
      return { valid: false, message }
    }
    if (typeof value === 'string' && value.trim() === '') {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证字符串长度范围
   * @param value 要验证的字符串
   * @param min 最小长度
   * @param max 最大长度
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static length(value: string, min: number, max: number, message?: string): { valid: boolean; message: string } {
    if (!value) {
      return { valid: true, message: '' }
    }
    const len = value.length
    if (len < min || len > max) {
      return {
        valid: false,
        message: message || `字符串长度必须在${min}到${max}个字符之间`
      }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证是否为有效的邮箱地址
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static email(value: string, message = '请输入有效的邮箱地址'): { valid: boolean; message: string } {
    if (!value) {
      return { valid: true, message: '' }
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(value)) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证是否为有效的手机号码
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static phone(value: string, message = '请输入有效的手机号码'): { valid: boolean; message: string } {
    if (!value) {
      return { valid: true, message: '' }
    }
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(value)) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证是否为有效的数字
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static number(value: any, message = '请输入有效的数字'): { valid: boolean; message: string } {
    if (value === null || value === undefined || value === '') {
      return { valid: true, message: '' }
    }
    const num = Number(value)
    if (isNaN(num)) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证数字范围
   * @param value 要验证的数字
   * @param min 最小值
   * @param max 最大值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static range(value: number, min: number, max: number, message?: string): { valid: boolean; message: string } {
    if (value === null || value === undefined) {
      return { valid: true, message: '' }
    }
    if (value < min || value > max) {
      return {
        valid: false,
        message: message || `数值必须在${min}到${max}之间`
      }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证是否为正整数
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static positiveInteger(value: any, message = '请输入有效的正整数'): { valid: boolean; message: string } {
    if (value === null || value === undefined || value === '') {
      return { valid: true, message: '' }
    }
    const num = Number(value)
    if (isNaN(num) || !Number.isInteger(num) || num <= 0) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证是否为有效的日期格式
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static date(value: string, message = '请输入有效的日期格式'): { valid: boolean; message: string } {
    if (!value) {
      return { valid: true, message: '' }
    }
    const date = new Date(value)
    if (isNaN(date.getTime())) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证两个值是否相等
   * @param value1 第一个值
   * @param value2 第二个值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static equal(value1: any, value2: any, message = '两次输入的值不一致'): { valid: boolean; message: string } {
    if (value1 !== value2) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证值是否在指定的选项列表中
   * @param value 要验证的值
   * @param options 选项列表
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static in(value: any, options: any[], message = '请选择有效的选项'): { valid: boolean; message: string } {
    if (!value) {
      return { valid: true, message: '' }
    }
    if (!options.includes(value)) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证是否为有效的URL地址
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static url(value: string, message = '请输入有效的URL地址'): { valid: boolean; message: string } {
    if (!value) {
      return { valid: true, message: '' }
    }
    try {
      new URL(value)
      return { valid: true, message: '' }
    } catch {
      return { valid: false, message }
    }
  }

  /**
   * 验证是否为有效的JSON字符串
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static json(value: string, message = '请输入有效的JSON格式'): { valid: boolean; message: string } {
    if (!value) {
      return { valid: true, message: '' }
    }
    try {
      JSON.parse(value)
      return { valid: true, message: '' }
    } catch {
      return { valid: false, message }
    }
  }

  /**
   * 验证是否为有效的赢单概率（0-100）
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static winProbability(value: number, message = '赢单概率必须在0到100之间'): { valid: boolean; message: string } {
    if (value === null || value === undefined) {
      return { valid: true, message: '' }
    }
    if (value < 0 || value > 100) {
      return { valid: false, message }
    }
    return { valid: true, message: '' }
  }

  /**
   * 验证是否为有效的客户状态
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static customerStatus(value: string, message = '请选择有效的客户状态'): { valid: boolean; message: string } {
    const validStatuses = ['potential', 'active', 'inactive', 'lost']
    return Validator.in(value, validStatuses, message)
  }

  /**
   * 验证是否为有效的销售阶段
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static salesStage(value: string, message = '请选择有效的销售阶段'): { valid: boolean; message: string } {
    const validStages = ['initial', '需求确认', '方案报价', '谈判', '成交', '失败']
    return Validator.in(value, validStages, message)
  }

  /**
   * 验证是否为有效的订单状态
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static orderStatus(value: string, message = '请选择有效的订单状态'): { valid: boolean; message: string } {
    const validStatuses = ['draft', 'submitted', 'approved', 'in_progress', 'completed', 'cancelled']
    return Validator.in(value, validStatuses, message)
  }

  /**
   * 验证是否为有效的工单状态
   * @param value 要验证的值
   * @param message 错误信息
   * @returns 验证结果对象
   */
  static ticketStatus(value: string, message = '请选择有效的工单状态'): { valid: boolean; message: string } {
    const validStatuses = ['open', 'in_progress', 'pending', 'resolved', 'closed']
    return Validator.in(value, validStatuses, message)
  }

  /**
   * 执行多个验证规则
   * @param value 要验证的值
   * @param validators 验证规则数组
   * @returns 第一个失败的验证结果，如果全部通过则返回成功
   */
  static validate(value: any, validators: Array<(value: any) => { valid: boolean; message: string }>): { valid: boolean; message: string } {
    for (const validator of validators) {
      const result = validator(value)
      if (!result.valid) {
        return result
      }
    }
    return { valid: true, message: '' }
  }
}

/**
 * 表单验证工具
 */
export class FormValidator {
  /**
   * 验证表单数据
   * @param formData 表单数据对象
   * @param rules 验证规则对象
   * @returns 验证结果对象
   */
  static validate(formData: any, rules: any): { valid: boolean; errors: any } {
    const errors: any = {}
    let isValid = true

    // 遍历规则对象
    for (const field in rules) {
      if (rules.hasOwnProperty(field)) {
        const fieldRules = rules[field]
        const value = formData[field]

        // 遍历该字段的所有规则
        for (const rule of fieldRules) {
          const result = rule(value)
          if (!result.valid) {
            errors[field] = result.message
            isValid = false
            break
          }
        }
      }
    }

    return { valid: isValid, errors }
  }
}
