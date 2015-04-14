package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by kanemorgan on 30/03/2015.
 */
@DependsOn(Login.class)
public class Collection {

    private static Http http = Sessions.get("admin");


    @Test
    public void whenCreatingACollection() throws IOException {
        CollectionDescription roundabout = new CollectionDescription();
        roundabout.name = Random.id();
        // TODO This line has been commented out for temporary convenience - to remove when dates fixed
        //roundabout.publishDate = new Date();

        // can create a collection
        create(roundabout, 200, http);
        // can't create a collection that already exist
        create(roundabout, 409, http);
        // can't create a collection without a name
        CollectionDescription anon = new CollectionDescription();
        create(anon, 400, http);

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
        CollectionDescription serverCollection = create(collection, 200, http);
        get(serverCollection.name, 200);
        org.junit.Assert.assertEquals(collection.name, serverCollection.name);

//   we can't get a collection that's not there
        get("unknown", 404);

    }

    @Test
    public void collectionCanBeDeleted() throws IOException {
        // Given
        //...a collection
        CollectionDescription collection = create(http);

        // When
        //...we delete it
        delete(collection.name, 200);

        // We expect
        //...it to be entirely deleted
        get(collection.name, HttpStatus.NOT_FOUND_404);
    }

    private String delete(String name, int expectedResponse) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        Response<String> deleteResponse = http.delete(endpoint, String.class);


        assertTrue(deleteResponse.statusLine.getStatusCode() == expectedResponse);

        return deleteResponse.body;
    }

    public static CollectionDescription create(CollectionDescription collection, int expectedResponse, Http http) throws IOException {
        Serialiser.getBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Response<String> createResponse = http.post(ZebedeeHost.collection, collection, String.class);


        assertTrue(createResponse.statusLine.getStatusCode() == expectedResponse);
        return collection;
    }


    public static CollectionDescription create(int expectedResponse, Http http) throws IOException {

        CollectionDescription collection = new CollectionDescription();
        collection.name = Random.id();

        // TODO This line has been commented out for temporary convenience - to remove when dates fixed
        //collection.publishDate = new Date();
        return create(collection, expectedResponse, http);
    }

    public static CollectionDescription create(Http http) throws IOException {
        return create(200, http);
    }

    private CollectionDescription get(String name, int expectedResponse) throws IOException {
        Endpoint idUrl = ZebedeeHost.collection.addPathSegment(name);
        Response<CollectionDescription> getResponse = http.get(idUrl, CollectionDescription.class);


        assertTrue(getResponse.statusLine.getStatusCode() == expectedResponse);

        return getResponse.body;
    }


}
