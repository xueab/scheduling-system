package org.example.vo.statistics.storeManager;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MonthAverageStaffWorkTimeVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String monthName;
    /**
     * 日均工作时长 h
     */
    private Double averageStaffWorkTime;
}