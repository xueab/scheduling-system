package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

/**
 * 省-市-区表，记录省市区的地理层级信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceCityRegion {
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
     * 地区名称（省/市/区）
     */
    private String name;

    /**
     * 地区类型：1-省，2-市，3-区
     */
    private Integer type;

    /**
     * 父级ID（省的parentId为0，市的parentId为省ID，区的parentId为市ID）
     */
    private Long parentId;
}
