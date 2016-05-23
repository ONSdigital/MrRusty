package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.CollectionDescription;
import com.github.onsdigital.test.json.CollectionType;
import com.github.onsdigital.test.json.Team;
import com.github.onsdigital.test.json.page.base.PageDescription;
import com.github.onsdigital.test.json.page.release.Release;
import com.github.onsdigital.test.json.serialiser.IsoDateSerializer;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;

import static com.github.onsdigital.test.AssertResponse.assertOk;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Api
@DependsOn(com.github.onsdigital.test.api.Permissions.class)
public class Collection extends ZebedeeApiTest {

    public Collection() {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());
    }

    /**
     * Test basic functionality
     * <p>
     * Create with publisher permissions should return {@link HttpStatus#OK_200}
     */
    @POST
    @Test
    public void canCreateCollectionWithPublisherPermissions() throws IOException {
        // Given
        // a collection description
        CollectionDescription roundabout = createCollectionDescription();

        // When
        // we post as a publisher
        Response<CollectionDescription> response = post(roundabout, context.getPublisher());

        // Expect
        // a response of 200 - success
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(roundabout.name, response.body.name);
        assertEquals(roundabout.publishDate, response.body.publishDate);
        assertTrue(StringUtils.isNotBlank(response.body.id));
    }

    /**
     * Test basic functionality
     * <p>
     * Create with publisher permissions should return {@link HttpStatus#OK_200}
     */
    @POST
    @Test
    public void canCreateCollectionAssociatedWithARelease() throws IOException {

        // Given an announced release.
        Release release = CreateRelease();
        String releaseUri = release.getUri().toString() + "/data.json";

        CollectionDescription announcementCollection = OneLineSetups.publishedCollection(context.getPublisher());
        assertOk(Content.create(announcementCollection.id, release, releaseUri, context.getPublisher()));

        assertOk(Complete.complete(announcementCollection.id, releaseUri, context.getPublisher()));
        assertOk(Review.review(announcementCollection.id, releaseUri, context.getSecondSetOfEyes()));
        assertOk(Approve.approve(announcementCollection.id, context.getPublisher()));
        Publish.publishAndWait(context, announcementCollection.id, context.getPublisher(), 10);

        // When we create a new collection with the release URI.
        CollectionDescription releaseCollection = createCollectionDescription();
        releaseCollection.releaseUri = release.getUri();
        releaseCollection = assertOk(Collection.post(releaseCollection, context.getPublisher())).body;

        // Then the release page is added to the collection, and the release page is set to published
        assertEquals(release.getUri().toString(), releaseCollection.releaseUri.toString());

        Release releasePage = assertOk(Content.get(releaseCollection.id, releaseUri, context.getPublisher(), Release.class)).body;
        assertTrue(releasePage.getDescription().getPublished());
    }

    private Release CreateRelease() {
        String releaseUri = "/releases/" + Random.id();
        Release release = new Release();
        release.setDescription(new PageDescription());
        release.getDescription().setPublished(false);
        release.getDescription().setReleaseDate(new DateTime().plusMinutes(5).toDate());
        release.setUri(URI.create(releaseUri));
        release.getDescription().setTitle(Random.id());
        return release;
    }

    /**
     * Creating an unnamed collection should return {@link HttpStatus#BAD_REQUEST_400}
     */
    @POST
    @Test
    public void postShouldReturn400IfNoNameSpecifiedForCreateCollection() throws IOException {
        // Given
        // an incomplete collection description
        CollectionDescription anon = new CollectionDescription();

        // When
        // we post using valid credentials
        Response<CollectionDescription> response = post(anon, context.getPublisher());

        // Expect
        // a response of 400 - Bad request
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    /**
     * written
     */
    @POST
    @Test
    public void postShouldReturn409IfCollectionNameAlreadyExists() throws IOException {

        // Given
        // an existing collection
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        // When
        // we try and create an identical collection
        Response<CollectionDescription> response = post(collection, context.getPublisher());

        // Expect
        // a reponse of 409 - Conflict
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }


    /**
     * Create without publisher permissions should return {@link HttpStatus#UNAUTHORIZED_401}
     */
    @POST
    @Test
    public void postShouldReturn401WithoutPublisherPermissions() throws IOException {

        // Given
        // a collection description
        CollectionDescription collection = createCollectionDescription();

        // When
        // we post as anyone but a publisher
        Response<CollectionDescription> responseScallywag = post(collection, context.getScallyWag());
        Response<CollectionDescription> responseViewer = post(collection, context.getViewer());

        // Expect
        // a response of 401 - unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseViewer.statusLine.getStatusCode());
    }

    /**
     * Viewer permissions should return {@link HttpStatus#OK_200} for any permitted collection, {@link HttpStatus#UNAUTHORIZED_401} otherwise
     */
    @POST
    @Test
    public void postShouldReturn200ForViewerWithPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = createCollectionDescription();
        CollectionDescription collectionAdmin = createCollectionDescription();
        collection = assertOk(post(collection, context.getPublisher())).body;
        collectionAdmin = assertOk(post(collectionAdmin, context.getPublisher())).body;

        // When
        // we attempt to retrieve it as an publisher
        Response<CollectionDescription> response = get(collection.id, context.getPublisher());
        Response<CollectionDescription> responseAdmin = get(collectionAdmin.id, context.getAdministrator());

        // We expect
        // a response of 200
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, responseAdmin.statusLine.getStatusCode());
    }

    /**
     * Publisher permissions should return {@link HttpStatus#OK_200} for any collection
     */
    @DELETE
    @Test
    public void collectionShouldBeDeletedWithPublisherPermissions() throws IOException {
        // Given
        //...a collection
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());
        CollectionDescription collection2 = OneLineSetups.publishedCollection(context.getPublisher());

        // When
        //...we delete it
        delete(collection.id, context.getPublisher());
        delete(collection2.id, context.getAdministrator());


        // We expect
        //...it to be entirely deleted
        Response<CollectionDescription> response = get(collection.id, context.getPublisher());
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());

        Response<CollectionDescription> responseAdmin = get(collection.id, context.getAdministrator());
        assertEquals(HttpStatus.NOT_FOUND_404, responseAdmin.statusLine.getStatusCode());
    }

    /**
     * All other permissions should return {@link HttpStatus#UNAUTHORIZED_401} for any collection
     */
    @DELETE
    @Test
    public void deleteShouldReturn401WithoutPublisherPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection1 = OneLineSetups.publishedCollection(context.getPublisher());
        CollectionDescription collection2 = OneLineSetups.publishedCollection(context.getPublisher());

        // When
        //...we we try and delete them delete it
        Response<String> deleteResponseScallywag = delete(collection1.id, context.getScallyWag());
        Response<String> deleteResponseViewer = delete(collection2.id, context.getViewer());

        // Then
        // delete should fail with unauthorized returned
        // + the collections should still exist
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseViewer.statusLine.getStatusCode());

        assertEquals(HttpStatus.OK_200, get(collection1.id, context.getPublisher()).statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, get(collection2.id, context.getPublisher()).statusLine.getStatusCode());
    }

    public static Response<String> delete(String name, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        return http.delete(endpoint, String.class);
    }

    public static Response<CollectionDescription> post(CollectionDescription collection, Http http) throws IOException {
        return http.post(ZebedeeHost.collection, collection, CollectionDescription.class);
    }

    public static CollectionDescription createCollectionDescription(Team... teams) {
        CollectionDescription collection = new CollectionDescription();
        collection.name = createCollectionNameForTest();
        collection.publishDate = new Date();
        collection.type = CollectionType.manual;

        if (teams.length > 0) {
            if (collection.teams == null) collection.teams = new ArrayList<>();
            for (Team team : teams) {
                collection.teams.add(team.name);
            }
        }

        return collection;
    }

    public static String createCollectionNameForTest() {
        return "Rusty_" + Random.id().substring(0, 10);
    }

    public static Response<CollectionDescription> get(String id, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.collection.addPathSegment(id);
        return http.get(idUrl, CollectionDescription.class);
    }

    /**
     * Generic method to wait for a collection related predicate to return true.
     * This can be used when waiting for an operation to complete that changes a collection property.
     *
     * Example:
     *
     * @param predicate - the predicate that is expected to return true.
     * @param context - the context to call the API.
     * @param collectionID - The ID of the collection to check.
     * @param secondsToWait - The maximum number of seconds to wait for the condition to be met.
     * @param failureMessage - The message to fail the test with if the timeout occurs.
     * @throws IOException
     */
    public static void waitFor(
            Predicate<Response<CollectionDescription>> predicate,
            Context context,
            String collectionID,
            int secondsToWait,
            String failureMessage) throws IOException {

        int count = 0;
        boolean result = false;

        while (count < secondsToWait) {
            Response<CollectionDescription> response = Collection.get(collectionID, context.getPublisher());

            if (predicate.test(response)) {
                result = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            count++;
        }

        if (!result)
            fail(failureMessage);
    }
}
