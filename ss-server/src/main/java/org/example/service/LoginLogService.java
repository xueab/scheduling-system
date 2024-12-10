package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.LoginLog;
import org.example.utils.PageUtils;

import java.util.Map;

public interface LoginLogService extends IService<LoginLog> {
    PageUtils queryPage(Map<String, Object> params, String token);
}
