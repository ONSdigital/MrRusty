package com.github.onsdigital.test.browser.PageObjects;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class CollectionsPage extends FlorencePage {

    By collectionNameInputLocator = By.id("collectionname");
    By teamSelectLocator = By.id("team");
    By scheduledPublishRadioLocator = By.id("scheduledpublish");
    By manualPublishRadioLocator = By.id("manualpublish");
    By daySelectLocator = By.id("day");
    By monthSelectLocator = By.id("month");
    By yearSelectLocator = By.id("year");
    By createCollectionButtonLocator = By.className("btn-collection-create");

    WebElement collectionNameInput;
    Select teamSelect;
    WebElement scheduledPublishRadio;
    WebElement manualPublishRadio;
    Select daySelect;
    Select monthSelect;
    Select yearSelect;
    WebElement createCollectionButton;

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
            teamSelect = new Select(find(teamSelectLocator));
            scheduledPublishRadio = find(scheduledPublishRadioLocator);
            manualPublishRadio = find(manualPublishRadioLocator);
            daySelect = new Select(find(daySelectLocator));
            monthSelect = new Select(find(monthSelectLocator));
            yearSelect = new Select(find(yearSelectLocator));
            createCollectionButton = find(createCollectionButtonLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    /**
     * Helper method to compose populating the form and submitting
     * @param name
     * @return
     */
    public BrowsePage createCollection(String name) {
        populateFormWithDefaults(name);
        return clickCreateCollection();
    }

    /**
     * Populate all fields to create a collection with defaults and a random name.
     *
     * @return
     */
    public CollectionsPage populateFormWithDefaults() {
        return populateFormWithDefaults(Random.id());
    }

    /**
     * Populate all fields to create a collection with defaults and the given name.
      * @param name
     * @return
     */
    public CollectionsPage populateFormWithDefaults(String name) {
        typeCollectionName(name);
        selectTeamByIndex(1);
        selectManualPublish();
        return this;
    }

    /**
     * Type the given collection name into the create new collection form.
     *
     * @param collectionName
     * @return
     */
    public CollectionsPage typeCollectionName(String collectionName) {
        collectionNameInput.clear();
        collectionNameInput.sendKeys(collectionName);
        return this;
    }

    /**
     * Select the team associated with the collection from the select box using the given index.
     *
     * @param index
     * @return
     */
    public CollectionsPage selectTeamByIndex(int index) {
        teamSelect.selectByIndex(index);
        return this;
    }

    /**
     * Select the team associated with the collection from the select box using the given text value.
     *
     * @param team
     * @return
     */
    public CollectionsPage selectTeamByVisibleText(String team) {
        teamSelect.selectByValue(team);
        return this;
    }

    /**
     * Select the team associated with the collection from the select box using the given team value.
     *
     * @param team
     * @return
     */
    public CollectionsPage selectTeamByValue(String team) {
        teamSelect.selectByValue(team);
        return this;
    }

    /**
     * Select the day of the collections release by index.
     *
     * @param index
     * @return
     */
    public CollectionsPage selectDayByIndex(int index) {
        daySelect.selectByIndex(index);
        return this;
    }

    /**
     * Select the month of the collections release by index.
     *
     * @param index
     * @return
     */
    public CollectionsPage selectMonthByIndex(int index) {
        monthSelect.selectByIndex(index);
        return this;
    }

    /**
     * Select the year of the collections release by value.
     *
     * @param value
     * @return
     */
    public CollectionsPage selectYear(int value) {
        yearSelect.selectByValue(Integer.toString(value));
        return this;
    }

    /**
     * Select a scheduled publish when creating a collection.
     *
     * @return
     */
    public CollectionsPage selectScheduledPublish() {
        scheduledPublishRadio.click();
        return this;
    }

    /**
     * Select a manual publish when creating a collection.
     *
     * @return
     */
    public CollectionsPage selectManualPublish() {
        manualPublishRadio.click();
        return this;
    }

    /**
     * Click the create collection button on the create collection form.
     * @return
     */
    public CollectionsPage clickCreateCollectionExpectingError() {
        createCollectionButton.click();
        return this;
    }

    public BrowsePage clickCreateCollection() {
        createCollectionButton.click();
        return new BrowsePage(driver);
    }

    public CollectionDetailsPage clickCollectionByName(String collectionName) {
        List<WebElement> collections = driver.findElements(By.cssSelector(".collections-select-table tr .collection-name"));

        for (WebElement collection : collections) {

            if (collection.getText().equals(collectionName)) {
                collection.click();
                return new CollectionDetailsPage(driver);
            }
        }

        throw new NotFoundException("Could not find collection with name: " + collectionName);
    }
}

