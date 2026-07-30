package com.heallens.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class EmergencySOSPage {
    private WebDriver driver;

    private By contactNameInput = By.id("sos-contact-name");
    private By contactPhoneInput = By.id("sos-contact-phone");
    private By contactRelationInput = By.id("sos-contact-relation");
    private By addContactBtn = By.id("add-sos-contact-btn");
    private By contactCardItem = By.cssSelector(".sos-contact-card");

    public EmergencySOSPage(WebDriver driver) {
        this.driver = driver;
    }

    public void fillContactForm(String name, String phone, String relation) {
        driver.findElement(contactNameInput).clear();
        driver.findElement(contactNameInput).sendKeys(name);
        driver.findElement(contactPhoneInput).clear();
        driver.findElement(contactPhoneInput).sendKeys(phone);
        driver.findElement(contactRelationInput).clear();
        driver.findElement(contactRelationInput).sendKeys(relation);
    }

    public void clickAddContact() {
        driver.findElement(addContactBtn).click();
    }

    public int getContactCount() {
        List<WebElement> cards = driver.findElements(contactCardItem);
        return cards.size();
    }
}
