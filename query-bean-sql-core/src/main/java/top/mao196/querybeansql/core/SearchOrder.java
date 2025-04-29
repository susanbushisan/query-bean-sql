package top.mao196.querybeansql.core;


import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author maoju
 * @since 2024/11/27
 **/
@Data
public class SearchOrder {

    private List<OrderBy> orderByList = new ArrayList<>();

    public SearchOrder addOrderBy(OrderByType order, String property) {
        OrderBy orderBy = new OrderBy();
        orderBy.setOrder(order);
        orderBy.setProperty(property);
        orderByList.add(orderBy);
        return this;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderBy {
        /**
         * null 或者asc或者desc
         */
        private OrderByType order;

        private String property;
    }

    @Getter
    @AllArgsConstructor
    public enum OrderByType {
        ASC("asc", "+"),
        DESC("desc", "-"),
        DEFAULT("", "");

        private final String type;

        private final String sign;
    }

    public static SearchOrder parse(String sort) {
        SearchOrder searchOrder = new SearchOrder();
        if (StrUtil.isBlankIfStr(sort)) {
            return searchOrder;
        }
        Iterable<String> iterableColumns = StrUtil.split(sort,",");
        for (String column : iterableColumns) {
            OrderByType order = OrderByType.DEFAULT;
            if (column.startsWith(OrderByType.DESC.getSign())) {
                order = OrderByType.DESC;
                column = column.substring(1);
            } else if (column.startsWith(OrderByType.ASC.getSign())) {
                order = OrderByType.ASC;
                column = column.substring(1);
            }
            searchOrder.addOrderBy(order, column);
        }
        return searchOrder;
    }

    public String toSort() {
       return this.orderByList.stream().map(x -> x.getOrder().getSign() + x.getProperty()).collect(Collectors.joining(StrUtil.COMMA));
    }
}
