package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.apache.http.protocol.HTTP;
import org.junit.Assert;
import org.junit.Test;

import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kanemorgan on 02/04/2015.
 */

@Api
@DependsOn(Content.class)
public class Approve {

    Http http = Sessions.get("admin");

    /**
     * Tests approval using simple collection setup and admin credentials
     *
     * written
     */
    @POST
    @Test
    public void shouldRespondOkayWhenWeApproveACollectionAsAdmin() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = Collection.create(http);

        // When
        // we approve it using admin credentials
        Response<String> response = approve(collection.name, http);

        // Expect
        // a response of okay
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Tests functionality of a successful call
     *
     * incomplete
     */
    @POST
    @Test
    public void shouldHaveApprovedCollectionAfterSuccessfulResponse() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = Collection.create(http);

        // When
        // we approve it using admin credentials and get an okay
        Response<String> response = approve(collection.name, http);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Expect
        // the collection is now approved
        // TODO: Code that tests whether a collection has been approved

    }

    /**
     * Tests that {@link HttpStatus#BAD_REQUEST_400} is when credentials not provided
     *
     * complete
     */
    @POST
    @Test
    public void shouldRespondBadRequestIfCredentialsAreNotProvided() throws IOException {
        // Given
        // a session that has no credentials and a collection
        Http httpNoCredentials = Sessions.get("shouldRespondBadRequestIfCredentialsAreNotProvided");
        CollectionDescription collection = Collection.create(http);

        // When
        // we approve it without credentials
        Response<String> response = approve(collection.name, httpNoCredentials);

        // Expect
        // a response of okay
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    /**
     * Tests that {@link HttpStatus#UNAUTHORIZED_401} is returned when user doesn't have approve permission
     *
     * May be split into separate tests based on levels
     *
     * incomplete
     */
    @POST
    @Test
    public void shouldRespondUnauthorizedIfPermissionsDoNotAllowApproval() {

    }

    /**
     * Tests that {@link HttpStatus#BAD_REQUEST_400} is returned when the collection doesn't exist
     *
     * written
     */
    @POST
    @Test
    public void shouldRespondBadRequestIfCollectionDoesntExist() throws IOException {
        // Given
        // a non-existent collection
        String randomCollectionName = Random.id();

        // When
        // we approve it using admin credentials
        Response<String> response = approve(randomCollectionName, http);

        // Expect
        // a bad request response
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }


    /**
     * Tests that {@link HttpStatus#CONFLICT_409} is returned when the collection has
     *
     * written
     */
    @POST
    @Test
    public void shouldReturnConflictForCollectionsThatHaveIncompleteItems() throws IOException {
        // Given
        // ...a collection with a file
        CollectionDescription collection = Collection.create(http);

        String filename = Random.id() + ".json";
        Content.create(collection.name, "shouldReturnConflictForCollectionsThatHaveIncompleteItems", "/approve/" + filename, 200, http);

        // When
        // we approve it using admin credentials
        Response<String> response = approve(collection.name, http);

        // We expect
        // ...the resource is in progress so the collection will not be approved
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }


    private static Response<String> approve(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.approve.addPathSegment(collectionID);
        return http.post(endpoint, null, String.class);
    }
}
