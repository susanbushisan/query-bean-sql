package top.mao196.querybeansql.core;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

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


    @JsonIgnore
    public SearchOrder getSortOrder() {
        return SearchOrder.parse(sort);
    }

    @JsonIgnore
    public void setSortOrder(SearchOrder order) {
        sort = order.toSort();
    }
}
