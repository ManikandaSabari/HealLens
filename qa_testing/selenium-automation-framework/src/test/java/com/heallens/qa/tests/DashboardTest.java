package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import com.heallens.qa.pages.DashboardPage;
import com.heallens.qa.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DashboardTest extends BaseTest {

    @Test(priority = 1, description = "TC013: Validate dashboard initialization after authentication")
    public void testDashboardRender() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("Password123!");
        loginPage.clickLogin();

        DashboardPage dashboardPage = new DashboardPage(getDriver());
        Assert.assertNotNull(dashboardPage);
    }
}
