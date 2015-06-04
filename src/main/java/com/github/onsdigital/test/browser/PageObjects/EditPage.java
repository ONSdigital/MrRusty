package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class EditPage extends WorkspacePage {

    By contentAccordionLocator = By.cssSelector("#content .edit-section__head");
    By saveButtonLocator = By.cssSelector(".btn-edit-save");
    By submitForReviewButtonLocator = By.cssSelector(".btn-edit-save-and-submit-for-review");
    By submitForApprovalButtonLocator = By.cssSelector(".btn-edit-save-and-submit-for-approval");

    WebElement contentAccordion;
    WebElement saveButton;
    WebElement submitForReviewButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected WorkspacePage initialisePage() {
        try {
            super.initialisePage();
            contentAccordion = waitAndFind(contentAccordionLocator);
            saveButton = find(saveButtonLocator);
            submitForReviewButton = find(submitForReviewButtonLocator);

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
    public EditPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    public EditPage openContentAccordion() {
        contentAccordion.click();
        scrollTo("#content");
        return this;
    }

    public PageSectionPartial addContentSection(String name) {

        WebElement contentAccordion = waitAndFind(By.cssSelector("#content")); // find the content accordion
        contentAccordion.findElement(By.cssSelector(".edit-section__head")).click(); // click the header to open it
        contentAccordion.findElement(By.cssSelector("#addSection")).click(); // add a new section

        contentAccordion = waitAndFind(By.cssSelector("#content")); // find the content accordion
        contentAccordion.findElement(By.cssSelector(".edit-section__head")).click(); // click the header to open it

        // get a list of all sections then pick the section that we want.
        List<WebElement> sections = contentAccordion.findElements(By.cssSelector("#sortable-sections"));
        WebElement newSection = sections.get(sections.size() - 1);

        List<WebElement> textAreas = newSection.findElements(By.cssSelector("textarea"));
        WebElement titleEntry = textAreas.get(0);
        titleEntry.sendKeys(name);

        return new PageSectionPartial(this.driver, newSection);
    }


    /**
     * Sets the title of the first section.
     *
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

    public CollectionsPage clickSubmitForReview() {
        submitForReviewButton.click();
        return new CollectionsPage(this.driver);
    }

    public CollectionsPage clickSubmitForApproval() {
        WebElement submitForApprovalButton = waitAndFind(submitForApprovalButtonLocator);
        submitForApprovalButton.click();
        return new CollectionsPage(this.driver);
    }
}
