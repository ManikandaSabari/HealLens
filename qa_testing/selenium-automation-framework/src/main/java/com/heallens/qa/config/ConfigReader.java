package com.heallens.qa.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try {
            properties = new Properties();
            FileInputStream fis = new FileInputStream("src/main/java/com/heallens/qa/config/config.properties");
            properties.load(fis);
        } catch (IOException e) {
            // Default fallback settings
            properties = new Properties();
            properties.setProperty("baseUrl", "http://localhost:5500/login.html");
            properties.setProperty("browser", "chrome");
            properties.setProperty("implicitWait", "10");
            properties.setProperty("explicitWait", "15");
        }
    }

    public static String getProperty(String key) {
        return System.getProperty(key, properties.getProperty(key));
    }

    public static String getBaseUrl() {
        return getProperty("baseUrl");
    }

    public static String getBrowser() {
        return getProperty("browser");
    }
}
