package top.mao196.querybeansql.view;

import lombok.Data;
import top.mao196.querybeansql.annotation.ViewField;
import top.mao196.querybeansql.annotation.ViewExposed;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图 - 用于测试占位符功能
 */
@Data
@ViewExposed(
        sql = "SELECT id, order_no, user_id, user_name, amount, status, created_at FROM \"order\" WHERE 1=1 ${userId? AND user_id = ${userId}} ${status? AND status = '${status}'}",
        name = "order_placeholder",
        desc = "订单视图（测试占位符功能）"
)
public class OrderPlaceholderView {
    private Long id;
    private String orderNo;

    @ViewField(columnName = "user_id")
    private Long userId;

    @ViewField(columnName = "user_name")
    private String userName;

    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}
