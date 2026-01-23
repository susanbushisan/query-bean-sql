package top.mao196.querybeansql.placeholder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.mao196.querybeansql.core.FilterCondition;
import top.mao196.querybeansql.core.FilterType;
import top.mao196.querybeansql.core.SearchEntitiesRequestDTO;
import top.mao196.querybeansql.core.SearchFilter;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 占位符上下文单元测试
 */
@DisplayName("占位符上下文测试")
class PlaceholderContextTest {

    @Test
    @DisplayName("创建空上下文")
    void testEmptyContext() {
        PlaceholderContext context = PlaceholderContext.empty();
        assertNull(context.get("any"));
        assertFalse(context.contains("any"));
    }

    @Test
    @DisplayName("从 Map 创建上下文")
    void testOf() {
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", 123);

        PlaceholderContext context = PlaceholderContext.of(params);
        assertEquals("value1", context.get("key1"));
        assertEquals(123, context.get("key2"));
    }

    @Test
    @DisplayName("getAsString 返回字符串形式")
    void testGetAsString() {
        Map<String, Object> params = new HashMap<>();
        params.put("intValue", 42);
        params.put("stringValue", "test");

        PlaceholderContext context = PlaceholderContext.of(params);
        assertEquals("42", context.getAsString("intValue"));
        assertEquals("test", context.getAsString("stringValue"));
        assertNull(context.getAsString("missing"));
    }

    @Test
    @DisplayName("contains 检查存在性")
    void testContains() {
        Map<String, Object> params = new HashMap<>();
        params.put("exists", "value");
        PlaceholderContext context = PlaceholderContext.of(params);

        assertTrue(context.contains("exists"));
        assertFalse(context.contains("missing"));
        assertFalse(context.contains("existsEmpty"));
    }

    @Test
    @DisplayName("空字符串视为不存在")
    void testEmptyStringNotExists() {
        Map<String, Object> params = new HashMap<>();
        params.put("empty", "");
        PlaceholderContext context = PlaceholderContext.of(params);

        // 空字符串视为不存在（条件不满足）
        assertFalse(context.contains("empty"));
        assertFalse(context.contains("emptyKey"));
    }

    @Test
    @DisplayName("Boolean.FALSE 视为不存在")
    void testBooleanFalseNotExists() {
        Map<String, Object> params = new HashMap<>();
        params.put("flag", Boolean.FALSE);
        PlaceholderContext context = PlaceholderContext.of(params);

        assertFalse(context.contains("flag"));
    }

    @Test
    @DisplayName("Boolean.TRUE 视为存在")
    void testBooleanTrueExists() {
        Map<String, Object> params = new HashMap<>();
        params.put("flag", Boolean.TRUE);
        PlaceholderContext context = PlaceholderContext.of(params);

        assertTrue(context.contains("flag"));
    }

    @Test
    @DisplayName("空集合视为不存在")
    void testEmptyCollectionNotExists() {
        Map<String, Object> params = new HashMap<>();
        params.put("emptyList", Collections.emptyList());
        PlaceholderContext context = PlaceholderContext.of(params);

        assertFalse(context.contains("emptyList"));
    }

    @Test
    @DisplayName("非空集合视为存在")
    void testNonEmptyCollectionExists() {
        Map<String, Object> params = new HashMap<>();
        params.put("list", Arrays.asList("a", "b"));
        PlaceholderContext context = PlaceholderContext.of(params);

        assertTrue(context.contains("list"));
    }

    @Test
    @DisplayName("从请求 DTO 构建上下文 - 基本参数")
    void testFromRequestWithParameters() {
        SearchEntitiesRequestDTO request = new SearchEntitiesRequestDTO();
        request.setParameters(Map.of("userId", "123", "status", "active"));

        PlaceholderContext context = PlaceholderContext.fromRequest(request);
        assertEquals("123", context.get("userId"));
        assertEquals("active", context.get("status"));
    }

    @Test
    @DisplayName("从请求 DTO 构建上下文 - 添加 limit 和 offset")
    void testFromRequestWithLimitOffset() {
        SearchEntitiesRequestDTO request = new SearchEntitiesRequestDTO();
        request.setLimit(100);
        request.setOffset(20);

        PlaceholderContext context = PlaceholderContext.fromRequest(request);
        assertEquals("100", context.get("limit"));
        assertEquals("20", context.get("offset"));
    }

