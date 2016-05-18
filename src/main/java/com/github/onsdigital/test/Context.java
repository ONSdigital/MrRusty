package com.github.onsdigital.test;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.test.api.ZebedeeHost;
import com.github.onsdigital.test.configuration.Configuration;
import com.github.onsdigital.test.json.Credentials;
import com.github.onsdigital.test.json.PermissionDefinition;
import com.github.onsdigital.test.json.User;

import java.io.IOException;

/**
 * Test context. Reusable data for tests thats lazy loaded. 
 * 
 * I.e. log in for particular users handled here will not reattempt login if already logged in.
 */
public class Context {

    public static final String FLO_TOKEN_HEADER = "x-florence-token";

    Http systemSession = Sessions.get("system");

    public static User systemUser = user("Florence Nightingale", Configuration.getSystemUsername());
    public static Credentials systemCredentials = credentials(systemUser.email, Configuration.getSystemUserPassword(), false);

    public static User adminUser = user("Matt Jukes", "in.charge@magicroundabout.ons.gov.uk");
    public static Credentials adminCredentials = credentials(adminUser.email, Random.password(8), false);

    public static User publisherUser = user("Paul Blusher", "dp@magicroundabout.ons.gov.uk");
    public static Credentials publisherCredentials = credentials(publisherUser.email, Random.password(8), false);

    public static User secondSetOfEyesUser = user("Myfanwy Morgan", "mm@magicroundabout.ons.gov.uk");
    public static Credentials secondSetOfEyesCredentials = credentials(secondSetOfEyesUser.email, Random.password(8), false);

    public static User viewerUser = user("Stacy To", "statto@magicroundabout.ons.gov.uk");
    public static Credentials viewerCredentials = credentials(viewerUser.email, Random.password(8), false);

    public static User newUserWithTemporaryPassword = user("New guy", "new@magicroundabout.ons.gov.uk");
    public static Credentials newUserCredentials = credentials(newUserWithTemporaryPassword.email, Random.password(8), true);

    public static User scallywagUser = user("Ha Querr", "script.kiddie@bluecat.com");
    public static Credentials scallywagCredentials = credentials(scallywagUser.email, Random.password(8), false);

    public static User dataVisPublisher = user("Tyrion Lanister", "data-vis@test.com");
    public static Credentials dataVisPublisherCredentials = credentials(dataVisPublisher.email, Configuration.getDefaultPassword(), false);

    private Http administrator;
    private Http publisher;
    private Http secondSetOfEyes;
    private Http thirdSetOfEyes;
    private Http viewer;
    private Http scallyWag;
    private Http dataVis;

    private String tokenAdministrator;
    private String tokenPublisher;
    private String tokenSecondSetOfEyes;
    private String tokenThirdSetOfEyes;
    private String tokenViewer;
    private String tokenDataVis;

    public Context() {
        administrator = Sessions.get("administrator");
        publisher = Sessions.get("publisher");
        secondSetOfEyes = Sessions.get("secondSetOfEyes");
        thirdSetOfEyes = Sessions.get("thirdSetOfEyes");
        viewer = Sessions.get("viewer");
        scallyWag = Sessions.get("scallywag");
        dataVis = Sessions.get("dataVis");
    }

    public void setup() throws Exception {
        loginAsSystemOwner();
        createUsers();
        setPasswords();
        setPermissions();
        loginUsers();
    }

    private void loginUsers() throws IOException {
        tokenAdministrator =  getAdministrator().post(ZebedeeHost.login, adminCredentials, String.class).body;
        tokenPublisher = getPublisher().post(ZebedeeHost.login, publisherCredentials, String.class).body;
        tokenSecondSetOfEyes = getSecondSetOfEyes().post(ZebedeeHost.login, secondSetOfEyesCredentials, String.class).body;
        tokenViewer = getViewer().post(ZebedeeHost.login, viewerCredentials, String.class).body;
        tokenDataVis = dataVis.post(ZebedeeHost.login, dataVisPublisherCredentials, String.class).body;

        administrator.addHeader(FLO_TOKEN_HEADER, tokenAdministrator);
        publisher.addHeader(FLO_TOKEN_HEADER, tokenPublisher);
        secondSetOfEyes.addHeader(FLO_TOKEN_HEADER, tokenSecondSetOfEyes);
        thirdSetOfEyes.addHeader(FLO_TOKEN_HEADER, tokenThirdSetOfEyes);
        viewer.addHeader(FLO_TOKEN_HEADER, tokenViewer);
        dataVis.addHeader(FLO_TOKEN_HEADER, tokenDataVis);
    }

    private void loginAsSystemOwner() throws IOException {
        Response<String> response = systemSession.post(ZebedeeHost.login, systemCredentials, String.class);
        checkOk(response, "Unable to log in as system user.");
        systemSession.addHeader("x-florence-token", response.body);
    }

