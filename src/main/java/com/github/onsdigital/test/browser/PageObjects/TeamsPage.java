package com.github.onsdigital.test.browser.PageObjects;

import com.github.onsdigital.selenium.PageObjectException;
import org.openqa.selenium.*;

import java.util.List;

public class TeamsPage extends FlorencePage {

    By teamNameInputLocator = By.id("create-team-name");
    By createTeamButtonLocator = By.className("btn-collection-create");

    WebElement teamNameInput;
    WebElement createTeamButton;

    public TeamsPage() {
        super();
        initialisePage();
    }

    /**
     * Check the expected elements are located in the page.
     */
    protected TeamsPage initialisePage() {
        try {
            super.initialisePage();
        } catch (NoSuchElementException exception) {
            throw new PageObjectException("Failed to recognise the " + this.getClass().getSimpleName() + " contents.", exception);
        }

        return this;
    }

    public TeamsPage createTeamName (String name) {

        teamNameInput = waitAndFind(teamNameInputLocator);
        createTeamButton = find(createTeamButtonLocator);
        teamNameInput.clear();
        teamNameInput.sendKeys(name);
        createTeamButton.click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new TeamsPage();
    }

    public TeamDetailsPage clickTeamByName(String teamName) {

        WebElement teamTable = driver.findElement(By.className("collections-select-table"));
        List<WebElement> teams = teamTable.findElements(By.tagName("tr"));

        for (WebElement team : teams) {

            if (team.getText().equals(teamName)) {
                team.click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new TeamDetailsPage();
            }
        }

        throw new NotFoundException("Could not find team with name: " + teamName);
    }



}
