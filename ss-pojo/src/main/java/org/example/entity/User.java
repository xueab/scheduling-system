package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户表，记录系统中用户的基本信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Integer isDeleted;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 所属门店ID
     */
    private Long storeId;

    /**
     * 用户类型：0-系统管理员，1-门店管理员，10-普通用户
     */
    private Integer type;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别：0-男，1-女
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 工作日偏好（喜欢星期几工作，多个用“|”分隔）
     */
    private String workDayPreference;
    /**
     * 所属企业id
     */
    private Integer enterpriseId;

    /**
     * 工作时间偏好（以时间段为单位）
     */
    private String workTimePreference;

    /**
     * 每天班次时长偏好
     */
    private String shiftLengthPreferenceOneDay;

    /**
     * 每周班次时长偏好
     */
    private String shiftLengthPreferenceOneWeek;
}
