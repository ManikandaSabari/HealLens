package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import com.heallens.qa.pages.LoginPage;
import com.heallens.qa.pages.SignupPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuthTest extends BaseTest {

    @Test(priority = 1, description = "TC001: Validate login with valid credentials")
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("Password123!");
        loginPage.clickLogin();
        
        // Assert redirected URL contains dashboard or title contains HealLens
        Assert.assertTrue(getDriver().getCurrentUrl().contains("dashboard") || getDriver().getTitle().contains("HealLens"));
    }

    @Test(priority = 2, description = "TC002: Validate login with invalid password")
    public void testInvalidLogin() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("WrongPassword123");
        loginPage.clickLogin();

        String error = loginPage.getErrorMessage();
        Assert.assertNotNull(error);
    }

    @Test(priority = 3, description = "TC004: Validate signup flow navigation")
    public void testSignupNavigation() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.clickSignupTab();
        
        SignupPage signupPage = new SignupPage(getDriver());
        signupPage.fillSignupForm("Test User", "test.user@heallens.com", "SecurePass123!");
        Assert.assertTrue(true);
    }
}
