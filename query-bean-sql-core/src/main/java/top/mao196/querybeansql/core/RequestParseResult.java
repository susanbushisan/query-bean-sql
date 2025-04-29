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

    private String where;

    private String order;

    private String limit;

    private Map<String,Object> params;


}
