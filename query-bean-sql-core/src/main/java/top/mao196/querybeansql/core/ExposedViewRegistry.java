package top.mao196.querybeansql.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import top.mao196.querybeansql.annotation.ViewExposed;
import top.mao196.querybeansql.annotation.ViewField;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author maoju
 * @since 2024/12/3
 */
@Slf4j
public class ExposedViewRegistry extends AbstractObjectRegistry {

    /**
     * 缓存的实体描述信息
     */
    private List<ViewDescriptor> descriptors;

    /**
     * 构建实体描述信息
     *
     * @return 实体描述信息列表
     */
    public List<ViewDescriptor> descriptors() {
        if (Objects.isNull(descriptors)) {
            if (CollUtil.isNotEmpty(candidateClasses)) {
                descriptors = candidateClasses.stream()
                        .map(type -> {
                            Class<?> entityClass = ClassUtils.resolveClassName(type, null);
                            ViewExposed exposed = Objects.requireNonNull(AnnotatedElementUtils.findMergedAnnotation(entityClass, ViewExposed.class));
                            String name = exposed.name();
                            String description = exposed.desc();
                            if (StrUtil.isEmpty(name)) {
                                String shortName = ClassUtils.getShortName(entityClass);
                                name = StrUtil.toCamelCase(shortName);
                            }
                            return ViewDescriptor.builder()
                                    .viewClass(entityClass)
                                    .name(name)
                                    .description(description)
                                    .sql(exposed.sql())
                                    .fields(findFieldsDescriptor(entityClass))
                                    .build();
                        })
                        .collect(Collectors.toList());
            } else {
                descriptors = Collections.emptyList();
            }
        }
        return descriptors;
    }

    public List<ViewFiledDescriptor> findFieldsDescriptor(Class<?> clz) {
        // 通过反射拿到类的属性
        return Arrays.stream(ClassUtil.getDeclaredFields(clz)).map(it->{
            ViewField viewField = AnnotatedElementUtils.findMergedAnnotation(it, ViewField.class);
            return ViewFiledDescriptor.builder()
                    .clz(it.getType())
                    .name(Objects.isNull(viewField) ? null : viewField.columnName())
                    .rawName(it.getName())
                    .description(Objects.isNull(viewField) ? "" : viewField.desc())
                    .build();
        }).toList();
    }

    /**
     * 根据名称查找实体描述信息
     *
     * @param name 名称
     * @return 实体描述信息
     */
    public ViewDescriptor findDescriptor(String name) {
        return descriptors().stream()
                .filter(it -> StrUtil.equals(name, it.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected TypeFilter getTypeFilter() {
        return new ExposedEntityTypeFilter();
    }

    private static class ExposedEntityTypeFilter implements TypeFilter {

        @Override
        public boolean match(@NonNull MetadataReader reader, @NonNull MetadataReaderFactory factory) {
            AnnotationMetadata metadata = reader.getAnnotationMetadata();
            return metadata.isConcrete() &&
                    metadata.isAnnotated(ViewExposed.class.getName());
        }
    }
}
