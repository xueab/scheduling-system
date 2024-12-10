package org.example.feign;

import org.example.feign.config.FeignConfig;
import org.example.entity.User;
import org.example.entity.UserRole;
import org.example.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "sss-system", configuration = FeignConfig.class, url = "${aggregation.remote-url:}")
public interface SystemFeignService {

    @GetMapping("/system/user/listUserByStoreId")
    public Result listUserByStoreId(@RequestParam("storeId") Long storeId);

    /**
     * 通过用户id集合查询相关用户信息
     *
     * @param userIds
     * @return userInfoVoList
     */
    @PostMapping("/system/user/listUserInfoVoByUserIds")
    public Result listUserInfoVoByUserIds(@RequestBody List<Long> userIds);

    /**
     * 根据企业id集合查询用户，并封装成字典
     *
     * @return Map<Long, UserEntity> idAndUserEntityMap
     */
    @PostMapping("/system/user/getUserMapByIdList")
    public Result getUserMapByIdList(@RequestBody List<Long> userIdList);

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/system/user/userInfoVo")
    public Result getUserInfoVoById(@RequestParam("id") Long id);

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/system/user/getUserById")
    public Result getUserById(@RequestParam("id") Long id);


    /**
     * 直接保存用户
     */
    @RequestMapping("/system/user/directSave")
    public Result directSave(@RequestBody User user);

    /**
     * 保存
     */
    @RequestMapping("/system/userrole/save")
    public Result saveUserRole(@RequestBody UserRole userRole);

    /**
     * 获取每个企业及其用户数量
     *
     * @param enterpriseIdList
     * @return
     */
    @PostMapping("/system/user/getEnterpriseIdAndUserNumMap")
    public Result getEnterpriseIdAndUserNumMap(@RequestBody List<Long> enterpriseIdList);


    /**
     * 通过用户id集合查询相关用户信息
     *
     * @param userIds
     * @return List<UserEntity> userList
     */
    @PostMapping("/system/user/listUserByUserIds")
    public Result listUserByUserIds(@RequestBody List<Long> userIds);

    /**
     * 获取门店中还没有被分配职位的用户列表
     *
     * @return List<UserEntity> userEntityList
     */
    @GetMapping("/system/user/getUserListWithoutPosition")
    public Result getUserListWithoutPosition(@RequestParam("token") String token);

    /**
     * 获取门店中还没有被分配职位的用户列表
     *
     * @return
     */
    @GetMapping("/system/user/getUserInfoVoListWithoutPosition")
    public Result getUserInfoVoListWithoutPosition(@RequestParam("token") String token);

    @PostMapping("/system/user/listAllMailByUserIdList")
    public Result listAllMailByUserIdList(@RequestBody List<Long> userIdList);

    @PostMapping("/system/user/getUserIdAndMailMapByUserIdList")
    public Result getUserIdAndMailMapByUserIdList(@RequestBody List<Long> userIdList);


    @GetMapping("/system/user/listUserEntityByStoreId")
    public Result listUserEntityByStoreId(@RequestParam("storeId") Long storeId);

    /**
     * 根据微信openId查询用户
     *
     * @param openid
     * @return
     */
    @RequestMapping("/system/user/getByOpenid")
    public Result getByOpenid(@RequestParam("openid") String openid);

    @RequestMapping("/system/user/update")
    public Result update(@RequestBody User user);

    @RequestMapping("/system/user/bindWechat")
    public Result bindWechat(@RequestBody User user);

    @RequestMapping("/system/user/getUserEntityByToken")
    public Result getUserEntityByToken(@RequestParam("token") String token);

    /**
     * 将权限信息存储到redis中
     *
     * @return
     */
    @PostMapping("/system/menu/storeAuthoritiesToRedis")
    public Result storeAuthoritiesToRedis(@RequestBody Map<String, Object> paramMap);
}
