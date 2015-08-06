package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
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
public class Teams {

    /**
     * Test basic GET functionality
     *
     * Get team should return a team {@link HttpStatus#OK_200}
     *
     * Required for OneLineSetups.newTeam()
     */
    @GET
    @Test
    public void canGetATeam() throws IOException {
        // Given
        // a team we have posted
        String teamName = "Rusty_" + Random.id();
        postTeam(teamName, Login.httpAdministrator);

        // When
        // we get the team
        Response<Team> response = getTeam(teamName, Login.httpAdministrator);
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
     * Required for OneLineSetups.newTeam()
     */
    @GET
    @Test
    public void canGetTeamList() throws IOException {
        // Given
        // a few random teams we have posted
        Team[] teams = {OneLineSetups.newTeam(),
                OneLineSetups.newTeam(),
                OneLineSetups.newTeam()};

        // When
        // we get the teams
        Response<TeamList> response = getTeamList(Login.httpAdministrator);
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
        Response<Team> response = getTeam(teamName, Login.httpAdministrator);

        // Expect
        // a response of 404
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }





    /**
     * Test basic create team functionality
     *
     * Create team with admin permissions should return {@link HttpStatus#OK_200}
     *
     * Required for OneLineSetups.newTeam()
     */
    @POST
    @Test
    public void canCreateATeamAsAdministrator() throws IOException {
        // Given
        String teamName = "Rusty_" + Random.id();

        // When
        // we post as an administrator
        Response<Boolean> response = postTeam(teamName, Login.httpAdministrator);

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
        Team team = OneLineSetups.newTeam();

        // When
        // we try to create an identical team
        Response<Boolean> response = postTeam(team.name, Login.httpAdministrator);

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
        Response<Boolean> response1 = postTeam(teamName1, Login.httpPublisher);
        Response<Boolean> response2 = postTeam(teamName1, Login.httpScallywag);
        Response<Boolean> response3 = postTeam(teamName1, Login.httpViewer);

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
        Team team = OneLineSetups.newTeam();
        User user = OneLineSetups.newActiveUserWithViewerPermissions();

        // When
        // we assign the user
        Response<Boolean> response = postMember(team.name, user.email, Login.httpAdministrator);

        // Then
        // we expect a response of 200 and the team to have the user in it
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        Team retrieved = getTeam(team.name, Login.httpAdministrator).body;
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
        User user = OneLineSetups.newActiveUserWithViewerPermissions();
        Team team = OneLineSetups.newTeam();

        // When
        // we try to assign using other permission levels
        Response<Boolean> response1 = postMember(team.name, user.email, Login.httpViewer);
        Response<Boolean> response2 = postMember(team.name, user.email, Login.httpScallywag);
        Response<Boolean> response3 = postMember(team.name, user.email, Login.httpPublisher);

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
        User user = OneLineSetups.newActiveUserWithViewerPermissions();
        String teamName = "Rusty_" + Random.id();

        // When
        // we try to create an identical team
        Response<Boolean> response = postMember(teamName, user.email, Login.httpAdministrator);

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
        Team team = OneLineSetups.newTeam();

        // When
        // we delete it with admin privileges then try to retrieve
        Response<Boolean> deleteResponse = deleteTeam(team.name, Login.httpAdministrator);
        Response<Team> getResponse = getTeam(team.name, Login.httpAdministrator);

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
        Team team1 = OneLineSetups.newTeam();
        Team team2 = OneLineSetups.newTeam();
        Team team3 = OneLineSetups.newTeam();

        // When
        // We try to delete with alternate privileges
        Response<Boolean> response1 = deleteTeam(team1.name, Login.httpPublisher);
        Response<Boolean> response2 = deleteTeam(team2.name, Login.httpViewer);
        Response<Boolean> response3 = deleteTeam(team3.name, Login.httpScallywag);

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
        Response<Boolean> response = deleteTeam(teamName, Login.httpAdministrator);

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
        User user = OneLineSetups.newActiveUserWithViewerPermissions();
        Team team = OneLineSetups.newTeam();
        postMember(team.name, user.email, Login.httpAdministrator);

        // When
        // we try to remove them from the team then recall then team
        Response<Boolean> response = deleteTeamMember(team.name, user.email, Login.httpAdministrator);
        Team teamAfter = getTeam(team.name, Login.httpAdministrator).body;

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
        Team team = OneLineSetups.newTeam();
        User user = OneLineSetups.newActiveUserWithViewerPermissions();
        postMember(team.name, user.email, Login.httpAdministrator);

        // When
        // we try to remove this user using alternate logins
        Response<Boolean> response1 = deleteTeamMember(team.name, user.email, Login.httpPublisher);
        Response<Boolean> response2 = deleteTeamMember(team.name, user.email, Login.httpViewer);
        Response<Boolean> response3 = deleteTeamMember(team.name, user.email, Login.httpScallywag);

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
        User user = OneLineSetups.newActiveUserWithViewerPermissions();
        String teamName = "Rusty_" + Random.id();

        // When
        // we try to remove this member from the team
        Response<Boolean> response = deleteTeamMember(teamName, user.email, Login.httpAdministrator);

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
}
