package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.entity.Base;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * user_role中间表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends Base implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 用户id
     */

    private Long userId;

}
