package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;

public class PublishingQueuePage extends FlorencePage {

    By publishTableLocator = By.className("publish-select-table");

    WebElement publishTable;

    public PublishingQueuePage() {
        super();
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected PublishingQueuePage initialisePage() {
        try {
            super.initialisePage();

            publishTable = waitAndFind(publishTableLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    public PublishingQueueDetailsPage selectManualPublish() {
        WebElement manualCell;
        try {
            manualCell = driver.findElement(By.xpath("//td[contains(., '[manual collection]')]"));
            manualCell.click();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Could not find [manual collection]");
        }
        waitForAnimations();
        return new PublishingQueueDetailsPage();

    }

    /**
     * Estabished 26/08/2078 as our example publishing date
     *
     * @return
     */
    public PublishingQueueDetailsPage selectPublishDateIn2078() {
        WebElement publishCell;
        try {
            publishCell = driver.findElement(By.xpath("//td[contains(., '2078')]"));
            publishCell.click();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Could not find [2078 collection]");
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new PublishingQueueDetailsPage();

    }
}

