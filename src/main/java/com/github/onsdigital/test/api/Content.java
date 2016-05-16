package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.CollectionDescription;
import com.github.onsdigital.test.json.EventType;
import com.github.onsdigital.test.json.Team;
import com.github.onsdigital.test.json.User;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

@Api
@DependsOn(com.github.onsdigital.test.api.Collection.class)
public class Content extends ZebedeeApiTest {


    /**
     * Test basic functionality for .json content
     */
    @POST
    @Test
    public void shouldAddContentIfAPublisher() throws IOException {

        // Given
        // an existing collection
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        // When - we create content
        String content = "{name:foo}";
        String uri = Random.id() + ".json";
        create(collection.id, content, uri, context.getPublisher());

        // Then
        // When we retrieve content we expect a 200 status and identical content
        Response<Path> response = get(collection.id, uri, context.getPublisher());

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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        // When - content is added with no file url
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collection.id);
        Response<String> createResponse = context.getPublisher().post(contentEndpoint, "", String.class);

        // Then - a 400 response code is returned
        assertEquals(HttpStatus.BAD_REQUEST_400, createResponse.statusLine.getStatusCode());
    }

    /**
     * Test returns {@link HttpStatus#CONFLICT_409} if file is currently being edited in another collection
     */
    @POST
    @Test
    public void filesOnlyEditableInOneCollection() throws IOException {
        CollectionDescription collection1 = OneLineSetups.publishedCollection(context.getPublisher());
        CollectionDescription collection2 = OneLineSetups.publishedCollection(context.getPublisher());
        String fileURI = Random.id() + ".json";

        // given the file exists in one collection
        Response<String> response1 = create(collection1.id, "content", fileURI, context.getPublisher());
        assertEquals(HttpStatus.OK_200, response1.statusLine.getStatusCode());

        // we can't create it in another collection
        Response<String> response = create(collection2.id, "content", fileURI, context.getPublisher());
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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());
        String fileUri = Random.id() + ".json";
        create(collection.id, "content", fileUri, context.getPublisher());

        // When
        // We overwrite it's content and retrieve the file contents
        create(collection.id, "new content", fileUri, context.getPublisher());
        Response<Path> pathResponse = get(collection.id, fileUri, context.getPublisher());

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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        String taxonomyNode = "economy/regionalaccounts/";
        String filename = Random.id() + ".jpg";

        // When
        // We attempt to upload the file to the taxonomy
        upload(collection.id, taxonomyNode + filename, file, context.getPublisher());

        // Then
        // The file should be where we expect it to be and exist
        Response<Path> response = download(collection.id, taxonomyNode + filename, context.getPublisher());
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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        File file = new File("src/main/resources/snail.jpg");
        String uri = "economy/regionalaccounts/" + Random.id() + ".jpg";

        upload(collection.id, uri, file, context.getPublisher());

        // When
        // We attempt to delete the file from the taxonomy
        delete(collection.id, uri, context.getPublisher());

        // Then
        //... the file should not exist
        Response<Path> response = download(collection.id, uri, context.getPublisher());
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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());
        String directory = "/economy/regionalaccounts/";
        String jsonUri = directory + Random.id() + ".json";
        Content.create(collection.id, "thisisContent", jsonUri, context.getPublisher());

        File file = new File("src/main/resources/snail.jpg");
        String jpgUri = "/economy/regionalaccounts/" + Random.id() + ".jpg";
        upload(collection.id, jpgUri, file, context.getPublisher());

        // When
        // we attempt to delete the directory
        delete(collection.id, directory, context.getPublisher());

        // Then
        // the files should not exist
        Response<Path> jsonResponse = download(collection.id, jsonUri, context.getPublisher());
        assertNull(jsonResponse);

        Response<Path> jpgResponse = download(collection.id, jpgUri, context.getPublisher());
        assertNull(jpgResponse);

        // And
        // A delete event should be present in the collection details
        CollectionDescription updatedCollection = Collection.get(collection.id, context.getPublisher()).body;
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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 1);
        String uri = collection.inProgressUris.get(0);

        // When
        // We delete the file from the taxonomy
        delete(collection.id, uri, context.getPublisher());

        // Then
        //... the file should not appear in the collection GET method
        Response<CollectionDescription> response = Collection.get(collection.id, context.getPublisher());
        assertEquals(0, response.body.inProgressUris.size());
    }

    /**
     * Test that when a user starts editing a page then deletes their work when they
     * next get content it will return the currently published page
     * <p>
     * WARNING: This needs to run with a genuine taxonomy node - REMOVED During content generation
     */
    @DELETE
    //@Test
    public void shouldNotReturnDeletedVersionOfExistingWebsiteFile() throws IOException {

        // Given
        // A specific collection for this test and a genuine website page
        //
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        String taxonomyNode = "/peoplepopulationandcommunity/birthsdeathsandmarriages/lifeexpectancies/timeseries/raid49/";
        String initialData = getBody(get(collection.id, taxonomyNode + "data.json", context.getPublisher()));

        // When
        // We create a new page and then delete it
        create(collection.id, "{'data': 'Dummy data'}", taxonomyNode + "data.json", context.getPublisher());
        delete(collection.id, taxonomyNode + "data.json", context.getPublisher());

        // Then
        // the original file should be returned by Content GET
        String response = getBody(get(collection.id, taxonomyNode + "data.json", context.getPublisher()));
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
     * <p>
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

    @GET
    @Test
    public void contentShouldBeSeenByATeamMemberWhenTheCollectionIsCreatedBeforeTheUser() throws IOException {

        // Given
        // A team + permissions for the team to access collection A + add alice to Alpha
        Team team = Teams.createTeam(context);
        CollectionDescription collection = createCollection(team);
        User viewer = Users.createTestViewerUser(context);
        Teams.addMemberToTeam(context.getAdministrator(), team, viewer);
        Http http = Sessions.get(viewer.email);

        // When we save some content and get it
        JsonObject content = getExamplePageContent();

        String uri = Random.id() + ".json";
        create(collection.id, content, uri, context.getPublisher());

        // Then
        // When we retrieve content we expect a 200 status and identical content
        Response<Path> response = get(collection.id, uri, http);

        // Then
        // we expect alice to have access to one and only one collection and for that to be collection A
        JsonObject newObject;
        try (FileInputStream inputStream = new FileInputStream(response.body.toFile())) {
            newObject = Serialiser.deserialise(inputStream, JsonObject.class);
        }

        // Then
        // we expect alice to have access to one and only one collection and for that to be collection A
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        assertEquals(Serialiser.serialise(content), Serialiser.serialise(newObject));
    }

    @GET
    @Test
    public void contentShouldBeSeenByATeamMember() throws IOException {

        // Given
        // A team + permissions for the team to access collection A + add alice to Alpha
        User viewer = Users.createTestViewerUser(context);
        Team team = Teams.createTeam(context);
        Teams.addMemberToTeam(context.getAdministrator(), team, viewer);
        CollectionDescription collection = createCollection(team);

        Http http = Sessions.get(viewer.email);

        // When we save some content and get it
        JsonObject content = getExamplePageContent();
        String uri = Random.id() + ".json";
        create(collection.id, content, uri, context.getPublisher());

        // Then
        // When we retrieve content we expect a 200 status and identical content
        Response<Path> response = get(collection.id, uri, http);

        JsonObject jsonObject;
        try (FileInputStream inputStream = new FileInputStream(response.body.toFile())) {
            jsonObject = Serialiser.deserialise(inputStream, JsonObject.class);
        }

        // Then
        // we expect alice to have access to one and only one collection and for that to be collection A
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        assertEquals(Serialiser.serialise(jsonObject),
                Serialiser.serialise(content));
    }

    @POST
    @Test
    public void shouldErrorWhenSavingContentToAnApprovedCollection() throws IOException {

        // Given
        // an approved collection
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());
        Approve.approve(collection.id, context.getPublisher());

        // When we attempt to save data.
        Response<String> response = create(collection.id, "content", "some/content/data.json", context.getPublisher());
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    private static JsonObject getExamplePageContent() throws IOException {
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/dummy_dataset/data.json")) {
            return Serialiser.deserialise(inputStream, JsonObject.class);
        }
    }


    private CollectionDescription createCollection(Team team) throws IOException {
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher(), team);
        OneLineSetups.publishedCollection(context.getPublisher()); // create another collection to ensure there are two collections in the system.
        return collection;
    }
}
