package top.mao196.querybeansql.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import top.mao196.querybeansql.annotation.ViewExposed;

import java.time.LocalDate;
import java.util.Date;

@Data
@ViewExposed(sql = "select id, name, age, birth, balance, create_time, create_by from \"user\"" ,name = "user", desc = "用户信息")
public class UserView {
    private Long id;
    private String name;
    private Integer age;
    private LocalDate birth;
    private Double balance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String createBy;
}
