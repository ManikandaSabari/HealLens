package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ClinicalHistoryPage {
    private WebDriver driver;

    private By historyListContainer = By.id("history-list-container");
    private By historyCardItem = By.cssSelector(".history-card");
    private By filterAllBtn = By.id("filter-all");
    private By filterImageScansBtn = By.id("filter-images");
    private By clearAllBtn = By.id("clear-all-history-btn");

    public ClinicalHistoryPage(WebDriver driver) {
        this.driver = driver;
    }

    public int getHistoryRecordCount() {
        List<WebElement> items = driver.findElements(historyCardItem);
        return items.size();
    }

    public void filterByImageScans() {
        driver.findElement(filterImageScansBtn).click();
    }

    public void clickClearAllHistory() {
        driver.findElement(clearAllBtn).click();
    }
}
