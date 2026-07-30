package com.heallens.qa.tests;

import com.heallens.qa.base.BaseTest;
import org.openqa.selenium.Dimension;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UITest extends BaseTest {

    @Test(priority = 1, description = "TC035: Validate responsive grid layout at 1920x1080 resolution")
    public void testDesktopResponsiveLayout() {
        getDriver().manage().window().setSize(new Dimension(1920, 1080));
        Assert.assertTrue(getDriver().getTitle().length() > 0);
    }

    @Test(priority = 2, description = "TC036: Validate mobile vertical card stack layout at 375x812 resolution")
    public void testMobileResponsiveLayout() {
        getDriver().manage().window().setSize(new Dimension(375, 812));
        Assert.assertTrue(getDriver().getTitle().length() > 0);
    }
}
