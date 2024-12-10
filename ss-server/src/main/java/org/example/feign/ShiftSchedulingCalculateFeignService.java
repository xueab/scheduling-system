package org.example.feign;

import org.example.feign.config.FeignConfig;
import org.example.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value = "shift-scheduling-calculate-service", configuration = FeignConfig.class, url = "${aggregation.remote-url:}")
public interface ShiftSchedulingCalculateFeignService {

    /**
     * 查询指定时间段繁忙的用户id
     *
     * @param params
     * @return
     */
    @PostMapping("/scheduling/shiftuser/listUserIdListIsBusy")
    public Result listUserIdIsBusy(@RequestBody Map<String, Object> params);

    /**
     * 根据一个日期段内 所有需要工作的员工id
     *
     * @param paramMap
     * @return
     */
    @PostMapping("/scheduling/shiftuser/listUserIdByDateSegment")
    public Result listUserIdByDateSegment(@RequestBody Map<String, Object> paramMap);

    /**
     * 获取系统的用户数量
     *
     * @return int count
     */
    @RequestMapping("/scheduling/schedulingTask/getAllTaskNum")
    public Result getAllTaskNum();

    /**
     * 根据工作日查询出所有需要工作的员工id
     *
     * @param paramMap
     * @return
     */
    @PostMapping("/scheduling/shiftuser/listUserIdByWorkDate")
    public Result listUserIdByWorkDate(@RequestBody Map<String, Object> paramMap);

    /**
     * 根据工作日查询出所有需要工作的员工id，及其所负责的班次
     *
     * @param paramMap
     * @return
     */
    @PostMapping("/scheduling/shiftuser/listStaffWorkDtoByWorkDate")
    public Result listStaffWorkDtoByWorkDate(@RequestBody Map<String, Object> paramMap);

    @PostMapping("/scheduling/schedulingdate/judgeOneDateIsRest")
    public Result judgeOneDateIsRest(@RequestBody Map<String, Object> paramMap);
}
