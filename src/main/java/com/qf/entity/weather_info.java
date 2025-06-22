package com.qf.entity;

public class weather_info {
    private String districtName;      // 区县名称
    private String fxDate;            // 日期
    private double tempMax;           // 最高气温(°C)
    private double tempMin;           // 最低气温(°C)
    private int humidity;             // 湿度(%)
    private double precip;            // 降水量(mm)
    private int uvIndex;              // 紫外线强度
    private String sunrise;           // 日出时间
    private String sunset;            // 日落时间
    private String moonPhase;         // 月相
    private String textDay;           // 白天天气描述
    private String textNight;         // 夜间天气描述
    private String windDirDay;        // 白天风向
    private String windScaleDay;      // 白天风力
    private String windDirNight;      // 夜间风向
    private String windScaleNight;    // 夜间风力
    private boolean success;          // 请求是否成功
    private String errorMessage;      // 错误信息

    public void WeatherInfo(String districtName) {
        this.districtName = districtName;
        this.success = false;
    }

    // Getter和Setter方法
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public String getFxDate() { return fxDate; }
    public void setFxDate(String fxDate) { this.fxDate = fxDate; }
    public double getTempMax() { return tempMax; }
    public void setTempMax(double tempMax) { this.tempMax = tempMax; }
    public double getTempMin() { return tempMin; }
    public void setTempMin(double tempMin) { this.tempMin = tempMin; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public double getPrecip() { return precip; }
    public void setPrecip(double precip) { this.precip = precip; }
    public int getUvIndex() { return uvIndex; }
    public void setUvIndex(int uvIndex) { this.uvIndex = uvIndex; }
    public String getSunrise() { return sunrise; }
    public void setSunrise(String sunrise) { this.sunrise = sunrise; }
    public String getSunset() { return sunset; }
    public void setSunset(String sunset) { this.sunset = sunset; }
    public String getMoonPhase() { return moonPhase; }
    public void setMoonPhase(String moonPhase) { this.moonPhase = moonPhase; }
    public String getTextDay() { return textDay; }
    public void setTextDay(String textDay) { this.textDay = textDay; }
    public String getTextNight() { return textNight; }
    public void setTextNight(String textNight) { this.textNight = textNight; }
    public String getWindDirDay() { return windDirDay; }
    public void setWindDirDay(String windDirDay) { this.windDirDay = windDirDay; }
    public String getWindScaleDay() { return windScaleDay; }
    public void setWindScaleDay(String windScaleDay) { this.windScaleDay = windScaleDay; }
    public String getWindDirNight() { return windDirNight; }
    public void setWindDirNight(String windDirNight) { this.windDirNight = windDirNight; }
    public String getWindScaleNight() { return windScaleNight; }
    public void setWindScaleNight(String windScaleNight) { this.windScaleNight = windScaleNight; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        if (!success) {
            return districtName + ": " + errorMessage;
        }
        return String.format(
                "%s - %s\n" +
                        "气温: %.1f°C~%.1f°C | 湿度: %d%% | 降水量: %.1f mm | 紫外线: %d\n" +
                        "天气: 白天%s | 夜间%s\n" +
                        "风力: 白天%s(%s) | 夜间%s(%s)\n" +
                        "日出/日落: %s/%s | 月相: %s",
                districtName, fxDate, tempMax, tempMin,
                humidity, precip, uvIndex,
                textDay, textNight,
                windDirDay, windScaleDay, windDirNight, windScaleNight,
                sunrise, sunset, moonPhase
        );
    }
}
