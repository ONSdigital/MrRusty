package com.github.onsdigital.test;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by kanemorgan on 30/03/2015.
 */
@DependsOn(Login.class)
public class Collection {

    static Endpoint collectionEndpoint = new Endpoint(Login.zebedeeHost, "collection");

    @Test
    public void whenCreatingACollection() throws IOException {
        CollectionDescription roundabout = new CollectionDescription();
        roundabout.name = Random.id();
        // TODO This line has been commented out for temporary convenience - to remove when dates fixed
        //roundabout.publishDate = new Date();

        // can create a collection
        create(roundabout, 200);
        // can't create a collection that already exist
        create(roundabout, 409);
        // can't create a collection without a name
        CollectionDescription anon = new CollectionDescription();
        create(anon, 400);

        //TODO This line has been commented out for temporary convenience
        //fail("Spurious");
    }


    @Test
    public void getCollection() throws IOException {

        CollectionDescription collection = new CollectionDescription();
        collection.name = Random.id();
        // TODO This line has been commented out for temporary convenience - to remove when dates fixed
        //collection.publishDate = new Date();

//    we can get the collection we just made
        CollectionDescription serverCollection = create(collection, 200);
        get(serverCollection.name, 200);
        org.junit.Assert.assertEquals(collection.name, serverCollection.name);

//   we can't get a collection that's not there
        get("unknown", 404);

    }

    @Test
    public void collectionCanBeDeleted() throws IOException {
        // Given
        //...a collection
        CollectionDescription collection = create();

        // When
        //...we delete it
        delete(collection.name, 200);

        // We expect
        //...it to be entirely deleted
        get(collection.name, HttpStatus.NOT_FOUND_404);
    }

    private String delete(String name, int expectedResponse) throws IOException
    {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint endpoint = new Endpoint(Login.zebedeeHost, "collection/" + name);
        Response<String> deleteResponse = http.delete(endpoint, String.class);


        assertTrue(deleteResponse.statusLine.getStatusCode() == expectedResponse);

        return deleteResponse.body;
    }

    public static CollectionDescription create(CollectionDescription collection, int expectedResponse) throws IOException {
        Serialiser.getBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Response<String> createResponse = http.post(collectionEndpoint, collection, String.class);


        assertTrue(createResponse.statusLine.getStatusCode() == expectedResponse);
        return collection;
    }


    public static CollectionDescription create(int expectedResponse) throws IOException {

        CollectionDescription collection = new CollectionDescription();
        collection.name = Random.id();

        // TODO This line has been commented out for temporary convenience - to remove when dates fixed
        //collection.publishDate = new Date();
        return create(collection, expectedResponse);
    }

    public static CollectionDescription create() throws IOException {
        return create(200);
    }

    private CollectionDescription get(String name, int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint idUrl = new Endpoint(Login.zebedeeHost, "collection/" + name);
        Response<CollectionDescription> getResponse = http.get(idUrl, CollectionDescription.class);


        assertTrue(getResponse.statusLine.getStatusCode() == expectedResponse);

        return getResponse.body;
    }



}
