package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Base object common to all pages in florence.
 */
public class FlorencePage extends PageObject {

    By collectionsLocator = By.className("nav--admin__item--collections");
    By usersLocator = By.className("nav--admin__item--users");
    By teamsLocator = By.className("nav--admin__item--teams");
    By publishingQueueLocator = By.className("nav--admin__item--publish");
    By logoutLocator = By.className("nav--admin__item--logout");

    By alertLocator = By.className("sweet-alert");

    WebElement collectionsMenuLink;
    WebElement usersMenuLink;
    WebElement teamsMenuLink;
    WebElement publishingQueueMenuLink;

    /**
     * Get the page source of the current page.
     *
     * @return
     */
    public String getSource() {
        return driver.getPageSource();
    }

    /**
     * Logout the current user.
     * This isn't declared as a class member as it is only
     * visible once the user has logged in. We cannot assume
     * it is always present in the page.
     *
     * @return
     */
    public LoginPage clickLogoutMenuLink() {
        try {
            WebElement logoutMenuLink = waitAndFind(logoutLocator);
            logoutMenuLink.click();
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the logout menu link.", exception);
        }

        return new LoginPage();
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
        return new CollectionsPage();
    }

    public TeamsPage clickTeamsMenuLink() {
        teamsMenuLink = waitAndFind(teamsLocator);
        this.teamsMenuLink.click();
        return new TeamsPage();
    }

    public UsersPage clickUsersMenuLink() {
        usersMenuLink = waitAndFind(usersLocator);
        this.usersMenuLink.click();
        return new UsersPage();
    }

    public PublishingQueuePage clickPublishingQueueMenuLink() {
        publishingQueueMenuLink = waitAndFind(publishingQueueLocator);
        this.publishingQueueMenuLink.findElement(By.linkText("Publishing queue")).click();

        return new PublishingQueuePage();
    }

    protected void acceptFlorenceAlert() {
        getAlert().findElement(By.className("confirm")).click();
    }

    protected WebElement getAlert() {
        WebElement alert = waitAndFind(alertLocator);
        return alert;
    }

}
