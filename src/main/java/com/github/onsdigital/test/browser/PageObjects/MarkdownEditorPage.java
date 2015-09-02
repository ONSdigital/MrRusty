package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MarkdownEditorPage extends PageObject {

    By chartButtonLocator = By.className("btn-markdown-editor-chart");
    By tableButtonLocator = By.className("btn-markdown-editor-table");
    By cancelButtonLocator = By.className("btn-markdown-editor-cancel");
    By saveButtonLocator = By.className("btn-markdown-editor-save");
    By exitButtonLocator = By.className("btn-markdown-editor-exit");
    By editorLocator = By.id("wmd-input");

    WebElement chartButton;
    WebElement tableButton;
    WebElement editor;
    WebElement cancelButton;
    WebElement saveButton;
    WebElement exitButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected void initialisePage() {
        try {
            cancelButton = waitAndFind(cancelButtonLocator);
            saveButton = find(saveButtonLocator);
            exitButton = find(exitButtonLocator);
            chartButton = find(chartButtonLocator);
            tableButton = find(tableButtonLocator);
            editor = waitAndFind(editorLocator);

        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }
    }

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public MarkdownEditorPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    public EditPage clickCancel() {
        cancelButton.click();
        return new EditPage(driver);
    }

    public MarkdownEditorPage clickSave() {
        saveButton.click();
        return this;
    }

    public TableBuilderPage clickCreateTable() {
        tableButton.click();
        return new TableBuilderPage(driver);
    }

    public EditPage clickSaveAndExit() {
        exitButton.click();
        return new EditPage(driver);
    }

    public MarkdownEditorPage typeContent(String content) {
        //editor.click();
        editor.sendKeys(content);
        return this;
    }
}
