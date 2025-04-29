package top.mao196.querybeansql.annotation;

import java.lang.annotation.*;

/**
 * view field annotation
 *
 * @author maoju
 * @since 2024-12-03
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewField {

    /**
     * view description
     *
     * @return description
     */
    String desc() default "";

    /**
     * view name, use to match view sql
     * @return name
     */
    String columnName() default "";
}
