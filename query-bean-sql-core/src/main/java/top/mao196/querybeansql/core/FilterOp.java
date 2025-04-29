package top.mao196.querybeansql.core;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FilterOp {
    EQUAL("=", "="),
    GREATER(">", ">"),
    GREATER_OR_EQUAL(">=", ">="),
    LESSER("<", "<"),
    LESSER_OR_EQUAL("<=", "<="),
    NOT_EQUAL("<>", "<>"),
    STARTS_WITH("startsWith", "like"),
    ENDS_WITH("endsWith", "like"),
    CONTAINS("contains", "like"),
    DOES_NOT_CONTAIN("doesNotContain", "not like"),
    IN("in", "in"),
    NOT_IN("notIn", "not in"),
    NOT_EMPTY("notEmpty", "is not null"),
    IS_NULL("isNull", "is null");

    @JsonValue
    private final String stringOp;

    private final String sqlOp;

    public static FilterOp fromStringOp(String stringOp) {
        for (FilterOp op : values()) {
            if (op.stringOp.equals(stringOp)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Invalid filter operator: " + stringOp);
    }
}