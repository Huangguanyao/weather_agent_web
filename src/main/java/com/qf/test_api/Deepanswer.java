package com.qf.test_api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deepanswer {
    private final HttpClient client;
    private final String apiKey;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public Deepanswer(String apiKey, String baseUrl) {
        this.client = HttpClient.newBuilder().build();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
    }

    public Deepanswer(String apiKey) {
        this(apiKey, "https://api.deepseek.com");
    }

    public String extractCity(String text) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", List.of(
                    Map.of(
                            "role", "system",
                            "content", "你是一个专业的天气数据解析器。请根据用户提供的天气信息，提取指定地区的天气情况，按照以下格式输出：\n1. 地区名称\n- 气温：XX~XX℃\n- 紫外线指数：X级（中等）\n- 湿度：XX%\n- 降水量：X\n\n降水量只保留整数部分。"
                    ),
                    Map.of("role", "user", "content", text)
            ));
            requestBody.put("temperature", 0.2);
            requestBody.put("max_tokens", 500);

            // 构建HTTP请求
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 打印调试信息
            System.out.println("API状态码: " + response.statusCode());
            System.out.println("API响应: " + response.body());

            // 解析响应
            if (response.statusCode() == 200) {
                ChatCompletionResponse chatResponse = objectMapper.readValue(
                        response.body(),
                        ChatCompletionResponse.class
                );

                if (!chatResponse.getChoices().isEmpty()) {
                    String city = chatResponse.getChoices().get(0).getMessage().getContent().trim();
                    System.out.println("内容: " + city);
                    return city.isEmpty() ? null : city;
                }
            }

            return null;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 内部类：表示聊天完成响应
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionResponse {
        @JsonProperty("choices")
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }
    }

    // 内部类：表示响应中的选择
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        @JsonProperty("message")
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    // 内部类：表示消息 - 添加@JsonIgnoreProperties
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("content")
        private String content;

        public String getContent() {
            return content;
        }
    }

    public static String main(String prompt) {
        String apiKey = "key";
        Deepanswer detector = new Deepanswer(apiKey);


        String question = weather.main(prompt);
        System.out.println("QUESYION"+question);
        String answer = detector.extractCity(question);



        return answer;
    }
}