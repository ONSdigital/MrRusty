package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObject;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by david on 08/04/2015.
 */
public class GoogleResultPage extends PageObject {

    private static String resultXpath = "//li[@class='g']//a";
    private ExecutorService pool = Executors.newCachedThreadPool();

    public GoogleResultPage(WebDriver driver) {
        super(driver);
    }

    public List<NameValuePair> getResults() {
        final List<NameValuePair> results = new ArrayList<>();

        for (final WebElement element : driver.findElements(By.xpath(resultXpath))) {

            final WebElement elementReference = element;
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    results.add(new BasicNameValuePair(element.getText(), element.getAttribute("href")));
//                    try {
//                        WebDriverWait wait = new WebDriverWait(driver, 2);
//                        wait.until(new ExpectedCondition<Boolean>() {
//                            public Boolean apply(WebDriver driver) {
//                                String dataHref = elementReference.getAttribute("data-href");
//                                if (dataHref != null)
//                                    return true;
//                                else
//                                    return false;
//                            }
//                        });
//
//                        System.out.println("\t" + elementReference.getAttribute("data-href"));
//                    } catch (TimeoutException e) {
//                        System.out.println("\t(no data-href)");
//                    }
                }
            });

            for (WebElement element1 : driver.findElements(By.cssSelector("[data-jiis]"))) {
                System.out.println(" > " + element.getText());
            }
        }

        return results;
    }
}
