package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TableSectionPartial extends EditPage {

    private static final By editButtonLocator = By.className("btn-markdown-edit");
    By deleteButtonLocator = By.className("btn-page-delete");

    WebElement editButton;
    WebElement deleteButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected TableSectionPartial initialisePage(WebElement section) {
        try {
            super.initialisePage();
            scrollTo("#table-edit_0");
            editButton = section.findElement(By.id("table-edit_0")); // the class is also applied to the copy button, hence select the second.

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
    public TableSectionPartial(WebDriver driver, WebElement section) {
        super();
        initialisePage(section);
    }

    public TableBuilderPage clickEdit() {
        editButton.click();
        return new TableBuilderPage();
    }

    public EditPage clickDelete() {
        deleteButton.click();
        return new EditPage();
    }
}
