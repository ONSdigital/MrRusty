package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DependsOn(Collection.class)
public class Content {

    Http http = Sessions.get("admin");

    @Test
    public void shouldAddContent() throws IOException {

        // Given - an existing collection
        CollectionDescription collection = Collection.create(http);

        // When - we create content
        String content = "{name:foo}";
        String uri = Random.id() + ".json";
        create(collection.name, content, uri, HttpStatus.OK_200, http);

        // Then - we get the content back when we get it
        String getResponse = get(collection.name, uri, HttpStatus.OK_200, http);
        assertEquals(content, getResponse);
    }

    @Test
    public void shouldReturn400WhenNoUriIsSpecified() throws IOException {

        // Given - an existing collection
        CollectionDescription collection = Collection.create(http);

        // When - content is added with no file url
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collection.name);
        Response<String> createResponse = http.post(contentEndpoint, "", String.class);

        // Then - a 400 response code is returned
        assertEquals(createResponse.statusLine.getStatusCode(), HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void filesOnlyEditableInOneCollection() throws IOException {
        CollectionDescription collection_1 = Collection.create(http);
        CollectionDescription collection_2 = Collection.create(http);

        String fileURI = Random.id() + ".json";

        // given the file exists in one collection
        create(collection_1.name, "content", fileURI, HttpStatus.OK_200, http);

        // we can't create it in another collection
        create(collection_2.name, "content", fileURI, HttpStatus.CONFLICT_409, http);
    }

    @Test
    public void shouldUpdateContent() throws IOException {
        // Given
        // A file created in a collection
        CollectionDescription collection_1 = Collection.create(http);
        String fileUri = Random.id() + ".json";
        create(collection_1.name, "content", fileUri, 200, http);

        // When
        // We overwrite it's content and retrieve the file contents
        create(collection_1.name, "new content", fileUri, 200, http);
        String serverResponse = get(collection_1.name, fileUri, 200, http);

        // We expect
        // The content should be the overwritten version
        assertEquals("new content", Serialiser.deserialise(serverResponse, String.class));
    }

    @Test
    public void shouldUploadFile() throws IOException {



        // Given
        // A file, a taxonomy node, and a collection
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create(http);

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        // When
        // We attempt to upload the file to the taxonomy
        upload(collection_1.name, taxonomyNode, filename, file, 200, http);

        // Then
        // The file should be where we expect it to be and exist
        download(collection_1.name, taxonomyNode, filename, true, http);
    }

    @Test
    public void shouldDeleteFile() throws IOException {



        // Given
        // We upload a file
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create(http);

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        upload(collection_1.name, taxonomyNode, filename, file, 200,http );

        // When
        // We attempt to delete the file from the taxonomy
        delete(collection_1.name, taxonomyNode, filename, 200, http);

        // Then
        //... the file should not exist
        download(collection_1.name, taxonomyNode, filename, false, http);
    }

    @Test
    public void shouldNotListDeletedFile() throws IOException {



        // Given
        // We upload a file
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create(http);

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        upload(collection_1.name, taxonomyNode, filename, file, 200, http);

        // When
        // We delete the file from the taxonomy
        delete(collection_1.name, taxonomyNode, filename, 200, http);

        // Then
        //... the file should not appear in the collection GET method
        CollectionDescription collection = Collection.get(collection_1.name, 200, http);
        assertEquals(0, collection.inProgressUris.size());
    }

    @Test
    public void shouldNotReturnDeletedVersionOfExistingWebsiteFile() throws IOException {




        // Given
        // A page file within a collection
        CollectionDescription collection_1 = new CollectionDescription();
        CollectionDescription collection_2 = null;

        collection_1.name = "shouldNotReturnDeletedVersionOfExistingWebsiteFile";
        try {
            collection_2 = Collection.get(collection_1.name, http);
        } finally {
            if (collection_2 == null) {
                collection_1 = Collection.create(collection_1, 200, http);
            } else {
                collection_1 = collection_2;
            }

        }


        String taxonomyNode = "/peoplepopulationandcommunity/birthsdeathsandmarriages/lifeexpectancies/timeseries/raid49/";
        String initialData = get(collection_1.name, taxonomyNode + "data.json", 200, http);

        // When
        // We create a new page and then delete it
        create(collection_1.name, "{'data': 'Dummy data'}", taxonomyNode + "data.json", 200, http);
        delete(collection_1.name, taxonomyNode, "data.json", 200, http);

        // Then
        //... the original file should be returned by Content GET
        String response = get(collection_1.name, taxonomyNode + "data.json", 200, http);
        assertEquals(initialData, response);
    }

    // Support methods
    private static void delete(String collectionName, String taxonomyNode, String filename, int expectedResponse, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", taxonomyNode + filename);

        Response<String> createResponse = http.delete(contentEndpoint, String.class);

        assertEquals(expectedResponse, createResponse.statusLine.getStatusCode());
    }

    public static void create(String collectionName, String content, String uri, int expectedResponse, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        Response<String> createResponse = http.post(contentEndpoint, content, String.class);
        assertEquals(createResponse.statusLine.getStatusCode(), expectedResponse);
    }


    public static String get(String collectionName, String uri, int expectedResponse, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        Response<Path> getResponse = http.get(contentEndpoint);
        assertEquals(expectedResponse, getResponse.statusLine.getStatusCode());

        return FileUtils.readFileToString(getResponse.body.toFile(),Charset.forName("UTF8"));
    }

    public static void upload(String collectionName, String node, String saveAsName, File file, int expectedResponse, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", node + saveAsName);

        Response<String> uploadResponse = http.post(contentEndpoint, file, String.class);

        assertEquals(expectedResponse, uploadResponse.statusLine.getStatusCode());

    }

    public static void download(String collectionName, String node, String saveAsName, boolean expectSuccess, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", node + saveAsName);


        Response<Path> path = http.get(contentEndpoint);


        assertEquals(expectSuccess, path != null); // Check a path has been returned
        if (expectSuccess) {
            assertTrue(Files.size(path.body) > 0);
        } // Check it is not null
    }


}
