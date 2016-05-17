package com.github.onsdigital.test.browser.PageObjects;


import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class TeamDetailsPage extends TeamsPage {

    By selectATeamTextLocator = By.cssSelector(".collection-select.text-align-center");
    By addRemoveMembersButtonLocator = By.className("btn-team-edit-members");
    By deleteTeamButtonLocator = By.className("btn-team-delete");
    By doneButtonLocator = By.className("btn-team-cancel");

    WebElement selectATeamText;
    WebElement addRemoveMembersButton;
    WebElement deleteTeamButton;
    WebElement doneButton;

    public TeamDetailsPage() {
        super();
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected TeamDetailsPage initialisePage() {
        try {
            super.initialisePage();
//            selectATeamText = waitAndFind(selectATeamTextLocator);
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    public TeamDetailsPage clickAddUserButton () {
        addRemoveMembersButton = find(addRemoveMembersButtonLocator);
        addRemoveMembersButton.click();
        return this;
    }

}
