package org.example.service.imp;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dao.UserDao;
import org.example.entity.User;
import org.example.exception.SSSException;

import org.example.feign.ShiftSchedulingCalculateFeignService;

import org.example.entity.Position;
import org.example.entity.Store;
import org.example.entity.UserPosition;
import org.example.enums.ResultCodeEnum;
import org.example.enums.UserCodeEnum;
import org.example.result.Result;
import org.example.feign.EnterpriseFeignService;
import org.example.vo.system.UserInfoVo;
import org.example.service.UserService;
import org.example.utils.*;
import org.example.vo.system.SysUserQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {


    @Autowired
    private EnterpriseFeignService enterpriseFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ShiftSchedulingCalculateFeignService shiftSchedulingCalculateFeignService;
    @Autowired
    private UserService userService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<User> page = this.page(
                new Query<User>().getPage(params),
                new QueryWrapper<User>()
        );

        return new PageUtils(page);
    }

    @Override
    public User getUserInfoByUsername(String username) {
        User user = baseMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user;
    }




    /**
     * @param page
     * @param limit
     * @param enterpriseId 当前用户所在企业id
     * @param storeId      当前用户所在门店id
     * @param userQueryVo
     * @return
     */
    @Override
    public PageUtils selectPage(Long page, Long limit, Long enterpriseId, Long storeId, int userType, SysUserQueryVo userQueryVo) {
        Page<User> pageMps = new Page<>(page, limit);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userType == UserCodeEnum.TYPE_SYSTEM_MANAGER.getCode()) {
            //--if--如果用户是系统管理员，只查询企业管理员
            queryWrapper.eq("type", UserCodeEnum.TYPE_ENTERPRISE_MANAGER.getCode());
        } else if (userType == UserCodeEnum.TYPE_ENTERPRISE_MANAGER.getCode()) {
            //--if--如果用户是企业管理员，查询企业的所有用户
            queryWrapper.eq("enterprise_id", enterpriseId)
                    //不能查询企业管理员
                    .ne("type", UserCodeEnum.TYPE_ENTERPRISE_MANAGER.getCode())
                    //不能查询系统管理员
                    .ne("type", UserCodeEnum.TYPE_SYSTEM_MANAGER.getCode());
        } else if (userType == UserCodeEnum.TYPE_STORE_MANAGER.getCode()) {
            //--if--如果用户是企业管理员，查询门店的所有用户
            queryWrapper.eq("store_id", storeId)
                    //不能查询门店管理员
                    .ne("type", UserCodeEnum.TYPE_STORE_MANAGER.getCode())
                    //不能查询企业管理员
                    .ne("type", UserCodeEnum.TYPE_ENTERPRISE_MANAGER.getCode())
                    //不能查询系统管理员
                    .ne("type", UserCodeEnum.TYPE_SYSTEM_MANAGER.getCode());
        }
