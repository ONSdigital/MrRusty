package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;

public class EditPage extends WorkspacePage {

    By contentAccordionLocator = By.cssSelector("#content .edit-section__head");
    By saveButtonLocator = By.cssSelector(".btn-edit-save");

    WebElement contentAccordion;
    WebElement saveButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected WorkspacePage initialisePage() {
        try {
            super.initialisePage();
            contentAccordion = waitAndFind(contentAccordionLocator);
            saveButton = find(saveButtonLocator);

        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the edit page contents.", exception);
        }

        return this;
    }


    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public EditPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    public EditPage openContentAccordion() {
        contentAccordion.click();
        scrollTo("#content");
        return this;
    }

    public WebElement addSection(String name) {
        waitAndFind(By.cssSelector("#addSection")).click();
        WebElement newSection = waitAndFind(By.cssSelector(".edit-section__content div:last"));
        WebElement titleEntry = newSection.findElement(By.cssSelector("textarea:first"));
        titleEntry.sendKeys(name);
        return newSection;
    }

    /**
     * Sets the title of the first section.
     * @param title
     * @return
     */
    public EditPage setSectionTitle(String title) {
        //WebElement newSection = waitAndFind(By.cssSelector(".edit-section__content div"));
        WebElement titleEntry = waitAndFind(By.cssSelector("#section-title_0"));
        titleEntry.clear();
        titleEntry.sendKeys(title);
        return this;
    }

    public EditPage clickSave() {
        saveButton.click();
        return this;
    }
}
