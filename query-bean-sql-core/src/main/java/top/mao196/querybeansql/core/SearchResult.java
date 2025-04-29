package top.mao196.querybeansql.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maoju
 * @since 2024/11/27
 **/
@AllArgsConstructor
@Data
@NoArgsConstructor
public class SearchResult<T> {

    private List<T> dataList;

    private Long count;
}
