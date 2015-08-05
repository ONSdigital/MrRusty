package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;

public class LoginPage extends FlorencePage {

    By usernameLocator = By.id("email");
    By passwordLocator = By.id("password");
    By loginButtonLocator = By.className("btn-florence-login");

    WebElement usernameInput;
    WebElement passwordInput;
    WebElement loginButton;

    public LoginPage() {
        super();
        initialisePage();
    }

    public LoginPage(WebDriver driver) {
        super(driver);
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected LoginPage initialisePage() {

        super.initialisePage();

        // if we cannot find the login form, logout
        try {
            usernameInput = find(usernameLocator);
        } catch (NoSuchElementException exception) {
            clickLogoutMenuLink();
        }

        try {
            usernameInput = waitAndFind(usernameLocator);
            passwordInput = find(passwordLocator);
            loginButton = find(loginButtonLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }
        return this;
    }

    /**
     * Composite method to type the username and password and then submit.
     * @param username
     * @param password
     */
    public CollectionsPage login(String username, String password) {
        //typeUsername(username);
        //typePassword(password);
        return clickLogin();
    }

    /**
     * Simulate clicking the login button on the login page expecting it to fail.
     */
    public LoginPage clickLoginExpectingFailure(String username, String password) {
        //typeUsername(username);
        //typePassword(password);
        loginButton.click();
        return new LoginPage(driver);
    }

    /**
     * Type the given username into the username field on the login page.
     * @param username
     */
    public LoginPage typeUsername(String username) {
        usernameInput.clear();
        usernameInput.sendKeys(username);
        return this;
    }

    /**
     * Type the given password into the password field on the login page.
     * @param password
     */
    public LoginPage typePassword(String password) {
        passwordInput.clear();
        passwordInput.sendKeys(password);
        return this;
    }

    /**
     * Simulate clicking the login button on the login page.
     */
    public CollectionsPage clickLogin() {
        passwordInput.submit();
        return new CollectionsPage(driver);
    }
}
