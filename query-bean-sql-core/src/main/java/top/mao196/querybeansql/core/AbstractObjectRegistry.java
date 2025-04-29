package top.mao196.querybeansql.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author maoju
 * @since 2024/12/3
 */
@Slf4j
public abstract class AbstractObjectRegistry implements InitializingBean, EnvironmentAware {
    protected static final String DEFAULT_BASE_PACKAGE = "*";
    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    /**
     * 扫描的基础包
     */
    protected String[] basePackageArray = {DEFAULT_BASE_PACKAGE};

    /**
     * 扫描后的候选class列表
     */
    protected Set<String> candidateClasses;

    /**
     * 环境变量
     */
    protected Environment environment;

    /**
     * 资源目录resolver
     */
    private ResourcePatternResolver resourcePatternResolver;

    /**
     * 元数据读取工厂
     */
    private MetadataReaderFactory metadataReaderFactory;


    public void setBasePackage(String[] basePackage) {
        this.basePackageArray = basePackage;
    }

    public void setResourcePatternResolver(@NonNull ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public ResourcePatternResolver getResourcePatternResolver() {
        if (Objects.isNull(this.resourcePatternResolver)) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return resourcePatternResolver;
    }

    public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
        this.metadataReaderFactory = metadataReaderFactory;
    }

    public MetadataReaderFactory getMetadataReaderFactory() {
        if (Objects.isNull(this.metadataReaderFactory)) {
            metadataReaderFactory = new CachingMetadataReaderFactory();
        }
        return metadataReaderFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        refreshExposedObjects();
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    protected void refreshExposedObjects() {
        this.candidateClasses = new LinkedHashSet<>();
        for (String basePackage : basePackageArray) {
            try {
                String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        resolveBasePackage(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;
                Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
                TypeFilter filter = getTypeFilter();
                for (Resource resource : resources) {
                    MetadataReader reader = getMetadataReaderFactory().getMetadataReader(resource);
                    if (filter.match(reader, getMetadataReaderFactory())) {
                        log.info(reader.getAnnotationMetadata().getClassName());
                        this.candidateClasses.add(reader.getAnnotationMetadata().getClassName());
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException("I/O failure during classpath scanning", ex);
            }
        }

    }

    /**
     * 将package转换成资源目录
     *
     * @param basePackage package
     * @return 资源目录
     */
    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.environment.resolveRequiredPlaceholders(basePackage));
    }

    /**
     * 元数据过滤器
     *
     * @return 过滤器
     */
    protected abstract TypeFilter getTypeFilter();
}
