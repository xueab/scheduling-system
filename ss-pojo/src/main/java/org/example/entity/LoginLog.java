package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

/**
 * 登录日志表，记录用户登录的相关日志信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Byte isDeleted;

    /**
     * 用户名
     */
    private String username;
    /**
     * 所属门店ID
     */
    private Long storeId;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录状态：0-失败，1-成功
     */
    private String status;

    /**
     * 访问时间
     */
    private Date accessTime;
}