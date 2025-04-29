package top.mao196.querybeansql.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author maoju
 * @since 2024-12-03
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewExposed {
    /**
     * view name
     *
     * @return entity name
     */
    @AliasFor("name")
    String value() default "";

    /**
     * view name
     *
     * @return entity name
     */
    String name() default "";

    /**
     * view description
     *
     * @return description
     */
    String desc() default "";

    /**
     * view sql
     * @return sql
     */
    String sql();
}
