package top.mao196.querybeansql.contoller;

import cn.hutool.core.lang.Dict;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mao196.querybeansql.core.ExposedViewRegistry;
import top.mao196.querybeansql.core.QueryBeanSqlException;
import top.mao196.querybeansql.core.ViewDescriptor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author maoju
 * @since  2025/3/14
 **/
@RestController
@RequestMapping("/rest/metadata/view")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "query-bean", name = "enable", havingValue = "true", matchIfMissing = true)
public class MetadataViewController {

    private final ExposedViewRegistry exposedViewRegistry;

    @GetMapping("/")
    public ResponseEntity<Object> getAllViewInfo() {
        return ResponseEntity.ok(exposedViewRegistry.descriptors()
                .stream()
                .map(it -> Dict.of("name", it.getName(), "description", it.getDescription()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{viewName}")
    public ResponseEntity<Object> getMetaClassInfo(@PathVariable("viewName") String viewName) {
        ViewDescriptor descriptor = exposedViewRegistry.findDescriptor(viewName);
        if (Objects.isNull(descriptor)) {
            throw new QueryBeanSqlException(String.format("entity with name: %s cannot be found", viewName));
        }
        return ResponseEntity.ok(Dict.of("name", descriptor.getName(),
                "description", descriptor.getDescription(),
                "properties", entityProperties(descriptor)));
    }

    /**
     * 获取实体的属性信息
     *
     * @param descriptor 实体信息
     * @return 属性列表
     */
    protected List<Map<String, Object>> entityProperties(ViewDescriptor descriptor) {
        return descriptor.getFields().stream()
                .map(x -> Dict.of("type", ClassUtils.getShortName(x.getClz()), "property", x.getRawName()))
                .collect(Collectors.toList());
    }
}
