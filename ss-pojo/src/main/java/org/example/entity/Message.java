package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

/**
 * 通知表，记录系统发送的通知信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
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
     * 创建者ID
     */
    private Long createUserId;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Byte isDeleted;

    /**
     * 是否已发布：0-未发布，1-已发布
     */
    private Integer isPublish;

    /**
     * 通知标题
     */
    private String subject;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 所属门店ID
     */
    private Long storeId;

    /**
     * 发布时间
     */
    private Date publishTime;
}