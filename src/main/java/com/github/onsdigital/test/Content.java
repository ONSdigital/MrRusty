package com.github.onsdigital.test;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.framework.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kanemorgan on 31/03/2015.
 */

@DependsOn(Collection.class)
public class Content {
    //TODO
    @Test
    public void createSpec() throws IOException {
        CollectionDescription thing1 = new CollectionDescription();
        thing1.name = Random.id();
        Collection.create(thing1, 200);

        String fileName = Random.id() + ".json";

        create(thing1.name, "{name:foo}", Random.id() + ".json", 200);
    }

    @Test
    public void filesOnlyEditableInOneCollection() throws IOException {
        CollectionDescription collection_1 = Collection.create();
        CollectionDescription collection_2 = Collection.create();

        String fileURI = Random.id() + ".json";

        // given the file exists in one collection
        create(collection_1.name, "content", fileURI, 200);

        // we can't create it in another collection
        create(collection_2.name, "content", fileURI, 409);
    }

    @Test
    public void shouldUpdateContent() throws IOException {
        CollectionDescription collection_1 = Collection.create();
        String fileUri = Random.id() + ".json";

        create(collection_1.name, "content", fileUri, 200);
        create(collection_1.name, "new content", fileUri, 200);

        String serverResponse = get(collection_1.name, fileUri, 200);
        assertEquals("new content", serverResponse);
    }

    public static void create(String collectionName, String content, String uri, int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint contentEndpoint = new Endpoint(Login.zebedeeHost, "content/" + collectionName).setParameter("uri", uri);

        Response<String> createResponse = http.post(contentEndpoint, content, String.class);
        assertEquals(createResponse.statusLine.getStatusCode(), expectedResponse);

    }


    public static String get(String collectionName, String uri, int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint contentEndpoint = new Endpoint(Login.zebedeeHost, "content/" + collectionName).setParameter("uri", uri);

        Response<String> getResponse = http.get(contentEndpoint, String.class);
        assertEquals(getResponse.statusLine.getStatusCode(), expectedResponse);
        return getResponse.body;
    }


}
