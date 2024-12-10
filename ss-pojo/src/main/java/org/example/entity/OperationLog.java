package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

/**
 * 操作日志表，记录系统中所有的操作日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 所属门店ID
     */
    private Long storeId;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Byte isDeleted;

    /**
     * 业务类型：1-用户管理，2-权限管理，3-订单处理等
     */
    private Byte businessType;

    /**
     * 操作人ID
     */
    private Long userId;

    /**
     * 操作IP
     */
    private String operIp;

    /**
     * 操作状态：0-失败，1-成功
     */
    private Byte status;

    /**
     * 错误信息（如果有）
     */
    private String errorMsg;
}