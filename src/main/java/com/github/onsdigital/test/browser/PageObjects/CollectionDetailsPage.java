package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CollectionDetailsPage extends CollectionsPage {

    By workOnCollectionButtonLocator = By.className("btn-collection-work-on");
    By approveCollectionButtonLocator = By.className("btn-collection-approve");

    WebElement workOnCollectionButton;


    public CollectionDetailsPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected CollectionDetailsPage initialisePage() {
        try {
            super.initialisePage();
            //workOnCollectionButton = waitAndFind(workOnCollectionButtonLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    public SelectedPagePartial clickCollectionPageByName(String pageName) {

        
        List<WebElement> pages = driver.findElements(By.cssSelector(".collection-selected .section-content ul.page-list"));

        for (WebElement page : pages) {

            System.out.println(page.getAttribute("innerHTML"));

            try {
                WebElement pageSpan = page.findElement(By.tagName("span"));

                if (pageSpan.getText().contains(pageName)) {
                    pageSpan.click();
                    return new SelectedPagePartial(this.driver, page);
                }
            } catch (NotFoundException exception) {
                // no pages in this page list - do nothing.
            }

        }

        throw new NotFoundException("Could not find page with name: " + pageName);
    }

    public CollectionsPage clickApprove() {
        WebElement approveButton = waitAndFind(approveCollectionButtonLocator);
        approveButton.click();

        // adding wait until we find a way of knowing when the approve is done.
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return new CollectionsPage(driver);
    }
}
