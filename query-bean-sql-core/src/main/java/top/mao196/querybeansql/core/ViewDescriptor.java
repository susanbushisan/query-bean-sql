package top.mao196.querybeansql.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author maoju
 * @since 2024/12/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewDescriptor {

    /**
     * name
     */
    private String name;

    /**
     * description
     */
    private String description;

    /**
     * sql
     */
    private String sql;

    /**
     * 对应的类型
     */
    private Class<?> viewClass;

    /**
     * 字段相关信息
     */
    private List<ViewFiledDescriptor> fields;

    /**
     * 根据字段名查找字段信息
     * @param fieldName 字段名
     * @return 字段信息，如果不存在则返回null
     */
    public ViewFiledDescriptor findFieldDescriptor(String fieldName) {
        for (ViewFiledDescriptor field : fields) {
            if (field.getRawName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }
}
