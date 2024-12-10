package org.example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-03-13 16:42:08
 */
@Mapper
public interface OperationLogDao extends BaseMapper<OperationLog> {

}
