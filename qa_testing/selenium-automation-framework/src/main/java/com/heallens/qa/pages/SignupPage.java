package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SignupPage {
    private WebDriver driver;

    private By fullNameInput = By.id("signup-name");
    private By emailInput = By.id("signup-email");
    private By passwordInput = By.id("signup-password");
    private By submitBtn = By.cssSelector("#signup-form button[type='submit']");
    private By signinTab = By.xpath("//button[contains(text(),'Sign In')]");

    public SignupPage(WebDriver driver) {
        this.driver = driver;
    }

    public void fillSignupForm(String fullName, String email, String password) {
        driver.findElement(fullNameInput).sendKeys(fullName);
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
    }

    public void clickSubmit() {
        driver.findElement(submitBtn).click();
    }

    public void clickSigninTab() {
        driver.findElement(signinTab).click();
    }
}
