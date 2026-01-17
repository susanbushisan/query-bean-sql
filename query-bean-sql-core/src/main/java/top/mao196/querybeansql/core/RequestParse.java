package top.mao196.querybeansql.core;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mao196.querybeansql.config.QueryBeanConfig;
import top.mao196.querybeansql.util.QueryUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author maoju
 * @since 2025/3/14
 **/
@Component
@RequiredArgsConstructor
public class RequestParse {

    private final QueryBeanConfig queryBeanConfig;

    public RequestParseResult parse(SearchEntitiesRequestDTO requestDTO, @NonNull ViewDescriptor viewDescriptor) {
        RequestParseResult result = new RequestParseResult();
        // 处理colum,查询视图所有的字段
        // 如果没有注解执行字段就用字段名的驼峰转下划线
        String columns = viewDescriptor.getFields().stream().map(x-> x.getColumnName() + " AS " + x.getRawName()).collect(Collectors.joining(", "));
        result.setColumn(columns);
        // 处理where中的条件
        SearchFilter filter = requestDTO.getFilter();
        if (filter != null) {
            List<FilterCondition> conditions = filter.getConditions();
            FilterCondition rootCondition = new FilterCondition();
            rootCondition.setGroup(FilterType.AND);
            rootCondition.setConditions(conditions);
            Map<String, Object> params = new HashMap<>();
            result.setWhere("WHERE " + parseConditions(rootCondition, params, viewDescriptor));
            result.setParams(params);
        }

        // 处理order by
        if (CollUtil.isNotEmpty(requestDTO.getSortOrder().getOrderByList())) {
            StringBuilder sb = new StringBuilder();
            sb.append("ORDER BY ");
            for (SearchOrder.OrderBy orderBy : requestDTO.getSortOrder().getOrderByList()) {
                // 验证排序字段是否属于视图，防止 SQL 注入
                ViewFiledDescriptor fieldDescriptor = viewDescriptor.findFieldDescriptor(orderBy.getProperty());
                if (fieldDescriptor == null) {
                    throw new QueryBeanSqlException("Invalid sort property: " + orderBy.getProperty());
                }
                sb.append(fieldDescriptor.getColumnName()).append(" ").append(orderBy.getOrder().getType()).append(",");
            }
            result.setOrder(sb.toString());
        }

        // 处理limit,如果limit没有只指定了offset将不会生效
        int limit;
        if (requestDTO.getLimit() != null){
            limit = requestDTO.getLimit() > queryBeanConfig.getMaxLimit() ? queryBeanConfig.getMaxLimit() : requestDTO.getLimit();
        }else {
            limit = queryBeanConfig.getMaxLimit();
        }
        if (requestDTO.getOffset() != null) {
            result.setLimit(String.format("LIMIT %d,%d", requestDTO.getOffset(), limit));
        } else {
            result.setLimit(String.format("LIMIT %d", limit));
        }

        return result;
    }


    public String parseConditions(FilterCondition conditionObj, Map<String, Object> params, ViewDescriptor viewDescriptor) {
        if (conditionObj.isGroup()) {
            return conditionObj.getConditions().stream()
                    .map(x -> parseConditions(x, params, viewDescriptor))
                    .collect(Collectors.joining(conditionObj.getGroup().name(), "(", ")"));
        } else {
            FilterOp operator = conditionObj.getOperator();
            ViewFiledDescriptor fieldDescriptor = viewDescriptor.findFieldDescriptor(conditionObj.getProperty());
            String columnName = fieldDescriptor.getColumnName();
            if (operator == FilterOp.IS_NULL || operator == FilterOp.NOT_EMPTY) {
                return String.format("%s %s", columnName, operator.getSqlOp());
            } else {
                String queryParamName = conditionObj.getProperty() + "_" + RandomUtil.randomString(RandomUtil.BASE_CHAR, 5);
                if (operator == FilterOp.IN || operator == FilterOp.NOT_IN) {
                    if (!(conditionObj.getValue() instanceof Collection<?>)) {
                        throw new QueryBeanSqlException(String.format("operator: %s, value: %s", operator, conditionObj.getValue()));
                    }
                    List<Object> parsedArrayValue = new ArrayList<>();
                    for (Object o : (Collection<?>) conditionObj.getValue()) {
                        parsedArrayValue.add(o);
                    }
                    params.put(queryParamName, parsedArrayValue);
                    return String.format("%s %s (:%s)", columnName, operator.getSqlOp(), queryParamName);
                    // 解析数组
                } else {
                    params.put(queryParamName, transValue(operator, conditionObj.getValue()));
                    return String.format("%s %s :%s", columnName, operator.getSqlOp(), queryParamName);
                }
            }
        }
    }

    public Object transValue(FilterOp operator, Object value) {
        return switch (operator) {
            case CONTAINS, DOES_NOT_CONTAIN -> "%" + QueryUtils.escapeForLike((String) value) + "%";
            case STARTS_WITH -> QueryUtils.escapeForLike((String) value) + "%";
            case ENDS_WITH -> "%" + QueryUtils.escapeForLike((String) value);
            default -> value;
        };
    }

}
