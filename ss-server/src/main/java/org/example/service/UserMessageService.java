package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.UserMessage;
import org.example.utils.PageUtils;

import java.util.Map;

/**
 * 用户-消息中间表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-03-20 15:43:46
 */
public interface UserMessageService extends IService<UserMessage> {

    PageUtils queryPage(Map<String, Object> params);
}

