package top.mao196.querybeansql.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FilterCondition {
    @JsonProperty("group")
    private FilterType group;
    @JsonProperty("conditions")
    private List<FilterCondition> conditions = new ArrayList<>();
    @JsonProperty("property")
    private String property;
    @JsonProperty("operator")
    private FilterOp operator;
    @JsonProperty("value")
    private Object value;

    public boolean isGroup() {
        return group != null;
    }
}