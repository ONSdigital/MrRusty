package com.github.onsdigital.selenium;

import com.github.onsdigital.test.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a way to access multiple {@link org.openqa.selenium.WebDriver}
 * Created by david on 08/04/2015.
 */
public class Drivers {

    /**
     * @see <a href="https://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/"
     * >https://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/</a>
     */
    static Map<String, WebDriver> drivers = java.util.Collections.synchronizedMap(new ConcurrentHashMap<String, WebDriver>(8, 0.9f, 1));

    /**
     * The default {@link DriverFactory} generates {@link WebDriver} instances with Javascript enabled.
     */
    public static DriverFactory driverFactory = new DriverFactory() {
        @Override
        public WebDriver newDriver() {

            WebDriver driver = null;

            String browserStackUrl = Configuration.getBrowserStackUrl();

            if (StringUtils.isNotBlank(browserStackUrl)) {
                DesiredCapabilities caps = new DesiredCapabilities();
                caps.setCapability("browser", "Chrome");
                caps.setCapability("browser_version", "31.0");
                caps.setCapability("os", "Windows");
                caps.setCapability("os_version", "7");
                caps.setCapability("resolution", "1600x1200");
                caps.setCapability("browserstack.debug", "true");

                try {
                    driver = new RemoteWebDriver(new URL(browserStackUrl), caps);
                    driver.manage().window().setSize(new Dimension(1600,1200));
                } catch (MalformedURLException e) {
                    throw new Error("Could not connect to BrowserStack with the given URL: " + browserStackUrl, e);
                }
            } else {

                try {
                    ChromeOptions options = new ChromeOptions();
//                    options.addArguments("--start-maximized");
//                    options.addArguments("--no-sandbox");
//                    options.addArguments("--disable-web-security");
//                    options.addArguments("--ignore-certificate-errors");
                    DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
                    desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
                    //desiredCapabilities.setCapability("resolution", "1600x1200");

                    driver = new RemoteWebDriver(new URL("http://localhost:9515"), desiredCapabilities);
                } catch (MalformedURLException e) {
                    throw new Error("Could not connect to ChromeDriver with the given URL: " + Configuration.getFlorenceUrl(), e);
                } catch (UnreachableBrowserException exception) {
                    throw new Error("Could not find browser, are you running chrome driver?", exception);
                }

                //driver = new FirefoxDriver();
                //driver.manage().window().maximize();
                driver.manage().window().setSize(new Dimension(1600,1200));
            }

            return driver;
        }
    };

    /**
     * If you want to use a different {@link WebDriver} setup, implement this interface and assign it to the {@link #driverFactory} field.
     */
    public interface DriverFactory {
        WebDriver newDriver();
    }

    /**
     * @param name A string to identify a particular driver.
     * @return A {@link WebDriver} for the given name, creating it if necessary.
     */
    public static WebDriver get(String name) {
        WebDriver driver = drivers.get(name);
        if (driver == null) {
            drivers.put(name, driver = driverFactory.newDriver());
        }
        return driver;
    }

    /**
     * Convenience method to obtain a default {@link WebDriver}.
     *
     * @return A {@link WebDriver}. The same driver will be returned each time.
     */
    public static WebDriver get() {
        return get("DEFAULT");
    }

    public static void quit() {
        for (WebDriver driver : drivers.values()) {
            driver.quit();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // Last-chance attempt to ensure everything is cleaned up:
        quit();
    }

//    makeAutoCloseable(WebDriver driver) {
//        InvocationHandler handler = new InvocationHandler(){};
//        MyInterface proxy = (MyInterface) Proxy.newProxyInstance(
//                MyInterface.class.getClassLoader(),
//                new Class[] { MyInterface.class },
//                handler);
//    }

//    interface CloseableDriver extends WebDriver, AutoCloseable {
//
//    }

}
