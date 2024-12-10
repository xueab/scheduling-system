package org.example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogDao extends BaseMapper<LoginLog> {
}
