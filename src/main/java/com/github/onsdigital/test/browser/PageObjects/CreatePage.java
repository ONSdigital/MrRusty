package com.github.onsdigital.test.browser.PageObjects;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.selenium.PageObjectException;
import org.joda.time.DateTime;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CreatePage extends WorkspacePage {

    //By locationInputLocator = By.id("location");
    By pageTypeSelectLocator = By.id("pagetype");
    By editionInputLocator = By.id("edition");
    By releaseDateInputLocator = By.id("releaseDate");
    By pageNameInputLocator = By.id("pagename");
    By createButtonLocator = By.className("btn-page-create");

    WebElement locationInput;
    Select pageTypeSelect;
    WebElement pageNameInput;
    WebElement createButton;

    /**
     * Check the expected elements are located in the page.
     */
    protected CreatePage initialisePage() {
        try {
            super.initialisePage();
            pageNameInput = waitAndFind(pageNameInputLocator);
            //locationInput = find(locationInputLocator);
            pageTypeSelect = new Select(find(pageTypeSelectLocator));

        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    /**
     * Constructor for a previously instantiated WebDriver object.
     *
     * @param driver
     */
    public CreatePage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    /**
     * Select the page type on the create page.
     *
     * @param type
     * @return
     */
    public CreatePage selectPageType(ContentType type) {
        pageTypeSelect.selectByValue(type.toString());
        return this;
    }

    /**
     * Type the location where this page will be created.
     *
     * @param location
     * @return
     */
    public CreatePage typeLocation(String location) {
        locationInput.clear();
        locationInput.sendKeys(location);
        return this;
    }



    /**
     * Type the name for the page.
     *
     * @param name
     * @return
     */
    public CreatePage typePageName(String name) {
        pageNameInput.clear();
        pageNameInput.sendKeys(name);
        return this;
    }

    /**
     * Type the edition for the page.
     *
     * @param edition
     * @return
     */
    public CreatePage typeEdition(String edition) {
        WebElement input = waitAndFind(editionInputLocator);
        input.clear();
        input.sendKeys(edition);
        return this;
    }

    /**
     * Type the date for the page.
     *
     * @param date
     * @return
     */
    public CreatePage typeReleaseDate(Date date) {
        WebElement input = waitAndFind(releaseDateInputLocator);
        input.clear();
        input.sendKeys(new SimpleDateFormat("dd/MM/yyyy").format(date));
        input.sendKeys(Keys.ENTER); // enter to close date picker

        // wait for date picker to not be visible. It obstructs the create button.
        (new WebDriverWait(driver, 2)).until(ExpectedConditions.invisibilityOfElementLocated(By.id("ui-datepicker-div")));

        return this;
    }

    /**
     * Click the create button on the create page form.
     * @return
     */
    public EditPage clickCreatePage() {
        createButton = waitAndFind(createButtonLocator);
        createButton.click();
        return new EditPage(driver);
    }

    /**
     * Helper method to create a page for the given type and name.
     * @param pageType
     * @param pageName
     * @return
     */
    public EditPage createPage(ContentType pageType, String pageName) {
        return selectPageType(pageType)
                .typeEdition(Random.id())
                .typeReleaseDate(new DateTime(new Date()).plusDays(1).toDate())
                .typePageName(pageName)
                .clickCreatePage();
    }

    public EditPage createRelease(String pageName) {
        return selectPageType(ContentType.release)
                .typeReleaseDate(new DateTime(new Date()).plusDays(1).toDate())
                .typePageName(pageName)
                .clickCreatePage();
    }
}
