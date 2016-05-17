package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PageSectionPartial extends EditPage {

    private static final By editButtonLocator = By.className("btn-markdown-edit");
    By deleteButtonLocator = By.className("btn-page-delete");

    WebElement editButton;
    WebElement deleteButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected PageSectionPartial initialisePage(WebElement section) {
        try {
            super.initialisePage();
            editButton = section.findElement(editButtonLocator);
            deleteButton = section.findElement(deleteButtonLocator);

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
    public PageSectionPartial(WebDriver driver, WebElement section) {
        super();
        initialisePage(section);
    }

    public MarkdownEditorPage clickEditPage() {
        editButton.click();
        return new MarkdownEditorPage();
    }

    public EditPage clickDeletePage() {
        deleteButton.click();
        return new EditPage();
    }
}
