/**
 * 权限管理工具
 */
export class PermissionManager {
  /**
   * 检查用户是否有权限访问某个路由
   * @param _routeName 路由名称
   * @returns 是否有权限
   */
  static hasPermission(_routeName: string): boolean {
    // 权限控制已禁用，所有路由都允许访问
    return true
  }

  /**
   * 获取用户权限列表
   * @returns 权限列表
   */
  static getUserPermissions(): string[] {
    // 权限控制已禁用，返回默认权限列表
    return ['admin']
  }

  /**
   * 设置用户权限列表
   * @param permissions 权限列表
   */
  static setUserPermissions(permissions: string[]): void {
    localStorage.setItem('userPermissions', JSON.stringify(permissions))
  }

  /**
   * 检查是否为超级管理员
   * @returns 是否为超级管理员
   */
  static isAdmin(): boolean {
    // 权限控制已禁用，默认所有用户都是超级管理员
    return true
  }

  /**
   * 检查用户是否有某个操作权限
   * @param _permission 权限名称
   * @returns 是否有权限
   */
  static can(_permission: string): boolean {
    // 权限控制已禁用，所有操作都允许执行
    return true
  }

  /**
   * 过滤路由列表，只返回用户有权限访问的路由
   * @param routes 路由列表
   * @returns 过滤后的路由列表
   */
  static filterRoutes(routes: any[]): any[] {
    // 权限控制已禁用，返回所有路由
    return routes
  }
}

/**
 * 权限检查装饰器
 * @param permission 权限名称
 * @returns 装饰器函数
 */
export function requirePermission(permission: string) {
  return function (_target: any, _propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value
    
    descriptor.value = function (...args: any[]) {
      if (PermissionManager.can(permission)) {
        return originalMethod.apply(this, args)
      } else {
        // 没有权限，抛出错误或执行其他处理
        throw new Error('没有操作权限')
      }
    }
    
    return descriptor
  }
}