    @Test
    @DisplayName("从请求 DTO 构建上下文 - 从 filter conditions 提取值")
    void testFromRequestWithFilter() {
        SearchEntitiesRequestDTO request = new SearchEntitiesRequestDTO();

        FilterCondition condition = new FilterCondition();
        condition.setProperty("userId");
        condition.setValue("456");

        SearchFilter filter = new SearchFilter();
        filter.setConditions(Collections.singletonList(condition));
        request.setFilter(filter);

        PlaceholderContext context = PlaceholderContext.fromRequest(request);
        assertEquals("456", context.get("userId"));
    }

    @Test
    @DisplayName("从请求 DTO 构建上下文 - 嵌套条件组")
    void testFromRequestWithNestedFilter() {
        SearchEntitiesRequestDTO request = new SearchEntitiesRequestDTO();

        FilterCondition innerCondition = new FilterCondition();
        innerCondition.setProperty("status");
        innerCondition.setValue("active");

        FilterCondition groupCondition = new FilterCondition();
        groupCondition.setGroup(FilterType.AND);
        groupCondition.setConditions(Collections.singletonList(innerCondition));

        SearchFilter filter = new SearchFilter();
        filter.setConditions(Collections.singletonList(groupCondition));
        request.setFilter(filter);

        PlaceholderContext context = PlaceholderContext.fromRequest(request);
        assertEquals("active", context.get("status"));
    }

    @Test
    @DisplayName("从请求 DTO 构建上下文 - 多个条件")
    void testFromRequestWithMultipleConditions() {
        SearchEntitiesRequestDTO request = new SearchEntitiesRequestDTO();

        FilterCondition condition1 = new FilterCondition();
        condition1.setProperty("userId");
        condition1.setValue("123");

        FilterCondition condition2 = new FilterCondition();
        condition2.setProperty("status");
        condition2.setValue("active");

        SearchFilter filter = new SearchFilter();
        filter.setConditions(Arrays.asList(condition1, condition2));
        request.setFilter(filter);

        PlaceholderContext context = PlaceholderContext.fromRequest(request);
        assertEquals("123", context.get("userId"));
        assertEquals("active", context.get("status"));
    }

    @Test
    @DisplayName("从请求 DTO 构建上下文 - IN 查询的多值")
    void testFromRequestWithInQuery() {
        SearchEntitiesRequestDTO request = new SearchEntitiesRequestDTO();

        FilterCondition condition = new FilterCondition();
        condition.setProperty("ids");
        condition.setValue(Arrays.asList("1", "2", "3"));

        SearchFilter filter = new SearchFilter();
        filter.setConditions(Collections.singletonList(condition));
        request.setFilter(filter);

        PlaceholderContext context = PlaceholderContext.fromRequest(request);
        assertTrue(context.get("ids") instanceof Collection);
    }

    @Test
    @DisplayName("从请求 DTO 构建上下文 - 参数与 filter 合并")
    void testFromRequestMergesParametersAndFilter() {
        SearchEntitiesRequestDTO request = new SearchEntitiesRequestDTO();
        request.setParameters(Map.of("fromParam", "value1"));

        FilterCondition filterCondition = new FilterCondition();
        filterCondition.setProperty("fromFilter");
        filterCondition.setValue("value2");

        SearchFilter filter = new SearchFilter();
        filter.setConditions(Collections.singletonList(filterCondition));
        request.setFilter(filter);

        PlaceholderContext context = PlaceholderContext.fromRequest(request);
        assertEquals("value1", context.get("fromParam"));
        assertEquals("value2", context.get("fromFilter"));
    }

    @Test
    @DisplayName("getAll 返回不可修改的映射")
    void testGetAllReturnsUnmodifiable() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");
        PlaceholderContext context = PlaceholderContext.of(params);

        Map<String, Object> all = context.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.put("newKey", "newValue"));
    }

    @Test
    @DisplayName("null 值检查")
    void testNullValue() {
        Map<String, Object> params = new HashMap<>();
        params.put("nullValue", null);
        PlaceholderContext context = PlaceholderContext.of(params);

        assertNull(context.get("nullValue"));
        assertFalse(context.contains("nullValue"));
    }
}
