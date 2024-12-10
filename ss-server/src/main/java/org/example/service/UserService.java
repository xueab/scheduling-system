package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.exception.SSSException;
import org.example.result.Result;
import org.example.vo.system.UserInfoVo;
import org.example.utils.PageUtils;
import org.example.entity.User;
import org.example.vo.system.SysUserQueryVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-02-06 15:50:29
 */
public interface UserService extends IService<User> {


    PageUtils queryPage(Map<String, Object> params);

    User getUserInfoByUsername(String username);


    PageUtils selectPage(Long page, Long limit, Long enterpriseId,Long storeId,int userType, SysUserQueryVo userQueryVo);

    UserInfoVo buildUserInfoVo(User user);

    List<UserInfoVo> buildUserInfoVoList(List<User> userList);

    boolean changePassword(String oldEncryptPassword, String oldPassword, String newPassword) throws SSSException;

    List<User> getUserListWithoutPosition(long storeId);


    List<UserInfoVo> listUserInfoVoByUserIds(List<Long> userIds);

    HashMap<Long, Long> getEnterpriseIdAndUserNumMap(List<Long> enterpriseIdList);


    void shuffleUserToDifferentStores(Long enterpriseId);

    List<User> listUserByUserIds(List<Long> userIds);

}

