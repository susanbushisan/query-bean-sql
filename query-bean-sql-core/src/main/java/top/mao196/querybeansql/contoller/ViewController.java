package top.mao196.querybeansql.contoller;


import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import top.mao196.querybeansql.core.*;

import java.util.*;

/**
 * @author maoju
 * @since 2025/3/14
 **/
@RestController
@RequestMapping("/rest/view")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "query-bean", name = "enable", havingValue = "true", matchIfMissing = true)
@Slf4j
public class ViewController {

    private final ExposedViewRegistry exposedViewRegistry;

    private final RequestParse requestParse;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @PostMapping("/{viewName}/search")
    public <T> ResponseEntity<SearchResult<T>> searchViewPost(@PathVariable("viewName") String viewName, @RequestBody SearchEntitiesRequestDTO requestDTO) {

        ViewDescriptor viewDescriptor = exposedViewRegistry.findDescriptor(viewName);

        if (viewDescriptor == null){
            throw new QueryBeanSqlException(String.format("view with name: %s cannot be found", viewName));
        }

        RequestParseResult result = requestParse.parse(requestDTO, viewDescriptor);
        Dict buildSqlParam = Dict.of("column", result.getColumn(),
                "where", result.getWhere(),
                "order", result.getOrder(),
                "limit", result.getLimit(),
                "viewSql", viewDescriptor.getSql());

        String querySqlTemplate = "SELECT {column} FROM ({viewSql}) _tmp {where} {order} {limit}";
        String formatQuerySql = StrUtil.format(querySqlTemplate, buildSqlParam, false);
        log.info("query view sql: [{}], param: [{}]",formatQuerySql, result.getParams());
        List<T> dataList = loadDataList(formatQuerySql, viewDescriptor, result.getParams());
        SearchResult<T> objectSearchResult = new SearchResult<>();
        objectSearchResult.setDataList(dataList);
        if (BooleanUtil.isTrue(requestDTO.getReturnCount())) {
            String countSqlTemplate = "SELECT count(*) FROM ({viewSql}) _tmp {where}";
            String formatCountSql = StrUtil.format(countSqlTemplate, buildSqlParam, false);
            log.info("query view count sql: [{}], param: [{}]",formatCountSql, result.getParams());
            objectSearchResult.setCount(getCount(formatCountSql, result.getParams()));
        }
        return ResponseEntity.ok(objectSearchResult);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> loadDataList(String sql, ViewDescriptor viewDescriptor, Map<String, Object> params) {
        return (List<T>) namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(viewDescriptor.getViewClass()));
    }

    private Long getCount(String sql, Map<String, Object> params) {
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }


}
