# Query Bean SQL

`Query Bean SQL` 是一个基于 Spring Boot 开发的项目，旨在简化视图查询操作，通过注解和配置化的方式，实现灵活的视图查询功能。

## 项目结构
项目主要包含两个模块：
- `query-bean-sql-core`：核心功能模块，包含视图注册、请求解析、异常处理等核心逻辑。
- `query-bean-sql-starter`：启动器模块，集成了项目的依赖和测试相关内容。

## 技术栈
- **Spring Boot**：版本 3.4.4，用于快速搭建项目。
- **MySQL**：作为数据库，提供数据存储服务。
- **Hutool**：提供常用工具类。

## 功能特性
- **视图暴露**：通过 `@ViewExposed` 注解暴露视图，支持自定义视图名称、描述和 SQL 语句。
- **请求解析**：根据请求参数解析查询条件、排序规则和分页信息。
- **自动配置**：通过 `QueryBeanAutoConfiguration` 实现自动配置，支持通过配置文件启用或禁用功能。

## 配置说明
在 `query-bean-sql-starter/src/main/resources/application.properties` 文件中，可以进行如下配置：
```properties
spring.application.name=query-bean-sql
spring.datasource.url=jdbc:mysql://localhost:3306/test?useSSL=false
spring.datasource.username=root
spring.datasource.password=123456
query-bean.enable=true
query-bean.basePackage=com.example.querybean
query-bean.maxLimit=10000
```
- query-bean.enable ：是否启用 Query Bean SQL 功能，默认为 true 。
- query-bean.basePackage ：注解扫描的包路径。
- query-bean.maxLimit ： search 接口返回数据的最大行数，默认为 10000。
## API 接口
### 元数据视图接口
- GET /rest/metadata/view/ ：获取所有视图信息。
- GET /rest/metadata/view/{viewName} ：获取指定视图的元数据信息。
### 视图查询接口
- POST /rest/view/{viewName}/search ：根据视图名称和请求参数进行数据查询。
## 贡献
如果您想为项目做出贡献，请提交 Pull Request 或者创建 Issue 提出建议。
