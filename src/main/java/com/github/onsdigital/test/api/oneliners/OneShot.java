package com.github.onsdigital.test.api.oneliners;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.*;
import com.github.onsdigital.test.json.*;
import com.github.onsdigital.test.json.serialiser.IsoDateSerializer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thomasridd on 11/09/15.
 */
public class OneShot {

    private static Http systemSession = Sessions.get("system");

    public static User systemUser = user("Florence Nightingale", "florence@magicroundabout.ons.gov.uk");
    public static Credentials systemCredentials = credentials(systemUser.email, "Doug4l");

    public static User adminUser = user("Matt Jukes", "in.charge@magicroundabout.ons.gov.uk");
    public static Credentials adminCredentials = credentials(adminUser.email, Random.password(8));

    public static User publisherUser = user("Paul Blusher", "dp@magicroundabout.ons.gov.uk");
    public static Credentials publisherCredentials = credentials(publisherUser.email, Random.password(8));

    public static User secondSetOfEyesUser = user("Myfanwy Morgan", "mm@magicroundabout.ons.gov.uk");
    public static Credentials secondSetOfEyesCredentials = credentials(secondSetOfEyesUser.email, Random.password(8));

    public static final Host zebedeeHost = new Host(StringUtils.defaultIfBlank(getValue("ZEBEDEE_HOST"), "http://localhost:8082"));

    public static final Endpoint login = new Endpoint(zebedeeHost, "login");
    public static final Endpoint users = new Endpoint(zebedeeHost, "users");
    public static final Endpoint password = new Endpoint(zebedeeHost, "password");
    public static final Endpoint permission = new Endpoint(zebedeeHost, "permission");
    public static final Endpoint approve = new Endpoint(zebedeeHost, "approve");
    public static final Endpoint collections = new Endpoint(zebedeeHost, "collections");
    public static final Endpoint collection = new Endpoint(zebedeeHost, "collection");
    public static final Endpoint content = new Endpoint(zebedeeHost, "content");
    public static final Endpoint transfer = new Endpoint(zebedeeHost, "transfer");
    public static final Endpoint browse = new Endpoint(zebedeeHost, "browse");
    public static final Endpoint complete = new Endpoint(zebedeeHost, "complete");
    public static final Endpoint review = new Endpoint(zebedeeHost, "review");
    public static final Endpoint teams = new Endpoint(zebedeeHost, "teams");
    public static final Endpoint dataservices = new Endpoint(zebedeeHost, "dataservices");
    public static final Endpoint cleanup = new Endpoint(zebedeeHost, "cleanup");
    public static final Endpoint publish = new Endpoint(zebedeeHost, "publish");
    public static final Endpoint collectionBrowseTree = new Endpoint(zebedeeHost, "collectionBrowseTree");
    public static final Endpoint collectionDetails = new Endpoint(zebedeeHost, "collectionDetails");


    public static Http httpAdministrator;
    public static Http httpPublisher;
    public static Http httpSecondSetOfEyes;
    private static String tokenAdministrator;
    private static String tokenPublisher;
    private static String tokenSecondSetOfEyes;

    public static void initialiseZebedeeConnection() throws IOException {
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());


        httpAdministrator = Sessions.get("administrator");
        httpPublisher = Sessions.get("publisher");
        httpSecondSetOfEyes = Sessions.get("secondSetOfEyes");

        Credentials credentials = OneShot.adminCredentials;
        Response<String> response = httpAdministrator.post(OneShot.login, credentials, String.class);
        tokenAdministrator = response.body;

        credentials = publisherCredentials;
        response = httpPublisher.post(OneShot.login, credentials, String.class);
        tokenPublisher = response.body;

        credentials = secondSetOfEyesCredentials;
        response = httpSecondSetOfEyes.post(OneShot.login, credentials, String.class);
        tokenSecondSetOfEyes = response.body;

