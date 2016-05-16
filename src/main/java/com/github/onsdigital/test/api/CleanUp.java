package com.github.onsdigital.test.api;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.json.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class CleanUp {
    /**
     * Run the cleanup
     *
     */
    public static void cleanUpAllCollectionsBeginningWithRusty(Context context) throws IOException {

        // We get the list of collections
        Endpoint endpoint = ZebedeeHost.collections;
        Response<CollectionDescriptions> getResponse = context.getPublisher().get(endpoint, CollectionDescriptions.class);

        if (getResponse == null) return;

        CollectionDescriptions descriptions = getResponse.body;

        if (descriptions == null) return;

        // Send a delete request for all those that begin with Rusty
        for(CollectionDescription description: descriptions) {
            if (description.name.startsWith("Rusty")) {
                CollectionDescription fullDescription = Collection.get(description.id, context.getPublisher()).body;

                if(fullDescription.inProgressUris != null) {
                    for (String uri : fullDescription.inProgressUris) {
                        deleteContent(fullDescription.id, uri, context.getPublisher());
                    }
                }

                if(fullDescription.completeUris != null) {
                    for (String uri : fullDescription.completeUris) {
                        deleteContent(fullDescription.id, uri, context.getPublisher());
                    }
                }

                if(fullDescription.reviewedUris != null) {
                    for (String uri : fullDescription.reviewedUris) {
                        deleteContent(fullDescription.id, uri, context.getPublisher());
                    }
                }
                deleteCollection(fullDescription.id, context.getPublisher());
            }
        }
    }

    public static void cleanUpAllUsersBeginningWithRusty(Context context) throws IOException {

        // We get the list of collections
        Endpoint usersEndpoint = ZebedeeHost.users;
        Response<UserList> getResponse = context.getAdministrator().get(usersEndpoint, UserList.class);

        if (getResponse == null) {
            return;
        }

        UserList userList = getResponse.body;


        // Send a delete request for all those that begin with Rusty
        for(User user: userList) {
            if (StringUtils.startsWithIgnoreCase(user.email, "rusty_")) {
                System.out.println("Deleting user " + user.name + " with email " + user.email);
                context.getAdministrator().delete(usersEndpoint.setParameter("email", user.email), String.class);
            }
        }
    }

    public static void cleanUpAllTeamsBeginningWithRusty(Context context) throws IOException {
        Response<TeamList> teamListResponse = Teams.getTeamList(context.getAdministrator());

        if (teamListResponse == null) {
            return;
        }

        TeamList teamList = teamListResponse.body;


        // Send a delete request for all those that begin with Rusty
        for(Team team: teamList.teams) {
            if (StringUtils.startsWithIgnoreCase(team.name, "rusty_")) {
                System.out.println("Deleting team " + team.name);
                Teams.deleteTeam(team.name, context.getAdministrator());
            }
        }
    }

    static Response<String> deleteCollection(String name, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        return http.delete(endpoint, String.class);
    }
    static Response<String> deleteContent(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);

        return http.delete(contentEndpoint, String.class);
    }
}
