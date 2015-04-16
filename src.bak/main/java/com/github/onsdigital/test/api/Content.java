package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.apache.commons.io.FileUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Created by kanemorgan on 31/03/2015.
 */

@DependsOn(Collection.class)
public class Content {

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

        // Given
        // A file created in a collection
        CollectionDescription collection_1 = Collection.create();
        String fileUri = Random.id() + ".json";
        create(collection_1.name, "content", fileUri, 200);

        // When
        // We overwrite it's content and retrieve the file contents
        create(collection_1.name, "new content", fileUri, 200);
        String serverResponse = get(collection_1.name, fileUri, 200);

        // We expect
        // The content should be the overwritten version
        assertEquals("new content", Serialiser.deserialise(serverResponse, String.class));
    }

    @Test
    public void shouldUploadFile() throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);

        // Given
        // A file, a taxonomy node, and a collection
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create();

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        // When
        // We attempt to upload the file to the taxonomy
        upload(collection_1.name, taxonomyNode, filename, file, 200);

        // Then
        // The file should be where we expect it to be and exist
        download(collection_1.name, taxonomyNode, filename, true);
    }

    @Test
    public void shouldDeleteFile() throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);

        // Given
        // We upload a file
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create();

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        upload(collection_1.name, taxonomyNode, filename, file, 200);

        // When
        // We attempt to delete the file from the taxonomy
        delete(collection_1.name, taxonomyNode, filename, 200);

        // Then
        //... the file should not exist
        download(collection_1.name, taxonomyNode, filename, false);
    }

    @Test
    public void shouldNotListDeletedFile() throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);

        // Given
        // We upload a file
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create();

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        upload(collection_1.name, taxonomyNode, filename, file, 200);

        // When
        // We delete the file from the taxonomy
        delete(collection_1.name, taxonomyNode, filename, 200);

        // Then
        //... the file should not appear in the collection GET method
        CollectionDescription collection = Collection.get(collection_1.name, 200);
        assertEquals(0, collection.inProgressUris.size());
    }

    @Test
    public void shouldNotReturnDeletedVersionOfExistingWebsiteFile() throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);


        // Given
        // A page file within a collection
        CollectionDescription collection_1 = new CollectionDescription();
        CollectionDescription collection_2 = null;
        collection_1.name = "shouldNotReturnDeletedVersionOfExistingWebsiteFile";
        try {
            collection_2 = Collection.get(collection_1.name);
        } finally {
            if(collection_2 == null) {
                collection_1 = Collection.create(collection_1, 200);
            } else {
                collection_1 = collection_2;
            }

        }


        String taxonomyNode = "/peoplepopulationandcommunity/birthsdeathsandmarriages/lifeexpectancies/timeseries/raid49/";
        String initialData = get(collection_1.name, taxonomyNode + "data.json", 200);

        // When
        // We create a new page and then delete it
        create(collection_1.name, "{'data': 'Dummy data'}", taxonomyNode + "data.json", 200);
        delete(collection_1.name, taxonomyNode, "data.json", 200);

        // Then
        //... the original file should be returned by Content GET
        String response = get(collection_1.name, taxonomyNode + "data.json", 200);
        assertEquals(initialData, response);
    }
    // Support methods
    private void delete(String collectionName, String taxonomyNode, String filename, int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint contentEndpoint = new Endpoint(Login.zebedeeHost, "content/" + collectionName).setParameter("uri", taxonomyNode + filename);

        Response<String> createResponse = http.delete(contentEndpoint, String.class);

        assertEquals(expectedResponse, createResponse.statusLine.getStatusCode());
    }

    public static void create(String collectionName, String content, String uri, int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint contentEndpoint = new Endpoint(Login.zebedeeHost, "content/" + collectionName).setParameter("uri", uri);

        Response<String> createResponse = http.post(contentEndpoint, content, String.class);
        assertEquals(expectedResponse, createResponse.statusLine.getStatusCode());

    }

    public static String get(String collectionName, String uri, int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint contentEndpoint = new Endpoint(Login.zebedeeHost, "content/" + collectionName).setParameter("uri", uri);

        Response<Path> getResponse = http.get(contentEndpoint);
        assertEquals(expectedResponse, getResponse.statusLine.getStatusCode());

        return FileUtils.readFileToString(getResponse.body.toFile(), Charset.forName("UTF8"));
    }

    public static void upload(String collectionName, String node, String saveAsName, File file, int expectedResponse) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint contentEndpoint = new Endpoint(Login.zebedeeHost, "content/" + collectionName).setParameter("uri", node + saveAsName);

        Response<String> uploadResponse = http.post(contentEndpoint, file, String.class);
        assertEquals(expectedResponse, uploadResponse.statusLine.getStatusCode());

    }

    public static void download(String collectionName, String node, String saveAsName, boolean expectSuccess) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint contentEndpoint = new Endpoint(Login.zebedeeHost, "content/" + collectionName).setParameter("uri", node + saveAsName);

        Response<Path> path = http.get(contentEndpoint);


        assertEquals(expectSuccess, path != null); // Check a path has been returned
        if(expectSuccess) { assertTrue(Files.size(path.body) > 0); } // Check it is not null
    }

}