    /**
     * Creates the users we want. NB the users may have been created by a previous test run.
     *
     * @throws IOException
     */
    private void createUsers() throws IOException {

        // Admin
        Response<User> admin = systemSession.post(ZebedeeHost.users, adminUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(admin, "Unable to create admin user " + adminUser);
        }

        // Publisher
        Response<User> publisher = systemSession.post(ZebedeeHost.users, publisherUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(publisher, "Unable to create publisher user " + publisherUser);
        }

        // Second set of eyes
        Response<User> secondSetOfEyes = systemSession.post(ZebedeeHost.users, secondSetOfEyesUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(secondSetOfEyes, "Unable to create publisher user " + secondSetOfEyesUser);
        }

        // Content Owner
        Response<User> contentOwner = systemSession.post(ZebedeeHost.users, viewerUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(contentOwner, "Unable to create content owner user " + viewerUser);
        }

        // New user with temp password
        Response<User> newUser = systemSession.post(ZebedeeHost.users, newUserWithTemporaryPassword, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(newUser, "Unable to create new user " + newUser);
        }

        // New user with temp password
        Response<User> dataVisDude = systemSession.post(ZebedeeHost.users, dataVisPublisher, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(newUser, "Unable to create data vis dude " + dataVisDude);
        }
    }

    private PermissionDefinition permission(User user, boolean admin, boolean editor, boolean dataVisPublisher) {

        PermissionDefinition permissionDefinition = new PermissionDefinition();
        permissionDefinition.email = user.email;
        permissionDefinition.admin = admin;
        permissionDefinition.editor = editor;
        permissionDefinition.dataVisPublisher = dataVisPublisher;
        return permissionDefinition;
    }

    private void setPasswords() throws IOException {
        setUserPassword(adminCredentials);
        setUserPassword(publisherCredentials);
        setUserPassword(secondSetOfEyesCredentials);
        setUserPassword(viewerCredentials);
        setUserPassword(newUserCredentials);
        setUserPassword(dataVisPublisherCredentials);

        Response<String> admin = systemSession.post(ZebedeeHost.password, newUserCredentials, String.class);
        checkOk(admin, "Unable to set password for user with email " + newUserCredentials.email);
    }

    /**
     * Need to run through the following steps to set the users password that is not a temporary password
     *  - reset the password via the system user
     *  - set the password
     * @param credentials
     * @throws IOException
     */
    private void setUserPassword(Credentials credentials) throws IOException {
        Response<String> admin = systemSession.post(ZebedeeHost.password, credentials, String.class);
        checkOk(admin, "Unable to set password for user with email " + credentials.email);

        Http userSession = new Http();

        Credentials changePasswordCredentials = new Credentials();
        changePasswordCredentials.email = credentials.email;
        changePasswordCredentials.password = credentials.password;
        changePasswordCredentials.oldPassword = credentials.password;

        Response<String> login = userSession.post(ZebedeeHost.password, changePasswordCredentials, String.class);
        checkOk(login, "Unable to set password for test user with email " + credentials.email);
    }

    /**
     *
     * @throws IOException
     */
    private void setPermissions() throws IOException {

        // Admin
        PermissionDefinition adminPermissionDefinition = permission(adminUser, true, false, false);
        Response<String> adminPermission = systemSession.post(ZebedeeHost.permission, adminPermissionDefinition, String.class);
        checkOk(adminPermission, "Unable to set admin permission for " + adminUser);

        // Publisher
        PermissionDefinition publisherPermissionDefinition = permission(publisherUser, false, true, false);
        Response<String> publisherPermission = systemSession.post(ZebedeeHost.permission, publisherPermissionDefinition, String.class);
        checkOk(publisherPermission, "Unable to set editor permission for " + publisherUser);

        // Second set of eyes
        PermissionDefinition secondSetOfEyesPermissionDefinition = permission(secondSetOfEyesUser, false, true, false);
        Response<String> secondSetOfEyesPermission = systemSession.post(ZebedeeHost.permission, secondSetOfEyesPermissionDefinition, String.class);
        checkOk(secondSetOfEyesPermission, "Unable to set editor permission for " + secondSetOfEyesUser);

        // Add content user to the economy team
        systemSession.post(ZebedeeHost.teams.addPathSegment("economy"), "", Boolean.class);
        systemSession.post(ZebedeeHost.teams.addPathSegment("economy").setParameter("email", viewerUser.email), "", Boolean.class);

        // new user
        PermissionDefinition newUserPermissionDefinition = permission(newUserWithTemporaryPassword, false, true, false);
        Response<String> newUserPermission = systemSession.post(ZebedeeHost.permission, newUserPermissionDefinition, String.class);
        checkOk(newUserPermission, "Unable to set editor permission for " + newUserWithTemporaryPassword);

        PermissionDefinition dataVisPubPermissionDefinition = permission(dataVisPublisher, false, false, true);
        Response<String> dataVisPermission = systemSession.post(ZebedeeHost.permission, dataVisPubPermissionDefinition, String.class);
        checkOk(adminPermission, "Unable to set admin permission for " + dataVisPublisher);
    }

    /**
     * Convenience method for generating user objects.
     *
     * @param name  The user's name
     * @param email The user's email
     * @return A {@link User} containing the given details.
     */
    public static User user(String name, String email) {
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
    public static Credentials credentials(String email, String password) {
        return credentials(email, password, null);
    }

    private static Credentials credentials(String email, String password, Boolean temporaryPassword) {
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

    public Http getAdministrator() {
        return administrator;
    }

    public Http getPublisher() {
        return publisher;
    }

    public Http getSecondSetOfEyes() {
        return secondSetOfEyes;
    }

    public Http getThirdSetOfEyes() {
        return thirdSetOfEyes;
    }

    public Http getViewer() {
        return viewer;
    }

    public Http getScallyWag() {
        return scallyWag;
    }

    public Http getDataVis() {
        return dataVis;
    }
}
