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
import com.github.onsdigital.test.json.EventType;
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
@DependsOn(com.github.onsdigital.test.api.Collection.class)
public class Content {


    /**
     * Test basic functionality for .json content
     */
    @POST
    @Test
    public void shouldAddContentIfAPublisher() throws IOException {

        // Given
        // an existing collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When - we create content
        String content = "{name:foo}";
        String uri = Random.id() + ".json";
        create(collection.id, content, uri, Login.httpPublisher);

        // Then
        // When we retrieve content we expect a 200 status and identical content
        Response<Path> response = get(collection.id, uri, Login.httpPublisher);

        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode()); // check status
        assertEquals(content, Serialiser.deserialise(getBody(response), String.class)); // check the content
    }

    /**
     * Test returns {@link HttpStatus#BAD_REQUEST_400} if no uri is specified
     */
    @POST
    @Test
    public void shouldReturn400WhenNoUriIsSpecified() throws IOException {

        // Given - an existing collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When - content is added with no file url
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collection.id);
        Response<String> createResponse = Login.httpPublisher.post(contentEndpoint, "", String.class);

        // Then - a 400 response code is returned
        assertEquals(HttpStatus.BAD_REQUEST_400, createResponse.statusLine.getStatusCode());
    }

    /**
     * Test returns {@link HttpStatus#CONFLICT_409} if file is currently being edited in another collection
     */
    @POST
    @Test
    public void filesOnlyEditableInOneCollection() throws IOException {
        CollectionDescription collection1 = OneLineSetups.publishedCollection();
        CollectionDescription collection2 = OneLineSetups.publishedCollection();
        String fileURI = Random.id() + ".json";

        // given the file exists in one collection
        Response<String> response1 = create(collection1.id, "content", fileURI, Login.httpPublisher);
        assertEquals(HttpStatus.OK_200, response1.statusLine.getStatusCode());

        // we can't create it in another collection
        Response<String> response = create(collection2.id, "content", fileURI, Login.httpPublisher);
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }

    /**
     * Test basic update functionality for .json content
     */
    @POST
    @Test
    public void shouldUpdateContent() throws IOException {
        // Given
        // A file created in a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();
        String fileUri = Random.id() + ".json";
        create(collection.id, "content", fileUri, Login.httpPublisher);

        // When
        // We overwrite it's content and retrieve the file contents
        create(collection.id, "new content", fileUri, Login.httpPublisher);
        Response<Path> pathResponse = get(collection.id, fileUri, Login.httpPublisher);

        // We expect
        // The content should be the overwritten version
        assertEquals("new content", Serialiser.deserialise(getBody(pathResponse), String.class));
    }

    /**
     * Test basic functionality for dataset upload
     */
    @POST
    @Test
    public void shouldUploadFile() throws IOException {

        // Given
        // A file, a taxonomy node, and a collection
        File file = new File("src/main/resources/snail.jpg");
        CollectionDescription collection = OneLineSetups.publishedCollection();

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        // When
        // We attempt to upload the file to the taxonomy
        upload(collection.id, taxonomyNode + filename, file, Login.httpPublisher);

        // Then
        // The file should be where we expect it to be and exist
        Response<Path> response = download(collection.id, taxonomyNode + filename, Login.httpPublisher);
        assertNotNull(response.body);
        assertTrue(Files.size(response.body) > 0);
    }

    /**
     * Test basic functionality for file deletes
     */
    @DELETE
    @Test
    public void shouldDeleteFile() throws IOException {

        // Given
        // We upload a file
        CollectionDescription collection = OneLineSetups.publishedCollection();

        File file = new File("src/main/resources/snail.jpg");
        String uri = "economy/regionalaccounts/" + Random.id() + ".jpg";

        upload(collection.id, uri, file, Login.httpPublisher);

        // When
        // We attempt to delete the file from the taxonomy
        delete(collection.id, uri, Login.httpPublisher);

        // Then
        //... the file should not exist
        Response<Path> response = download(collection.id, uri, Login.httpPublisher);
        assertNull(response);
    }

    /**
     * Test that given a directory the delete method deletes all files
     * in that directory.
     */
    @DELETE
    @Test
    public void shouldDeleteFilesInDirectory() throws IOException {

        // Given
        // We have a collection with content and an uploaded file
        CollectionDescription collection = OneLineSetups.publishedCollection();
        String directory = "/economy/regionalaccounts/";
        String jsonUri = directory + Random.id() + ".json";
        Content.create(collection.id, "thisisContent", jsonUri, Login.httpPublisher);

        File file = new File("src/main/resources/snail.jpg");
        String jpgUri = "/economy/regionalaccounts/" + Random.id() + ".jpg";
        upload(collection.id, jpgUri, file, Login.httpPublisher);

        // When
        // we attempt to delete the directory
        delete(collection.id, directory, Login.httpPublisher);

        // Then
        // the files should not exist
        Response<Path> jsonResponse = download(collection.id, jsonUri, Login.httpPublisher);
        assertNull(jsonResponse);

        Response<Path> jpgResponse = download(collection.id, jpgUri, Login.httpPublisher);
        assertNull(jpgResponse);

        // And
        // A delete event should be present in the collection details
        CollectionDescription updatedCollection = Collection.get(collection.id, Login.httpPublisher).body;
        assertTrue(updatedCollection.eventsByUri.get(directory).hasEventForType(EventType.DELETED));
    }

    /**
     * Test system does not list a deleted file in the Collection.get() details
     */
    @DELETE
    @Test
    public void shouldNotListDeletedFile() throws IOException {

        // Given
        // We upload a file
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);
        String uri = collection.inProgressUris.get(0);

        // When
        // We delete the file from the taxonomy
        delete(collection.id, uri, Login.httpPublisher);

        // Then
        //... the file should not appear in the collection GET method
        Response<CollectionDescription> response = Collection.get(collection.id, Login.httpPublisher);
        assertEquals(0, response.body.inProgressUris.size());
    }

    /**
     * Test that when a user starts editing a page then deletes their work when they
     * next get content it will return the currently published page
     * <p/>
     * WARNING: This needs to run with a genuine taxonomy node - REMOVED During content generation
     */
    @DELETE
    //@Test
    public void shouldNotReturnDeletedVersionOfExistingWebsiteFile() throws IOException {

        // Given
        // A specific collection for this test and a genuine website page
        //
        CollectionDescription collection = OneLineSetups.publishedCollection();

        String taxonomyNode = "/peoplepopulationandcommunity/birthsdeathsandmarriages/lifeexpectancies/timeseries/raid49/";
        String initialData = getBody(get(collection.id, taxonomyNode + "data.json", Login.httpPublisher));

        // When
        // We create a new page and then delete it
        create(collection.id, "{'data': 'Dummy data'}", taxonomyNode + "data.json", Login.httpPublisher);
        delete(collection.id, taxonomyNode + "data.json", Login.httpPublisher);

        // Then
        // the original file should be returned by Content GET
        String response = getBody(get(collection.id, taxonomyNode + "data.json", Login.httpPublisher));
        assertEquals(initialData, response);
    }

    // Support methods ___________________________________________

    /**
     * @param collectionName the parent collection folder
     * @param uri            the file uri
     * @param http           the session
     * @return
     * @throws IOException
     */
    public static Response<String> delete(String collectionName, String uri, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);

        return http.delete(contentEndpoint, String.class);
    }

    /**
     * Create content using a simple string
     *
     * @param collectionName the parent collection folder
     * @param content        string content to save
     * @param uri            uri to save as
     * @param http           session
     * @return
     * @throws IOException
     */
    public static Response<String> create(String collectionName, Object content, String uri, Http http) throws IOException {

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        return http.post(contentEndpoint, content, String.class);
    }

    /**
     * Response from a get request
     * <p/>
     * To get body content as a string pair with getBody()
     *
     * @param collectionName the parent collection folder
     * @param uri            uri of the content
     * @param http           session
     * @return
     * @throws IOException
     */
    public static Response<Path> get(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        return http.get(contentEndpoint);
    }

    public static <T> Response<T> get(String collectionName, String uri, Http http, Class<T> responseClass) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);
        return http.get(contentEndpoint, responseClass);
    }

    /**
     * Extracts the contents of a (generally temporary) path to a string
     *
     * @param path
     * @return
     * @throws IOException
     */
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
