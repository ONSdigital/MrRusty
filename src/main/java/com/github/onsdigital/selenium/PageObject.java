package com.github.onsdigital.selenium;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URISyntaxException;

/**
 * Created by david on 08/04/2015.
 */
public class PageObject {

    protected WebDriver driver;

    protected PageObject(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Helper method for shorthand finding of elements.
     * @param selector
     * @return
     */
    protected WebElement find(By selector) {
        return driver.findElement(selector);
    }

    /**
     * Helper method for waiting for an element to be present before finding.
     * @param selector
     * @return
     */
    protected WebElement waitAndFind(By selector) {
        try {
            return (new WebDriverWait(driver, 5)).until(ExpectedConditions.presenceOfElementLocated(selector));
        } catch (TimeoutException timeoutException) {
            System.out.println(driver.getPageSource());
            throw timeoutException;
        }
    }

    protected void openIfNecessary(String url) {
        String currentUrl = driver.getCurrentUrl();
        URIBuilder builder = null;
        try {
            builder = new URIBuilder(currentUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (!StringUtils.equalsIgnoreCase(url, builder.getHost())) {
            driver.get(url);
        }
    }
}
