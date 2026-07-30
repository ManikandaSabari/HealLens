package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ForgotPasswordPage {
    private WebDriver driver;

    private By resetPasswordModal = By.id("reset-password-modal");
    private By newPasswordInput = By.id("reset-new-password");
    private By confirmPasswordInput = By.id("reset-confirm-password");
    private By updatePasswordSubmitBtn = By.cssSelector("#reset-password-form button[type='submit']");

    public ForgotPasswordPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isResetModalDisplayed() {
        return driver.findElement(resetPasswordModal).isDisplayed();
    }

    public void enterNewPassword(String newPass, String confirmPass) {
        driver.findElement(newPasswordInput).sendKeys(newPass);
        driver.findElement(confirmPasswordInput).sendKeys(confirmPass);
    }

    public void submitPasswordUpdate() {
        driver.findElement(updatePasswordSubmitBtn).click();
    }
}
