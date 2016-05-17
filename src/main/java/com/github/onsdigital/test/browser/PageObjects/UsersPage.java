package com.github.onsdigital.test.browser.PageObjects;


import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class UsersPage extends FlorencePage {

    By userNameInputLocator = By.id("create-user-username");
    By userEmailInputLocator = By.id("create-user-email");
    By userPasswordInputLocator = By.id("create-user-password");
    By userTypeAdministratorRadioButtonLocator = By.id("admin-type");
    By userTypePublisherRadioButtonLocator = By.id("publisher-type");
    By userTypeViewerRadioButtonLocator = By.id("viewer-type");
    By createUserButtonLocator = By.className("btn-collection-create");
    By userCreatedConfirmButtonLocator = By.className("confirm");

    WebElement userNameInput;
    WebElement userEmailInput;
    WebElement userPasswordInput;
    WebElement userTypeAdministratorRadioButton;
    WebElement userTypePublisherRadioButton;
    WebElement userTypeViewerRadioButton;
    WebElement createUserButton;
    WebElement userCreatedConfirmButton;

    public UsersPage() {
        super();
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected UsersPage initialisePage() {
        try {
            super.initialisePage();
            userNameInput = waitAndFind(userNameInputLocator);
            userEmailInput = find(userEmailInputLocator);
            userPasswordInput = find(userPasswordInputLocator);
            userTypeAdministratorRadioButton = find(userTypeAdministratorRadioButtonLocator);
            userTypePublisherRadioButton = find(userTypePublisherRadioButtonLocator);
            userTypeViewerRadioButton = find(userTypeViewerRadioButtonLocator);
            createUserButton = find(createUserButtonLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    private void PopulateUserFields(String name, String email, String password) {
        userNameInput.clear();
        userNameInput.sendKeys(name);
        userEmailInput.clear();
        userEmailInput.sendKeys(email);
        userPasswordInput.clear();
        userPasswordInput.sendKeys(password);
    }

    public UsersPage CreateAdministratorUser (String name, String email, String password) {
        PopulateUserFields(name, email, password);
        userTypeAdministratorRadioButton.click();
        createUserButton.click();
        userCreatedConfirmButton = waitAndFind(userCreatedConfirmButtonLocator);
        userCreatedConfirmButton.click();
        initialisePage();
        return this;
    }

    public UsersPage CreatePublisherUser (String name, String email, String password) {
        PopulateUserFields(name, email, password);
        userTypePublisherRadioButton.click();
        createUserButton.click();
        userCreatedConfirmButton = waitAndFind(userCreatedConfirmButtonLocator);
        userCreatedConfirmButton.click();
        initialisePage();
        return this;
    }

    public UsersPage CreateViewerUser (String name, String email, String password) {
        PopulateUserFields(name, email, password);
        userTypeViewerRadioButton.click();
        createUserButton.click();
        userCreatedConfirmButton = waitAndFind(userCreatedConfirmButtonLocator);
        userCreatedConfirmButton.click();
        initialisePage();
        return this;
    }

}
