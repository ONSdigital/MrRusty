package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;

/**
 * Base class for all workspace pages containing the common elements
 * such as the preview window and workspace navigation.
 */
public class WorkspacePage extends FlorencePage {

    By browseButtonLocator = By.className("nav--workspace__browse");
    By createButtonLocator = By.className("nav--workspace__create");
    By editButtonLocator = By.className("nav--workspace__edit");
    By reviewButtonLocator = By.className("nav--workspace__review");
    By previewWindowLocator = By.id("iframe");

    WebElement browseButton;
    WebElement createButton;
    WebElement editButton;
    WebElement reviewButton;
    WebElement previewWindow;

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public WorkspacePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected WorkspacePage initialisePage() {
        try {
            super.initialisePage();
            browseButton = waitAndFind(browseButtonLocator);
            createButton = find(createButtonLocator);
            editButton = find(editButtonLocator);
            reviewButton = find(reviewButtonLocator);
            previewWindow = find(previewWindowLocator);

            checkForPreviewDisclaimer();
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the workspace page contents.", exception);
        }

        return this;
    }

    /**
     * Click the create page menu link.
     *
     * @return
     */
    public CreatePage clickCreate() {
        createButton.click();
        return new CreatePage(driver);
    }

    /**
     * Click the edit page menu link.
     *
     * @return
     */
    public EditPage clickEdit() {
        editButton.click();
        return new EditPage(driver);
    }


    /**
     * Get the current URL that is displayed in the preview window.
     *
     * @return
     */
    public String currentPreviewUrl() {
        return previewWindow.getAttribute("src");
    }

    /**
     * Get the HTML source of the preview window.
     * @return
     */
    public String previewSource() {
        return (String)((JavascriptExecutor)driver).executeScript("return $('#iframe').contents().find('body').html();");
    }

    /**
     * Click a link in the preview window for the given selector.
     *
     * @param selector
     */
    public WorkspacePage clickPreviewLink(By selector) {
        System.out.println("Clicking preview link:" + selector.toString());
        driver.switchTo().frame(previewWindow); // switch the driver to the iframe
        waitAndFind(selector).click();
        driver.switchTo().defaultContent(); // switch the driver back to the main page.
        return this;
    }

    /**
     *
     * @param selector
     * @return
     */
    public WorkspacePage clickPreviewMenuLink(By selector) {
        System.out.println("Clicking preview link:" + selector.toString());
        driver.switchTo().frame(previewWindow); // switch the driver to the iframe
        waitForVisibleAndFind(selector).click();
        driver.switchTo().defaultContent(); // switch the driver back to the main page.
        return this;
    }

    private void checkForPreviewDisclaimer() {

        try {
            driver.switchTo().frame(previewWindow); // switch the driver to the iframe
            driver.findElement(By.cssSelector(".rightButton a")).click();
            driver.switchTo().defaultContent(); // switch the driver back to the main page.
        } catch (Exception e) {
            driver.switchTo().defaultContent(); // switch the driver back to the main page.
            return;
        }
    }
}
