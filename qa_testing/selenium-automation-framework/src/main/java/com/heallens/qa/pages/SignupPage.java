package com.heallens.qa.pages;

import com.heallens.qa.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SignupPage {
    private WebDriver driver;

    private By fullNameInput = By.id("signup-fullname");
    private By emailInput = By.id("signup-email");
    private By passwordInput = By.id("signup-password");
    private By submitBtn = By.cssSelector("#signup-form button[type='submit']");
    private By signinTab = By.cssSelector("span[onclick*='signin']");

    public SignupPage(WebDriver driver) {
        this.driver = driver;
    }

    public void fillSignupForm(String fullName, String email, String password) {
        WebElement nameElem = WaitUtils.waitForElementVisible(driver, fullNameInput, 10);
        nameElem.clear();
        nameElem.sendKeys(fullName);

        WebElement emailElem = WaitUtils.waitForElementVisible(driver, emailInput, 10);
        emailElem.clear();
        emailElem.sendKeys(email);

        WebElement passElem = WaitUtils.waitForElementVisible(driver, passwordInput, 10);
        passElem.clear();
        passElem.sendKeys(password);
    }

    public void clickSubmit() {
        WebElement element = WaitUtils.waitForElementClickable(driver, submitBtn, 10);
        element.click();
    }

    public void clickSigninTab() {
        WebElement element = WaitUtils.waitForElementClickable(driver, signinTab, 10);
        element.click();
    }
}
