package com.example.weixin.model;

public class WeatherData {
    private String fxDate;
    private String tempMax;
    private String tempMin;
    private String textDay;
    private String textNight;
    private String iconDay;
    private String iconNight;
    private String humidity;
    private String precip;
    private String pressure;
    private String windScaleDay;
    private String windDirDay;
    private String vis;
    private String uvIndex;
    private String cloud;
    private String sunrise;
    private String sunset;

    // 构造函数
    public WeatherData() {}

    public WeatherData(String fxDate, String tempMax, String tempMin, String textDay, 
                      String textNight, String iconDay, String iconNight, String humidity,
                      String precip, String pressure, String windScaleDay, String windDirDay,
                      String vis, String uvIndex, String cloud, String sunrise, String sunset) {
        this.fxDate = fxDate;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.textDay = textDay;
        this.textNight = textNight;
        this.iconDay = iconDay;
        this.iconNight = iconNight;
        this.humidity = humidity;
        this.precip = precip;
        this.pressure = pressure;
        this.windScaleDay = windScaleDay;
        this.windDirDay = windDirDay;
        this.vis = vis;
        this.uvIndex = uvIndex;
        this.cloud = cloud;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    // Getter和Setter方法
    public String getFxDate() { return fxDate; }
    public void setFxDate(String fxDate) { this.fxDate = fxDate; }

    public String getTempMax() { return tempMax; }
    public void setTempMax(String tempMax) { this.tempMax = tempMax; }

    public String getTempMin() { return tempMin; }
    public void setTempMin(String tempMin) { this.tempMin = tempMin; }

    public String getTextDay() { return textDay; }
    public void setTextDay(String textDay) { this.textDay = textDay; }

    public String getTextNight() { return textNight; }
    public void setTextNight(String textNight) { this.textNight = textNight; }

    public String getIconDay() { return iconDay; }
    public void setIconDay(String iconDay) { this.iconDay = iconDay; }

    public String getIconNight() { return iconNight; }
    public void setIconNight(String iconNight) { this.iconNight = iconNight; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }

    public String getPrecip() { return precip; }
    public void setPrecip(String precip) { this.precip = precip; }

    public String getPressure() { return pressure; }
    public void setPressure(String pressure) { this.pressure = pressure; }

    public String getWindScaleDay() { return windScaleDay; }
    public void setWindScaleDay(String windScaleDay) { this.windScaleDay = windScaleDay; }

    public String getWindDirDay() { return windDirDay; }
    public void setWindDirDay(String windDirDay) { this.windDirDay = windDirDay; }

    public String getVis() { return vis; }
    public void setVis(String vis) { this.vis = vis; }

    public String getUvIndex() { return uvIndex; }
    public void setUvIndex(String uvIndex) { this.uvIndex = uvIndex; }

    public String getCloud() { return cloud; }
    public void setCloud(String cloud) { this.cloud = cloud; }

    public String getSunrise() { return sunrise; }
    public void setSunrise(String sunrise) { this.sunrise = sunrise; }

    public String getSunset() { return sunset; }
    public void setSunset(String sunset) { this.sunset = sunset; }

    // 获取数值型数据的方法（用于图表绘制）
    public float getTempMaxFloat() {
        try {
            return Float.parseFloat(tempMax);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getTempMinFloat() {
        try {
            return Float.parseFloat(tempMin);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getHumidityFloat() {
        try {
            return Float.parseFloat(humidity);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getPrecipFloat() {
        try {
            return Float.parseFloat(precip);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getPressureFloat() {
        try {
            return Float.parseFloat(pressure);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getUvIndexFloat() {
        try {
            return Float.parseFloat(uvIndex);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }
}
