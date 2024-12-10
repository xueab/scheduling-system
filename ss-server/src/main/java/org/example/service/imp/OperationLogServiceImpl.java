package org.example.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dao.OperationLogDao;
import org.example.entity.OperationLog;
import org.example.entity.User;
import org.example.enums.UserCodeEnum;
import org.example.service.OperationLogService;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.example.utils.PageUtils;
import org.example.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("operationLogService")
class OperationLogServiceImpl extends ServiceImpl<OperationLogDao, OperationLog> implements OperationLogService {
    @Autowired
    private OperationLogDao OperationLogDao;
    @Autowired
    private UserService userService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, String token) {
        Long userId = Long.parseLong(JwtUtil.getUserId(token));
        User user = userService.getById(userId);
        String enterpriseId = JwtUtil.getEnterpriseId(token);
        String storeId = JwtUtil.getStoreId(token);
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();

        if (user.getType() == UserCodeEnum.TYPE_SYSTEM_MANAGER.getCode().intValue()) {
            //--if--系统管理员，可以查询所有日志
        } else if (user.getType() == UserCodeEnum.TYPE_ENTERPRISE_MANAGER.getCode().intValue()) {
            //--if--企业管理员，只能查询企业的日志
            queryWrapper.eq("enterprise_id", enterpriseId);
        } else if (user.getType() == UserCodeEnum.TYPE_STORE_MANAGER.getCode().intValue()) {
            //--if--门店管理员，只能查询门店的日志
            queryWrapper.eq("store_id", storeId);
        } else {
            //--if--普通用户，什么都查不出来
            queryWrapper.eq("id", -1);
        }

        queryWrapper.orderByDesc("create_time");
        IPage<OperationLog> page = this.page(
                new Query<OperationLog>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public OperationLog getById(Long id) {
        return OperationLogDao.selectById(id);
    }

}