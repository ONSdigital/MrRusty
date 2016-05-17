package com.github.onsdigital.test.browser;

import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.base.FlorenceBrowserTest;
import com.github.onsdigital.test.browser.PageObjects.*;
import com.github.onsdigital.test.browser.model.User;
import com.github.onsdigital.test.json.Credentials;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

@DependsOn({Login.class})
public class Teams extends FlorenceBrowserTest {

    Credentials publisher = Context.systemCredentials;


    @POST
    @Test
    public void shouldAddNewTeam() throws IOException {

        // Given the collections page after logging in
        CollectionsPage collectionPage = new LoginPage().login(publisher.email, publisher.password);

        String teamName = Teams.createTeamName();

        // Move to teams page
        TeamsPage teamsPage = collectionPage.clickTeamsMenuLink();

        // Create a new team
        teamsPage.createTeamName(teamName);
    }

    @POST
    @Test
    public void shouldAddUserToTeam() throws IOException {

        // Given the collections page after logging in
        CollectionsPage collectionPage = new LoginPage().login(publisher.email, publisher.password);
        String teamName = Teams.createTeamName();

        // Create a user
        User user1 = new User();
        UsersPage usersPage = collectionPage.clickUsersMenuLink();
        usersPage.CreatePublisherUser(user1.name, user1.email, user1.password);

        // Move to teams page
        TeamsPage teamsPage = collectionPage.clickTeamsMenuLink();

        // Create a new team
        teamsPage.createTeamName(teamName);

        // Select a team
        TeamDetailsPage teamDetailsPage = teamsPage.clickTeamByName(teamName);

        // Add user to a team
        teamDetailsPage.clickAddUserButton();
    }

    private static String createTeamName() {
        return com.github.onsdigital.test.api.Teams.createTeamName();
    }


}
