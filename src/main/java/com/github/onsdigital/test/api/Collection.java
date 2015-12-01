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
import com.github.onsdigital.test.json.CollectionType;
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
import java.util.Date;

import static com.github.onsdigital.test.AssertResponse.assertOk;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Api
@DependsOn(com.github.onsdigital.test.api.Permissions.class)
public class Collection {

    private static Http http = Login.httpAdministrator;

    public Collection() {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());
    }

    /**
     * Test basic functionality
     * <p/>
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
        Response<CollectionDescription> response = post(roundabout, Login.httpPublisher);

        // Expect
        // a response of 200 - success
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(roundabout.name, response.body.name);
        assertEquals(roundabout.publishDate, response.body.publishDate);
        assertTrue(StringUtils.isNotBlank(response.body.id));
    }

    /**
     * Test basic functionality
     * <p/>
     * Create with publisher permissions should return {@link HttpStatus#OK_200}
     */
    @POST
    @Test
    public void canCreateCollectionAssociatedWithARelease() throws IOException {

        // Given an announced release.
        Release release = CreateRelease();
        String releaseUri = release.getUri().toString() + "/data.json";

        CollectionDescription announcementCollection = OneLineSetups.publishedCollection();
        assertOk(Content.create(announcementCollection.id, release, releaseUri, Login.httpPublisher));

        assertOk(Complete.complete(announcementCollection.id, releaseUri, Login.httpPublisher));
        assertOk(Review.review(announcementCollection.id, releaseUri, Login.httpSecondSetOfEyes));
        assertOk(Approve.approve(announcementCollection.id, Login.httpPublisher));
        Publish.publishAndWait(announcementCollection.id, Login.httpPublisher, 10);

        // When we create a new collection with the release URI.
        CollectionDescription releaseCollection = createCollectionDescription();
        releaseCollection.releaseUri = release.getUri();
        releaseCollection = assertOk(Collection.post(releaseCollection, Login.httpPublisher)).body;

        // Then the release page is added to the collection, and the release page is set to published
        assertEquals(release.getUri().toString(), releaseCollection.releaseUri.toString());

        Release releasePage = assertOk(Content.get(releaseCollection.id, releaseUri, Login.httpPublisher, Release.class)).body;
        assertTrue(releasePage.getDescription().getPublished());
    }

    private Release CreateRelease() {
        String releaseUri = "/releases/" + Random.id() ;
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
        Response<CollectionDescription> response = post(anon, Login.httpPublisher);

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
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we try and create an identical collection
        Response<CollectionDescription> response = post(collection, Login.httpPublisher);

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
        Response<CollectionDescription> responseScallywag = post(collection, Login.httpScallywag);
        Response<CollectionDescription> responseViewer = post(collection, Login.httpViewer);

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
        collection = post(collection, Login.httpPublisher).body;

        // When
        // we attempt to retrieve it as an publisher
        Response<CollectionDescription> response = post(collection, Login.httpPublisher);
        Response<CollectionDescription> responseAdmin = post(collectionAdmin, Login.httpAdministrator);

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
        CollectionDescription collection = OneLineSetups.publishedCollection();
        CollectionDescription collection2 = OneLineSetups.publishedCollection();

        // When
        //...we delete it
        delete(collection.id, Login.httpPublisher);
        delete(collection2.id, Login.httpAdministrator);


        // We expect
        //...it to be entirely deleted
        Response<CollectionDescription> response = get(collection.id, Login.httpPublisher);
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());

        Response<CollectionDescription> responseAdmin = get(collection.id, Login.httpAdministrator);
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
        CollectionDescription collection1 = OneLineSetups.publishedCollection();
        CollectionDescription collection2 = OneLineSetups.publishedCollection();

        // When
        //...we we try and delete them delete it
        Response<String> deleteResponseScallywag = delete(collection1.id, Login.httpScallywag);
        Response<String> deleteResponseViewer = delete(collection2.id, Login.httpViewer);

        // Then
        // delete should fail with unauthorized returned
        // + the collections should still exist
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseViewer.statusLine.getStatusCode());

        assertEquals(HttpStatus.OK_200, get(collection1.id, Login.httpPublisher).statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, get(collection2.id, Login.httpPublisher).statusLine.getStatusCode());
    }

    public static Response<String> delete(String name, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        return http.delete(endpoint, String.class);
    }

    public static Response<CollectionDescription> post(CollectionDescription collection, Http http) throws IOException {
        return http.post(ZebedeeHost.collection, collection, CollectionDescription.class);
    }

    public static CollectionDescription createCollectionDescription() {
        CollectionDescription collection = new CollectionDescription();
        collection.name = createCollectionNameForTest();
        collection.publishDate = new Date();
        collection.type = CollectionType.manual;
        return collection;
    }

    public static String createCollectionNameForTest() {
        return "Rusty_" + Random.id().substring(0, 10);
    }

    public static Response<CollectionDescription> get(String id, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.collection.addPathSegment(id);
        return http.get(idUrl, CollectionDescription.class);
    }
}
