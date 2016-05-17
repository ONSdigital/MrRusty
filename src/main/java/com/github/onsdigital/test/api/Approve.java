package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by kanemorgan on 02/04/2015.
 */

@Api
@DependsOn(com.github.onsdigital.test.api.Content.class)
public class Approve  extends ZebedeeApiTest {

    private final Http publisher = context.getPublisher();

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     */
    @POST
    @Test
    public void shouldRespondOkayWhenWeApproveACollectionAsPublisher() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        // When
        // we approve it using admin credentials
        Response<String> response = approve(collection.id, publisher);

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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        // When
        // we approve it using admin credentials and get an okay
        Response<String> response = approve(collection.id, publisher);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Expect
        // the collection is now approved
        CollectionDescription collectionDescription = Collection.get(collection.id, publisher).body;
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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

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
        CollectionDescription collection1 = OneLineSetups.publishedCollection(context.getPublisher());
        CollectionDescription collection3 = OneLineSetups.publishedCollection(context.getPublisher());

        // When
        //...we approve it with non publisher credentials
        Response<String> responseScallywag = approve(collection1.id, context.getScallyWag());
        Response<String> responseViewer = approve(collection3.id, context.getViewer());

        // Then
        // approve should fail and return Unauthorised
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseScallywag.statusLine.getStatusCode());
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
        Response<String> response = approve(randomCollectionName, publisher);

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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 1);

        // When
        // we try to approve it using appropriate credentials
        Response<String> response = approve(collection.id, publisher);

        // We expect
        // the resource is in progress so the collection will not be approved
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }

    public static Response<String> approve(String collectionID, Http http) throws IOException {
        return approve(collectionID, http, 30);
    }

    public static Response<String> approve(String collectionID, Http http, int secondsToWait) throws IOException {
        Endpoint endpoint = ZebedeeHost.approve.addPathSegment(collectionID);
        Response<String> response = http.post(endpoint, null, String.class);

        // if the status is not OK then just return as the approval has failed.
        if (response.statusLine.getStatusCode() != org.apache.http.HttpStatus.SC_OK) {
            return response;
        }

        int count = 0;
        boolean approved = false;

        while (count < secondsToWait) {
            Response<CollectionDescription> getCollectionResponse = Collection.get(collectionID, context.getPublisher());
            System.out.println("getCollectionResponse.body.approvedStatus = " + getCollectionResponse.body.approvedStatus);
            if (getCollectionResponse.body.approvedStatus) {
                approved = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            count++;
        }

        if (!approved)
            fail("Collection was not approved within " + secondsToWait + " seconds.");
        
        return response;
    }

}
