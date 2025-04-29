package top.mao196.querybeansql.core;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author maoju
 * @since 2024/12/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewFiledDescriptor {

    /**
     * name
     */
    private String name;

    /**
     * description
     */
    private String description;

    /**
     * class中的字段名称
     */
    private String rawName;

    /**
     * 对应的类型
     */
    private Class<?> clz;

    public String getColumnName() {
        return StrUtil.isNotEmpty(getName()) ? getName() : StrUtil.toUnderlineCase(getRawName());
    }
}