//        System.out.println("userQueryVo.getPositionIdArr():"+userQueryVo.getPositionIdArr());
        if (userQueryVo.getPositionIdArr() == null) {

        } else if (userQueryVo.getPositionIdArr().size() == 0) {
            //职位为空，一个用户都不用查
            queryWrapper.eq("id", -1);
        } else {
            //查出职位对应的所有用户Id
            List<Long> userIdList = enterpriseFeignService.listUserIdList(userQueryVo.getPositionIdArr());
            if (userIdList.size() > 0) {
                queryWrapper.in("id", userIdList);
            } else {
                queryWrapper.eq("id", -1);
            }
        }
        if (userQueryVo.getSearchStoreId() != null) {
            queryWrapper.in("store_id", userQueryVo.getSearchStoreId());
        }
        if (userQueryVo.getSearchUserType() != null) {
            queryWrapper.in("type", userQueryVo.getSearchUserType());
        }

        queryWrapper.orderByDesc("create_time");

        String keyword = userQueryVo.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            queryWrapper.like("username", "%" + keyword + "%").or()
                    .like("name", "%" + keyword + "%").or()
                    .like("phone", "%" + keyword + "%").or()
                    .like("mail", "%" + keyword + "%");
        }

        ///查询用户的繁忙状态
        if (userQueryVo.getIsNeedSearchBusyStatus() != null && userQueryVo.getIsNeedSearchBusyStatus() == true && userQueryVo.getBusyStatus() != null) {
            //--if--需要查询用书在当前班次时间段内是否繁忙
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
                if (userQueryVo.getBusyStatus() == 0) {
                    //--if--需要查询空闲的用户
                    queryWrapper.notIn("id", userIdListIsBusy);
                } else {
                    //--if--需要查询繁忙的用户
                    queryWrapper.in("id", userIdListIsBusy);
                }
            }
        }

        ///查询日期段内有班次安排的员工
        if (userQueryVo.getStartDate() != null && userQueryVo.getEndDate() != null) {
            //查询日期段内有班次安排的员工id
            HashMap<String, Object> param = new HashMap<>();
            param.put("storeId", storeId);
            param.put("startDate", userQueryVo.getStartDate().toString());
            param.put("endDate", userQueryVo.getEndDate().toString());
            if (userQueryVo.getTaskId() != null) {
                param.put("taskId", userQueryVo.getTaskId());
            }
            Result r = shiftSchedulingCalculateFeignService.listUserIdByDateSegment(param);
            if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                List<Long> userIdList = r.getData("userIdList", new TypeReference<List<Long>>() {
                });
                if (userIdList.size() > 0) {
                    queryWrapper.in("id", userIdList);
                }
            }
        }

        baseMapper.selectPage(pageMps, queryWrapper);
        return new PageUtils(pageMps);
    }

    /**
     * 根据user构建出userInfoVo
     *
     * @param user
     * @return
     */
    @Override
    public UserInfoVo buildUserInfoVo(User user) {
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(user, userInfoVo);

        // 1.获取之前的请求头数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //查询企业名称
        CompletableFuture<Void> enterpriseCompletableFuture = CompletableFuture.runAsync(() -> {
            //2.每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            if (user.getEnterpriseId() != null) {
                Result r = enterpriseFeignService.getEnterpriseEntityById(Long.valueOf(user.getEnterpriseId()));
                if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
//                    Enterprise enterprise = r.getData("enterprise", new TypeReference<Enterprise>() {
//                    });
//                    userInfoVo.setEnterpriseName(enterprise.getName());
                }
            }
        }, executor);

        //查询门店名称
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            //2.每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            if (user.getStoreId() != null) {
                Result r = enterpriseFeignService.getStoreEntityById(user.getStoreId());
                if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                    Store store = r.getData("store", new TypeReference<Store>() {
                    });
                    userInfoVo.setStoreName(store.getName());
                }
            }
        }, executor);


        //查询职位名称
        CompletableFuture<Void> positionCompletableFuture = CompletableFuture.runAsync(() -> {
            //2.每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            Result r1 = enterpriseFeignService.infoUserPositionByUserId(user.getId());
            if (r1.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                UserPosition UserPosition = r1.getData("userPosition", new TypeReference<UserPosition>() {
                });
                if (UserPosition != null) {
                    Result r2 = enterpriseFeignService.getPositionEntityById(UserPosition.getPositionId());
                    if (r2.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                        Position position = r2.getData("position", new TypeReference<Position>() {
                        });
                        userInfoVo.setPositionName(position.getName());
                        userInfoVo.setPositionId(position.getId());
                    }
                }
            }
        }, executor);

        //构建偏好数据
        CompletableFuture<Void> preferenceCompletableFuture = CompletableFuture.runAsync(() -> {
            //2.每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<Integer> workDayPreferenceList = new ArrayList<>();
            ///工作日偏好
            if (!StringUtils.isEmpty(user.getWorkDayPreference())) {
                String[] wordDayList = user.getWorkDayPreference().split("\\|");
                for (String s : wordDayList) {
                    workDayPreferenceList.add(Integer.parseInt(s));
                }
            }
            userInfoVo.setWorkDayPreferenceList(workDayPreferenceList);
        }, executor);

        try {
            CompletableFuture.allOf(enterpriseCompletableFuture,
                    storeCompletableFuture,
                    positionCompletableFuture,
                    preferenceCompletableFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return userInfoVo;
    }

    @Override
    public List<UserInfoVo> buildUserInfoVoList(List<User> userList) {
        ////声明变量
        //存储所有企业Id
        HashSet<Long> enterpriseIdSet = new HashSet<>();
        //存储所有门店Id
        HashSet<Long> storeIdSet = new HashSet<>();
        //存储所有用户Id
        List<Long> userIdList = new ArrayList<>();
        //存储封装的用户信息集合
        List<UserInfoVo> userInfoVoList = new ArrayList<>();

        ////初始化数据
        for (User user : userList) {
//            if (user.getEnterpriseId() == null || user.getStoreId() == null) {
//                System.out.println("user:"+user.toString());
//                continue;
//            }
            enterpriseIdSet.add(Long.valueOf(user.getEnterpriseId()));
            userIdList.add(user.getId());
            storeIdSet.add(user.getStoreId());
            //复制基本信息
            UserInfoVo userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(user, userInfoVo);
            //工作日偏好数据处理
            List<Integer> workDayPreferenceList = new ArrayList<>();
            if (!StringUtils.isEmpty(user.getWorkDayPreference())) {
                String[] wordDayList = user.getWorkDayPreference().split("\\|");
                for (String s : wordDayList) {
                    workDayPreferenceList.add(Integer.parseInt(s));
                }
            }

            userInfoVo.setWorkDayPreferenceList(workDayPreferenceList);
            userInfoVoList.add(userInfoVo);
        }

        // 1.获取之前的请求头数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ////查询所有用户所对应的企业名称
        CompletableFuture<Void> enterpriseCompletableFuture = CompletableFuture.runAsync(() -> {
            //2.每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            if (enterpriseIdSet.size() > 0) {
                System.out.println("查询所有用户所对应的企业名称");
                Result r = enterpriseFeignService.getEnterpriseMapByIdList(new ArrayList<>(enterpriseIdSet));
                if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                    //存储企业id及其对应的企业
//                    Map<Long, Enterprise> idAndEnterpriseMap = r.getData("idAndEnterpriseMap", new TypeReference<Map<Long, Enterprise>>() {
//                    });
//                    for (UserInfoVo userInfoVo : userInfoVoList) {
//                        if (userInfoVo.getEnterpriseId() != null && idAndEnterpriseMap.get(userInfoVo.getEnterpriseId()) != null) {
//                            userInfoVo.setEnterpriseName(idAndEnterpriseMap.get(userInfoVo.getEnterpriseId()).getName());
//                        } else {
//                            userInfoVo.setEnterpriseName(null);
//                        }
//                    }

                }

            }
        }, executor);

        ////查询门店信息
        //查询门店名称
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            //2.每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            if (storeIdSet.size() > 0) {
                System.out.println("查询门店信息");
                Result r = enterpriseFeignService.getStoreMapByIdList(new ArrayList<>(storeIdSet));
                if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                    Map<Long, Store> idAndStoreMap = r.getData("idAndStoreMap", new TypeReference<Map<Long, Store>>() {});
                    for (UserInfoVo userInfoVo : userInfoVoList) {
                        if (userInfoVo.getStoreId() != null && idAndStoreMap.get(userInfoVo.getStoreId()) != null) {
                            userInfoVo.setStoreName(idAndStoreMap.get(userInfoVo.getStoreId()).getName());
                        } else {
                            userInfoVo.setStoreName(null);
                        }
                    }
                }
            }
        }, executor);

        ////查询职位名称
        CompletableFuture<Void> positionCompletableFuture = CompletableFuture.runAsync(() -> {
            //2.每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            System.out.println("查询职位名称");
            if (userIdList.size() > 0) {
                Result r = enterpriseFeignService.getUserIdAndPositionIdMapByUserIdList(userIdList);
                if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                    Map<Long, Long> userIdAndPositionIdMap = r.getData("userIdAndPositionIdMap", new TypeReference<Map<Long, Long>>() {
                    });

                    List<Long> positionIdList = new ArrayList<>();
                    for (Map.Entry<Long, Long> entry : userIdAndPositionIdMap.entrySet()) {
                        positionIdList.add(entry.getValue());
                    }

                    Result r2 = enterpriseFeignService.getPositionMapByIdList(positionIdList);
                    if (r2.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
                        Map<Long, Position> idAndPositionMap = r2.getData("idAndPositionMap", new TypeReference<Map<Long, Position>>() {
                        });
//                        List<UserInfoVo> userInfoVoListWhoHaveNotPosition = new ArrayList<>();
                        for (UserInfoVo userInfoVo : userInfoVoList) {
                            if (userIdAndPositionIdMap.get(userInfoVo.getId()) != null) {
                                userInfoVo.setPositionId(userIdAndPositionIdMap.get(userInfoVo.getId()));
                                userInfoVo.setPositionName(idAndPositionMap.get(userInfoVo.getPositionId()).getName());
                            } else {
                                userInfoVo.setPositionId(null);
                                userInfoVo.setPositionName(null);
//                                userInfoVoListWhoHaveNotPosition.add(userInfoVo);
                            }
                        }
                        //移除没有职位的员工，因为没办法分配工作
//                        userInfoVoList.removeAll(userInfoVoListWhoHaveNotPosition);
                    }
                }
            }

        }, executor);

        try {
            CompletableFuture.allOf(enterpriseCompletableFuture,
                    storeCompletableFuture,
                    positionCompletableFuture).get();
            return userInfoVoList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean changePassword(String token, String oldPassword, String newPassword) throws SSSException {
        String username = JwtUtil.getUsername(token);
        User user = this.getUserInfoByUsername(username);
        String oldEncryptPassword = user.getPassword();

        //判断旧密码是否正确
        if (!EncryptionUtil.isSaltMd5Match(oldPassword, oldEncryptPassword)) {
            throw new SSSException(ResultCodeEnum.FAIL.getCode(), "原密码不合法");
        }

        //存储新密码
        if (CheckUtil.passwordCheck(newPassword)) {
            user.setPassword(EncryptionUtil.saltMd5Encrypt(newPassword));
            baseMapper.updateById(user);
            return true;
        } else {
            throw new SSSException(ResultCodeEnum.FAIL.getCode(), "新密码不合法");
        }
    }

    /**
     * 获取门店中还没有被分配职位的用户列表
     *
     * @param storeId
     * @return
     */
    @Override
    public List<User> getUserListWithoutPosition(long storeId) {
        return baseMapper.getUserListWithoutPosition(storeId);
    }
    @Override
    public List<UserInfoVo> listUserInfoVoByUserIds(List<Long> userIds) {
        List<User> userList = new ArrayList<>();
        if (userIds.size() > 0) {
            userList.addAll(baseMapper.selectList(new QueryWrapper<User>().in("id", userIds)));
        }
        return this.buildUserInfoVoList(userList);
    }

    @Override
    public List<User> listUserByUserIds(List<Long> userIds) {
        List<User> userList = new ArrayList<>();
        if (userIds.size() > 0) {
            userList.addAll(baseMapper.selectList(new QueryWrapper<User>().in("id", userIds)));
        }
        return userList;
    }

    @Override
    public HashMap<Long, Long> getEnterpriseIdAndUserNumMap(List<Long> enterpriseIdList) {
        HashMap<Long, Long> enterpriseIdAndUserNumMap = new HashMap<>();
        for (Long enterpriseId : enterpriseIdList) {
            Long count = (long) userService.count(new QueryWrapper<User>().eq("enterprise_id", enterpriseId));
            enterpriseIdAndUserNumMap.put(enterpriseId, count);
        }
        return enterpriseIdAndUserNumMap;
    }

    /**
     * 打乱企业的用户到不同的门店
     *
     * @param enterpriseId
     */
    @Override
    public void shuffleUserToDifferentStores(Long enterpriseId) {
        Random random = new Random();
        ///查询企业的所有普通用户
        List<User> allUserList = baseMapper.selectList(new QueryWrapper<User>()
                .eq("enterprise_id", enterpriseId)
                .eq("type", UserCodeEnum.TYPE_ORDINARY_USER.getCode()));
        ///随机分配门店
        List<Long> storeIdList = null;
        Result r = enterpriseFeignService.listAllStoreByAppointEnterpriseId(enterpriseId);
        if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
            List<Store> storeList = r.getData("list", new TypeReference<List<Store>>() {
            });
            storeIdList = storeList.stream().map(Store::getId).collect(Collectors.toList());
            for (User user : allUserList) {
                user.setStoreId(storeList.get(random.nextInt(storeList.size())).getId());
            }
        }
        ///查询出每个门店所拥有的positionIdList
        Result r2 = enterpriseFeignService.getStoreIdAndPositionList(storeIdList);
        Map<Long, List<Position>> storeIdAndPositionList = null;
        if (r2.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
            storeIdAndPositionList = r2.getData("storeIdAndPositionList", new TypeReference<Map<Long, List<Position>>>() {
            });
        }

        ///随机分配职位
        //将用户原本的职位删除
        List<Long> userIdList = allUserList.stream().map(User::getId).collect(Collectors.toList());
        Result r1 = enterpriseFeignService.deleteUserPositionByUserIdList(userIdList);
        if (r1.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
            ///分配新的职位
            List<UserPosition> newUserPositionList = new ArrayList<>();
            for (User user : allUserList) {
                UserPosition userPosition = new UserPosition();
                userPosition.setUserId(user.getId());
                List<Position> positionList = storeIdAndPositionList.get(user.getStoreId());
                userPosition.setPositionId(positionList.get(random.nextInt(positionList.size())).getId());
                newUserPositionList.add(userPosition);
            }
            enterpriseFeignService.saveUserPositionList(newUserPositionList);
        }

        ///修改用户
        this.updateBatchById(allUserList);
    }


}