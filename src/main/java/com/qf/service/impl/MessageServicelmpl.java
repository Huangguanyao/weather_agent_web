package com.qf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qf.entity.Message;
import com.qf.mapper.MessageMapper;
import com.qf.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServicelmpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
}