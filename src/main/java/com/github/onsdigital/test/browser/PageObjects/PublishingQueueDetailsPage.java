package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import com.github.onsdigital.test.json.CollectionDescription;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class PublishingQueueDetailsPage extends PublishingQueuePage {

    public PublishingQueueDetailsPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected PublishingQueueDetailsPage initialisePage() {
        try {
            super.initialisePage();

            //collectionsTable = waitAndFind(collectionsTableLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    private WebElement findCollectionSection(String collectionName) {
        List<WebElement> collections = driver.findElements(By.className("collections-section"));

        for (WebElement collection : collections) {
            WebElement head = collection.findElement(By.className("collections-section__head")).findElement(By.className("collection-name"));
            if (head.getText().equals(collectionName)) {

                String uid = collection.findElement(By.className("collections-section__head")).getAttribute("id");
                System.out.println(uid);

                try {
                    scrollTo(uid);
                } catch (WebDriverException e) {
                    System.out.println("Could not scroll to cell " + uid);
                }
                return collection;
            }
        }
        throw new PageObjectException("Publish: Failed to recognise the " + collectionName + " collection in list.");
    }

    public void clickCollectionSection(String collectionName) {
        WebElement collectionSection = findCollectionSection(collectionName);
        collectionSection.click();
    }

    public CollectionsPage unlockCollectionWithName(String collectionName) {
        WebElement collectionSection = findCollectionSection(collectionName);

        WebElement buttonSection = collectionSection
                .findElement(By.className("collections-section__content"))
                .findElement(By.className("btn-collection-unlock"));
        buttonSection.click();

        // click the acceptance alert dialog
        acceptAlert();

        new WebDriverWait(driver, 5).until(ExpectedConditions.invisibilityOfElementLocated(By.className("section-head")));

        // return to the collections page
        return clickCollectionsMenuLink();
    }

    public CollectionsPage publishCollectionWithName(String collectionName) {
        WebElement collectionSection = findCollectionSection(collectionName);

        WebElement buttonSection = collectionSection
                .findElement(By.className("collections-section__content"))
                .findElement(By.className("btn-collection-publish"));
        buttonSection.click();

        // click the acceptance alert dialog
        acceptAlert();

        new WebDriverWait(driver, 5).until(ExpectedConditions.invisibilityOfElementLocated(By.className("hourglass")));
        new WebDriverWait(driver, 5).until(ExpectedConditions.invisibilityOfElementLocated(By.className("section-head")));

        // return to the collections page
        clickCollectionsMenuLink();
        return new CollectionsPage(driver);
    }

}

