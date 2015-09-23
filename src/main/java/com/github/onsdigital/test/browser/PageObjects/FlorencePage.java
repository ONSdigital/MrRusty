package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.Drivers;
import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import com.github.onsdigital.test.configuration.Configuration;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Base object common to all pages in florence.
 */
public class FlorencePage extends PageObject {

    By collectionsLocator = By.className("nav--admin__item--collections");
    By usersLocator = By.className("nav--admin__item--users");
    By publishingQueueLocator = By.className("nav--admin__item--publish");
    By logoutLocator = By.className("nav--admin__item--logout");

    WebElement collectionsMenuLink;
    WebElement usersMenuLink;
    WebElement publishingQueueMenuLink;

    protected FlorencePage() {
        super(Drivers.get());
        openIfNecessary(Configuration.getFlorenceUrl());
    }

    /**
     * Get the page source of the current page.
     * @return
     */
    public String getSource() {
        return driver.getPageSource();
    }

    protected FlorencePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Logout the current user.
     * This isn't declared as a class member as it is only
     * visible once the user has logged in. We cannot assume
     * it is always present in the page.
     * @return
     */
    public LoginPage clickLogoutMenuLink()
    {
        try {
            WebElement logoutMenuLink = waitAndFind(logoutLocator);
            logoutMenuLink.click();
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the logout menu link.", exception);
        }

        return new LoginPage(driver);
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected FlorencePage initialisePage() {
        try {

        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the admin menu.", exception);
        }
        return this;
    }

    public CollectionsPage clickCollectionsMenuLink() {
        collectionsMenuLink = waitAndFind(collectionsLocator);
        this.collectionsMenuLink.click();
        return new CollectionsPage(driver);
    }

    public PublishingQueuePage clickPublishingQueueMenuLink() {
        publishingQueueMenuLink = find(publishingQueueLocator);
        this.publishingQueueMenuLink.click();
        return new PublishingQueuePage(driver);
    }
}
