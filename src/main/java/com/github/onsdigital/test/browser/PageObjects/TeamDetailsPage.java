package com.github.onsdigital.test.browser.PageObjects;


import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public class TeamDetailsPage extends TeamsPage {

    By selectATeamTextLocator = By.cssSelector(".collection-select.text-align-center");
    By addRemoveMembersButtonLocator = By.className("btn-team-edit-members");
    By deleteTeamButtonLocator = By.className("btn-team-delete");
    By doneButtonLocator = By.className("btn-team-cancel");

    WebElement selectATeamText;
    WebElement addRemoveMembersButton;
    WebElement deleteTeamButton;
    WebElement doneButton;

    public TeamDetailsPage(WebDriver driver) {
        super(driver);
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


    public TeamDetailsPage dragAndDropUser(String teamUser) {

        initialisePage();

        List<WebElement> users = driver.findElements(By.cssSelector(".ui-draggable.ui-draggable-handle"));

        WebElement to = driver.findElement(By.cssSelector("ul.team-list.ui-droppable"));

        WebElement done = driver.findElement(By.className("btn-team-selector-cancel"));

        for (WebElement user : users) {

            if (user.getText().equals(teamUser)) {

                (new Actions(driver)).dragAndDrop(user, to).perform();

                done.click();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        return this;
    }

}
