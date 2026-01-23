package top.mao196.querybeansql.core;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author maoju
 * @since 2024/11/27
 **/
@Data
public class SearchEntitiesRequestDTO {
    private SearchFilter filter;
    private Integer limit;
    private Integer offset;
    private String sort;
    private Boolean returnCount;

    /**
     * 占位符参数
     * 用于在 SQL 模板中替换占位符
     * 例如: {{userId}} 会从 parameters 中获取 userId 的值
     */
    private Map<String, Object> parameters;

    /**
     * 字段选择器
     * 指定要返回的字段列表，缺省则返回所有字段
     * 例如: ["id", "name", "createdAt"]
     */
    private List<String> fields;


    @JsonIgnore
    public SearchOrder getSortOrder() {
        return SearchOrder.parse(sort);
    }

    @JsonIgnore
    public void setSortOrder(SearchOrder order) {
        sort = order.toSort();
    }
}
