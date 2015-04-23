package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
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
@DependsOn(Content.class)
public class Approve {

    Http http = Login.httpPublisher;

    /**
     * Tests approval using simple collection setup and admin credentials
     *
     */
    @POST
    @Test
    public void shouldRespondOkayWhenWeApproveACollectionAsAdmin() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);

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
     */
    @POST
    @Test
    public void shouldHaveApprovedCollectionAfterSuccessfulResponse() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);

        // When
        // we approve it using admin credentials and get an okay
        Response<String> response = approve(collection.name, http);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Expect
        // the collection is now approved
        CollectionDescription collectionDescription = Collection.get(collection.name, http).body;
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
        Http httpNoCredentials = Sessions.get("shouldRespondForbiddenIfCredentialsAreNotProvided");
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);

        // When
        // we approve it without credentials
        Response<String> response = approve(collection.name, httpNoCredentials);

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
        // a session that has no credentials and a collection
        // Given
        // a collection
        CollectionDescription collection1 = Collection.createCollectionDescription();
        CollectionDescription collection2 = Collection.createCollectionDescription();
        CollectionDescription collection3 = Collection.createCollectionDescription();
        Collection.post(collection1, Login.httpPublisher);
        Collection.post(collection2, Login.httpPublisher);
        Collection.post(collection3, Login.httpPublisher);

        // When
        //...we delete it
        Response<String> responseScallywag = approve(collection1.name, Login.httpScallywag);
        Response<String> responseAdministrator = approve(collection1.name, Login.httpAdministrator);
        Response<String> responseViewer = approve(collection1.name, Login.httpViewer);

        // Then
        // delete should fail and the collections should still exist
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
     * Tests that {@link HttpStatus#CONFLICT_409} is returned when the collection has
     * <p/>
     * written
     */
    @POST
    @Test
    public void shouldReturnConflictForCollectionsThatHaveIncompleteItems() throws IOException {
        // Given
        // ...a collection with a file
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);

        String filename = Random.id() + ".json";
        Content.create(collection.name, "shouldReturnConflictForCollectionsThatHaveIncompleteItems", "/approve/" + filename, Login.httpPublisher);

        // When
        // we approve it using admin credentials
        Response<String> response = approve(collection.name, Login.httpPublisher);

        // We expect
        // ...the resource is in progress so the collection will not be approved
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }


    private static Response<String> approve(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.approve.addPathSegment(collectionID);
        return http.post(endpoint, null, String.class);
    }
}
