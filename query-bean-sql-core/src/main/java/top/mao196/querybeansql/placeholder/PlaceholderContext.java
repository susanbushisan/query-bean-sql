package top.mao196.querybeansql.placeholder;

import top.mao196.querybeansql.core.FilterCondition;
import top.mao196.querybeansql.core.SearchEntitiesRequestDTO;
import top.mao196.querybeansql.core.SearchFilter;

import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 占位符上下文，包含运行时参数值
 * 参数来源：请求体的 parameters 字段和 filter conditions 中的值
 *
 * @author maoju
 * @since 2025/1/23
 */
public class PlaceholderContext {

    private final Map<String, Object> parameters;

    /**
     * 私有构造函数，使用 fromRequest 工厂方法创建实例
     */
    private PlaceholderContext(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * 创建空的 PlaceholderContext（用于测试）
     */
    public static PlaceholderContext empty() {
        return new PlaceholderContext(new HashMap<>());
    }

    /**
     * 使用给定的参数创建 PlaceholderContext（用于测试）
     */
    public static PlaceholderContext of(Map<String, Object> params) {
        return new PlaceholderContext(new HashMap<>(params));
    }

    /**
     * 从请求 DTO 构建上下文
     */
    public static PlaceholderContext fromRequest(SearchEntitiesRequestDTO request) {
        Map<String, Object> params = new HashMap<>();

        // 1. 添加 parameters 字段中的参数
        if (request.getParameters() != null) {
            params.putAll(request.getParameters());
        }

        // 2. 从 filter conditions 中提取值
        if (request.getFilter() != null) {
            extractValuesFromFilter(request.getFilter(), params);
        }

        // 3. 添加特殊参数：limit 和 offset
        if (request.getLimit() != null) {
            params.put("limit", String.valueOf(request.getLimit()));
        }
        if (request.getOffset() != null) {
            params.put("offset", String.valueOf(request.getOffset()));
        }

        return new PlaceholderContext(params);
    }

    /**
     * 从 filter 条件树中递归提取所有值
     */
    private static void extractValuesFromFilter(SearchFilter filter, Map<String, Object> params) {
        if (filter == null || filter.getConditions() == null) {
            return;
        }

        for (FilterCondition condition : filter.getConditions()) {
            extractValuesFromCondition(condition, params);
        }
    }

    /**
     * 从 FilterCondition 中递归提取值
     */
    private static void extractValuesFromCondition(FilterCondition condition, Map<String, Object> params) {
        if (condition == null) {
            return;
        }

        if (condition.isGroup()) {
            // 递归处理嵌套条件组
            List<FilterCondition> conditions = condition.getConditions();
            if (conditions != null) {
                for (FilterCondition child : conditions) {
                    extractValuesFromCondition(child, params);
                }
            }
        } else {
            // 添加条件值
            String property = condition.getProperty();
            Object value = condition.getValue();
            if (property != null && value != null) {
                params.put(property, convertValue(value));
            }
        }
    }

    /**
     * 转换值为字符串（用于占位符替换）
     */
    private static Object convertValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection) {
            // 多值情况，返回列表
            return value;
        }
        // 基本类型直接返回
        return value;
    }

    /**
     * 获取参数值
     */
    public Object get(String name) {
        return parameters.get(name);
    }

    /**
     * 获取参数值（字符串形式）
     */
    public String getAsString(String name) {
        Object value = parameters.get(name);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    /**
     * 检查参数是否存在且非空
     */
    public boolean contains(String name) {
        Object value = parameters.get(name);
        return value != null && !isEmptyValue(value);
    }

    /**
     * 检查值是否为空
     * 注意：Boolean.FALSE 也被视为"空"，因为条件块中需要能表达"条件为假"
     */
    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Boolean && !((Boolean) value)) {
            // Boolean.FALSE 视为空值（条件不满足）
            return true;
        }
        if (value instanceof String && ((String) value).isEmpty()) {
            return true;
        }
        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 获取所有参数（用于调试）
     */
    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(parameters);
    }
}
