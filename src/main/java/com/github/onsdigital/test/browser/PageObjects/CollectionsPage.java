package com.github.onsdigital.test.browser.PageObjects;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CollectionsPage extends FlorencePage {

    By collectionNameInputLocator = By.id("collectionname");
    By teamSelectLocator = By.id("team");
    By scheduledPublishRadioLocator = By.id("scheduledpublish");
    By manualPublishRadioLocator = By.id("manualpublish");
    By dateInputLocator = By.id("date");
    By hourInputLocator = By.id("hour");
    By minInputLocator = By.id("min");
    By createCollectionButtonLocator = By.className("btn-collection-create");

    WebElement collectionNameInput;
    Select teamSelect;
    WebElement scheduledPublishRadio;
    WebElement manualPublishRadio;
    WebElement dateInput;
    WebElement createCollectionButton;
    Select hourInput;
    Select minInput;

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
            createCollectionButton = find(createCollectionButtonLocator);
            dateInput = find(dateInputLocator);
            hourInput = new Select(find(hourInputLocator));
            minInput = new Select(find(minInputLocator));

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
     * Helper method to compose populating the form and submitting
     * @param name
     * @return
     */
    public BrowsePage createScheduledCollection(String name) {
        populateFormWithScheduledDefaults(name);
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
     * Populate all fields to create a collection with defaults and a random name.
     *
     * @return
     */
    public CollectionsPage populateFormWithScheduledDefaults() {
        return populateFormWithScheduledDefaults(Random.id());
    }

    /**
     * Populate all fields to create a collection with defaults and the given name.
     * @param name
     * @return
     */
    public CollectionsPage populateFormWithScheduledDefaults(String name) {
        typeCollectionName(name);
        selectTeamByIndex(1);
        selectScheduledPublish();

        typeDate("26/08/2078");
        selectHour(8);
        selectMinute(1);

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
     * Type the given date value into the collection form.
     *
     * @param date
     * @return
     */
    public CollectionsPage typeDate(Date date) {
        dateInput.clear();
        dateInput.sendKeys(new SimpleDateFormat("dd/MM/yyyy").format(date));
        return this;
    }
    /**
     * Type the given date value into the collection form.
     *
     * @param date
     * @return
     */
    public CollectionsPage typeDate(String date) {
        dateInput.clear();
        dateInput.sendKeys(date);
        dateInput.sendKeys(Keys.ENTER);

        // wait for date picker to not be visible. It obstructs the create button.
        (new WebDriverWait(driver, 2)).until(ExpectedConditions.invisibilityOfElementLocated(By.id("ui-datepicker-div")));

        return this;
    }

    /**
     * Select the hour to publish a scheduled collection.
     *
     * @param index
     * @return
     */
    public CollectionsPage selectHour(int index) {
        hourInput.selectByIndex(index);
        return this;
    }
    /**
     * Select the hour to publish a scheduled collection.
     *
     * @param hour
     * @return
     */
    public CollectionsPage selectHour(String hour) {
        hourInput.selectByValue(hour);
        return this;
    }

    /**
     * Select the minute to publish a scheduled collection.
     *
     * @param index
     * @return
     */
    public CollectionsPage selectMinute(int index) {
        minInput.selectByIndex(index);
        return this;
    }
    /**
     * Select the minute to publish a scheduled collection.
     *
     * @param min
     * @return
     */
    public CollectionsPage selectMinute(String min) {
        minInput.selectByValue(min);
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
        this.acceptAlert();
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
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new CollectionDetailsPage(driver);
            }
        }

        throw new NotFoundException("Could not find collection with name: " + collectionName);
    }
}

