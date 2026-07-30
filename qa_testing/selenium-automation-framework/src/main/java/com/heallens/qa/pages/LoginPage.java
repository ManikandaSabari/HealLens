package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {
    private WebDriver driver;

    // Locators
    private By emailInput = By.id("email");
    private By passwordInput = By.id("password");
    private By loginSubmitBtn = By.cssSelector("#email-form button[type='submit']");
    private By signupTab = By.xpath("//button[contains(text(),'Sign Up')]");
    private By forgotPasswordLink = By.cssSelector(".forgot-link");
    private By googleAuthBtn = By.cssSelector(".google-btn");
    private By loginErrorBanner = By.id("login-error-banner");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void enterEmail(String email) {
        driver.findElement(emailInput).clear();
        driver.findElement(emailInput).sendKeys(email);
    }

    public void enterPassword(String password) {
        driver.findElement(passwordInput).clear();
        driver.findElement(passwordInput).sendKeys(password);
    }

    public void clickLogin() {
        driver.findElement(loginSubmitBtn).click();
    }

    public void clickSignupTab() {
        driver.findElement(signupTab).click();
    }

    public void clickForgotPassword() {
        driver.findElement(forgotPasswordLink).click();
    }

    public void clickGoogleAuth() {
        driver.findElement(googleAuthBtn).click();
    }

    public String getErrorMessage() {
        WebElement banner = driver.findElement(loginErrorBanner);
        return banner.isDisplayed() ? banner.getText() : "";
    }
}
