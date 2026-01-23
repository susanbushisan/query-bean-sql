package top.mao196.querybeansql.placeholder;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 占位符解析器
 * 支持 ${} 语法：
 * - ${name} - 变量替换
 * - ${name:default} - 带默认值
 * - ${name? content} - 后缀条件，条件为真时渲染 content
 * - ${name == 'value'? content} - 值匹配条件
 *
 * @author maoju
 * @since 2025/1/23
 */
public class FunctionPlaceholderParser {

    private final PlaceholderContext context;

    public FunctionPlaceholderParser(PlaceholderContext context) {
        this.context = context;
    }

    /**
     * 解析 SQL 模板中的所有占位符
     */
    public String parse(String sql) {
        return parseInternal(sql);
    }

    private String parseInternal(String sql) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = sql.length();

        while (i < len) {
            // 查找 ${ 开头
            int dollarIdx = sql.indexOf("${", i);
            if (dollarIdx == -1) {
                result.append(sql, i, len);
                break;
            }

            result.append(sql, i, dollarIdx);
            i = dollarIdx + 2;

            // 找到对应的 }
            int closeIdx = findMatchingClose(sql, i);
            if (closeIdx == -1) {
                result.append("${");
                break;
            }

            String content = sql.substring(i, closeIdx);
            String rendered = renderPlaceholder(content);

            result.append(rendered);
            i = closeIdx + 1; // 跳过 }
        }

        return result.toString();
    }

    /**
     * 查找匹配的 }（处理嵌套的 ${}）
     */
    private int findMatchingClose(String sql, int start) {
        int depth = 1;
        int i = start;
        int len = sql.length();

        while (i < len && depth > 0) {
            if (sql.startsWith("${", i)) {
                depth++;
                i += 2;
            } else if (sql.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
                i++;
            } else {
                i++;
            }
        }
        return -1;
    }

    /**
     * 渲染占位符内容
     */
    private String renderPlaceholder(String content) {
        content = content.trim();
        if (content.isEmpty()) return "";

        // 检查是否是后缀条件 ${var? content}
        int qIdx = content.indexOf('?');
        if (qIdx > 0) {
            String condition = content.substring(0, qIdx).trim();
            String body = content.substring(qIdx + 1).trim();

            if (condition.contains("==")) {
                // 值比较: ${status == 'active'? body}
                String[] parts = condition.split("==", 2);
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim();

                // 去掉值的引号
                if (expectedValue.startsWith("'") && expectedValue.endsWith("'")) {
                    expectedValue = expectedValue.substring(1, expectedValue.length() - 1);
                }

                Object actualValue = context.get(varName);
                boolean conditionMet = actualValue != null && expectedValue.equals(String.valueOf(actualValue));

                return conditionMet ? parseInternal(body).trim() : "";
            } else {
                // 存在性检查: ${userId? AND user_id = ${userId}}
                boolean conditionMet = context.contains(condition);

                if (conditionMet) {
                    return parseInternal(body).trim();
                } else {
                    return "";
                }
            }
        }

        // 简单占位符: ${name} 或 ${name:default}
        return renderSimplePlaceholder(content);
    }

    /**
     * 渲染简单占位符
     */
    private String renderSimplePlaceholder(String content) {
        // 检查是否有默认值: ${name:default}
        if (content.contains(":")) {
            int colonIdx = content.indexOf(':');
            String name = content.substring(0, colonIdx).trim();
            String defaultValue = content.substring(colonIdx + 1).trim();

            Object value = context.get(name);
            if (value != null && !isEmptyValue(value)) {
                return escapeSqlValue(value);
            }
            return defaultValue;
        }

        // 普通变量
        Object value = context.get(content);
        if (value != null && !isEmptyValue(value)) {
            return escapeSqlValue(value);
        }
        return "";
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) return true;
        if (value instanceof String && ((String) value).isEmpty()) return true;
        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) return true;
        return false;
    }

    private String escapeSqlValue(Object value) {
        if (value instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object item : (Collection<?>) value) {
                if (!first) sb.append(", ");
                sb.append(escapeSingleValue(item));
                first = false;
            }
            return sb.toString();
        }
        return escapeSingleValue(value);
    }

    private String escapeSingleValue(Object value) {
        String str = String.valueOf(value);
        if (str.contains("'")) str = str.replace("'", "''");
        if (str.isEmpty() || !Pattern.matches("^[a-zA-Z0-9_.]+$", str)) {
            return "'" + str + "'";
        }
        return str;
    }

    public static String parse(String sql, PlaceholderContext context) {
        return new FunctionPlaceholderParser(context).parse(sql);
    }
}
