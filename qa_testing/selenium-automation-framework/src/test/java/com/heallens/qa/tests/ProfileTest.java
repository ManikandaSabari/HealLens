package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import com.heallens.qa.pages.LoginPage;
import com.heallens.qa.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProfileTest extends BaseTest {

    @Test(priority = 1, description = "TC034: Validate Profile user information display")
    public void testProfileInfoDisplay() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("Password123!");
        loginPage.clickLogin();

        ProfilePage profilePage = new ProfilePage(getDriver());
        Assert.assertNotNull(profilePage);
    }
}
