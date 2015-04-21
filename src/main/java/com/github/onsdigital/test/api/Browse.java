package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.DirectoryListing;
import org.junit.Assert;
import org.junit.Test;
import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.GET;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Api
@DependsOn({Login.class, Collection.class})
public class Browse {

    private static Http http = Login.httpPublisher;
    private static Endpoint browseEndpoint = ZebedeeHost.browse;

    /**
     * Test basic functionality
     *
     * TODO Update with permissions
     */
    @GET
    @Test
    public void shouldReturn200WithValidCollectionName() throws IOException {
        // Given
        // admin login XXXXXXXX & a collection
        CollectionDescription collection = Collection.create(http);

        // When
        // we call get
        Response<DirectoryListing> getResponse = http.get(browseEndpoint.addPathSegment(collection.name), DirectoryListing.class);

        // Expect
        // a correct call
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), 200);
    }

    /**
     * Expect {@link HttpStatus#NOT_FOUND_404} if no collection is specified
     *
     * TODO Update with permissions
     */
    @GET
    @Test
    public void shouldReturn404IfNoPathSpecified() throws IOException {
        // Given
        // admin login XXXXXXX

        // When
        // we call get without a collection specified
        Response<DirectoryListing> getResponse = http.get(browseEndpoint, DirectoryListing.class);

        // Expect
        // Response of {@link HttpStatus#NOT_FOUND_404}
        assertEquals(getResponse.statusLine.getStatusCode(), HttpStatus.NOT_FOUND_404);
    }

    /**
     * Expect {@link HttpStatus#NOT_FOUND_404} if URI does not exist
     *
     * TODO Update with permissions
     */
    @GET
    @Test
    public void shouldReturn404WithInvalidURI() throws IOException {
        // Given
        // admin login XXXXXXXX & a collection
        CollectionDescription collection = Collection.create(http);

        // When
        // we call get
        Response<DirectoryListing> getResponse = http.get(browseEndpoint.addPathSegment(collection.name).setParameter("uri", "/shouldReturn404WithInvalidURI"), DirectoryListing.class);

        // Expect
        // a correct call
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), HttpStatus.NOT_FOUND_404);
    }

    /**
     * Expect {@link HttpStatus#BAD_REQUEST_400} if URI does exist but is not a folder
     *
     * TODO Update with publisher permissions
     */
    @GET
    @Test
    public void shouldReturn400WhenURIisFileNotDirectory() throws IOException {
        // Given
        // admin login XXXXXXXX & a collection with a file in it
        CollectionDescription collection = Collection.create(http);
        String uri = "/shouldReturn400WhenURIisFileNotDirectory/" + Random.id() + ".json";
        Content.create(collection.name,"shouldReturn400WhenURIisFileNotDirectory",uri, Login.httpPublisher);

        // When
        // we call get
        Response<DirectoryListing> getResponse = http.get(browseEndpoint.addPathSegment(collection.name).setParameter("uri", uri), DirectoryListing.class);

        // Expect
        // a bad call
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), HttpStatus.BAD_REQUEST_400);
    }


    /**
     * Expect {@link HttpStatus#UNAUTHORIZED_401} for admin user
     *
     * TODO Update with permissions
     */
    @GET
    @Test
    public void shouldReturn401ForAdminUser() throws IOException {
        // Given
        // admin login XXXXXXXX & a collection
        CollectionDescription collection = Collection.create(http);

        // When
        // we call get
        Response<DirectoryListing> getResponse = http.get(browseEndpoint.addPathSegment(collection.name), DirectoryListing.class);

        // Expect
        // a correct call
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), 200);
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
}
