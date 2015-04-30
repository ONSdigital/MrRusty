package com.github.onsdigital.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

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
     * The default {@link DriverFactory} generates {@link HtmlUnitDriver} instances with Javascript enabled.
     */
    public static DriverFactory driverFactory = new DriverFactory() {
        @Override
        public WebDriver newDriver() {
//            return new HtmlUnitDriver(false);
            return new FirefoxDriver();
           //return new ChromeDriver();

//            HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER_11);
//            driver.setJavascriptEnabled(true);
//            return driver;
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
