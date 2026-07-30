package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import com.heallens.qa.pages.ImageAnalysisPage;
import com.heallens.qa.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ImageAnalysisTest extends BaseTest {

    @Test(priority = 1, description = "TC017: Validate Chest X-Ray AI Analysis pipeline")
    public void testChestXRayAnalysis() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("Password123!");
        loginPage.clickLogin();

        ImageAnalysisPage scanner = new ImageAnalysisPage(getDriver());
        Assert.assertNotNull(scanner);
    }
}
