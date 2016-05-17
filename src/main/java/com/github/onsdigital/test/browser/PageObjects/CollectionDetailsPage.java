package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Nullable;
import java.util.List;

public class CollectionDetailsPage extends CollectionsPage {

    By workOnCollectionButtonLocator = By.className("btn-collection-work-on");
    By approveCollectionButtonLocator = By.className("btn-collection-approve");

    WebElement workOnCollectionButton;


    public CollectionDetailsPage() {
        super();
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected CollectionDetailsPage initialisePage() {
        try {
            super.initialisePage();
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    /**
     * Click the work on collection button and expect the browse screen to be loaded.
     * @return
     */
    public BrowsePage clickWorkOnCollection() {
        workOnCollectionButton = waitAndFind(workOnCollectionButtonLocator);
        workOnCollectionButton.click();
        return new BrowsePage();
    }

    public SelectedPagePartial clickCollectionPageByName(String pageName) {


        List<WebElement> pages = driver.findElements(By.cssSelector(".collection-selected .section-content ul.page-list li"));

        for (WebElement page : pages) {

            System.out.println(page.getAttribute("innerHTML"));

            try {
                WebElement pageSpan = page.findElement(By.tagName("span"));
                System.out.println("pageSpan.getText() = " + pageSpan.getText());
                if (pageSpan.getText().contains(pageName)) {
                    pageSpan.click();
                    return new SelectedPagePartial(page);
                }
            } catch (NotFoundException exception) {
                // no pages in this page list - do nothing.
                System.out.println("exception = " + exception.getMessage());
            }

        }

        throw new NotFoundException("Could not find page with name: " + pageName);
    }

    public CollectionsPage clickApprove() {
        WebElement approveButton = waitAndFind(approveCollectionButtonLocator);
        approveButton.click();

        new WebDriverWait(driver, 5).until(ExpectedConditions.invisibilityOfElementLocated(By.className("hourglass")));
        new WebDriverWait(driver, 5).until(ExpectedConditions.invisibilityOfElementLocated(By.className("collection-selected")));


        return new CollectionsPage();
    }

    private void waitForAnimationToFinish() {
        new WebDriverWait(driver, 5).until(animationFinished());
    }

    /**
     * Return true if the collection details pane has an animation in progress.
     *
     * @return
     */
    private static boolean hasAnimationFinished() {
        boolean isAnimated = (boolean) PageObject.runScript("return jQuery('.collection-selected').is(':animated')");
        //System.out.println("Checking isAnimated...." + isAnimated);
        return !isAnimated;
    }

    private static ExpectedCondition<Boolean> animationFinished() {
        return new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(WebDriver input) {
                return hasAnimationFinished();
            }
        };
    }
}
