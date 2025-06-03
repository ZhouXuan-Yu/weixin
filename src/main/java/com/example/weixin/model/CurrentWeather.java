package com.example.weixin.model;

public class CurrentWeather {
    private String obsTime;
    private String temp;
    private String feelsLike;
    private String icon;
    private String text;
    private String wind360;
    private String windDir;
    private String windScale;
    private String windSpeed;
    private String humidity;
    private String precip;
    private String pressure;
    private String vis;
    private String cloud;
    private String dew;

    // 构造函数
    public CurrentWeather() {}

    public CurrentWeather(String obsTime, String temp, String feelsLike, String icon, 
                         String text, String wind360, String windDir, String windScale,
                         String windSpeed, String humidity, String precip, String pressure,
                         String vis, String cloud, String dew) {
        this.obsTime = obsTime;
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.icon = icon;
        this.text = text;
        this.wind360 = wind360;
        this.windDir = windDir;
        this.windScale = windScale;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.precip = precip;
        this.pressure = pressure;
        this.vis = vis;
        this.cloud = cloud;
        this.dew = dew;
    }

    // Getter和Setter方法
    public String getObsTime() { return obsTime; }
    public void setObsTime(String obsTime) { this.obsTime = obsTime; }

    public String getTemp() { return temp; }
    public void setTemp(String temp) { this.temp = temp; }

    public String getFeelsLike() { return feelsLike; }
    public void setFeelsLike(String feelsLike) { this.feelsLike = feelsLike; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getWind360() { return wind360; }
    public void setWind360(String wind360) { this.wind360 = wind360; }

    public String getWindDir() { return windDir; }
    public void setWindDir(String windDir) { this.windDir = windDir; }

    public String getWindScale() { return windScale; }
    public void setWindScale(String windScale) { this.windScale = windScale; }

    public String getWindSpeed() { return windSpeed; }
    public void setWindSpeed(String windSpeed) { this.windSpeed = windSpeed; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }

    public String getPrecip() { return precip; }
    public void setPrecip(String precip) { this.precip = precip; }

    public String getPressure() { return pressure; }
    public void setPressure(String pressure) { this.pressure = pressure; }

    public String getVis() { return vis; }
    public void setVis(String vis) { this.vis = vis; }

    public String getCloud() { return cloud; }
    public void setCloud(String cloud) { this.cloud = cloud; }

    public String getDew() { return dew; }
    public void setDew(String dew) { this.dew = dew; }

    // 获取数值型数据的方法
    public float getTempFloat() {
        try {
            return Float.parseFloat(temp);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getFeelsLikeFloat() {
        try {
            return Float.parseFloat(feelsLike);
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

    public float getPressureFloat() {
        try {
            return Float.parseFloat(pressure);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getVisFloat() {
        try {
            return Float.parseFloat(vis);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public float getWindSpeedFloat() {
        try {
            return Float.parseFloat(windSpeed);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }
}
