package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base class for all workspace pages containing the common elements
 * such as the preview window and workspace navigation.
 */
public class WorkspacePage extends FlorencePage {

    By browseButtonLocator = By.className("nav--workspace__browse");
    By createButtonLocator = By.className("nav--workspace__create");
    By editButtonLocator = By.className("nav--workspace__edit");
    By previewWindowLocator = By.id("iframe");

    WebElement browseButton;
    WebElement createButton;
    WebElement editButton;
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
            previewWindow = find(previewWindowLocator);

            checkForPreviewDisclaimer();
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    /**
     * Click the create page menu link.
     *
     * @return
     */
    public CreatePage clickCreateMenuItem() {
        createButton.click();
        return new CreatePage(driver);
    }

    /**
     * Click the edit page menu link.
     *
     * @return
     */
    public EditPage clickEditMenuItem() {
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
        // apply a wait to ensure the page is loaded
        (new WebDriverWait(driver, 5)).until(ExpectedConditions.visibilityOf(find(previewWindowLocator)));
        return (String)((JavascriptExecutor)driver).executeScript("return $('#iframe').contents().find('body').html();");
    }

    /**
     * Click a link in the navigation menu in the preview window for the given path.
     * @param path
     * @return
     */
    public WorkspacePage clickPreviewNavigationLink(String path) {
        String selector = String.format("[ons-nav-item='%s'] a", path);
        return clickPreviewLink(By.cssSelector(selector));
    }

    /**
     * Click a link in the preview window for the given selector.
     *
     * @param selector
     */
    public WorkspacePage clickPreviewLink(By selector) {
        //System.out.println("Clicking preview link:" + selector.toString());
        driver.switchTo().frame(previewWindow); // switch the driver to the iframe
        waitAndFind(selector).click();
        driver.switchTo().defaultContent(); // switch the driver back to the main page.
        // wait for the page to refresh
        (new WebDriverWait(driver, 5)).until(ExpectedConditions.visibilityOf(find(previewWindowLocator)));
        return this;
    }

    private void checkForPreviewDisclaimer() {

        try {
            driver.switchTo().frame(previewWindow); // switch the driver to the iframe
            driver.findElement(By.cssSelector(".btn-modal-continue")).click();
            driver.switchTo().defaultContent(); // switch the driver back to the main page.
        } catch (Exception e) {
            driver.switchTo().defaultContent(); // switch the driver back to the main page.
            return;
        }
    }
}
