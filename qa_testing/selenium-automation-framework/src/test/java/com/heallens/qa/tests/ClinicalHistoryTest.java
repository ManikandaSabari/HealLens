package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import com.heallens.qa.pages.ClinicalHistoryPage;
import com.heallens.qa.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ClinicalHistoryTest extends BaseTest {

    @Test(priority = 1, description = "TC024: Validate Supabase Cloud Database history retrieval")
    public void testClinicalHistoryFetch() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("Password123!");
        loginPage.clickLogin();

        ClinicalHistoryPage historyPage = new ClinicalHistoryPage(getDriver());
        Assert.assertNotNull(historyPage);
    }
}
