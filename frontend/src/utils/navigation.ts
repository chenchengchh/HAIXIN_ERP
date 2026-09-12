import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 页面返回处理函数
 * @param router - Vue Router实例
 * @param hasUnsavedChanges - 是否有未保存的更改
 * @param customMessage - 自定义提示消息
 * @returns Promise<boolean> - 返回是否成功执行返回操作
 */
export const handleBackNavigation = async (
  router: any,
  hasUnsavedChanges: boolean = false,
  customMessage: string = '您有未保存的更改，确定要返回吗？'
): Promise<boolean> => {
  // 如果没有未保存的更改，直接返回
  if (!hasUnsavedChanges) {
    router.back()
    return Promise.resolve(true)
  }

  // 如果有未保存的更改，显示确认提示
  try {
    await ElMessageBox.confirm(customMessage, '确认返回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 用户确认返回
    router.back()
    return Promise.resolve(true)
  } catch (error) {
    // 用户取消返回
    return Promise.resolve(false)
  }
}

/**
 * 页面跳转处理函数
 * @param router - Vue Router实例
 * @param to - 目标路由
 * @param hasUnsavedChanges - 是否有未保存的更改
 * @param customMessage - 自定义提示消息
 * @returns Promise<boolean> - 返回是否成功执行跳转操作
 */
export const handleNavigation = async (
  router: any,
  to: string | any,
  hasUnsavedChanges: boolean = false,
  customMessage: string = '您有未保存的更改，确定要离开吗？'
): Promise<boolean> => {
  // 如果没有未保存的更改，直接跳转
  if (!hasUnsavedChanges) {
    router.push(to)
    return Promise.resolve(true)
  }

  // 如果有未保存的更改，显示确认提示
  try {
    await ElMessageBox.confirm(customMessage, '确认离开', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 用户确认跳转
    router.push(to)
    return Promise.resolve(true)
  } catch (error) {
    // 用户取消跳转
    return Promise.resolve(false)
  }
}

export const safePush = async (router: any, to: any, message: string = '页面未配置或路由不存在'): Promise<boolean> => {
  try {
    const resolved = router.resolve(to)
    if (!resolved?.matched?.length) {
      ElMessage.warning(message)
      return false
    }
    await router.push(to)
    return true
  } catch (e: any) {
    ElMessage.error(e?.message || '页面跳转失败')
    return false
  }
}

/**
 * 检查表单是否有未保存的更改
 * @param initialForm - 初始表单数据
 * @param currentForm - 当前表单数据
 * @returns boolean - 是否有未保存的更改
 */
export const hasFormChanges = (initialForm: any, currentForm: any): boolean => {
  // 简单比较对象是否相等
  return JSON.stringify(initialForm) !== JSON.stringify(currentForm)
}

/**
 * 导航状态管理类
 * 用于管理页面导航状态，包括未保存的更改、页面层级等
 */
export class NavigationManager {
  private static instance: NavigationManager
  private pageStack: string[] = []
  private unsavedChangesMap: Map<string, boolean> = new Map()

  private constructor() {}

  /**
   * 获取导航管理器实例
   */
  public static getInstance(): NavigationManager {
    if (!NavigationManager.instance) {
      NavigationManager.instance = new NavigationManager()
    }
    return NavigationManager.instance
  }

  /**
   * 添加页面到页面栈
   * @param path - 页面路径
   */
  public addPage(path: string): void {
    // 避免重复添加相同路径
    if (this.pageStack[this.pageStack.length - 1] !== path) {
      this.pageStack.push(path)
    }
  }

  /**
   * 从页面栈中移除页面
   * @param path - 页面路径
   */
  public removePage(path: string): void {
    const index = this.pageStack.indexOf(path)
    if (index > -1) {
      this.pageStack.splice(index, 1)
    }
  }

  /**
   * 清空页面栈
   */
  public clearPageStack(): void {
    this.pageStack = []
  }

  /**
   * 设置页面是否有未保存的更改
   * @param path - 页面路径
   * @param hasChanges - 是否有未保存的更改
   */
  public setUnsavedChanges(path: string, hasChanges: boolean): void {
    this.unsavedChangesMap.set(path, hasChanges)
  }

  /**
   * 获取页面是否有未保存的更改
   * @param path - 页面路径
   * @returns boolean - 是否有未保存的更改
   */
  public hasUnsavedChanges(path: string): boolean {
    return this.unsavedChangesMap.get(path) || false
  }

  /**
   * 清除页面的未保存更改标记
   * @param path - 页面路径
   */
  public clearUnsavedChanges(path: string): void {
    this.unsavedChangesMap.delete(path)
  }

  /**
   * 清空所有未保存更改标记
   */
  public clearAllUnsavedChanges(): void {
    this.unsavedChangesMap.clear()
  }

  /**
   * 获取页面栈
   * @returns string[] - 页面栈
   */
  public getPageStack(): string[] {
    return [...this.pageStack]
  }

  /**
   * 获取上一个页面路径
   * @returns string | null - 上一个页面路径
   */
  public getPreviousPage(): string | null {
    if (this.pageStack.length < 2) {
      return null
    }
    const previousPage = this.pageStack[this.pageStack.length - 2]
    return previousPage || null
  }
}
