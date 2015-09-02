package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TableBuilderPage extends PageObject {

    //<input type="text" id="table-title" placeholder="[Title]" value="">
    // <input type="file" name="files" id="files">
    // <input type="submit" value="Submit">
    //<button class="btn-table-builder-create">Save Table</button>
    //<button class="btn-table-builder-cancel">Cancel</button>

    By tableNameEntryLocator = By.id("table-title");
    By fileEntryLocator = By.id("files");
    By submitButtonLocator = By.id("files");
    By createButtonLocator = By.className("btn-table-builder-create");
    By cancelButtonLocator = By.className("btn-table-builder-cancel");

    WebElement tableNameEntry;
    WebElement fileEntry;
    WebElement submitButton;
    WebElement createButton;
    WebElement cancelButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected void initialisePage() {
        try {
            cancelButton = waitAndFind(cancelButtonLocator);
            createButton = find(createButtonLocator);
            submitButton = find(submitButtonLocator);
            fileEntry = find(fileEntryLocator);
            tableNameEntry = find(tableNameEntryLocator);

        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }
    }

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public TableBuilderPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    public MarkdownEditorPage clickCancel() {
        cancelButton.click();
        return new MarkdownEditorPage(driver);
    }

    public MarkdownEditorPage clickCreate() {
        createButton.click();
        return new MarkdownEditorPage(driver);
    }

    public TableBuilderPage typeTableName(String tableName) {
        tableNameEntry.sendKeys(tableName);
        return this;
    }

    public TableBuilderPage set(String tableName) {
        tableNameEntry.sendKeys(tableName);
        return this;
    }
}
