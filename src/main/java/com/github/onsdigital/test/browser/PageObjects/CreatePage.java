package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class CreatePage extends WorkspacePage {

    By locationInputLocator = By.id("location");
    By pageTypeSelectLocator = By.id("pagetype");
    By pageNameInputLocator = By.id("pagename");
    By createButtonLocator = By.id(".btn-edit-save");

    WebElement locationInput;
    Select pageTypeSelect;
    WebElement pageNameInput;
    WebElement createButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected CreatePage initialisePage() {
        try {
            super.initialisePage();
            locationInput = find(locationInputLocator);
            pageTypeSelect = new Select(find(pageTypeSelectLocator));
            pageNameInput = find(pageNameInputLocator);
            createButton = find(createButtonLocator);

        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the create page contents.", exception);
        }

        return this;
    }

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public CreatePage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    /**
     * Select the page type on the create page.
     * @param type
     * @return
     */
    public CreatePage selectPageType(PageTypes type) {
        pageTypeSelect.selectByValue(type.toString());
        return this;
    }

    /**
     * Type the location where this page will be created.
     * @param location
     * @return
     */
    public CreatePage typeLocation(String location) {
        locationInput.clear();
        locationInput.sendKeys(location);
        return this;
    }

    /**
     * Type the name for the page.
     * @param name
     * @return
     */
    public CreatePage typePageName(String name) {
        pageNameInput.clear();
        pageNameInput.sendKeys(name);
        return this;
    }

    public EditPage ClickCreate() {
        createButton.click();
        return new EditPage(driver);
    }
}
