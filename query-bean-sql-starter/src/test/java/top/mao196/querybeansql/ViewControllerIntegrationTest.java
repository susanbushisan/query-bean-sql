package top.mao196.querybeansql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ViewController 集成测试
 * 测试 /rest/view/{viewName}/search 端点
 *
 * 测试请求使用 Map 构建，可直接复制 JSON 到接口工具（如 Postman）中复现测试场景
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ViewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("基本查询测试")
    class BasicSearchTests {

        @Test
        @DisplayName("查询所有用户 - 无条件查询")
        void searchAllUsers() throws Exception {
            // 请求示例: {"limit": 10}
            Map<String, Object> request = Map.of("limit", 10);

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(2)))
                    .andExpect(jsonPath("$.dataList[*].name", containsInAnyOrder("susan", "alice")));
        }

        @Test
        @DisplayName("查询返回记录数")
        void searchWithReturnCount() throws Exception {
            // 请求示例: {"limit": 10, "returnCount": true}
            Map<String, Object> request = Map.of(
                    "limit", 10,
                    "returnCount", true
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(2)))
                    .andExpect(jsonPath("$.count", is(2)));
        }
    }

    @Nested
    @DisplayName("筛选测试")
    class FilterTests {

        @Test
        @DisplayName("等于筛选 - EQUAL")
        void filterByEqual() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "name", "operator": "=", "value": "susan"}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "name",
                                    "operator", "=",
                                    "value", "susan"
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)))
                    .andExpect(jsonPath("$.dataList[0].name", is("susan")));
        }

        @Test
        @DisplayName("大于筛选 - GREATER")
        void filterByGreater() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "age", "operator": ">", "value": 25}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "age",
                                    "operator", ">",
                                    "value", 25
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)))
                    .andExpect(jsonPath("$.dataList[0].name", is("susan")));
        }

        @Test
        @DisplayName("小于筛选 - LESSER")
        void filterByLesser() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "age", "operator": "<", "value": 25}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "age",
                                    "operator", "<",
                                    "value", 25
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)))
                    .andExpect(jsonPath("$.dataList[0].name", is("alice")));
        }

        @Test
        @DisplayName("包含筛选 - CONTAINS")
        void filterByContains() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "name", "operator": "contains", "value": "ali"}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "name",
                                    "operator", "contains",
                                    "value", "ali"
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)))
                    .andExpect(jsonPath("$.dataList[0].name", is("alice")));
        }

        @Test
        @DisplayName("IN筛选 - IN")
        void filterByIn() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "name", "operator": "in", "value": ["susan", "alice"]}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "name",
                                    "operator", "in",
                                    "value", List.of("susan", "alice")
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(2)));
        }

        @Test
        @DisplayName("NOT_EQUAL 筛选")
        void filterByNotEqual() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "name", "operator": "<>", "value": "susan"}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "name",
                                    "operator", "<>",
                                    "value", "susan"
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)))
                    .andExpect(jsonPath("$.dataList[0].name", is("alice")));
        }

        @Test
        @DisplayName("IS_NULL 筛选 - 字段为空")
        void filterByIsNull() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "name", "operator": "isNull"}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "name",
                                    "operator", "isNull"
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(0)));
        }

        @Test
        @DisplayName("大于等于筛选 - GREATER_OR_EQUAL")
        void filterByGreaterOrEqual() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "age", "operator": ">=", "value": 28}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "age",
                                    "operator", ">=",
                                    "value", 28
                            ))
                    )
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)))
                    .andExpect(jsonPath("$.dataList[0].name", is("susan")));
        }
    }

    @Nested
    @DisplayName("分页测试")
    class PaginationTests {

        @Test
        @DisplayName("LIMIT 分页")
        void paginationWithLimit() throws Exception {
            // 请求示例: {"limit": 1}
            Map<String, Object> request = Map.of("limit", 1);

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)));
        }

        @Test
        @DisplayName("OFFSET 分页")
        void paginationWithOffset() throws Exception {
            // 请求示例: {"limit": 10, "offset": 1}
            Map<String, Object> request = Map.of(
                    "limit", 10,
                    "offset", 1
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("完整查询测试")
    class FullSearchTests {

        @Test
        @DisplayName("筛选 + 分页 + 返回计数")
        void fullSearchWithFilterAndPagination() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "age", "operator": ">=", "value": 20}]}, "limit": 10, "returnCount": true}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "age",
                                    "operator", ">=",
                                    "value", 20
                            ))
                    ),
                    "limit", 10,
                    "returnCount", true
            );

            mockMvc.perform(post("/rest/view/user/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataList", hasSize(2)))
                    .andExpect(jsonPath("$.count", is(2)));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("无效视图名称 - 包含特殊字符")
        void invalidViewNameWithSpecialChars() throws Exception {
            // 请求示例: {"limit": 10}
            Map<String, Object> request = Map.of("limit", 10);

            // 验证会抛出异常
            assertThrows(Exception.class, () -> {
                mockMvc.perform(post("/rest/view/user';DROP TABLE user;--/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn();
            });
        }

        @Test
        @DisplayName("不存在的视图")
        void nonExistentView() throws Exception {
            // 请求示例: {"limit": 10}
            Map<String, Object> request = Map.of("limit", 10);

            // 验证会抛出异常
            assertThrows(Exception.class, () -> {
                mockMvc.perform(post("/rest/view/non_existent_view/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn();
            });
        }

        @Test
        @DisplayName("无效的操作符")
        void invalidOperator() throws Exception {
            // 请求示例: {"filter": {"conditions": [{"property": "name", "operator": "invalid_op", "value": "test"}]}}
            Map<String, Object> request = Map.of(
                    "filter", Map.of(
                            "conditions", List.of(Map.of(
                                    "property", "name",
                                    "operator", "invalid_op",
                                    "value", "test"
                            ))
                    )
            );

            // 验证会抛出异常或返回错误状态码
            try {
                MvcResult result = mockMvc.perform(post("/rest/view/user/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn();

                assertTrue(result.getResponse().getStatus() >= 400);
            } catch (Exception e) {
                // 异常也是可接受的结果
            }
        }

        @Test
        @DisplayName("无效的排序字段")
        void invalidSortField() throws Exception {
            // 请求示例: {"sort": "+invalid_field"}
            Map<String, Object> request = Map.of("sort", "+invalid_field");

            // 验证会抛出异常
            assertThrows(Exception.class, () -> {
                mockMvc.perform(post("/rest/view/user/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn();
            });
        }
    }
}
