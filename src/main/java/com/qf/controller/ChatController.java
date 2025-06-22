package com.qf.controller;

import com.qf.entity.Message;
import com.qf.service.MessageService;
import com.qf.test_api.Deepanswer;
import com.qf.test_api.Deepsuit;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import com.qf.test_api.weather;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class ChatController {

    private final ChatClient chatClient;
    @Autowired
    private MessageService messageService;
    private final OllamaChatModel ollamaChatModel;

    @RequestMapping(value = "/chat")
    public Flux<ServerSentEvent<String>> chat(String prompt) {
        // 保存用户提问
        Message userMsg = new Message();
        userMsg.setSender("user");
        userMsg.setContent(prompt);
        userMsg.setCreated_at(LocalDateTime.now());
        messageService.save(userMsg);

        // 调用天气API获取结果（直接返回字符串）
        String weatherResult1 = Deepanswer.main(prompt);
        String weatherResult2 = Deepsuit.main(prompt+weatherResult1);

        String weatherResult=weatherResult1+weatherResult2;

        // 直接返回字符串（不再转换为JSON）
        return Flux.just(weatherResult)
                .map(data -> ServerSentEvent.builder(data).event("message").build())
                .doOnComplete(() -> {
                    // 保存AI回答（原始字符串）
                    Message aiMsg = new Message();
                    aiMsg.setSender("AI");
                    aiMsg.setContent(weatherResult);
                    aiMsg.setCreated_at(LocalDateTime.now());
                    messageService.save(aiMsg);
                });
    }

    // 移除错误的convertMapToJson方法（不再需要）
    // 如果其他地方需要，应正确处理Map类型
    /*private String convertMapToJson(String map) {
        // 原方法存在类型错误，已移除
    }*/

    @RequestMapping("/messages")
    public List<Message> getMessages() {
        return messageService.list();
    }
}