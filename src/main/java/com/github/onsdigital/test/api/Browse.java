package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.json.CollectionDescription;
import com.github.onsdigital.test.json.DirectoryListing;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Api
@DependsOn({Login.class, Collection.class})
public class Browse {

    /**
     * Test basic functionality
     *
     *
     */
    @GET
    @Test
    public void shouldReturn200WithValidCollectionName() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we call browse
        Response<DirectoryListing> getResponse = browse(collection.id, Login.httpPublisher);

        // Expect
        // a correct call
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), 200);
    }

    /**
     * Expect {@link HttpStatus#BAD_REQUEST_400} if no collection is specified
     *
     */
    @GET
    @Test
    public void shouldReturnBadRequestIfNoPathSpecified() throws IOException {
        // Given
        // n/a

        // When
        // we call get without a collection specified
        Response<DirectoryListing> getResponse = Login.httpPublisher.get(ZebedeeHost.browse, DirectoryListing.class);

        // Expect
        // Response of {@link HttpStatus#BAD_REQUEST_400}
        assertEquals(HttpStatus.BAD_REQUEST_400, getResponse.statusLine.getStatusCode());
    }

    /**
     * Expect {@link HttpStatus#NOT_FOUND_404} if URI does not exist
     *
     */
    @GET
    @Test
    public void shouldReturnNotFoundWithInvalidURI() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we call get
        Response<DirectoryListing> getResponse = browse(collection.id, "/invalidUri", Login.httpPublisher);

        // Expect
        // response of {@link HttpStatus#NOT_FOUND_404}
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), HttpStatus.NOT_FOUND_404);
    }

    /**
     * Expect {@link HttpStatus#BAD_REQUEST_400} if URI does exist but is not a folder
     *
     */
    @GET
    @Test
    public void shouldReturnBadRequestWhenURIisFileNotDirectory() throws IOException {
        // Given
        // a collection with a file in it
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);
        String uri = collection.inProgressUris.get(0);

        // When
        // we try to browse for a file not the directory
        Response<DirectoryListing> getResponse = browse(collection.id, uri, Login.httpPublisher);

        // Then
        // we expect a bad request response
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), HttpStatus.BAD_REQUEST_400);
    }


    /**
     * Expect {@link HttpStatus#UNAUTHORIZED_401} for admin user
     *
     */
    @GET
    //@Test
    public void shouldReturn401ForAdminUser() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we call get
        Response<DirectoryListing> getResponse =  browse(collection.id, Login.httpAdministrator);

        // Expect
        // a correct call
        Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, getResponse.statusLine.getStatusCode());
    }

    /**
     * Expect {@link HttpStatus#UNAUTHORIZED_401} for user with permissions. {@link HttpStatus#UNAUTHORIZED_401} otherwise
     *
     * TODO Update with permissions
     */
    @GET
    @Test
    public void shouldReturn200ForViewerWithPermissionsOtherwise401() throws IOException {

    }

    private static Response<DirectoryListing> browse(String collectionID, String uri, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.browse.addPathSegment(collectionID).setParameter("uri", uri);
        return http.get(endpoint, DirectoryListing.class);
    }
    private static Response<DirectoryListing> browse(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.browse.addPathSegment(collectionID);
        return http.get(endpoint, DirectoryListing.class);
    }
}
