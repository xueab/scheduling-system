package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Base {

    /**
     * 设置主键自增
     */

    private Long id;

    /**
     * 是否删除 0：未删除 1：已删除
     */

    private Integer isDeleted = 0;


    private Date createTime;


    private Date updateTime;
}
