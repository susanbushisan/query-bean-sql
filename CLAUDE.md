# CLAUDE.md

本文档为 Claude Code (claude.ai/code) 在本代码库中工作时提供指导。

## 项目概述

Query Bean SQL 是一个 Spring Boot 3.4.4 库，通过注解驱动的配置简化视图查询操作。它将数据库视图暴露为 REST 端点，并内置支持筛选、排序和分页功能。

## 构建命令

```bash
# 构建所有模块
./mvnw clean install

# 运行测试
./mvnw test


# 构建时跳过测试
./mvnw clean install -DskipTests
```

## 架构

### 模块结构

- **query-bean-sql-core**：核心库模块，包含注解处理、视图注册、请求解析和 REST 控制器
- **query-bean-sql-starter**：Spring Boot 应用启动器，包含 MySQL 驱动、Knife4j OpenAPI 文档和示例视图

### 核心流程

1. **视图发现**：`ExposedViewRegistry`（继承自 `AbstractObjectRegistry`）使用 Spring 的 `MetadataReader` API 扫描类路径中带有 `@ViewExposed` 注解的类
2. **请求处理**：`RequestParse` 将 `SearchEntitiesRequestDTO` 转换为 SQL 组件（WHERE、ORDER BY、LIMIT）
3. **查询执行**：`ViewController` 构建参数化 SQL 查询并通过 `NamedParameterJdbcTemplate` 执行
4. **响应映射**：使用 `BeanPropertyRowMapper` 将结果映射到视图类

### 关键组件

| 组件 | 用途 |
|------|------|
| `@ViewExposed` | 标记一个类为暴露的视图；定义 SQL、名称和描述 |
| `@ViewField` | 可选；将 Java 字段映射到数据库列名 |
| `ExposedViewRegistry` | 发现 `@ViewExposed` 类并存储 `ViewDescriptor` 元数据 |
| `RequestParse` | 将筛选/排序/分页请求转换为 SQL WHERE 子句 |
| `ViewController` | 处理 `/rest/view/{viewName}/search` POST 端点 |
| `MetadataViewController` | 在 `/rest/metadata/view/` 提供视图元数据服务 |

### 请求/响应流程

```
POST /rest/view/{viewName}/search
├── 请求体：SearchEntitiesRequestDTO（筛选、排序、限制、偏移、返回计数）
├── RequestParse 从 SearchFilter 树构建参数化 WHERE 子句
├── 执行：SELECT {columns} FROM ({viewSql}) _tmp WHERE {conditions} ORDER BY {order} LIMIT {limit}
└── 响应：SearchResult<T>（数据列表，可选计数）
```

### 配置属性

```properties
query-bean.enable=true                    # 启用/禁用该功能
query-bean.basePackage=com.example.view   # 用于扫描 @ViewExposed 的包
query-bean.maxLimit=10000                 # 每个查询返回的最大行数
```

## API 端点

- `GET /rest/metadata/view/` - 列出所有暴露的视图
- `GET /rest/metadata/view/{viewName}` - 获取视图元数据和属性
- `POST /rest/view/{viewName}/search` - 使用筛选、排序、分页查询视图数据

## 过滤运算符

`SearchFilter.conditions[].operator` 支持以下运算符：
- `EQUAL`, `NOT_EQUAL`, `GREATER`, `LESSER`, `GREATER_OR_EQUAL`, `LESSER_OR_EQUAL`
- `CONTAINS`, `DOES_NOT_CONTAIN`, `STARTS_WITH`, `ENDS_WITH`
- `IN`, `NOT_IN`, `IS_NULL`, `NOT_EMPTY`

## 项目规划

> **注意**：本项目后续作为其他 Spring Boot 项目的一部分，不独立部署。以下功能不会实现：
> - 全局异常处理（由宿主应用统一处理）
> - 查询缓存（由宿主应用通过 Redis 等实现）
> - 查询限流（由宿主应用通过网关实现）
> - 聚合函数（不支持 GROUP BY/聚合操作）

### 功能增强

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 字段选择器 | 支持通过 `fields` 参数指定返回字段，减少数据传输 | P1 |
| 游标分页 | 支持基于ID的游标分页，避免 OFFSET 性能问题 | P2 |
| 多数据库支持 | 抽象 SQL 生成层，支持 PostgreSQL、SQL Server 等 | P2 |
| 字段名大小写处理 | 优化不同数据库的字段名大小写敏感处理 | P3 |

