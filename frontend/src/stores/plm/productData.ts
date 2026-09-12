import { defineStore } from 'pinia'
import { unwrapListResponse, unwrapPageResponse, unwrapResponseData } from '@/api'
import { plmApi } from '@/api/plm'
import { bomApi } from '@/api/bom'

// 产品数据管理状态管理
// 管理产品数据，包括物料库、BOM数据、文档数据等
export const useProductDataStore = defineStore('productData', {
  state: () => ({
    // 物料库数据
    materials: [] as any[],
    // BOM数据
    boms: [] as any[],
    // 文档数据
    documents: [] as any[],
    // 选中的物料ID
    selectedMaterialId: null as string | null,
    // 选中的BOM ID
    selectedBomId: null as string | null,
    // 选中的文档ID
    selectedDocumentId: null as string | null,
    // 物料搜索关键词
    materialSearchKeyword: '',
    // 物料类型过滤
    materialTypeFilter: 'all',
    // 文档搜索关键词
    documentSearchKeyword: '',
    // 文档类型过滤
    documentTypeFilter: 'all',
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),

  getters: {
    // 获取筛选后的物料列表
    filteredMaterials: (state) => {
      return state.materials.filter(material => {
        const matchesKeyword = state.materialSearchKeyword === '' || 
          (material.name && material.name.toLowerCase().includes(state.materialSearchKeyword.toLowerCase())) ||
          (material.code && material.code.toLowerCase().includes(state.materialSearchKeyword.toLowerCase()))
        const matchesType = state.materialTypeFilter === 'all' || material.type === state.materialTypeFilter
        return matchesKeyword && matchesType
      })
    },

    // 获取筛选后的文档列表
    filteredDocuments: (state) => {
      return state.documents.filter(doc => {
        const matchesKeyword = state.documentSearchKeyword === '' || 
          (doc.title && doc.title.toLowerCase().includes(state.documentSearchKeyword.toLowerCase())) ||
          (doc.code && doc.code.toLowerCase().includes(state.documentSearchKeyword.toLowerCase()))
        const matchesType = state.documentTypeFilter === 'all' || doc.type === state.documentTypeFilter
        return matchesKeyword && matchesType
      })
    },

    // 获取当前选中的物料
    currentMaterial: (state) => {
      if (!state.selectedMaterialId) return null
      return state.materials.find(material => material.id === state.selectedMaterialId) || null
    },

    // 获取当前选中的BOM
    currentBom: (state) => {
      if (!state.selectedBomId) return null
      return state.boms.find(bom => bom.id === state.selectedBomId) || null
    },

    // 获取当前选中的文档
    currentDocument: (state) => {
      if (!state.selectedDocumentId) return null
      return state.documents.find(doc => doc.id === state.selectedDocumentId) || null
    }
  },

  actions: {
    // 设置物料库数据
    setMaterials(materials: any[]) {
      this.materials = materials
    },

    // 设置BOM数据
    setBoms(boms: any[]) {
      this.boms = boms
    },

    // 设置文档数据
    setDocuments(documents: any[]) {
      this.documents = documents
    },

    // 设置选中的物料ID
    setSelectedMaterialId(materialId: string) {
      this.selectedMaterialId = materialId
    },

    // 设置选中的BOM ID
    setSelectedBomId(bomId: string) {
      this.selectedBomId = bomId
    },

    // 设置选中的文档ID
    setSelectedDocumentId(documentId: string) {
      this.selectedDocumentId = documentId
    },

    // 设置物料搜索关键词
    setMaterialSearchKeyword(keyword: string) {
      this.materialSearchKeyword = keyword
    },

    // 设置物料类型过滤
    setMaterialTypeFilter(type: string) {
      this.materialTypeFilter = type
    },

    // 设置文档搜索关键词
    setDocumentSearchKeyword(keyword: string) {
      this.documentSearchKeyword = keyword
    },

    // 设置文档类型过滤
    setDocumentTypeFilter(type: string) {
      this.documentTypeFilter = type
    },

    // 设置分页
    setPagination(page: number, pageSize: number) {
      this.pagination.page = page
      this.pagination.pageSize = pageSize
    },

    // 重置物料筛选条件
    resetMaterialFilters() {
      this.materialSearchKeyword = ''
      this.materialTypeFilter = 'all'
      this.pagination.page = 1
    },

    // 重置文档筛选条件
    resetDocumentFilters() {
      this.documentSearchKeyword = ''
      this.documentTypeFilter = 'all'
      this.pagination.page = 1
    },

    async fetchMaterials() {
      try {
        const response: any = await plmApi.productData.getProductsByPage({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.materialSearchKeyword || undefined,
          type: this.materialTypeFilter !== 'all' ? this.materialTypeFilter : undefined
        })
        const page = unwrapPageResponse<any>(response)
        const rows = page.list.map((p: any) => ({
          id: String(p.id ?? ''),
          code: p.productCode,
          name: p.productName,
          type: p.productType || p.productCategory || 'all',
          spec: p.productModel || p.productSpec || '',
          unit: p.unit || '件',
          version: p.version || '',
          status: p.status || ''
        }))
        this.setMaterials(rows)
        this.pagination.total = page.total || rows.length
      } catch (error) {
        console.error('获取物料库数据失败:', error)
      }
    },

    // 异步获取BOM数据（模拟API调用）
    async fetchBoms() {
      try {
        const productId = this.selectedMaterialId || (this.materials?.[0]?.id as string | undefined)
        if (!productId) {
          this.setBoms([])
          return
        }
        const product = this.materials.find(m => m.id === productId)
        const productCode = product?.code

        if (productCode) {
          const matRes: any = await bomApi.getMaterialList({ code: productCode, page: 1, size: 1 })
          const matRecords = unwrapPageResponse<any>(matRes).list
          const materialId = Array.isArray(matRecords) && matRecords.length > 0 ? matRecords[0]?.id : null

          if (materialId) {
            const treeRes: any = await bomApi.getBomTree(materialId, product?.version || undefined)
            const treeData = unwrapResponseData<any>(treeRes)
            if (treeData && Array.isArray(treeData?.nodes)) {
              const rootNode = {
                id: 'root',
                name: product?.name || '产品',
                code: productCode || '',
                spec: product?.spec || '',
                unit: product?.unit || '件',
                qty: 1,
                children: [] as any[]
              }

              const mapNode = (node: any): any => {
                const line = node?.line || {}
                const children = Array.isArray(node?.children) ? node.children : []
                return {
                  id: String(line.id ?? ''),
                  name: line.childMaterialName || '',
                  code: line.childMaterialCode || '',
                  spec: '',
                  unit: line.unit || '件',
                  qty: Number(line.quantity ?? 0),
                  children: children.map(mapNode)
                }
              }

              rootNode.children = treeData.nodes.map(mapNode)
              const header = treeData.bomHeader || {}
              this.setBoms([{
                id: String(header.id ?? productId),
                productCode: productCode,
                productName: product?.name || '产品',
                version: header.version || product?.version || '',
                type: '工程BOM',
                status: String(header.status ?? ''),
                createTime: header.createdTime ? String(header.createdTime).slice(0, 10) : '',
                createUser: header.createdBy || '',
                tree: rootNode
              }])
              return
            }
          }
        }

        const response: any = await plmApi.productData.getBomsByProductId(productId)
        const list = unwrapListResponse<any>(response)

        const nodeMap = new Map<string, any>()
        const rootNode = {
          id: 'root',
          name: this.materials.find(m => m.id === productId)?.name || '产品',
          code: this.materials.find(m => m.id === productId)?.code || '',
          spec: this.materials.find(m => m.id === productId)?.spec || '',
          unit: this.materials.find(m => m.id === productId)?.unit || '件',
          qty: 1,
          children: [] as any[]
        }

        list.forEach((r: any) => {
          const node = {
            id: String(r.id ?? ''),
            name: r.materialName,
            code: r.materialCode,
            spec: r.materialSpec,
            unit: r.unit,
            qty: Number(r.quantity ?? 0),
            children: [] as any[]
          }
          nodeMap.set(String(r.id ?? ''), node)
        })

        list.forEach((r: any) => {
          const id = String(r.id ?? '')
          const parentId = r.parentId !== null && r.parentId !== undefined ? String(r.parentId) : ''
          const node = nodeMap.get(id)
          if (!node) return
          if (parentId && nodeMap.has(parentId)) {
            nodeMap.get(parentId).children.push(node)
          } else {
            rootNode.children.push(node)
          }
        })

        const bom = {
          id: String(productId),
          productCode: rootNode.code,
          productName: rootNode.name,
          version: list?.[0]?.version || '',
          type: '工程BOM',
          status: list?.[0]?.status || '',
          createTime: '',
          createUser: list?.[0]?.createdBy || '',
          tree: rootNode
        }

        this.setBoms([bom])
      } catch (error) {
        console.error('获取BOM数据失败:', error)
      }
    },

    async fetchDocuments() {
      try {
        const response: any = await plmApi.productData.getDocumentsByPage({
          page: this.pagination.page,
          size: this.pagination.pageSize,
          keyword: this.documentSearchKeyword || undefined,
          type: this.documentTypeFilter !== 'all' ? this.documentTypeFilter : undefined
        })
        const page = unwrapPageResponse<any>(response)
        const rows = page.list.map((d: any) => ({
          id: String(d.id ?? ''),
          code: d.docCode,
          title: d.title,
          type: d.docType,
          category: d.category,
          version: d.version,
          status: d.status,
          author: d.author,
          createTime: d.createdTime ? String(d.createdTime).slice(0, 10) : '',
          updateTime: d.updatedTime ? String(d.updatedTime).slice(0, 10) : '',
          fileSize: d.fileSize,
          fileType: d.fileFormat,
          fileName: d.fileName,
          filePath: d.filePath,
          remark: d.remark
        }))
        this.setDocuments(rows)
        this.pagination.total = page.total || rows.length
      } catch (error) {
        console.error('获取文档数据失败:', error)
      }
    },

    async createMaterial(data: any) {
      const response: any = await plmApi.productData.createProduct(data)
      return unwrapResponseData<any>(response)
    },

    async updateMaterial(id: string | number, data: any) {
      const response: any = await plmApi.productData.updateProduct(id, data)
      return unwrapResponseData<any>(response)
    },

    async deleteMaterial(id: string | number) {
      await plmApi.productData.deleteProduct(id)
      return true
    },

    async createDocument(data: any) {
      const payload = {
        docCode: data.code,
        title: data.title,
        docType: data.type,
        category: data.category,
        version: data.version,
        status: data.status,
        author: data.author,
        fileName: data.fileName,
        filePath: data.filePath,
        fileSize: data.fileSize,
        fileFormat: data.fileType,
        remark: data.remark
      }
      const response: any = await plmApi.productData.createDocument(payload)
      return unwrapResponseData<any>(response)
    },

    async updateDocument(id: string | number, data: any) {
      const payload = {
        id,
        docCode: data.code,
        title: data.title,
        docType: data.type,
        category: data.category,
        version: data.version,
        status: data.status,
        author: data.author,
        fileName: data.fileName,
        filePath: data.filePath,
        fileSize: data.fileSize,
        fileFormat: data.fileType,
        remark: data.remark
      }
      const response: any = await plmApi.productData.updateDocument(id, payload)
      return unwrapResponseData<any>(response)
    },

    async deleteDocument(id: string | number) {
      await plmApi.productData.deleteDocument(id)
      return true
    },

    async uploadDocument(formData: FormData) {
      const response: any = await plmApi.productData.uploadDocument(formData)
      return unwrapResponseData<any>(response)
    },

    async downloadDocument(id: string | number) {
      return plmApi.productData.downloadDocument(id)
    },

    async checkoutBom(productId: string | number) {
      await plmApi.productData.checkoutBom(productId)
      return true
    },

    async checkinBom(productId: string | number) {
      await plmApi.productData.checkinBom(productId)
      return true
    },

    async releaseBom(productId: string | number) {
      await plmApi.productData.releaseBom(productId)
      return true
    },

    async compareBomVersions(productId: string | number, fromVersion: string, toVersion: string) {
      const response: any = await plmApi.productData.compareBomVersions(productId, fromVersion, toVersion)
      return unwrapResponseData<any>(response)
    }
  }
})
