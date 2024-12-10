package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 门店表，记录门店的基本信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

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
     * 门店名称
     */
    private String name;

    /**
     * 省份ID
     */
    private Long provinceId;

    /**
     * 城市ID
     */
    private Long cityId;

    /**
     * 区域ID
     */
    private Long regionId;

    /**
     * 详细地址
     */
    private String address;
    /**
     * 所属企业id
     */
    private Integer enterpriseId;

    /**
     * 门店工作场所面积
     */
    private java.math.BigDecimal size;

    /**
     * 门店状态：0-营业中，1-休息中
     */
    private Integer status;
}
