package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Message;
import org.example.vo.enterprise.MessageItemVo;
import org.example.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 消息发送服务
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-02-06 15:50:30
 */
public interface MessageService extends IService<Message> {

    /**
     * 发送邮件消息
     * @param userIdList
     * @param message
     */
    void sendMailMessage(List<Long> userIdList,String subject, String message, Integer type);

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<Message> queryWrapper);

    void updateMessagePublishStatus(Long messageId, Integer isPublish);

    List<MessageItemVo> listMessageOfUser(Long userId, Long enterpriseId, Long storeId);

    List<MessageItemVo> buildMessageItemVoList(List<Message> messageEntityList);
}

