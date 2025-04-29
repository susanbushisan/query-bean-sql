package top.mao196.querybeansql.core;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maoju
 * @since 2024/11/27
 **/
@Data
public class SearchFilter {

    private List<FilterCondition> conditions = new ArrayList<>();

    public void addCondition(FilterCondition condition) {
        conditions.add(condition);
    }

}
