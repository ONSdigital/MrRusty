package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kanemorgan on 02/04/2015.
 */

@Api
@DependsOn(com.github.onsdigital.test.api.Content.class)
public class Approve {

    Http http = Login.httpPublisher;

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     */
    @POST
    @Test
    public void shouldRespondOkayWhenWeApproveACollectionAsPublisher() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we approve it using admin credentials
        Response<String> response = approve(collection.id, Login.httpPublisher);

        // Expect
        // a response of okay
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Tests functionality of a successful call
     *
     */
    @POST
    @Test
    public void shouldHaveApprovedCollectionAfterSuccessfulResponse() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we approve it using admin credentials and get an okay
        Response<String> response = approve(collection.id, Login.httpPublisher);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Expect
        // the collection is now approved
        CollectionDescription collectionDescription = Collection.get(collection.id, Login.httpPublisher).body;
        assertEquals(true, collectionDescription.approvedStatus);
    }

    /**
     * Tests that {@link HttpStatus#FORBIDDEN_403} is when credentials not provided
     *
     */
    @POST
    @Test
    public void shouldRespondUnauthorizedIfCredentialsAreNotProvided() throws IOException {
        // Given
        // a session that has no credentials and a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we approve it without credentials
        Http httpNoCredentials = Sessions.get("noCredentials");
        Response<String> response = approve(collection.id, httpNoCredentials);

        // Expect
        // a response of forbidden
        assertEquals(HttpStatus.UNAUTHORIZED_401, response.statusLine.getStatusCode());
    }

    /**
     * Tests that {@link HttpStatus#UNAUTHORIZED_401} is returned when user doesn't have approve permission
     *
     */
    @POST
    @Test
    public void shouldRespondUnauthorizedIfPermissionsDoNotAllowApproval() throws IOException {
        // Given
        // a collection
        CollectionDescription collection1 = OneLineSetups.publishedCollection();
        CollectionDescription collection2 = OneLineSetups.publishedCollection();
        CollectionDescription collection3 = OneLineSetups.publishedCollection();

        // When
        //...we approve it with non publisher credentials
        Response<String> responseScallywag = approve(collection1.id, Login.httpScallywag);
        Response<String> responseAdministrator = approve(collection2.id, Login.httpAdministrator);
        Response<String> responseViewer = approve(collection3.id, Login.httpViewer);

        // Then
        // approve should fail and return Unauthorised
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseAdministrator.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseViewer.statusLine.getStatusCode());
    }

    /**
     * Tests that {@link HttpStatus#BAD_REQUEST_400} is returned when the collection doesn't exist
     *
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
     * Tests that {@link HttpStatus#CONFLICT_409} is returned when the collection has incomplete content
     */
    @POST
    @Test
    public void shouldReturnConflictForCollectionsThatHaveIncompleteItems() throws IOException {
        // Given
        // ...a collection with a file
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);

        // When
        // we try to approve it using appropriate credentials
        Response<String> response = approve(collection.id, Login.httpPublisher);

        // We expect
        // the resource is in progress so the collection will not be approved
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }


    public static Response<String> approve(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.approve.addPathSegment(collectionID);
        return http.post(endpoint, null, String.class);
    }
}
