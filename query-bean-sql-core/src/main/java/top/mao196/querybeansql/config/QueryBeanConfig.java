package top.mao196.querybeansql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author maoju
 * @since 2025/4/29
 */
@Data
@Component
@ConfigurationProperties(prefix = "query-bean")
public class QueryBeanConfig {
    /**
     * 是否开启
     */
    private boolean enable = true;

    /**
     * 注解扫描的包
     */
    private String[] basePackage;

    /**
     * search接口返回数据最大行数
     */
    private int maxLimit = 10000;
}
