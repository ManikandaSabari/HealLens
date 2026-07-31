package com.heallens.qa.pages;

import com.heallens.qa.config.ConfigReader;
import com.heallens.qa.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;

public class LoginPage {
    private WebDriver driver;

    // Locators
    private By emailInput = By.id("email");
    private By passwordInput = By.id("password");
    private By loginSubmitBtn = By.cssSelector("#email-form button[type='submit']");
    private By signupTab = By.xpath("//*[contains(text(),'Sign Up') or contains(text(),'Create Account')]");
    private By forgotPasswordLink = By.cssSelector(".forgot-link");
    private By googleAuthBtn = By.cssSelector(".google-btn");
    private By loginErrorBanner = By.id("login-error-banner");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        ensureOnLoginPage();
    }

    private void ensureOnLoginPage() {
        if (driver != null) {
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl == null || !currentUrl.contains("login.html")) {
                String baseUrl = ConfigReader.getBaseUrl();
                String loginUrl = baseUrl;
                if (!baseUrl.contains("login.html")) {
                    loginUrl = baseUrl.endsWith("/") ? baseUrl + "login.html" : baseUrl + "/login.html";
                }
                driver.get(loginUrl);
            }
        }
    }

    public void enterEmail(String email) {
        ensureOnLoginPage();
        WebElement element = WaitUtils.waitForElementVisible(driver, emailInput, 10);
        element.clear();
        element.sendKeys(email);
    }

    public void enterPassword(String password) {
        ensureOnLoginPage();
        WebElement element = WaitUtils.waitForElementVisible(driver, passwordInput, 10);
        element.clear();
        element.sendKeys(password);
    }

    public void clickLogin() {
        ensureOnLoginPage();
        WebElement element = WaitUtils.waitForElementClickable(driver, loginSubmitBtn, 10);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public void clickSignupTab() {
    ensureOnLoginPage();
    WebElement element = WaitUtils.waitForElementClickable(driver, signupTab, 10);

    ((JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView(true);", element);

    ((JavascriptExecutor) driver)
            .executeScript("arguments[0].click();", element);
}

    public void clickForgotPassword() {
        ensureOnLoginPage();
        WebElement element = WaitUtils.waitForElementClickable(driver, forgotPasswordLink, 10);
        element.click();
    }

    public void clickGoogleAuth() {
        ensureOnLoginPage();
        WebElement element = WaitUtils.waitForElementClickable(driver, googleAuthBtn, 10);
        element.click();
    }

    public String getErrorMessage() {
        ensureOnLoginPage();
        try {
            WebElement banner = WaitUtils.waitForElementVisible(driver, loginErrorBanner, 5);
            return banner.isDisplayed() ? banner.getText() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
