package org.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.dam.annotation.Idempotent;
//import org.example.enums.IdempotentSceneEnum;
//import org.example.enums.IdempotentTypeEnum;
import org.example.exception.SSSException;
import org.example.feign.ShiftSchedulingCalculateFeignService;
import org.example.entity.User;
import org.example.entity.UserRole;
import org.example.enums.ResultCodeEnum;
import org.example.result.Result;
import org.example.vo.system.UserInfoVo;
import org.example.service.UserRoleService;
import org.example.service.UserService;
import org.example.utils.EncryptionUtil;
import org.example.utils.JwtUtil;
import org.example.utils.PageUtils;

import org.example.vo.system.SysUserQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * @author dam
 * @email 1782067308@qq.com
 * @date 2022-12-03 11:10:46
 */
@RestController
@RequestMapping("/system/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private ShiftSchedulingCalculateFeignService shiftSchedulingCalculateFeignService;
    private static final String title = "用户管理";

    @PostMapping("/{page}/{limit}")
    @PreAuthorize("hasAnyAuthority('bnt.user.list','bnt.storeUser.list')")
    public Result index(
            @PathVariable Long page,
            @PathVariable Long limit,
            @RequestBody SysUserQueryVo userQueryVo,
            HttpServletRequest httpServletRequest) {
        Long enterpriseId = Long.parseLong(JwtUtil.getEnterpriseId(httpServletRequest.getHeader("token")));
        Long storeId = Long.parseLong(JwtUtil.getStoreId(httpServletRequest.getHeader("token")));
        int userType = Integer.parseInt(JwtUtil.getUserType(httpServletRequest.getHeader("token")));

        PageUtils pageModel = userService.selectPage(page, limit, enterpriseId, storeId, userType, userQueryVo);

        ///封装vo数据
        List<User> userList = (List<User>) pageModel.getList();
        List<Long> userIdList = new ArrayList<>();
        List<UserInfoVo> userInfoVoList = userService.buildUserInfoVoList(userList);
        for (User user : userList) {
            userIdList.add(user.getId());
        }

        ///查询用户的繁忙状态
        if (userQueryVo.getBusyStatus() == null) {
            if (userQueryVo.getIsNeedSearchBusyStatus() != null && userQueryVo.getIsNeedSearchBusyStatus() == true) {
                //--if--需要查询用户在当前班次时间段内是否繁忙
                Date shiftStartDate = userQueryVo.getShiftStartDate();
                Date shiftEndDate = userQueryVo.getShiftEndDate();
                Map<String, Object> param = new HashMap<>();
                param.put("shiftStartDate", shiftStartDate);
                param.put("shiftEndDate", shiftEndDate);
                param.put("storeId", storeId);
                Result r = shiftSchedulingCalculateFeignService.listUserIdIsBusy(param);
                if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                    List<Long> userIdListIsBusy = r.getData("userIdListIsBusy", new TypeReference<List<Long>>() {
                    });
                    for (UserInfoVo userInfoVo : userInfoVoList) {
                        if (userIdListIsBusy.indexOf(userInfoVo.getId()) != -1) {
                            //--if--用户繁忙
                            userInfoVo.setIsBusy(true);
                        } else {
                            userInfoVo.setIsBusy(false);
                        }
                    }
                }
            }
        } else {
            if (userQueryVo.getBusyStatus() == 0) {
                for (UserInfoVo userInfoVo : userInfoVoList) {
                    //--if--查询的数据都是空闲状态的
                    userInfoVo.setIsBusy(false);
                }
            } else {
                for (UserInfoVo userInfoVo : userInfoVoList) {
                    userInfoVo.setIsBusy(true);
                }
            }
        }

        pageModel.setList(userInfoVoList);

        return Result.ok().addData("page", pageModel);
    }

    @GetMapping("/listUserByStoreId")
//    @PreAuthorize("hasAnyAuthority('bnt.user.list','bnt.storeUser.list')")
    public Result listUserByStoreId(@RequestParam("storeId") Long storeId) {
        long start = System.currentTimeMillis();
        List<User> userList = userService.list(new QueryWrapper<User>().eq("store_id", storeId).eq("is_deleted", 0));
//        System.out.println("0：" + (System.currentTimeMillis() - start) + "ms");
        List<UserInfoVo> userInfoVoList = userService.buildUserInfoVoList(userList);
//        System.out.println("1：" + (System.currentTimeMillis() - start) + "ms");
        return Result.ok().addData("userInfoVoList", userInfoVoList);
    }

    @GetMapping("/listUserByStoreId")
