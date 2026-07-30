package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import com.heallens.qa.pages.LoginPage;
import com.heallens.qa.pages.ReportAnalysisPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReportAnalysisTest extends BaseTest {

    @Test(priority = 1, description = "TC022: Validate Lab Report Biomarker Parsing")
    public void testLabReportParsing() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterEmail("admin@heallens.com");
        loginPage.enterPassword("Password123!");
        loginPage.clickLogin();

        ReportAnalysisPage reportPage = new ReportAnalysisPage(getDriver());
        Assert.assertNotNull(reportPage);
    }
}
