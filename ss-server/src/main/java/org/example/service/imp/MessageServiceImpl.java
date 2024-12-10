package org.example.service.imp;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dao.MessageDao;
import org.example.feign.SystemFeignService;
//import org.example.feign.ThirdPartyFeignService;
//import com.dam.model.dto.third_party.EmailDto;
import org.example.entity.Message;
import org.example.entity.UserMessage;
import org.example.entity.User;
import org.example.enums.ResultCodeEnum;
import org.example.result.Result;
import org.example.vo.enterprise.MessageItemVo;
import org.example.service.MessageService;
import org.example.service.UserMessageService;
import org.example.utils.PageUtils;
import org.example.utils.Query;
//import org.example.utils.mail.MailUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("messageService")
public class MessageServiceImpl extends ServiceImpl<MessageDao, Message> implements MessageService {
//    @Autowired
//    private ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    private SystemFeignService systemFeignService;
    @Autowired
    private UserMessageService userMessageService;

    /**
     * 发送邮件消息
     *
     * @param userIdList
     * @param message
     */
    @Override
    public void sendMailMessage(List<Long> userIdList, String subject, String message, Integer type) {
        Result r = systemFeignService.listAllMailByUserIdList(userIdList);
        if (r.getCode() != ResultCodeEnum.SUCCESS.getCode().intValue()) {
            return;
        }

    }
    @Override
    public PageUtils queryPage(Map<String, Object> params, QueryWrapper<Message> queryWrapper) {

        IPage<Message> page = this.page(
                new Query<Message>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void updateMessagePublishStatus(Long messageId, Integer isPublish) {
        Message Message = new Message();
        Message.setIsPublish(isPublish);
        if (isPublish == 1) {
            Message.setPublishTime(new Date());
        } else {
            Message.setPublishTime(null);
        }
        Message.setId(messageId);
        baseMapper.updateById(Message);
    }

    @Override
    public List<MessageItemVo> listMessageOfUser(Long userId, Long enterpriseId, Long storeId) {
        List<Message> MessageList = new ArrayList<>();

        //查询企业公开的消息
        MessageList.addAll(baseMapper.selectList(new QueryWrapper<Message>().eq("type", 0).eq("enterprise_id", enterpriseId).eq("is_publish", 1)));

        //查询门店公开的消息
        MessageList.addAll(baseMapper.selectList(new QueryWrapper<Message>().eq("type", 1).eq("store_id", storeId).eq("is_publish", 1)));

        //查询给用户看的消息
        List<UserMessage> userMessageList = userMessageService.list(new QueryWrapper<UserMessage>().eq("user_id", userId));
        List<Long> messageIdList = userMessageList.stream().map(UserMessage::getMessageId).collect(Collectors.toList());
        if (messageIdList.size() > 0) {
            MessageList.addAll(baseMapper.selectList(new QueryWrapper<Message>().in("id", messageIdList)));
        }

        //对消息按照时间升序排序
        Collections.sort(MessageList, (o1, o2) -> {
            long o1Time = o1.getPublishTime().getTime();
            long o2Time = o2.getPublishTime().getTime();
            return Long.compare(o1Time, o2Time);
        });

        //封装vo
        List<MessageItemVo> messageItemVoList = this.buildMessageItemVoList(MessageList);

        return messageItemVoList;
    }

    /**
     * 封装信息vo
     *
     * @param MessageList
     * @return
     */
    @Override
    public List<MessageItemVo> buildMessageItemVoList(List<Message> MessageList) {
        HashSet<Long> userIdSet = new HashSet<>();
        List<MessageItemVo> messageItemVoList = new ArrayList<>();
        for (Message Message : MessageList) {
            if (Message.getCreateUserId()!=null&&Message.getCreateUserId() != -1) {
                userIdSet.add(Message.getCreateUserId());
            }
//            if (Message.getPublishUserId()!=null&&Message.getPublishUserId() != -1) {
//                userIdSet.add(Message.getPublishUserId());
//            }

            MessageItemVo messageItemVo = new MessageItemVo();
            BeanUtils.copyProperties(Message, messageItemVo);
            messageItemVoList.add(messageItemVo);
        }
        Result r = systemFeignService.getUserMapByIdList(new ArrayList<>(userIdSet));
        if (r.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
            Map<Long, User> idAndUserEntityMap = r.getData("idAndUserEntityMap", new TypeReference<Map<Long, User>>() {
            });
            for (MessageItemVo messageItemVo : messageItemVoList) {
                if (messageItemVo.getPublishUserId()!=null&&messageItemVo.getPublishUserId() != -1) {
                    User userEntity = idAndUserEntityMap.get(messageItemVo.getPublishUserId());
                    messageItemVo.setPublishUsername(userEntity.getUsername());
                    messageItemVo.setPublishName(userEntity.getName());
                   // messageItemVo.setPublishAvatar(userEntity.getAvatar());
                } else {
                    messageItemVo.setPublishUsername("小智");
                    messageItemVo.setPublishName("小智");
                    messageItemVo.setPublishAvatar("https://smart-scheduling-system-13184.oss-cn-beijing.aliyuncs.com/2023-03-29/734f0211-9425-4e9e-8437-d864ffed4d5f_logo3_collapse.png");
                }
            }
        }
        return messageItemVoList;
    }
}
