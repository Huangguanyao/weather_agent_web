package com.qf.test_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class get_city {
    // 存储结构：市级名称 → 下属区县ID列表
    private static Map<String, List<String>> cityDistrictsMap = new HashMap<>();
    // ID → 区县名称的映射
    private static Map<String, String> idToDistrictMap = new HashMap<>();
    private static Map<String, String> cityIdMap = new HashMap<>(); // 城市名称→自身ID

    // 直辖市列表
    private static final Set<String> MUNICIPALITIES = Set.of("北京", "上海", "天津", "重庆");

    static {
        loadCityData();
    }

    private static void loadCityData() {
        try (InputStream is = get_city.class.getClassLoader().getResourceAsStream("city_data.csv")) {
            if (is == null) {
                throw new IOException("资源文件 not found: city_data.csv");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                br.readLine(); // 跳过标题行

                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 10) { // 确保有足够的列
                        String locationId = parts[0];    // 第1列：Location_ID
                        String provinceName = parts[1];  // 第2列：省份名称
                        String districtName = parts[2];  // 第3列：区县名称
                        String cityNameLevel8 = parts[7]; // 第8列：市级名称（直辖市）
                        String cityNameLevel10 = parts[9]; // 第10列：市级名称（非直辖市）

                        String shortCityName;

                        // 判断是否为直辖市
                        if (MUNICIPALITIES.contains(provinceName)) {
                            // 直辖市：使用第8列作为市级名称
                            shortCityName = cityNameLevel8.replace("市", "").trim();
                        } else {
                            // 非直辖市：使用第10列作为市级名称
                            shortCityName = cityNameLevel10.replace("市", "").trim();
                        }

                        // 构建各种映射
                        cityDistrictsMap.computeIfAbsent(shortCityName, k -> new ArrayList<>())
                                .add(locationId);
                        cityIdMap.put(shortCityName, locationId);
                        idToDistrictMap.put(locationId, districtName); // ID到区县名称的映射
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("加载城市数据失败: " + e.getMessage());
        }
    }

    public static List<String> main(String prompt) {
        String apiKey = "key";
        DeepSeekcity detector = new DeepSeekcity(apiKey);

        // 示例问题：提取"郑州"并获取其下属区县ID
        String question = prompt;
        String targetCity = detector.extractCity(question);
        System.out.println("提取的城市: " + targetCity);

        // 获取下属区县ID列表
        List<String> districtIds = getDistrictIdsByCity(targetCity);
        System.out.println(targetCity + " 下属区县ID: " + districtIds);

        return districtIds;
    }

    // 根据城市名称获取下属区县ID列表
    private static List<String> getDistrictIdsByCity(String cityName) {
        return cityDistrictsMap.getOrDefault(cityName, Collections.emptyList());
    }

    // 根据ID获取区县名称
    public static String getDistrictNameById(String locationId) {
        return idToDistrictMap.getOrDefault(locationId, "未知区县");
    }


}