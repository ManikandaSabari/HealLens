package com.heallens.qa.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream("src/main/java/com/heallens/qa/config/config.properties");
            properties.load(fis);
        } catch (IOException e) {
            // Default fallback settings
            properties.setProperty("baseUrl", "https://heallens.vercel.app/");
            properties.setProperty("browser", "chrome");
            properties.setProperty("implicitWait", "10");
            properties.setProperty("explicitWait", "15");
        }
    }

    public static String getProperty(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        String envVar = System.getenv(key);
        if (envVar != null && !envVar.trim().isEmpty()) {
            return envVar.trim();
        }
        String envUpper = System.getenv(key.toUpperCase());
        if (envUpper != null && !envUpper.trim().isEmpty()) {
            return envUpper.trim();
        }
        return properties.getProperty(key, "");
    }

    public static String getBaseUrl() {
        String url = getProperty("baseUrl");
        if (url == null || url.trim().isEmpty()) {
            url = getProperty("BASE_URL");
        }
        if (url == null || url.trim().isEmpty()) {
            url = "https://heallens.vercel.app/";
        }
        return url;
    }

    public static String getBrowser() {
        String browser = getProperty("browser");
        return (browser != null && !browser.trim().isEmpty()) ? browser : "chrome";
    }
}
