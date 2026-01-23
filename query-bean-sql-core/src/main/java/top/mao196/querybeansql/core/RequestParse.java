package top.mao196.querybeansql.core;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mao196.querybeansql.config.QueryBeanConfig;
import top.mao196.querybeansql.placeholder.FunctionPlaceholderParser;
import top.mao196.querybeansql.placeholder.PlaceholderContext;
import top.mao196.querybeansql.util.QueryUtils;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author maoju
 * @since 2025/3/14
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestParse {

    private final QueryBeanConfig queryBeanConfig;

    public RequestParseResult parse(SearchEntitiesRequestDTO requestDTO, @NonNull ViewDescriptor viewDescriptor) {
        RequestParseResult result = new RequestParseResult();

        // 先处理 SQL 模板中的占位符
        String processedSql = processSqlPlaceholder(viewDescriptor.getSql(), requestDTO);

        // 处理 column,根据 fields 参数构建列
        // 如果没有指定 fields，则返回所有字段
        String columns = buildColumns(viewDescriptor, requestDTO.getFields());
        result.setColumn(columns);

        // 设置处理后的 SQL
        result.setViewSql(processedSql);

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
            String orderPart = requestDTO.getSortOrder().getOrderByList().stream()
                    .map(orderBy -> {
                        // 验证排序字段是否属于视图，防止 SQL 注入
                        ViewFiledDescriptor fieldDescriptor = viewDescriptor.findFieldDescriptor(orderBy.getProperty());
                        if (fieldDescriptor == null) {
                            throw new QueryBeanSqlException("Invalid sort property: " + orderBy.getProperty());
                        }
                        return fieldDescriptor.getColumnName() + " " + orderBy.getOrder().getType();
                    })
                    .collect(Collectors.joining(", "));
            result.setOrder("ORDER BY " + orderPart);
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

            // 验证运算符是否与字段类型兼容
            validateOperatorForType(operator, fieldDescriptor);

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

    /**
     * 处理 SQL 模板中的占位符
     * @param sql 原始 SQL 模板
     * @param requestDTO 请求参数
     * @return 处理后的 SQL
     */
    private String processSqlPlaceholder(String sql, SearchEntitiesRequestDTO requestDTO) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }
        log.info("[PLACEHOLDER] Processing SQL: {}", sql);
        // 构建占位符上下文
        PlaceholderContext context = PlaceholderContext.fromRequest(requestDTO);
        // 解析占位符
        String result = FunctionPlaceholderParser.parse(sql, context);
        log.info("[PLACEHOLDER] Result SQL: {}", result);
        log.info("[PLACEHOLDER] Parameters: {}", context.getAll());
        return result;
    }

    /**
     * 根据字段选择器构建 SQL 列
     * @param viewDescriptor 视图描述符
     * @param requestedFields 请求的字段列表，null 或空表示返回所有字段
     * @return 列名字符串
     */
    private String buildColumns(ViewDescriptor viewDescriptor, List<String> requestedFields) {
        // 如果没有指定字段，返回所有字段
        if (CollUtil.isEmpty(requestedFields)) {
            return viewDescriptor.getFields().stream()
                    .map(x -> x.getColumnName() + " AS " + x.getRawName())
                    .collect(Collectors.joining(", "));
        }

        // 根据请求的字段构建列
        return requestedFields.stream()
                .map(fieldName -> {
                    ViewFiledDescriptor fieldDescriptor = viewDescriptor.findFieldDescriptor(fieldName);
                    if (fieldDescriptor == null) {
                        throw new QueryBeanSqlException("Invalid field: " + fieldName);
                    }
                    return fieldDescriptor.getColumnName() + " AS " + fieldDescriptor.getRawName();
                })
                .collect(Collectors.joining(", "));
    }

    /**
     * 验证运算符是否与字段类型兼容
     * @param operator 运算符
     * @param fieldDescriptor 字段描述符
     * @throws QueryBeanSqlException 如果运算符不兼容
     */
    private void validateOperatorForType(FilterOp operator, ViewFiledDescriptor fieldDescriptor) {
        Class<?> fieldType = fieldDescriptor.getClz();
        Set<FilterOp> allowedOps = getAllowedOperatorsForType(fieldType);

        if (!allowedOps.contains(operator)) {
            throw new QueryBeanSqlException(
                    String.format("Operator '%s' is not supported for field type '%s'. Allowed operators: %s",
                            operator.getStringOp(),
                            fieldType.getSimpleName(),
                            allowedOps.stream().map(FilterOp::getStringOp).collect(Collectors.joining(", "))));
        }
    }

    /**
     * 根据字段类型获取允许的运算符集合
     * @param fieldType 字段的 Java 类型
     * @return 允许的运算符集合
     */
    private Set<FilterOp> getAllowedOperatorsForType(Class<?> fieldType) {
        Set<FilterOp> operators = new HashSet<>();

        // 所有类型都支持的运算符
        operators.add(FilterOp.IS_NULL);
        operators.add(FilterOp.NOT_EMPTY);

        if (isStringType(fieldType)) {
            // 字符串类型支持的操作符
            operators.add(FilterOp.EQUAL);
            operators.add(FilterOp.NOT_EQUAL);
            operators.add(FilterOp.CONTAINS);
            operators.add(FilterOp.DOES_NOT_CONTAIN);
            operators.add(FilterOp.STARTS_WITH);
            operators.add(FilterOp.ENDS_WITH);
            operators.add(FilterOp.IN);
            operators.add(FilterOp.NOT_IN);
        } else if (isNumericType(fieldType)) {
            // 数值类型支持的操作符
            operators.add(FilterOp.EQUAL);
            operators.add(FilterOp.NOT_EQUAL);
            operators.add(FilterOp.GREATER);
            operators.add(FilterOp.GREATER_OR_EQUAL);
            operators.add(FilterOp.LESSER);
            operators.add(FilterOp.LESSER_OR_EQUAL);
            operators.add(FilterOp.IN);
            operators.add(FilterOp.NOT_IN);
        } else if (isBooleanType(fieldType)) {
            // 布尔类型支持的操作符
            operators.add(FilterOp.EQUAL);
            operators.add(FilterOp.NOT_EQUAL);
        } else if (isDateTimeType(fieldType)) {
            // 日期时间类型支持的操作符
            operators.add(FilterOp.EQUAL);
            operators.add(FilterOp.NOT_EQUAL);
            operators.add(FilterOp.GREATER);
            operators.add(FilterOp.GREATER_OR_EQUAL);
            operators.add(FilterOp.LESSER);
            operators.add(FilterOp.LESSER_OR_EQUAL);
        }

        return operators;
    }

    /**
     * 判断是否为字符串类型
     */
    private boolean isStringType(Class<?> type) {
        return CharSequence.class.isAssignableFrom(type) || type == String.class;
    }

    /**
     * 判断是否为数值类型
     */
    private boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type)
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class;
    }

    /**
     * 判断是否为布尔类型
     */
    private boolean isBooleanType(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    /**
     * 判断是否为日期时间类型
     */
    private boolean isDateTimeType(Class<?> type) {
        return Temporal.class.isAssignableFrom(type)
                || type == java.time.LocalDate.class
                || type == java.time.LocalDateTime.class
                || type == java.time.LocalTime.class
                || type == java.util.Date.class
                || type == java.sql.Timestamp.class
                || type == java.sql.Date.class;
    }

}
