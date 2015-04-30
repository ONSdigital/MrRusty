package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CollectionsPage extends FlorencePage {

    By collectionNameInputLocator = By.id("collectionname");

    WebElement collectionNameInput;

    public CollectionsPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected CollectionsPage initialisePage() {
        try {
            super.initialisePage();
            collectionNameInput = waitAndFind(collectionNameInputLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the collections page.", exception);
        }

        return this;
    }
}
