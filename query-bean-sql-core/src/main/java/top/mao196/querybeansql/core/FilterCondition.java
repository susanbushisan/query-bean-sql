package top.mao196.querybeansql.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FilterCondition {
    private FilterType group;
    private List<FilterCondition> conditions = new ArrayList<>();
    private String property;
    private FilterOp operator;
    private Object value;

    public boolean isGroup() {
        return group != null;
    }
}