package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.Team;
import com.github.onsdigital.test.json.TeamList;
import com.github.onsdigital.test.json.User;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 28/04/15.
 */
@DependsOn(Users.class)

@Api
public class Teams extends ZebedeeApiTest {

    /**
     * Test basic GET functionality
     *
     * Get team should return a team {@link HttpStatus#OK_200}
     *
     * Required for createTeam(context)
     */
    @GET
    @Test
    public void canGetATeam() throws IOException {
        // Given
        // a team we have posted
        String teamName = "Rusty_" + Random.id();
        postTeam(teamName, context.getAdministrator());

        // When
        // we get the team
        Response<Team> response = getTeam(teamName, context.getAdministrator());
        Team returnedTeam = response.body;

        // Expect
        // a response of 200 - success
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(teamName, returnedTeam.name);
    }
    /**
     * Test get team list functionality
     *
     * Get team should return a team {@link HttpStatus#OK_200}
     *
     * Required for createTeam(context)
     */
    @GET
    @Test
    public void canGetTeamList() throws IOException {
        // Given
        // a few random teams we have posted
        Team[] teams = {createTeam(context),
                createTeam(context),
                createTeam(context)};

        // When
        // we get the teams
        Response<TeamList> response = getTeamList(context.getAdministrator());
        TeamList teamList = response.body;

        // Expect
        // a response of 200 and our teams appear in the response
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        assertTrue(teamList.teams.size() >= 3);
        for(Team team1: teams) {
            boolean teamFound = false;
            for(Team team2: teamList.teams) {
                if(team1.name.equals(team2.name)) { teamFound = true; break;}
            }
            assertEquals(true, teamFound);
        }

    }
    /**
     * Get team should return team {@link HttpStatus#NOT_FOUND_404} if the team name specified doesn't exist
     *
     */
    @GET
    @Test
    public void returnsNotFoundForGetTeamWithNonexistentTeams() throws IOException {
        // Given
        // a team we have posted
        String teamName = "Rusty_" + Random.id();

        // When
        // we get the team
        Response<Team> response = getTeam(teamName, context.getAdministrator());

        // Expect
        // a response of 404
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }





