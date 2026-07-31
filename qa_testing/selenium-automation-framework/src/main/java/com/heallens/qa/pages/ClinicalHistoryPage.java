package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ClinicalHistoryPage {
    private WebDriver driver;

    private By historyListContainer = By.cssSelector("#history-list, #history-list-container");
    private By historyCardItem = By.cssSelector(".history-card, .history-item");
    private By filterAllBtn = By.cssSelector("#history-tab-image, #filter-all");
    private By filterImageScansBtn = By.cssSelector("#history-tab-image, #filter-images");
    private By clearAllBtn = By.cssSelector("#btn-clear-history, #clear-all-history-btn");

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
