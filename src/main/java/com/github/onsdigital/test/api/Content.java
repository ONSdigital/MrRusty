package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

@Api
@DependsOn(Collection.class)
public class Content {


    /**
     * Test basic functionality for .json content
     *
     * TODO - Add permissions functionality
     */
    @POST
    @Test
    public void shouldAddContentIfAPublisher() throws IOException {

        // Given
        // an existing collection
        CollectionDescription collection = Collection.create(Login.httpPublisher);

        // When - we create content
        String content = "{name:foo}";
        String uri = Random.id() + ".json";
        create(collection.name, content, uri, Login.httpPublisher);

        // Then
        // When we retrieve content we expect a 200 status and identical content
        Response<Path> response = get(collection.name, uri, Login.httpPublisher);

        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode()); // check status
        assertEquals(content, Serialiser.deserialise(getBody(response), String.class)); // check the content
    }

    /**
     * Test returns {@link HttpStatus#BAD_REQUEST_400} if no uri is specified
     *
     * TODO - Add permissions functionality
     */
    @POST
    @Test
    public void shouldReturn400WhenNoUriIsSpecified() throws IOException {

        // Given - an existing collection
        CollectionDescription collection = Collection.create(Login.httpPublisher);

        // When - content is added with no file url
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collection.name);
        Response<String> createResponse = Login.httpPublisher.post(contentEndpoint, "", String.class);

        // Then - a 400 response code is returned
        assertEquals(HttpStatus.BAD_REQUEST_400, createResponse.statusLine.getStatusCode());
    }

    /**
     * Test returns {@link HttpStatus#CONFLICT_409} if file is currently being edited in another collection
     *
     * TODO - Add permissions functionality
     */
    @POST
    @Test
    public void filesOnlyEditableInOneCollection() throws IOException {
        CollectionDescription collection_1 = Collection.create(Login.httpPublisher);
        CollectionDescription collection_2 = Collection.create(Login.httpPublisher);

        String fileURI = Random.id() + ".json";

        // given the file exists in one collection
        Response<String> response1 = create(collection_1.name, "content", fileURI, Login.httpPublisher);
        assertEquals(HttpStatus.OK_200, response1.statusLine.getStatusCode());

        // we can't create it in another collection
        Response<String> response = create(collection_2.name, "content", fileURI, Login.httpPublisher);
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }

    /**
     * Test basic update functionality for .json content
     *
     */
    @POST
    @Test
    public void shouldUpdateContent() throws IOException {
        // Given
        // A file created in a collection
        CollectionDescription collection_1 = Collection.create(Login.httpPublisher);
        String fileUri = Random.id() + ".json";
        create(collection_1.name, "content", fileUri, Login.httpPublisher);

        // When
        // We overwrite it's content and retrieve the file contents
        create(collection_1.name, "new content", fileUri, Login.httpPublisher);
        Response<Path> pathResponse = get(collection_1.name, fileUri, Login.httpPublisher);

        // We expect
        // The content should be the overwritten version
        assertEquals("new content", Serialiser.deserialise(getBody(pathResponse), String.class));
    }

    /**
     * Test basic functionality for dataset upload
     *
     */
    @POST
    @Test
    public void shouldUploadFile() throws IOException {

        // Given
        // A file, a taxonomy node, and a collection
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create(Login.httpPublisher);

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        // When
        // We attempt to upload the file to the taxonomy
        upload(collection_1.name, taxonomyNode + filename, file, Login.httpPublisher);

        // Then
        // The file should be where we expect it to be and exist
        Response<Path> response = download(collection_1.name, taxonomyNode + filename, Login.httpPublisher);
        assertNotNull(response.body);
        assertTrue(Files.size(response.body) > 0);
    }

    /**
     * Test basic functionality for file delete
     *
     * TODO - Add permissions functionality
     */
    @DELETE
    @Test
    public void shouldDeleteFile() throws IOException {

        // Given
        // We upload a file
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create(Login.httpPublisher);

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        upload(collection_1.name, taxonomyNode + filename, file, Login.httpPublisher);

        // When
        // We attempt to delete the file from the taxonomy
        delete(collection_1.name, taxonomyNode + filename, Login.httpPublisher);

        // Then
        //... the file should not exist
        Response<Path> response = download(collection_1.name, taxonomyNode + filename, Login.httpPublisher);
        assertNull(response);
    }

    /**
     * Test system does not list a deleted file in the Collection.get() details
     *
     */
    @DELETE
    @Test
    public void shouldNotListDeletedFile() throws IOException {

        // Given
        // We upload a file
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection_1 = Collection.create(Login.httpPublisher);

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        upload(collection_1.name, taxonomyNode + filename, file, Login.httpPublisher);

        // When
        // We delete the file from the taxonomy
        delete(collection_1.name, taxonomyNode + filename, Login.httpPublisher);

        // Then
        //... the file should not appear in the collection GET method
        CollectionDescription collection = Collection.get(collection_1.name, 200, Login.httpPublisher);
        assertEquals(0, collection.inProgressUris.size());
    }

    /**
     * Test that when a user starts editing a page then deletes their work when they
     * next get content it will return the currently published page
     *
     */
    @DELETE
    @Test
    public void shouldNotReturnDeletedVersionOfExistingWebsiteFile() throws IOException {

        // Given
        // A page file within an existing collection
        //
        CollectionDescription collection_1 = new CollectionDescription();
        CollectionDescription collection_2 = null;
        collection_1.name = "shouldNotReturnDeletedVersionOfExistingWebsiteFile";
        try {
            collection_2 = Collection.get(collection_1.name, Login.httpPublisher);
        } finally {
            if (collection_2 == null) {
                collection_1 = Collection.create(collection_1, 200, Login.httpPublisher);
            } else {
                collection_1 = collection_2;
            }
        }
        String taxonomyNode = "/peoplepopulationandcommunity/birthsdeathsandmarriages/lifeexpectancies/timeseries/raid49/";
        String initialData = getBody( get(collection_1.name, taxonomyNode + "data.json", Login.httpPublisher) );

        // When
        // We create a new page and then delete it
        create(collection_1.name, "{'data': 'Dummy data'}", taxonomyNode + "data.json", Login.httpPublisher);
        delete(collection_1.name, taxonomyNode + "data.json", Login.httpPublisher);

        // Then
        //... the original file should be returned by Content GET
        String response = getBody( get(collection_1.name, taxonomyNode + "data.json", Login.httpPublisher));
        assertEquals(initialData, response);
    }

    // Support methods ___________________________________________

    /**
     *
     * @param collectionName the parent collection file
     * @param uri the file uri
     * @param http the session
     * @return
     * @throws IOException
     */
    private static Response<String>  delete(String collectionName, String uri, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);

        return http.delete(contentEndpoint, String.class);
    }

    /**
     * Create content using a simple string
     *
     * @param collectionName
     * @param content string content to save
     * @param uri uri to save as
     * @param http session
     * @return
     * @throws IOException
     */
    public static Response<String> create(String collectionName, String content, String uri, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);

        return http.post(contentEndpoint, content, String.class);
    }

    public static Response<Path> get(String collectionName, String uri, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        return http.get(contentEndpoint);
    }

    public static String getBody(Response<Path> path) throws IOException {
        return FileUtils.readFileToString(path.body.toFile(), Charset.forName("UTF8"));
    }

    public static Response<String> upload(String collectionName, String uri, File file, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        return http.post(contentEndpoint, file, String.class);
    }

    public static Response<Path> download(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        return http.get(contentEndpoint);
    }


}
