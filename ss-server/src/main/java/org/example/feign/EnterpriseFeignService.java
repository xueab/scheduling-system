package org.example.feign;

import org.example.feign.config.FeignConfig;
import org.example.entity.UserPosition;
import org.example.result.Result;

import org.example.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@FeignClient( value = "sss-enterprise", configuration = FeignConfig.class, url = "${aggregation.remote-url:}")
public interface EnterpriseFeignService {

    /**
     * 根据企业id查询出旗下所有门店
     *
     * @param enterpriseId
     * @return List<StoreEntity> list
     */
    @RequestMapping("/enterprise/store/listAllStoreByAppointEnterpriseId")
    public Result listAllStoreByAppointEnterpriseId(@RequestParam("enterpriseId") Long enterpriseId);

    /**
     * 根据门店id获取排班规则
     *
     * @param storeId
     * @return
     */
    @RequestMapping("/enterprise/schedulingrule/getSchedulingRuleVoByGiveStoreId")
    public Result getSchedulingRuleVoByGiveStoreId(@RequestParam("storeId") Long storeId);

    /**
     * 将门店规则复制一遍
     */
    @RequestMapping("/enterprise/schedulingrule/copySchedulingRule")
    public Result copySchedulingRule(@RequestParam("storeId") Long storeId);

    /**
     * 根据id获取排班规则
     *
     * @param ruleId
     * @return
     */
    @RequestMapping("/enterprise/schedulingrule/getSchedulingRuleVoById")
    public Result  getSchedulingRuleVoById(@RequestParam("ruleId") Long ruleId);

    /**
     * 根据门店id查询所有职位
     */
    @RequestMapping("/enterprise/position/listByStoreId")
    public Result  listByStoreId(@RequestParam("storeId") Long storeId);

    /**
     * 根据职位id集合查询职位集合
     *
     * @return List<PositionEntity>  positionEntityList
     */
    @PostMapping("/enterprise/position/listPositionListByPositionIdList")
    public Result  listPositionListByPositionIdList(@RequestBody List<Long> positionIdList);

    /**
     * 根据企业id集合查询企业，并封装成字典
     *
     * @return
     */
    @PostMapping("/enterprise/userposition/getUserIdAndPositionIdMapByUserIdList")
    public Result  getUserIdAndPositionIdMapByUserIdList(@RequestBody List<Long> userIdList);

    /**
     * 发送消息通知用户
     *
     * @return
     */
    @PostMapping("/enterprise/message/sendMesToUserList")
    public Result  sendMesToUserList(@RequestBody Map<String, Object> paramMap);

    /**
     * 根据企业id查询企业信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/enterprise/enterprise/info/{id}")
    public Result  getEnterpriseEntityById(@PathVariable("id") Long id);

    /**
     * 获取系统的企业数量
     *
     * @return int count
     */
    @RequestMapping("/enterprise/enterprise/getAllEnterpriseNum")
    public Result  getAllEnterpriseNum();


    /**
     * 根据门店id查询门店信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/enterprise/store/info/{id}")
    public Result  getStoreEntityById(@PathVariable("id") Long id);

    /**
     * 获取系统的门店数量
     *
     * @return int count
     */
    @RequestMapping("/enterprise/store/getAllStoreNum")
    public Result  getAllStoreNum();

    /**
     * 根据企业id集合查询门店，并封装成字典
     *
     * @return
     */
    @PostMapping("/enterprise/store/getStoreMapByIdList")
    public Result  getStoreMapByIdList(@RequestBody List<Long> storeIdList);


    /**
     * 根据企业id集合查询企业，并封装成字典
     *
     * @return
     */
    @PostMapping("/enterprise/enterprise/getEnterpriseMapByIdList")
    public Result  getEnterpriseMapByIdList(@RequestBody List<Long> enterpriseIdList);

    /**
     * 根据职位id查询职位信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/enterprise/position/info/{id}")
    public Result  getPositionEntityById(@PathVariable("id") Long id);

    /**
     * 查询出所有叶子节点数据
     *
     * @return
     */
    @RequestMapping("/enterprise/position/listAllSonPosition")
    public Result  listAllSonPosition(@RequestParam("storeId") Long storeId);

    /**
     * 给定门店id集合，查询出每个门店的所有叶子节点数据
     *
     * @return Map<Long, List<PositionEntity>> storeIdAndPositionList
     */
    @RequestMapping("/enterprise/position/getStoreIdAndPositionList")
    public Result  getStoreIdAndPositionList(@RequestBody List<Long> storeIdList);

    /**
     * 根据企业id集合查询门店，并封装成字典
     *
     * @return
     */
    @PostMapping("/enterprise/position/getPositionMapByIdList")
    public Result  getPositionMapByIdList(@RequestBody List<Long> positionIdList);


    /**
     * 保存
     */
    @RequestMapping("/enterprise/userposition/saveBatch")
    public Result  saveUserPositionList(@RequestBody List<UserPosition> userPositionList);

    /**
     * 根据用户id获取所匹配的职位绑定信息
     *
     * @param userId
     * @return
     */
    @RequestMapping("/enterprise/userposition/infoByUserId")
    public Result  infoUserPositionByUserId(@RequestParam("userId") Long userId);


    /**
     * 查询指定职位的用户id
     *
     * @param PositionIdList
     * @return
     */
    @RequestMapping("/enterprise/userposition/listUserIdList")
    public List<Long> listUserIdList(@RequestParam("PositionIdList") List<Long> PositionIdList);

    /**
     * 根据用户id集合删除其职位
     */
    @RequestMapping("/enterprise/userposition/deleteUserPositionByUserIdList")
    public Result  deleteUserPositionByUserIdList(@RequestBody List<Long> userIdList);


    /**
     * 新增定时任务
     *
     * @return ResultMap
     */
    @PostMapping(path = "/enterprise/quartz/addJob")
    @ResponseBody
    public Result  addJob(Map<String, Object> paramMap);

    /**
     * 删除任务
     *
     * @return ResultMap
     */
    @PostMapping(path = "/enterprise/quartz/deleteJob")
    @ResponseBody
    public Result  deleteJob(Map<String, Object> paramMap);


}
