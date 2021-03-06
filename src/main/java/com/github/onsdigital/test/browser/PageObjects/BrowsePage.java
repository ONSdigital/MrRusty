package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class BrowsePage extends WorkspacePage {

    By browseTreeLocator = By.className("page-item--home_page");
    By editButtonLocator = By.className("btn-browse-edit");
    By createButtonLocator = By.className("btn-browse-create");
    By deleteButtonLocator = By.className("btn-browse-delete");

    WebElement browseTree;

    public BrowsePage() {
        super();
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected BrowsePage initialisePage() {
        try {
            super.initialisePage();
            browseTree = waitAndFind(browseTreeLocator);
            //collectionNameInput = find(collectionNameInputLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    /**
     * Select an item in the browse tree for the given path.
     *
     * @param path
     * @return
     */
    public BrowsePage clickBrowseTreeItem(String path) {
        try {
            WebElement treeNode = (new WebDriverWait(driver, 5)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-url='" + path + "'] span")));
            treeNode.click();
        } catch (TimeoutException timeoutException) {
            //System.out.println(driver.getPageSource());
            throw timeoutException;
        }
        return this;
    }


    /**
     * Helper method to create a page in a fixed location for the given name.
     *
     * @return
     */
    public BrowsePage NavigateToT3Page() {
        return clickBrowseTreeItem("/")
                .clickBrowseTreeItem("/economy")
                .clickBrowseTreeItem("/economy/environmentalaccounts");
    }

    /**
     * Search for the currently visible create
     *
     * @return
     */
    public CreatePage clickCreateForCurrentPage() {

        WebElement button = driver.findElement(By.className("tree-nav-holder")).
                findElement(By.className("selected"))
                .findElement(createButtonLocator);
        button.click();

        return new CreatePage();
    }

    public EditPage clickEditForCurrentPage() {

        WebElement button = driver.findElement(By.className("tree-nav-holder")).
                findElement(By.className("selected"))
                .findElement(createButtonLocator);
        button.click();

        return new EditPage();
    }

    public BrowsePage clickDeleteForCurrentPage() {
        List<WebElement> elements = driver.findElements(deleteButtonLocator);
        for (WebElement element : elements) {
            if (element.isDisplayed()) {
                element.click();
                break;
            }
        }
        return new BrowsePage();
    }
}
