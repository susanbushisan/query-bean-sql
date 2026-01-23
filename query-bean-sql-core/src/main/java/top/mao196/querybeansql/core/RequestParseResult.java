package top.mao196.querybeansql.core;

import lombok.Data;

import java.util.Map;

/**
 * @author maoju
 * @since 2025/3/14
 **/
@Data
public class RequestParseResult {

    private String column;

    /**
     * 处理后的视图 SQL（占位符已替换）
     */
    private String viewSql;

    private String where;

    private String order;

    private String limit;

    private Map<String,Object> params;


}
