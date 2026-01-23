package top.mao196.querybeansql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 占位符功能集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("占位符功能集成测试")
class PlaceholderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试参数替换")
    void testParameterReplacement() throws Exception {
        // 直接使用 SearchEntitiesRequestDTO 创建请求
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", 1);

        Map<String, Object> request = new HashMap<>();
        request.put("parameters", parameters);
        request.put("limit", 10);

        mockMvc.perform(post("/rest/view/order_placeholder/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList", org.hamcrest.Matchers.hasSize(2)));
    }

    @Test
    @DisplayName("测试条件块 - 条件为真")
    void testBlockConditionTrue() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "active");

        Map<String, Object> request = new HashMap<>();
        request.put("parameters", parameters);
        request.put("limit", 10);

        mockMvc.perform(post("/rest/view/order_placeholder/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.dataList[*].status", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("active"))));
    }

    @Test
    @DisplayName("测试条件块 - 条件为假（值不匹配）")
    void testBlockConditionFalse() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "pending");
        parameters.put("hasStatus", true); // status 存在，但值不匹配

        Map<String, Object> request = new HashMap<>();
        request.put("parameters", parameters);
        request.put("limit", 10);

        mockMvc.perform(post("/rest/view/order_placeholder/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList", org.hamcrest.Matchers.hasSize(1))); // 只有 pending 的订单
    }

    @Test
    @DisplayName("测试无参数时返回所有数据")
    void testNoParameters() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("limit", 10);

        mockMvc.perform(post("/rest/view/order_placeholder/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList", org.hamcrest.Matchers.hasSize(4)));
    }
}
