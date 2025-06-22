package com.qf.test_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class weather {
    // 和风天气API基础URL
    private static final String API_BASE_URL = "https://nf7dn8ftud.re.qweatherapi.com";
    // 替换为你的API密钥
    private static final String API_KEY = "key";
    // User-Agent标识
    private static final String USER_AGENT = "WeatherAPI-Test/1.0";
    // 线程池大小
    private static final int THREAD_POOL_SIZE = 5;

    public static String main(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("===== 和风天气API批量调用测试 =====");
        StringBuilder responseBuilder = new StringBuilder();

        try {
            // 获取location列表
            List<String> locations = get_city.main(prompt);
            System.out.println("待查询地点ID: " + locations);

            // 将ID转换为区县名称
            List<String> districtNames = locations.stream()
                    .map(get_city::getDistrictNameById)
                    .collect(Collectors.toList());
            System.out.println("待查询区县: " + districtNames);

            String timeRange = "now";

            // 调用天气API获取数据并提取完整信息
            Map<String, ApiResponse> apiResponses = batchCallWeatherAPI(locations, timeRange, districtNames);

//            // 构建包含所有响应的字符串
//            responseBuilder.append("===== API响应汇总 =====\n\n");
//            for (Map.Entry<String, ApiResponse> entry : apiResponses.entrySet()) {
//                String location = entry.getKey();
//                ApiResponse response = entry.getValue();
//
//                responseBuilder.append("------------------------\n");
//                responseBuilder.append(String.format("地点ID: %s\n", location));
//                responseBuilder.append(String.format("区县名称: %s\n", response.getDistrictName()));
//                responseBuilder.append("API响应内容:\n");
//                responseBuilder.append(response.getRawResponse());
//                responseBuilder.append("\n------------------------\n\n");
//            }

            // 显示结果
            responseBuilder.append("===== 汇总 =====\n\n");
            for (String location : locations) {
                String districtName = get_city.getDistrictNameById(location);
                ApiResponse response = apiResponses.get(location);
                String weatherInfo = response != null ? response.getFormattedWeatherInfo() : "获取失败";

                responseBuilder.append(String.format("%s: %s\n", districtName, weatherInfo));
            }

            // 示例：通过地区名称查询天气
            if (!apiResponses.isEmpty()) {
                String sampleLocation = apiResponses.keySet().iterator().next();
                ApiResponse sampleResponse = apiResponses.get(sampleLocation);
                System.out.printf("\n示例查询: %s 的天气信息: %s\n",
                        sampleResponse.getDistrictName(), sampleResponse.getFormattedWeatherInfo());
            }

        } catch (Exception e) {
            System.out.println("发生错误：" + e.getMessage());
            e.printStackTrace();
            responseBuilder.append("错误信息: ").append(e.getMessage());
        } finally {
            scanner.close();
        }

        return responseBuilder.toString();
    }

    /**
     * 批量调用天气API并保存原始响应
     */
    private static Map<String, ApiResponse> batchCallWeatherAPI(List<String> locations, String timeRange,
                                                                List<String> districtNames)
            throws IOException, InterruptedException {
        Map<String, ApiResponse> results = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < locations.size(); i++) {
            String location = locations.get(i);
            String districtName = districtNames.get(i);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String response = callWeatherAPI(location, timeRange);
                    String weatherInfo = extractWeatherInfo(response);

                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.setDistrictName(districtName);
                    apiResponse.setRawResponse(response);
                    apiResponse.setFormattedWeatherInfo(weatherInfo);

                    results.put(location, apiResponse);
                } catch (Exception e) {
                    System.err.println("处理地点 " + location + " 时出错: " + e.getMessage());

                    ApiResponse errorResponse = new ApiResponse();
                    errorResponse.setDistrictName(districtName);
                    errorResponse.setFormattedWeatherInfo("获取失败: " + e.getMessage());

                    results.put(location, errorResponse);
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        return results;
    }

    /**
     * 调用单个天气API
     */
    private static String callWeatherAPI(String location, String timeRange) throws IOException, InterruptedException {
        String apiPath = getApiPath(timeRange);
        String url = String.format("%s/%s?location=%s&key=%s", API_BASE_URL, apiPath, location, API_KEY);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Encoding", "gzip")
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            byte[] responseBody = response.body();
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(responseBody));
                 InputStreamReader reader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8)) {
                StringBuilder result = new StringBuilder();
                int c;
                while ((c = reader.read()) != -1) {
                    result.append((char) c);
                }
                return result.toString();
            }
        } else {
            System.out.println("API请求失败，地点: " + location + ", 状态码: " + response.statusCode());
            return "API请求失败，状态码: " + response.statusCode();
        }
    }

    /**
     * 从API响应中提取完整天气信息并格式化为字符串
     */
    private static String extractWeatherInfo(String responseBody) {
        System.out.println("原始响应: " + responseBody);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            // 检查API状态
            String code = root.path("code").asText();
            if (!"200".equals(code)) {
                return "API错误: " + code + ", " + root.path("message").asText();
            }

            StringBuilder infoBuilder = new StringBuilder();

            // 根据时间范围提取不同数据
            if (root.has("daily")) {
                // 日线数据 (如3d)
                JsonNode dailyArray = root.get("daily");
                if (dailyArray.isArray() && dailyArray.size() > 0) {
                    JsonNode firstDay = dailyArray.get(0);

                    // 提取所需字段
                    String fxDate = firstDay.path("fxDate").asText();
                    double tempMax = firstDay.path("tempMax").asDouble();
                    double tempMin = firstDay.path("tempMin").asDouble();
                    int uvIndex = firstDay.path("uvIndex").asInt();
                    int humidity = firstDay.path("humidity").asInt();
                    double precip = firstDay.path("precip").asDouble();

                    // 构建格式化字符串
                    infoBuilder.append("日期: ").append(fxDate).append(" | ");
                    infoBuilder.append("气温: ").append(tempMin).append("°C~").append(tempMax).append("°C | ");
                    infoBuilder.append("紫外线: ").append(uvIndex).append(" | ");
                    infoBuilder.append("湿度: ").append(humidity).append("% | ");
                    infoBuilder.append("降水量: ").append(precip).append(" mm");
                } else {
                    return "未找到daily数据";
                }
            } else if (root.has("hourly")) {
                // 小时线数据 (如24h)
                JsonNode hourlyArray = root.get("hourly");
                if (hourlyArray.isArray() && hourlyArray.size() > 0) {
                    JsonNode firstHour = hourlyArray.get(0);

                    // 提取所需字段
                    String fxDate = firstHour.path("fxTime").asText();
                    double temp = firstHour.path("temp").asDouble();
                    int humidity = firstHour.path("humidity").asInt();
                    double precip = firstHour.path("precip").asDouble();

                    // 构建格式化字符串
                    infoBuilder.append("时间: ").append(fxDate).append(" | ");
                    infoBuilder.append("温度: ").append(temp).append("°C | ");
                    infoBuilder.append("湿度: ").append(humidity).append("% | ");
                    infoBuilder.append("降水量: ").append(precip).append(" mm");

                    // 尝试从daily获取更多信息
                    if (root.has("daily") && root.get("daily").isArray() && root.get("daily").size() > 0) {
                        JsonNode daily = root.get("daily").get(0);
                        double tempMax = daily.path("tempMax").asDouble();
                        double tempMin = daily.path("tempMin").asDouble();
                        int uvIndex = daily.path("uvIndex").asInt();

                        infoBuilder.append(" | 日气温: ").append(tempMin).append("°C~").append(tempMax).append("°C");
                        infoBuilder.append(" | 紫外线: ").append(uvIndex);
                    }
                } else {
                    return "未找到hourly数据";
                }
            } else {
                return "未找到天气数据";
            }

            return infoBuilder.toString();
        } catch (Exception e) {
            System.err.println("解析天气信息时出错: " + e.getMessage());
            return "解析错误: " + e.getMessage();
        }
    }

    /**
     * 根据时间范围确定API路径
     */
    private static String getApiPath(String timeRange) {
        if (timeRange.endsWith("d")) {
            return "v7/weather/" + timeRange;
        } else if (timeRange.endsWith("h")) {
            return "v7/weather/" + timeRange;
        } else {
            return "v7/weather/3d";
        }
    }

    /**
     * API响应包装类
     */
    static class ApiResponse {
        private String districtName;
        private String rawResponse;
        private String formattedWeatherInfo;

        public String getDistrictName() {
            return districtName;
        }

        public void setDistrictName(String districtName) {
            this.districtName = districtName;
        }

        public String getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(String rawResponse) {
            this.rawResponse = rawResponse;
        }

        public String getFormattedWeatherInfo() {
            return formattedWeatherInfo;
        }

        public void setFormattedWeatherInfo(String formattedWeatherInfo) {
            this.formattedWeatherInfo = formattedWeatherInfo;
        }
    }
}