package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ReportAnalysisPage {
    private WebDriver driver;

    private By reportFileInput = By.id("lab-file-input");
    private By runAnalysisBtn = By.cssSelector("#lab-run-analysis-btn, #run-lab-analysis-btn");
    private By reportResultContainer = By.cssSelector("#lab-result-card, #lab-report-result");
    private By saveReportBtn = By.cssSelector("#save-lab-report-btn, .save-report-btn");

    public ReportAnalysisPage(WebDriver driver) {
        this.driver = driver;
    }

    public void uploadReportFile(String filePath) {
        driver.findElement(reportFileInput).sendKeys(filePath);
    }

    public void clickRunAnalysis() {
        driver.findElement(runAnalysisBtn).click();
    }

    public boolean isReportResultDisplayed() {
        return driver.findElement(reportResultContainer).isDisplayed();
    }

    public void clickSaveReport() {
        driver.findElement(saveReportBtn).click();
    }
}
