package top.mao196.querybeansql.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.mao196.querybeansql.core.ExposedViewRegistry;

/**
 * @author maoju
 * @since 2025/4/29
 */
@Configuration
@ConditionalOnProperty(prefix = "query-bean", name = "enable", havingValue = "true", matchIfMissing = true)
public class QueryBeanAutoConfiguration {

    @Bean
    public ExposedViewRegistry exposedViewRegistry(){
        return new ExposedViewRegistry();
    }
}
