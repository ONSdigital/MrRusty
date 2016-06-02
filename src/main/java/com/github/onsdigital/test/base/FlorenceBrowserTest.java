package com.github.onsdigital.test.base;

import com.github.onsdigital.selenium.PageObject;
import com.github.onsdigital.test.configuration.Configuration;
import com.github.webdriverextensions.Bot;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import com.github.webdriverextensions.internal.junitrunner.DriverPathLoader;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class FlorenceBrowserTest extends PublishingIntegrationTest {
    static {
        // invoke the framework method to set the driver paths as expected.
        DriverPathLoader.loadDriverPaths(null);
    }

    @Before
    public void setUp() throws Exception {

        //System.out.println("Running setup in FlorenceBrowserTest");

        String browserStackUrl = Configuration.getBrowserStackUrl();
        if (StringUtils.isNotBlank(browserStackUrl)) {
            try {
                WebDriverExtensionsContext.setDriver(new RemoteWebDriver(new URL(browserStackUrl), DesiredCapabilities.chrome()));
            } catch (MalformedURLException e) {
                throw new Error("Could not connect to BrowserStack with the given URL: " + browserStackUrl, e);
            }
        } else {
            WebDriverExtensionsContext.setDriver(new ChromeDriver());
            Bot.driver().manage().window().setSize(new Dimension(1600, 1200));
        }

        PageObject.openIfNecessary(Configuration.getFlorenceUrl());
    }

    @After
    public void tearDown() throws Exception {
        Bot.driver().quit();
        WebDriverExtensionsContext.removeDriver();
    }
}
