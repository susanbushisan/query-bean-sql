package top.mao196.querybeansql.placeholder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 占位符解析器单元测试
 */
@DisplayName("占位符解析器测试")
class FunctionPlaceholderParserTest {

    @Test
    @DisplayName("基本参数替换")
    void testSimpleReplacement() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "123");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user WHERE user_id = ${userId}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user WHERE user_id = 123", result);
    }

    @Test
    @DisplayName("多个参数替换")
    void testMultipleReplacements() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "123");
        params.put("status", "active");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user WHERE user_id = ${userId} AND status = '${status}'";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user WHERE user_id = 123 AND status = 'active'", result);
    }

    @Test
    @DisplayName("参数为空时使用默认值")
    void testDefaultValue() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", null);
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user WHERE user_id = ${userId:-1}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user WHERE user_id = -1", result);
    }

    @Test
    @DisplayName("后缀条件为真时保留内容")
    void testSuffixConditionTrue() {
        Map<String, Object> params = new HashMap<>();
        params.put("hasOrder", true);
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user ${hasOrder? JOIN order o}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertTrue(result.contains("JOIN order o"));
    }

    @Test
    @DisplayName("后缀条件为假时移除内容")
    void testSuffixConditionFalse() {
        Map<String, Object> params = new HashMap<>();
        params.put("hasOrder", false);
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user WHERE 1=1 ${hasOrder? AND order_id IS NOT NULL}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user WHERE 1=1 ", result);
    }

    @Test
    @DisplayName("后缀条件比较 - 等于")
    void testSuffixConditionEquals() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM order ${status == 'active'? WHERE status = 'active'}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertTrue(result.contains("WHERE status = 'active'"));
    }

    @Test
    @DisplayName("后缀条件比较 - 不等于")
    void testSuffixConditionNotEquals() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "pending");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM order ${status == 'active'? WHERE status = 'active'}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertFalse(result.contains("WHERE"));
    }

    @Test
    @DisplayName("后缀条件嵌套占位符")
    void testSuffixConditionWithNestedPlaceholder() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "123");
        params.put("status", "active");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM order WHERE 1=1 ${userId? AND user_id = ${userId}} ${status? AND status = '${status}'}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM order WHERE 1=1 AND user_id = 123 AND status = 'active'", result);
    }

    @Test
    @DisplayName("空 SQL")
    void testEmptySql() {
        PlaceholderContext context = PlaceholderContext.empty();
        String result = FunctionPlaceholderParser.parse("", context);
        assertEquals("", result);
    }

    @Test
    @DisplayName("无占位符 SQL")
    void testNoPlaceholder() {
        PlaceholderContext context = PlaceholderContext.empty();
        String sql = "SELECT * FROM user WHERE id = 1";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals(sql, result);
    }

    @Test
    @DisplayName("多个后缀条件同时为真")
    void testMultipleSuffixConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "123");
        params.put("status", "active");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM order ${userId? AND user_id = ${userId}} ${status? AND status = '${status}'}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM order AND user_id = 123 AND status = 'active'", result);
    }

    @Test
    @DisplayName("多个后缀条件混合真假")
    void testMixedSuffixConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "123");
        // status 不设置，条件为假
        PlaceholderContext context = PlaceholderContext.of(params);

        // 注意：两个条件之间的空格会被保留
        String sql = "SELECT * FROM order WHERE 1=1 ${userId? AND user_id = ${userId}} ${status? AND status = '${status}'}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM order WHERE 1=1 AND user_id = 123 ", result);
    }

    @Test
    @DisplayName("值包含单引号时自动转义")
    void testEscapeSingleQuote() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "O'Reilly");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM author WHERE name = ${name}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM author WHERE name = 'O''Reilly'", result);
    }

    @Test
    @DisplayName("IN 查询中使用占位符")
    void testInQueryWithPlaceholder() {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList("1", "2", "3"));
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user WHERE id IN (${ids})";
        String result = FunctionPlaceholderParser.parse(sql, context);
        // 字符串值会加引号
        assertEquals("SELECT * FROM user WHERE id IN (1, 2, 3)", result);
    }

    @Test
    @DisplayName("占位符位于 SQL 开头")
    void testPlaceholderAtStart() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "123");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "${userId} IN (SELECT user_id FROM order)";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("123 IN (SELECT user_id FROM order)", result);
    }

    @Test
    @DisplayName("占位符位于 SQL 末尾")
    void testPlaceholderAtEnd() {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", "100");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user LIMIT ${limit}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user LIMIT 100", result);
    }

    @Test
    @DisplayName("连续多个占位符")
    void testConsecutivePlaceholders() {
        Map<String, Object> params = new HashMap<>();
        params.put("col1", "a");
        params.put("col2", "b");
        params.put("col3", "c");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT ${col1}, ${col2}, ${col3} FROM table";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT a, b, c FROM table", result);
    }

    @Test
    @DisplayName("条件比较使用单引号")
    void testConditionWithSingleQuotes() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM order ${status == 'active'? WHERE status = 'active'}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertTrue(result.contains("WHERE status = 'active'"));
    }

    @Test
    @DisplayName("集合为空时使用默认值")
    void testEmptyCollectionWithDefault() {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Collections.emptyList());
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM user WHERE id IN (${ids:0})";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user WHERE id IN (0)", result);
    }

    @Test
    @DisplayName("后缀条件内的变量不存在时忽略")
    void testConditionWithMissingVariable() {
        Map<String, Object> params = new HashMap<>();
        params.put("hasOrder", true);
        // userId 不设置
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM order ${hasOrder? AND user_id = ${userId}}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        // 变量不存在时返回空字符串，所以结果是 AND user_id =
        assertEquals("SELECT * FROM order AND user_id =", result);
    }

    @Test
    @DisplayName("未闭合的占位符保持原样")
    void testUnclosedPlaceholder() {
        PlaceholderContext context = PlaceholderContext.empty();
        String sql = "SELECT * FROM user WHERE id = ${";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user WHERE id = ${", result);
    }

    @Test
    @DisplayName("嵌套占位符")
    void testNestedPlaceholders() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "user");
        params.put("id", "123");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM ${tableName} WHERE id = ${id}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM user WHERE id = 123", result);
    }

    @Test
    @DisplayName("条件内包含完整 WHERE 子句")
    void testConditionWithWhereClause() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", "123");
        PlaceholderContext context = PlaceholderContext.of(params);

        String sql = "SELECT * FROM order ${userId? WHERE user_id = ${userId} AND status = 'active'}";
        String result = FunctionPlaceholderParser.parse(sql, context);
        assertEquals("SELECT * FROM order WHERE user_id = 123 AND status = 'active'", result);
    }
}
