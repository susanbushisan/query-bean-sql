package top.mao196.querybeansql;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MetadataViewController 集成测试
 * 测试 /rest/metadata/view/ 端点
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MetadataViewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("获取视图列表测试")
    class GetAllViewsTests {

        @Test
        @DisplayName("获取所有暴露的视图列表")
        void getAllViews() throws Exception {
            mockMvc.perform(get("/rest/metadata/view/"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("user")))
                    .andExpect(jsonPath("$[0].description", is("用户信息")));
        }

        @Test
        @DisplayName("验证返回的视图数据结构")
        void viewStructure() throws Exception {
            mockMvc.perform(get("/rest/metadata/view/"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]", allOf(
                            hasKey("name"),
                            hasKey("description")
                    )));
        }
    }

    @Nested
    @DisplayName("获取单个视图元数据测试")
    class GetViewMetadataTests {

        @Test
        @DisplayName("获取 user 视图的元数据")
        void getUserViewMetadata() throws Exception {
            mockMvc.perform(get("/rest/metadata/view/user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("user")))
                    .andExpect(jsonPath("$.description", is("用户信息")))
                    .andExpect(jsonPath("$.properties", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("验证视图属性的类型信息")
        void viewPropertyTypes() throws Exception {
            mockMvc.perform(get("/rest/metadata/view/user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.properties[*].property", containsInAnyOrder(
                            "id", "name", "age", "birth", "balance", "createTime", "createBy"
                    )));
        }

        @Test
        @DisplayName("验证每个属性的数据结构")
        void propertyStructure() throws Exception {
            mockMvc.perform(get("/rest/metadata/view/user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.properties[0]", allOf(
                            hasKey("property"),
                            hasKey("type")
                    )));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("获取不存在的视图应返回错误")
        void getNonExistentView() throws Exception {
            // 验证会抛出异常
            assertThrows(Exception.class, () -> {
                mockMvc.perform(get("/rest/metadata/view/non_existent"))
                        .andReturn();
            });
        }

        @Test
        @DisplayName("无效的视图名称应返回错误")
        void invalidViewName() throws Exception {
            // 验证会抛出异常
            assertThrows(Exception.class, () -> {
                mockMvc.perform(get("/rest/metadata/view/user';DROP TABLE"))
                        .andReturn();
            });
        }
    }
}