    /**
     * Test basic create team functionality
     *
     * Create team with admin permissions should return {@link HttpStatus#OK_200}
     *
     * Required for createTeam(context)
     */
    @POST
    @Test
    public void canCreateATeamAsAdministrator() throws IOException {
        // Given
        String teamName = "Rusty_" + Random.id();

        // When
        // we post as an administrator
        Response<Boolean> response = postTeam(teamName, context.getAdministrator());

        // Expect
        // a response of 200 - success
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     *
     * Create team with already existing name should return {@link HttpStatus#CONFLICT_409}
     *
     */
    @POST
    @Test
    public void cannotCreateATeamIfTeamWithNameAlreadyExists() throws IOException {
        // Given
        // a team
        Team team = createTeam(context);

        // When
        // we try to create an identical team
        Response<Boolean> response = postTeam(team.name, context.getAdministrator());

        // Expect
        // a response of Conflict 409
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }

    /**
     *
     * Create team without publisher permissions should return {@link HttpStatus#UNAUTHORIZED_401}
     *
     */
    @POST
    @Test
    public void cannotCreateATeamIfNotAnAdministrator() throws IOException {
        // Given
        // a team
        String teamName1 = "Rusty_" + Random.id();
        String teamName2 = "Rusty_" + Random.id();
        String teamName3 = "Rusty_" + Random.id();

        // When
        // we try to create an identical team
        Response<Boolean> response1 = postTeam(teamName1, context.getPublisher());
        Response<Boolean> response2 = postTeam(teamName1, context.getScallyWag());
        Response<Boolean> response3 = postTeam(teamName1, context.getViewer());

        // Expect
        // a response of Unauthorized for each
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }





    /**
     * Test assign team member functionality
     *
     * Assign with administrator permissions should return {@link HttpStatus#OK_200}
     */
    @POST
    @Test
    public void canAssignAUserAsTeamMember() throws IOException {
        // Given
        // a team and a random user
        Team team = createTeam(context);
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);

        // When
        // we assign the user
        Response<Boolean> response = postMember(team.name, user.email, context.getAdministrator());

        // Then
        // we expect a response of 200 and the team to have the user in it
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        Team retrieved = getTeam(team.name, context.getAdministrator()).body;
        assertEquals(1, retrieved.members.size());
        assertTrue(user.email.equalsIgnoreCase(retrieved.members.iterator().next()));
    }

    /**
     *
     * Assign with publisher permissions should return {@link HttpStatus#UNAUTHORIZED_401}
     */
    @POST
    @Test
    public void cannotAssignATeamMemberToATeamIfNotAnAdministrator() throws IOException {
        // Given
        // a team and a user
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
        Team team = createTeam(context);

        // When
        // we try to assign using other permission levels
        Response<Boolean> response1 = postMember(team.name, user.email, context.getViewer());
        Response<Boolean> response2 = postMember(team.name, user.email, context.getScallyWag());
        Response<Boolean> response3 = postMember(team.name, user.email, context.getPublisher());

        // Expect
        // a response of Unauthorized 401
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }

    /**
     *
     * Assign with publisher permissions should return {@link HttpStatus#OK_200}
     */
    @POST
    @Test
    public void returnsNotFoundOnAssignTeamMemberToNonexistentTeam() throws IOException {
        // Given
        // a team
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
        String teamName = "Rusty_" + Random.id();

        // When
        // we try to create an identical team
        Response<Boolean> response = postMember(teamName, user.email, context.getAdministrator());

        // Expect
        // a response of Conflict 409
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }



    @DELETE
    /**
     * Test delete team functionality
     *
     * Delete with administrator permissions should return {@link HttpStatus#OK_200}
     */
    @Test
    public void canDeleteATeamAsAdministrator() throws IOException {
        // Given
        // a team
        Team team = createTeam(context);

        // When
        // we delete it with admin privileges then try to retrieve
        Response<Boolean> deleteResponse = deleteTeam(team.name, context.getAdministrator());
        Response<Team> getResponse = getTeam(team.name, context.getAdministrator());

        // Then
        // the delete response to be successful and the get response to respond Not Found
        assertEquals(HttpStatus.OK_200, deleteResponse.statusLine.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND_404, getResponse.statusLine.getStatusCode());
    }

    /**
     *
     * Delete without admin privileges ought to return {@link HttpStatus#UNAUTHORIZED_401}
     */
    @DELETE
    @Test
    public void cannotDeleteATeamIfNotAnAdministrator() throws IOException {
        // Given
        // some teams
        Team team1 = createTeam(context);
        Team team2 = createTeam(context);
        Team team3 = createTeam(context);

        // When
        // We try to delete with alternate privileges
        Response<Boolean> response1 = deleteTeam(team1.name, context.getPublisher());
        Response<Boolean> response2 = deleteTeam(team2.name, context.getViewer());
        Response<Boolean> response3 = deleteTeam(team3.name, context.getScallyWag());

        // Then
        // We expect each to return a failed result
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }

    /**
     *
     * Delete a non existent team ought to return {@link HttpStatus#NOT_FOUND_404}
     */
    @DELETE
    @Test
    public void cannotDeleteATeamWhichDoesntExist() throws IOException {
        // Given
        // a random team name
        String teamName = "Rusty_" + Random.id();

        // When
        // we try to delete the collection
        Response<Boolean> response = deleteTeam(teamName, context.getAdministrator());

        // Then
        // we expect a response of not found
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    @DELETE
    /**
     * Test remove user from a team functionality
     *
     * Remove with administrator permissions should return {@link HttpStatus#OK_200}
     */
    @Test
    public void canRemoveAMemberFromATeamAsAdministrator() throws IOException {
        // Given
        // a team with a user
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
        Team team = createTeam(context);
        postMember(team.name, user.email, context.getAdministrator());

        // When
        // we try to remove them from the team then recall then team
        Response<Boolean> response = deleteTeamMember(team.name, user.email, context.getAdministrator());
        Team teamAfter = getTeam(team.name, context.getAdministrator()).body;

        // Then
        // we expect a response of ok and the team to be without users
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(0, teamAfter.members.size());

    }

    /**
     *
     * Trying to remove without admin privileges ought to return {@link HttpStatus#UNAUTHORIZED_401}
     */
    @DELETE
    @Test
    public void cannotRemoveAMemberFromATeamIfNotAnAdministrator() throws IOException {
        // Given
        // a team with a bunch of members
        Team team = createTeam(context);
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
        postMember(team.name, user.email, context.getAdministrator());

        // When
        // we try to remove this user using alternate logins
        Response<Boolean> response1 = deleteTeamMember(team.name, user.email, context.getPublisher());
        Response<Boolean> response2 = deleteTeamMember(team.name, user.email, context.getViewer());
        Response<Boolean> response3 = deleteTeamMember(team.name, user.email, context.getScallyWag());

        // Then
        // we expect Unauthorized failure each time
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }

    /**
     *
     * Trying to remove from a non-existent team ought to return {@link HttpStatus#NOT_FOUND_404}
     */
    @DELETE
    @Test
    public void cannotRemoveAMemberFromATeamWhichDoesntExist() throws IOException {
        // Given
        // a user and a team name
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
        String teamName = "Rusty_" + Random.id();

        // When
        // we try to remove this member from the team
        Response<Boolean> response = deleteTeamMember(teamName, user.email, context.getAdministrator());

        // Then
        // we should fail with a Not Found error
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    // Helper methods
    public static Response<Boolean> postTeam(String teamName, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.teams.addPathSegment(teamName);
        return http.post(contentEndpoint, "", Boolean.class);
    }

    public static Response<Boolean> postMember(String teamName, String email, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.teams.addPathSegment(teamName).setParameter("email", email);
        return http.post(contentEndpoint, "", Boolean.class);
    }

    public static Response<Team> getTeam(String teamName, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.teams.addPathSegment(teamName);
        return http.get(idUrl, Team.class);
    }

    public static Response<TeamList> getTeamList(Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.teams;
        return http.get(idUrl, TeamList.class);
    }

    public static Response<Boolean> deleteTeam(String teamName, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.teams.addPathSegment(teamName);
        return http.delete(endpoint, Boolean.class);
    }
    public static Response<Boolean> deleteTeamMember(String teamName, String email, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.teams.addPathSegment(teamName).setParameter("email", email);
        return http.delete(endpoint, Boolean.class);
    }

    public static void addMemberToTeam(Http administrator, Team team, User user) throws IOException {
        Response<Boolean> postMemberResponse = Teams.postMember(team.name, user.email, administrator);
        assertEquals(HttpStatus.OK_200, postMemberResponse.statusLine.getStatusCode());
    }

    /**
     * Creates and posts an empty team
     *
     * @return The {@link Team}
     *
     * @throws IOException
     */
    public static Team createTeam(Context context) throws IOException {
        // Post a new team with name
        String teamName = createTeamName();
        Teams.postTeam(teamName, context.getAdministrator());
        // Retrieve the created Team object
        Team team = Teams.getTeam(teamName, context.getAdministrator()).body;
        return team;
    }
    /**
     * Creates and posts a team with members
     *
     * @param numberOfUsers the number of users to add to the team
     *
     * @return The {@link Team}
     *
     * @throws IOException
     */
    public static Team createTeam(Context context, int numberOfUsers) throws IOException {
        // Post a new team with name
        Team team = createTeam(context);
        for(int i = 0; i < numberOfUsers; i++) {
            User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
            Teams.postMember(team.name, user.email, context.getAdministrator());
        }
        return team;
    }


    public static String createTeamName() {
        return "Rusty_" + Random.id().substring(0, 5) + "_team";
    }
}
