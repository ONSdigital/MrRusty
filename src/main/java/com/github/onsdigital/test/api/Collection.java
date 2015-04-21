package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.serialiser.IsoDateSerializer;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@Api
@DependsOn(LoginAdmin.class)
public class Collection {

    private static Http http = Sessions.get("admin");

    public Collection() {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());
    }


    /**
     * Test basic functionality
     *
     * Create with publisher permissions should return {@link HttpStatus#OK_200}
     *
     * TODO - Fix specific publisher permissions
     */
    @POST
    @Test
    public void canCreateCollectionWithPublisherPermissions() throws IOException {
        // Given
        // a collection description
        CollectionDescription roundabout = createCollectionDescription();

        // When
        // we post as a publisher
        create(roundabout, 200, http);

        // Expect
        // a response of 200
    }
    /**
     * Creating an unnamed collection should return {@link HttpStatus#BAD_REQUEST_400}
     *
     * TODO - Fix specific publisher permissions
     */
    @POST
    @Test
    public void shouldReturn400IfNoNameSpecifiedForCreateCollection() throws IOException {
        // Given an incomplete collection description
        CollectionDescription anon = new CollectionDescription();

        // When we work
        create(anon, 400, http);
    }
    /**
     * written
     */
    @POST
    @Test
    public void shouldReturn409IfCollectionNameAlreadyExists() throws IOException {

        // Given
        // an existing collection
        CollectionDescription description = create(http);
        String existingName = description.name;

        // When we try and create a collection with the same name
        // Then we get a 409
        create(description, HttpStatus.CONFLICT_409, http);
    }


    /**
     * Create without publisher permissions should return {@link HttpStatus#UNAUTHORIZED_401}
     *
     * TODO
     */
    @POST
    @Test
    public void shouldReturn401WithoutPublisherPermissions() throws IOException {

    }


    /**
     * Viewer permissions should return {@link HttpStatus#OK_200} for any permitted collection, {@link HttpStatus#UNAUTHORIZED_401} otherwise
     *
     * TODO
     */
    @GET
    @Test
    public void shouldReturn200ForViewerWithPermissionsOtherwise401() throws IOException {

    }
    /**
     * Admins should return {@link HttpStatus#UNAUTHORIZED_401}
     *
     * TODO
     */
    @GET
    @Test
    public void shouldReturn401WithAdminPermissions() throws IOException {

    }

    /**
     * Publisher permissions should return {@link HttpStatus#OK_200} for any collection
     *
     * TODO - Fix specific publisher permissions
     */
    @DELETE
    @Test
    public void collectionShouldBeDeletedWithPublisherPermissions() throws IOException {
        // Given
        //...a collection
        CollectionDescription collection = create(http);

        // When
        //...we delete it
        delete(collection.name, 200, http);

        // We expect
        //...it to be entirely deleted
        get(collection.name, HttpStatus.NOT_FOUND_404, http);
    }

    /**
     * All other permissions should return {@link HttpStatus#UNAUTHORIZED_401} for any collection
     *
     * TODO - Fix permissions
     */
    @DELETE
    @Test
    public void deleteShouldReturn400WithoutPublisherPermissions() throws IOException {

    }











    private String delete(String name, int expectedResponse, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        Response<String> deleteResponse = http.delete(endpoint, String.class);
        assertEquals(expectedResponse, deleteResponse.statusLine.getStatusCode());
        return deleteResponse.body;
    }



    public static CollectionDescription create(CollectionDescription collection, int expectedResponse, Http http) throws IOException {
        Response<String> createResponse = http.post(ZebedeeHost.collection, collection, String.class);
        assertEquals(expectedResponse, createResponse.statusLine.getStatusCode());
        return collection;
    }

    public static CollectionDescription create(int expectedResponse, Http http) throws IOException {
        CollectionDescription collectionDescription = createCollectionDescription();
        return create(collectionDescription, expectedResponse, http);
    }


    public static CollectionDescription createCollectionDescription() {
        CollectionDescription collection = new CollectionDescription();
        collection.name = Random.id();
        collection.publishDate = new Date();
        return collection;
    }
    public static CollectionDescription create(Http http) throws IOException {
        return create(200, http);
    }

    public static CollectionDescription get(String name, int expectedResponse, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.collection.addPathSegment(name);
        Response<CollectionDescription> getResponse = http.get(idUrl, CollectionDescription.class);
        assertEquals(expectedResponse, getResponse.statusLine.getStatusCode());
        return getResponse.body;
    }

    public static CollectionDescription get(String name, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.collection.addPathSegment(name);
        Response<CollectionDescription> getResponse = http.get(idUrl, CollectionDescription.class);
        return getResponse.body;
    }
}
