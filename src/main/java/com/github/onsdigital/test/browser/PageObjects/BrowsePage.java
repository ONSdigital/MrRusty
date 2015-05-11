package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BrowsePage extends WorkspacePage {

    By browseTreeLocator = By.className("page-item");

    WebElement browseTree;

    public BrowsePage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected BrowsePage initialisePage() {
        try {
            super.initialisePage();
//            browseTree = waitAndFind(browseTreeLocator);
            //collectionNameInput = find(collectionNameInputLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the browse page.", exception);
        }

        return this;
    }

    /**
     * Select an item in the browse tree for the given path.
     * @param path
     * @return
     */
    public BrowsePage clickTreeItem(String path) {
        WebElement treeNode = this.waitAndFind(By.cssSelector("[data-url='" + path + "'] span"));
        treeNode.click();
        return this;
    }
}
