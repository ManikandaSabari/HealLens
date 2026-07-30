package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProfilePage {
    private WebDriver driver;

    private By profileName = By.id("profile-user-name");
    private By profileEmail = By.id("profile-user-email");
    private By registrationDate = By.id("profile-created-at");

    public ProfilePage(WebDriver driver) {
        this.driver = driver;
    }

    public String getProfileName() {
        return driver.findElement(profileName).getText();
    }

    public String getProfileEmail() {
        return driver.findElement(profileEmail).getText();
    }
}
