package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.Drivers;
import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.selenium.PageObjectException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by david on 08/04/2015.
 */
public class GoogleHomepage extends PageObject {

    private static String searchBoxXpath = "//input[@type='text' and @name='q']";

    public GoogleHomepage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        driver.get("http://google.com/ncr");
    }

    void openIfNecessary() {
        String currentUrl = driver.getCurrentUrl();
        URIBuilder builder = null;
        try {
            builder = new URIBuilder(currentUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (!StringUtils.equalsIgnoreCase("google.com", builder.getHost())) {
            open();
        }
    }

    public GoogleResultPage search(String search) {

        // Initial setup:
        openIfNecessary();
        checkSearchControls();

        // Perform the search:
        WebElement searchBox = driver.findElement(By.xpath(searchBoxXpath));
        searchBox.sendKeys(search);
        searchBox.submit();

        // Return the next PageObject:
        return new GoogleResultPage(driver);
    }

    private void checkSearchControls() {
        List<WebElement> elements;

        // Check the search box:
        elements = driver.findElements(By.xpath(searchBoxXpath));
        if (elements.size() != 1) {
            String message;
            if (elements.size() == 0) {
                message = "Could not find the search box for XPath " + searchBoxXpath;
            } else {
                message = "Found " + elements.size() + " search boxes for XPath " + searchBoxXpath;
            }
            throw new PageObjectException(message);
        }
    }

    public static void main(String[] args) {
        try {
            WebDriver driver = Drivers.get();
            GoogleHomepage googleHomepage = new GoogleHomepage(driver);
            GoogleResultPage resultPage = googleHomepage.search("David Carboni");
            System.out.println(resultPage.getResults());
            String currentUrl = driver.getCurrentUrl();
            System.out.println("currentUrl = " + currentUrl);
        } finally {
            Drivers.quit();
        }
    }
}
