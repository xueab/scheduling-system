package org.example.service.imp;

import org.example.dao.UserMessageDao;
import org.example.entity.UserMessage;
import org.example.utils.PageUtils;
import org.example.utils.Query;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import org.example.service.UserMessageService;


@Service("userMessageService")
public class UserMessageServiceImpl extends ServiceImpl<UserMessageDao, UserMessage> implements UserMessageService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UserMessage> page = this.page(
                new Query<UserMessage>().getPage(params),
                new QueryWrapper<UserMessage>()
        );

        return new PageUtils(page);
    }



}