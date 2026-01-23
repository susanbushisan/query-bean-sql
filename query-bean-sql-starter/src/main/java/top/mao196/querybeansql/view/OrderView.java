package top.mao196.querybeansql.view;

import lombok.Data;
import top.mao196.querybeansql.annotation.ViewExposed;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图
 * 用于集成测试
 */
@Data
@ViewExposed(
        sql = "SELECT id, order_no, user_id, user_name, amount, status, created_at FROM \"order\"",
        name = "order",
        desc = "订单视图用于集成测试"
)
public class OrderView {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}