//    @PreAuthorize("hasAnyAuthority('bnt.user.list','bnt.storeUser.list')")
    public Result listUserEntityByStoreId(@RequestParam("storeId") Long storeId) {
        long start = System.currentTimeMillis();
        List<User> userList = userService.list(new QueryWrapper<User>().eq("store_id", storeId).eq("is_deleted", 0));
        return Result.ok().addData("userList", userList);
    }

    /**
     * 根据企业id集合查询用户，并封装成字典
     *
     * @return Map<Long, User> idAndUserMap
     */
    @PostMapping("/getUserMapByIdList")
    public Result getUserMapByIdList(@RequestBody List<Long> userIdList) {
        Map<Long, User> idAndUserMap = new HashMap<>();
        if (userIdList.size() > 0) {
            List<User> userList = userService.list(new QueryWrapper<User>().in("id", userIdList).eq("is_deleted", 0));
            for (User user : userList) {
                idAndUserMap.put(user.getId(), user);
            }
        }
        return Result.ok().addData("idAndUserMap", idAndUserMap);
    }

    /**
     * 列表
     */
/*    @RequestMapping("/list")
    public Result list(@RequestParam Map<String, Object> params) {
        PageUtils page = userService.queryPage(params);

        return Result.ok().addData("page", page);
    }*/

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @PreAuthorize("hasAnyAuthority('bnt.user.list','bnt.storeUser.list')")
    public Result info(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        return Result.ok().addData("data", user);
    }

    /**
     * 信息
     */
    @RequestMapping("/getUserById")
    @PreAuthorize("hasAnyAuthority('bnt.user.list','bnt.storeUser.list')")
    public Result getUserById(@RequestParam("id") Long id) {
        User user = userService.getById(id);
        return Result.ok().addData("user", user);
    }



    @RequestMapping("/userInfoVo")
    public Result userInfoVo(@RequestParam("id") Long id) {
        User user = userService.getById(id);
        UserInfoVo userInfoVo = null;
        if (user != null) {
            userInfoVo = userService.buildUserInfoVo(user);
        } else {
            System.out.println("id:" + id + "对应的用户为空");
        }
        return Result.ok().addData("data", userInfoVo);
    }

    /**
     * 通过用户id集合查询相关用户信息
     *
     * @param userIds
     * @return
     */
    @PostMapping("/listUserInfoVoByUserIds")
    public Result listUserInfoVoByUserIds(@RequestBody List<Long> userIds) {
        long start = System.currentTimeMillis();
        List<UserInfoVo> userInfoVoList = userService.listUserInfoVoByUserIds(userIds);
//        System.out.println("listUserInfoVoByUserIds：" + (System.currentTimeMillis() - start) + "ms");
        return Result.ok().addData("userInfoVoList", userInfoVoList);
    }

    /**
     * 通过用户id集合查询相关用户信息
     *
     * @param userIds
     * @return List<User> userList
     */
    @PostMapping("/listUserByUserIds")
    public Result listUserByUserIds(@RequestBody List<Long> userIds) {
        List<User> userList = userService.listUserByUserIds(userIds);
        return Result.ok().addData("userList", userList);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @PreAuthorize("hasAnyAuthority('bnt.user.add','bnt.storeUser.add')")
    public Result save(@RequestBody User user) {
        user.setPassword(EncryptionUtil.saltMd5Encrypt(user.getPassword()));
     //   user.setAvatar("https://smart-scheduling-system-13184.oss-cn-beijing.aliyuncs.com/2023-02-14/338fc2a2-62d4-4196-85f3-c479acf28ff4_头像.png");
        long count = userService.count(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (count > 0) {
            return Result.error(ResultCodeEnum.FAIL.getCode(), "该用户名已经存在，请更换用户名");
        } else {
            userService.save(user);
            return Result.ok();
        }
    }

    /**
     * 直接保存用户
     */
    @RequestMapping("/directSave")
    @PreAuthorize("hasAnyAuthority('bnt.user.add','bnt.storeUser.add')")
    public Result directSave(@RequestBody User user) {
        userService.save(user);
        ///赋予用户角色
        UserRole UserRole = new UserRole();
        UserRole.setRoleId(3L);
        UserRole.setUserId(user.getId());
        userRoleService.save(UserRole);
        return Result.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('bnt.user.update','bnt.storeUser.update')")

//    @Idempotent(
//            message = "正在执行用户信息修改流程，请稍后...",
//            scene = IdempotentSceneEnum.RESTAPI,
//            type = IdempotentTypeEnum.PARAM
//    )
    public Result update(@RequestBody User user) {
//        System.out.println("save->user:" + user.toString());
        boolean b = userService.updateById(user);
//        System.out.println("更新是否成功：" + b);
//        try {
//            Thread.sleep(10000L);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return Result.ok();
    }


    /**
     * 获取每个企业及其用户数量
     *
     * @param enterpriseIdList
     * @return
     */
    @PostMapping("/getEnterpriseIdAndUserNumMap")
    public Result getEnterpriseIdAndUserNumMap(@RequestBody List<Long> enterpriseIdList) {
        HashMap<Long, Long> enterpriseIdAndUserNumMap = userService.getEnterpriseIdAndUserNumMap(enterpriseIdList);
        return Result.ok().addData("enterpriseIdAndUserNumMap", enterpriseIdAndUserNumMap);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete/{useId}")
    @PreAuthorize("hasAnyAuthority('bnt.user.delete','bnt.storeUser.delete')")
    public Result delete(@PathVariable Long useId) {
        userService.removeById(useId);

        return Result.ok();
    }


    /**
     * 删除
     */
    @PostMapping("/deleteBatch")
    @PreAuthorize("hasAnyAuthority('bnt.user.delete','bnt.storeUser.delete')")
    public Result deleteBatch(@RequestBody Long[] ids) {
        userService.removeByIds(Arrays.asList(ids));

        return Result.ok();
    }



    /**
     * 根据token获取用户信息
     *
     * @param request
     * @return
     */
    @RequestMapping("getUserInfoVoByToken")
    public Result getUserInfoVoByToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        //调用jwt工具类方法，根据request对象获取头信息，返回用户id
        String username = JwtUtil.getUsername(token);
        User user = userService.getUserInfoByUsername(username);
        UserInfoVo userInfoVo = userService.buildUserInfoVo(user);
        return Result.ok().addData("userInfoVo", userInfoVo);
    }

    /**
     * 根据token获取用户信息
     *
     * @param
     * @return
     */
    @RequestMapping("getUserByToken")
    public Result getUserByToken(@RequestParam("token") String token) {
        System.out.println("getUserByToken,token:" + token);
        //调用jwt工具类方法，根据request对象获取头信息，返回用户id
        String username = JwtUtil.getUsername(token);
        System.out.println("getUserByToken,username:" + username);
        User user = userService.getUserInfoByUsername(username);
        System.out.println("getUserByToken,user:" + JSON.toJSONString(user));
        return Result.ok().addData("user", user);
    }

    @RequestMapping("changePassword")
    public Result changePassword(@RequestParam("oldPassword") String oldPassword,
                            @RequestParam("newPassword") String newPassword,
                            HttpServletRequest request) {
        String token = request.getHeader("token");

        try {
            userService.changePassword(token, oldPassword, newPassword);
            return Result.ok();
        } catch (SSSException e) {
            e.printStackTrace();
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取门店中还没有被分配职位的用户列表
     *
     * @return
     */
    @GetMapping("getUserInfoVoListWithoutPosition")
    public Result getUserInfoVoListWithoutPosition(@RequestParam("token") String token) {
        long storeId = Long.parseLong(JwtUtil.getStoreId(token));
        List<User> userList = userService.getUserListWithoutPosition(storeId);
        List<UserInfoVo> userInfoVoList = userService.buildUserInfoVoList(userList);
        return Result.ok().addData("data", userInfoVoList);
    }

    /**
     * 获取门店中还没有被分配职位的用户列表
     *
     * @return List<User> userList
     */
    @GetMapping("getUserListWithoutPosition")
    public Result getUserListWithoutPosition(@RequestParam("token") String token) {
        long storeId = Long.parseLong(JwtUtil.getStoreId(token));
        List<User> userList = userService.getUserListWithoutPosition(storeId);
        return Result.ok().addData("userList", userList);
    }

//    /**
//     * 随机生成用户数据
//     *
//     * @return
//     */
//    @GetMapping("generateUserData")
//    public Result generateUserData() {
//        List<User> userList = new UserDataGenerateUtil().generateUserData(2000);
//        for (User user : userList) {
////            System.out.println("user:" + user);
//            user.setPassword(EncryptionUtil.saltMd5Encrypt(user.getPassword()));
//            userService.save(user);
//        }
//        return Result.ok();
//    }
//



    @GetMapping("shuffleUserToDifferentStores")
    public Result shuffleUserToDifferentStores(@RequestParam("enterpriseId") Long enterpriseId) {
        userService.shuffleUserToDifferentStores(enterpriseId);
        return Result.ok();
    }
}
