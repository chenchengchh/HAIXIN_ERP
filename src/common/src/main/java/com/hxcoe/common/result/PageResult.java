package com.hxcoe.common.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果类
 *
 * @param <T> 响应数据类型
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    @JsonProperty("size")
    private Integer pageSize;

    @JsonProperty("page")
    private Integer currentPage;

    /**
     * 总页数
     */
    private Long totalPages;

    @JsonProperty("list")
    private List<T> records;

    /**
     * 是否有前一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有后一页
     */
    private Boolean hasNext;

    public PageResult() {
    }

    public PageResult(
            Long total,
            Integer pageSize,
            Integer currentPage,
            Long totalPages,
            List<T> records,
            Boolean hasPrevious,
            Boolean hasNext
    ) {
        this.total = total;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.records = records;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    /**
     * 构建分页响应结果
     *
     * @param total       总记录数
     * @param pageSize    每页记录数
     * @param currentPage 当前页码
     * @param records     数据列表
     * @param <T>         响应数据类型
     * @return 分页响应结果
     */
    public static <T> PageResult<T> build(Long total, Integer pageSize, Integer currentPage, List<T> records) {
        // 计算总页数
        long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;

        // 判断是否有前一页和后一页
        boolean hasPrevious = currentPage > 1;
        boolean hasNext = currentPage < totalPages;

        return new PageResult<>(total, pageSize, currentPage, totalPages, records, hasPrevious, hasNext);
    }

    /**
     * 构建空分页响应结果
     *
     * @param <T> 响应数据类型
     * @return 空分页响应结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L, 0, 0, 0L, null, false, false);
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }
}
