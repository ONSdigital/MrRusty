package com.github.onsdigital.selenium;

import org.openqa.selenium.WebDriver;

/**
 * Created by david on 08/04/2015.
 */
public class PageObject {

    protected WebDriver driver;

    protected PageObject(WebDriver driver) {
        this.driver = driver;
    }
}
