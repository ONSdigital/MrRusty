package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SelectedPagePartial extends CollectionsPage {

    private static final By editButtonLocator = By.className("btn-page-edit");
    private static final By moveButtonLocator = By.className("btn-page-move");
    private static final By deleteButtonLocator = By.className("btn-page-delete");

    WebElement editButton;
    WebElement moveButton;
    WebElement deleteButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected SelectedPagePartial initialisePage(WebElement section) {
        try {
            super.initialisePage();
            editButton = find(editButtonLocator);
            moveButton = find(moveButtonLocator);
            deleteButton = find(deleteButtonLocator);

        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public SelectedPagePartial(WebDriver driver, WebElement section) {
        super(driver);
        initialisePage(section);
    }

    public EditPage clickEditFile() {
        editButton.click();
        return new EditPage(driver);
    }

    public EditPage clickDeleteFile() {
        deleteButton.click();
        return new EditPage(driver);
    }
}
