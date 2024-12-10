package org.example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.Message;

/**
 * 通知表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-03-20 15:43:46
 */
@Mapper
public interface MessageDao extends BaseMapper<Message> {

}
