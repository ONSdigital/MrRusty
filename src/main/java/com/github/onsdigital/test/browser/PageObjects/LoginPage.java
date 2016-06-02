package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import com.github.onsdigital.test.json.Credentials;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class LoginPage extends FlorencePage {

    By usernameLocator = By.id("email");
    By passwordLocator = By.id("password");
    By loginButtonLocator = By.id("login");

    By currentPasswordLocator = By.id("password-old");
    By newPasswordLocator = By.id("password-new");
    By confirmPasswordLocator = By.id("password-confirm");
    By updatePasswordButtonLocator = By.id("update-password");


    WebElement usernameInput;
    WebElement passwordInput;
    WebElement loginButton;

    WebElement currentPassword;
    WebElement passwordNew;
    WebElement confirmPassword;
    WebElement updatePassword;

    public LoginPage() {
        super();
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
     * Helper method to login a user for the given credentials object.
     * @param credentials
     * @return
     */
    public CollectionsPage login(Credentials credentials) {
        return login(credentials.email, credentials.password);
    }

    /**
     * Composite method to type the username and password and then submit.
     * @param username
     * @param password
     */
    public CollectionsPage login(String username, String password) {
        typeUsername(username);
        typePassword(password);
        return clickLogin();
    }

    public LoginPage loginExpectFailure(String username, String password) {
        typeUsername(username);
        typePassword(password);
        loginButton.click();
        return new LoginPage();
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
        loginButton.click();
        return new CollectionsPage();
    }

    /**
     * Simulate compulsory change of password for new user
     */
    public CollectionsPage changePassword(String oldPassword, String newPassword) {
        currentPassword = waitAndFind(currentPasswordLocator);
        passwordNew = find(newPasswordLocator);
        confirmPassword = find(confirmPasswordLocator);
        updatePassword = find(updatePasswordButtonLocator);

        currentPassword.sendKeys(oldPassword);
        passwordNew.sendKeys(newPassword);
        confirmPassword.sendKeys(newPassword);
        updatePassword.click();

        By confirmButtonLocator = By.className("confirm");
        WebElement confirmButton = waitAndFind(confirmButtonLocator);
        confirmButton.click();

        return new CollectionsPage();
    }
}
