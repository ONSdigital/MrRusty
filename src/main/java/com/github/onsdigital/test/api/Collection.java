package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
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

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@DependsOn(LoginAdmin.class)

public class Collection {

    private static Http http = Sessions.get("admin");

    public Collection() {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());
    }

    @Test
    public void whenCreatingACollection() throws IOException {
        CollectionDescription roundabout = createCollectionDescription();

        // can create a collection
        create(roundabout, 200, http);
        // can't create a collection that already exist
        create(roundabout, 409, http);
        // can't create a collection without a name
        CollectionDescription anon = new CollectionDescription();
        create(anon, 400, http);
    }

    @Test
    public void getCollection() throws IOException {

        CollectionDescription collection = createCollectionDescription();
        CollectionDescription serverCollection = create(collection, 200, http);
        get(serverCollection.name, 200, http);
        assertEquals(collection.name, serverCollection.name);

        //   we can't get a collection that's not there
        get("unknown", 404, http);
    }

    @Test
    public void shouldReturn409IfUpdateNameAlreadyExists() throws IOException {

        // Given
        // an existing collection
        CollectionDescription description = create(http);
        String existingName = description.name;

        // When we try and create a collection with the same name
        // Then we get a 409
        create(description, HttpStatus.CONFLICT_409, http);
    }

    @Test
    public void collectionShouldBeDeleted() throws IOException {
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
