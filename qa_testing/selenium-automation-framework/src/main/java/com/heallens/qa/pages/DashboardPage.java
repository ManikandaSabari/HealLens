package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage {
    private WebDriver driver;

    private By userProfileHeader = By.cssSelector(".user-profile");
    private By userNameDisplay = By.id("user-name-display");
    private By logoutBtn = By.id("logout-btn");
    private By hamburgerMenuBtn = By.id("hamburger-btn");

    // Sidebar Links
    private By navScanner = By.xpath("//a[contains(@href,'#scanner') or contains(text(),'Scanner')]");
    private By navLabAnalyzer = By.xpath("//a[contains(text(),'Lab Analyzer')]");
    private By navHistory = By.xpath("//a[contains(text(),'History')]");
    private By navEmergencyContacts = By.xpath("//a[contains(text(),'Emergency Contacts')]");
    private By navProfile = By.xpath("//a[contains(text(),'Profile')]");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    public String getUserNameText() {
        return driver.findElement(userNameDisplay).getText();
    }

    public void clickLogout() {
        driver.findElement(logoutBtn).click();
    }

    public void clickHamburgerMenu() {
        driver.findElement(hamburgerMenuBtn).click();
    }

    public void navigateToScanner() {
        driver.findElement(navScanner).click();
    }

    public void navigateToLabAnalyzer() {
        driver.findElement(navLabAnalyzer).click();
    }

    public void navigateToHistory() {
        driver.findElement(navHistory).click();
    }

    public void navigateToEmergencyContacts() {
        driver.findElement(navEmergencyContacts).click();
    }

    public void navigateToProfile() {
        driver.findElement(navProfile).click();
    }
}
