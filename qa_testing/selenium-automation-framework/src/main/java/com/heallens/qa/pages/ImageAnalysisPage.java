package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class ImageAnalysisPage {
    private WebDriver driver;

    private By bodyPartSelect = By.id("body-part-select");
    private By fileInput = By.id("file-input");
    private By analyzeBtn = By.id("analyze-btn");
    private By resultCard = By.cssSelector("#result-card, #analysis-result-card");
    private By diseaseName = By.cssSelector("#res-disease, #result-disease-name");
    private By confidenceValue = By.cssSelector("#res-confidence, #result-confidence-value");
    private By severityBadge = By.cssSelector("#res-severity-badge, #result-severity-badge");

    public ImageAnalysisPage(WebDriver driver) {
        this.driver = driver;
    }

    public void selectBodyPart(String part) {
        Select select = new Select(driver.findElement(bodyPartSelect));
        select.selectByVisibleText(part);
    }

    public void uploadImageFile(String absoluteFilePath) {
        driver.findElement(fileInput).sendKeys(absoluteFilePath);
    }

    public void clickAnalyze() {
        driver.findElement(analyzeBtn).click();
    }

    public boolean isResultCardDisplayed() {
        return driver.findElement(resultCard).isDisplayed();
    }

    public String getDiseaseName() {
        return driver.findElement(diseaseName).getText();
    }

    public String getConfidence() {
        return driver.findElement(confidenceValue).getText();
    }

    public String getSeverity() {
        return driver.findElement(severityBadge).getText();
    }
}
