package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.json.CollectionDescription;
import com.github.onsdigital.test.json.serialiser.IsoDateSerializer;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Api
@DependsOn(com.github.onsdigital.test.api.Permissions.class)
public class Collection {

    private static Http http = Login.httpAdministrator;

    public Collection() {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());
    }

    /**
     * Test basic functionality
     * <p/>
     * Create with publisher permissions should return {@link HttpStatus#OK_200}
     */
    @POST
    @Test
    public void canCreateCollectionWithPublisherPermissions() throws IOException {
        // Given
        // a collection description
        CollectionDescription roundabout = createCollectionDescription();

        // When
        // we post as a publisher
        Response<CollectionDescription> response = post(roundabout, Login.httpPublisher);

        // Expect
        // a response of 200 - success
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(roundabout.name, response.body.name);
        assertEquals(roundabout.publishDate, response.body.publishDate);
        assertTrue(StringUtils.isNotBlank(response.body.id));
    }

    /**
     * Creating an unnamed collection should return {@link HttpStatus#BAD_REQUEST_400}
     */
    @POST
    @Test
    public void postShouldReturn400IfNoNameSpecifiedForCreateCollection() throws IOException {
        // Given
        // an incomplete collection description
        CollectionDescription anon = new CollectionDescription();

        // When
        // we post using valid credentials
        Response<CollectionDescription> response = post(anon, Login.httpPublisher);

        // Expect
        // a response of 400 - Bad request
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    /**
     * written
     */
    @POST
    @Test
    public void postShouldReturn409IfCollectionNameAlreadyExists() throws IOException {

        // Given
        // an existing collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we try and create an identical collection
        Response<CollectionDescription> response = post(collection, Login.httpPublisher);

        // Expect
        // a reponse of 409 - Conflict
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }


    /**
     * Create without publisher permissions should return {@link HttpStatus#UNAUTHORIZED_401}
     */
    @POST
    @Test
    public void postShouldReturn401WithoutPublisherPermissions() throws IOException {

        // Given
        // a collection description
        CollectionDescription collection = createCollectionDescription();

        // When
        // we post as anyone but a publisher
        Response<CollectionDescription> responseAdmin = post(collection, Login.httpAdministrator);
        Response<CollectionDescription> responseScallywag = post(collection, Login.httpScallywag);
        Response<CollectionDescription> responseViewer = post(collection, Login.httpViewer);

        // Expect
        // a response of 401 - unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseAdmin.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseViewer.statusLine.getStatusCode());
    }

    /**
     * Viewer permissions should return {@link HttpStatus#OK_200} for any permitted collection, {@link HttpStatus#UNAUTHORIZED_401} otherwise
     */
    @GET
    @Test
    public void getShouldReturn200ForViewerWithPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = createCollectionDescription();
        collection = post(collection, Login.httpPublisher).body;

        // When
        // we attempt to retrieve it as an publisher
        Response<CollectionDescription> response = get(collection.id, Login.httpPublisher);

        // We expect
        // a response of 200
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Admins should return {@link HttpStatus#UNAUTHORIZED_401}
     */
    @GET
    @Test
    public void getShouldReturn401WithAdminPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = createCollectionDescription();
        collection = post(collection, Login.httpPublisher).body;

        // When
        // we attempt to retrieve it as an administrator
        Response<CollectionDescription> response = get(collection.id, Login.httpAdministrator);

        // We expect
        // a response of 401 - unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED_401, response.statusLine.getStatusCode());
    }

    /**
     * Publisher permissions should return {@link HttpStatus#OK_200} for any collection
     */
    @DELETE
    @Test
    public void collectionShouldBeDeletedWithPublisherPermissions() throws IOException {
        // Given
        //...a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        //...we delete it
        delete(collection.id, Login.httpPublisher);

        // We expect
        //...it to be entirely deleted
        Response<CollectionDescription> response = get(collection.id, Login.httpPublisher);
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    /**
     * All other permissions should return {@link HttpStatus#UNAUTHORIZED_401} for any collection
     */
    @DELETE
    @Test
    public void deleteShouldReturn401WithoutPublisherPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection1 = OneLineSetups.publishedCollection();
        CollectionDescription collection2 = OneLineSetups.publishedCollection();
        CollectionDescription collection3 = OneLineSetups.publishedCollection();

        // When
        //...we we try and delete them delete it
        Response<String> deleteResponseScallywag = delete(collection1.id, Login.httpScallywag);
        Response<String> deleteResponseAdministrator = delete(collection2.id, Login.httpAdministrator);
        Response<String> deleteResponseViewer = delete(collection3.id, Login.httpViewer);

        // Then
        // delete should fail with unauthorized returned
        // + the collections should still exist
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseAdministrator.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseViewer.statusLine.getStatusCode());

        assertEquals(HttpStatus.OK_200, get(collection1.id, Login.httpPublisher).statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, get(collection2.id, Login.httpPublisher).statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, get(collection3.id, Login.httpPublisher).statusLine.getStatusCode());
    }

    public static Response<String> delete(String name, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        return http.delete(endpoint, String.class);
    }

    public static Response<CollectionDescription> post(CollectionDescription collection, Http http) throws IOException {
        return http.post(ZebedeeHost.collection, collection, CollectionDescription.class);
    }

    public static CollectionDescription createCollectionDescription() {
        CollectionDescription collection = new CollectionDescription();
        collection.name = createCollectionNameForTest();
        collection.publishDate = new Date();
        return collection;
    }

    public static String createCollectionNameForTest() {
        return "Rusty_" + Random.id().substring(0,10);
    }

    public static Response<CollectionDescription> get(String id, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.collection.addPathSegment(id);
        return http.get(idUrl, CollectionDescription.class);
    }
}