        httpAdministrator.addHeader("x-florence-token", tokenAdministrator);
        httpPublisher.addHeader("x-florence-token", tokenPublisher);
        httpSecondSetOfEyes.addHeader("x-florence-token", tokenSecondSetOfEyes);
    }

    /**
     * Creates a collection with no content
     *
     * @return The collection's {@link CollectionDescription}
     * @throws IOException
     */
    public static CollectionDescription publishedCollection() throws IOException {
        CollectionDescription collection = createCollectionDescription();
        return postCollection(collection, OneShot.httpPublisher).body;
    }
    public static CollectionDescription createCollectionDescription() {
        SimpleDateFormat format = new SimpleDateFormat("hh-mm-ss");
        String collectionTime = format.format(new Date());

        CollectionDescription collection = new CollectionDescription();
        collection.name = "Rusty_OneShot_"  + collectionTime + "_" + Random.id().substring(0, 10);;
        collection.publishDate = new Date();
        collection.type = CollectionType.manual;
        return collection;
    }

    private static String getValue(String key) {
        return StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
    }

    public static Response<CollectionDescription> postCollection(CollectionDescription collection, Http http) throws IOException {
        return http.post(OneShot.collection, collection, CollectionDescription.class);
    }

    public static Response<String> approve(String collectionID) throws IOException {
        Endpoint endpoint = OneShot.approve.addPathSegment(collectionID);
        return httpPublisher.post(endpoint, null, String.class);
    }








    public static void setup() throws Exception {
        loginAsSystemOwner();
        createUsers();
        setPasswords();
        setPermissions();

        initialiseZebedeeConnection();
    }

    private static void loginAsSystemOwner() throws IOException {
        Response<String> response = systemSession.post(OneShot.login, systemCredentials, String.class);
        checkOk(response, "Unable to log in as system user.");
        systemSession.addHeader("x-florence-token", response.body);
    }

    /**
     * Creates the users we want. NB the users may have been created by a previous test run.
     *
     * @throws IOException
     */
    private static void createUsers() throws IOException {

        // Admin
        Response<User> admin = systemSession.post(OneShot.users, adminUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(admin, "Unable to create admin user " + adminUser);
        }

        // Publisher
        Response<User> publisher = systemSession.post(OneShot.users, publisherUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(publisher, "Unable to create publisher user " + publisherUser);
        }

        // Second set of eyes
        Response<User> secondSetOfEyes = systemSession.post(OneShot.users, secondSetOfEyesUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(secondSetOfEyes, "Unable to create publisher user " + secondSetOfEyesUser);
        }
    }

    private static PermissionDefinition permission(User user, boolean admin, boolean editor) {

        PermissionDefinition permissionDefinition = new PermissionDefinition();
        permissionDefinition.email = user.email;
        permissionDefinition.admin = admin;
        permissionDefinition.editor = editor;
        return permissionDefinition;
    }

    private static void setPasswords() throws IOException {

        // Admin
        Response<String> admin = systemSession.post(OneShot.password, adminCredentials, String.class);
        checkOk(admin, "Unable to set password for admin user.");

        // Publisher
        Response<String> publisher = systemSession.post(OneShot.password, publisherCredentials, String.class);
        checkOk(publisher, "Unable to set password for publisher user.");

        // Publisher
        Response<String> secondSetOfEyes = systemSession.post(OneShot.password, secondSetOfEyesCredentials, String.class);
        checkOk(secondSetOfEyes, "Unable to set password for publisher user.");
    }

    /**
     *
     * @throws IOException
     */
    private static void setPermissions() throws IOException {

        // Admin
        PermissionDefinition adminPermissionDefinition = permission(adminUser, true, false);
        Response<String> adminPermission = systemSession.post(OneShot.permission, adminPermissionDefinition, String.class);
        checkOk(adminPermission, "Unable to set admin permission for " + adminUser);

        // Publisher
        PermissionDefinition publisherPermissionDefinition = permission(publisherUser, false, true);
        Response<String> publisherPermission = systemSession.post(OneShot.permission, publisherPermissionDefinition, String.class);
        checkOk(publisherPermission, "Unable to set editor permission for " + publisherUser);

        // Second set of eyes
        PermissionDefinition secondSetOfEyesPermissionDefinition = permission(secondSetOfEyesUser, false, true);
        Response<String> secondSetOfEyesPermission = systemSession.post(OneShot.permission, secondSetOfEyesPermissionDefinition, String.class);
        checkOk(secondSetOfEyesPermission, "Unable to set editor permission for " + secondSetOfEyesUser);

    }

    /**
     * Convenience method for generating user objects.
     *
     * @param name  The user's name
     * @param email The user's email
     * @return A {@link User} containing the given details.
     */
    private static User user(String name, String email) {
        User user = new User();
        user.name = name;
        user.email = email;
        return user;
    }


    /**
     * Convenience method for generating login credentials.
     *
     * @param email    The email address
     * @param password The password
     * @return A {@link Credentials} instance containing the given details.
     */
    private static Credentials credentials(String email, String password) {
        Credentials credentials = new Credentials();
        credentials.email = email;
        credentials.password = password;
        return credentials;
    }

    /**
     * Checks for a 200 response code.
     * Throws an exception with the given message if the code is different from 200.
     *
     * @param response The {@link Response} to check.
     * @param message  The exception message to use.
     */
    private static void checkOk(Response<?> response, String message) {
        if (response.statusLine.getStatusCode() != 200) {
            throw new RuntimeException(message + " (response code: " + response.statusLine.getStatusCode() + ")");
        }
    }

    public static void main(String[] args) throws Exception {
        OneShot.setup();

    }

    public static Response<String> upload(String collectionName, String uri, File file, Http http) throws IOException {
        Endpoint contentEndpoint = content.addPathSegment(collectionName).setParameter("uri", uri);
        return http.post(contentEndpoint, file, String.class);
    }
}
