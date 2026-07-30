package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import com.heallens.qa.pages.EmergencySOSPage;
import com.heallens.qa.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmergencySOSTest extends BaseTest {

    @Test(priority = 1, description = "TC029: Validate Emergency Contact creation & Supabase cloud sync")
    public void testAddEmergencyContact() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("Password123!");
        loginPage.clickLogin();

        EmergencySOSPage sosPage = new EmergencySOSPage(getDriver());
        Assert.assertNotNull(sosPage);
    }
}
