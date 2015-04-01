package com.github.onsdigital.test;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.framework.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import junit.framework.Assert;
import org.junit.Test;
import sun.org.mozilla.javascript.internal.Function;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Created by kanemorgan on 30/03/2015.
 */
@DependsOn(Login.class)
public class Collection {

    static Endpoint collectionEndpoint = new Endpoint(Login.zebedeeHost, "collection");

    @Test
    public static void whenCreatingACollection() throws IOException {
        CollectionDescription roundabout = new CollectionDescription();
        roundabout.name = Random.id();
        roundabout.publishDate = new Date();

        // can create a collection
        create(roundabout, 200);
        // can't create a collection that already exist
        create(roundabout,409);
        // can't create a collection without a name
        CollectionDescription anon = new CollectionDescription();
        create(anon,400);
    }

    public static CollectionDescription create(CollectionDescription collection,int expectedResponse) throws IOException {
        Serialiser.getBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Http http = new Http();
        http.addHeader("X-Florence-Token",Login.florenceToken);
        Response<String> createResponse = http.post(collectionEndpoint, collection, String.class);
        assertTrue(createResponse.statusLine.getStatusCode() == expectedResponse);
        return collection;
    }


    public static CollectionDescription create(int expectedResponse) throws IOException {

        CollectionDescription collection = new CollectionDescription();
        collection.name = Random.id();
        collection.publishDate = new Date();
        return create(collection,expectedResponse);
    }

    public static CollectionDescription create() throws IOException {
        return create(200);
    }


    @Test
    public static void getCollection() throws IOException {

        CollectionDescription collection = new CollectionDescription();
        collection.name = Random.id();
        collection.publishDate = new Date();

//    we can get the collection we just made
        CollectionDescription serverCollection = create(collection,200);
        get(serverCollection.name,200);
        org.junit.Assert.assertEquals(collection.name,serverCollection.name);

//   we can't get a collection that's not there
        get("unknown",404);


    }

    private static CollectionDescription get(String name,int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token",Login.florenceToken);
        Endpoint idUrl = new Endpoint( Login.zebedeeHost,"collection/"+name);
        Response<CollectionDescription> getResponse = http.get(idUrl,CollectionDescription.class);

        assertTrue(getResponse.statusLine.getStatusCode() == expectedResponse);

        return getResponse.body;
    }






}
