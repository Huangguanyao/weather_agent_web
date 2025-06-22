package com.qf.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //标识此类是一个配置类
public class CommonConfiguration {

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel){
        return ChatClient
                .builder(ollamaChatModel)
                .defaultSystem("你是判断钓鱼网站的专家，你要帮我判断我问的url是否有可能为钓鱼网站，中文回答")
                .build();
    }

}