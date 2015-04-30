package com.github.onsdigital.test;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.Setup;
import com.github.onsdigital.test.api.ZebedeeHost;
import com.github.onsdigital.zebedee.json.Credentials;
import com.github.onsdigital.zebedee.json.PermissionDefinition;
import com.github.onsdigital.zebedee.json.User;

import java.io.IOException;

/**
 * Created by david on 17/04/2015.
 */
public class SetupBeforeTesting implements Setup {

    Http systemSession = Sessions.get("system");

    public static User systemUser = user("Florence Nightingale", "florence@magicroundabout.ons.gov.uk");
    public static Credentials systemCredentials = credentials(systemUser.email, "Doug4l");

    public static User adminUser = user("Matt Jukes", "in.charge@magicroundabout.ons.gov.uk");
    public static Credentials adminCredentials = credentials(adminUser.email, Random.password(8));

    public static User publisherUser = user("Paul Blusher", "dp@magicroundabout.ons.gov.uk");
    public static Credentials publisherCredentials = credentials(publisherUser.email, Random.password(8));

    public static User contentOwnerUser = user("Stacy To", "statto@magicroundabout.ons.gov.uk");
    public static Credentials contentOwnerCredentials = credentials(contentOwnerUser.email, Random.password(8));

    public static User scallywagUser = user("Ha Querr", "script.kiddie@bluecat.com");
    public static Credentials scallywagCredentials = credentials(scallywagUser.email, Random.password(8));

    @Override
    public void setup() throws Exception {
        loginAsSystemOwner();
        createUsers();
        setPasswords();
        setPermissions();
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

        // Content Owner
        Response<User> contentOwner = systemSession.post(ZebedeeHost.users, contentOwnerUser, User.class);
        if (admin.statusLine.getStatusCode() != 409) {
            checkOk(contentOwner, "Unable to create content owner user " + contentOwnerUser);
        }
    }

    private PermissionDefinition permission(User user, boolean admin, boolean editor, String teamName) {

        PermissionDefinition permissionDefinition = new PermissionDefinition();
        permissionDefinition.email = user.email;
        permissionDefinition.admin = admin;
        permissionDefinition.editor = editor;
        return permissionDefinition;
    }

    private void setPasswords() throws IOException {

        // Admin
        Response<String> admin = systemSession.post(ZebedeeHost.password, adminCredentials, String.class);
        checkOk(admin, "Unable to set password for admin user.");

        // Publisher
        Response<String> publisher = systemSession.post(ZebedeeHost.password, publisherCredentials, String.class);
        checkOk(publisher, "Unable to set password for publisher user.");

        // Content Owner
        Response<String> contentOwner = systemSession.post(ZebedeeHost.password, contentOwnerCredentials, String.class);
        checkOk(contentOwner, "Unable to set password for content owner user.");
    }

    /**
     *
     * @throws IOException
     */
    private void setPermissions() throws IOException {

        // Admin
        PermissionDefinition adminPermissionDefinition = permission(adminUser, true, false, null);
        Response<String> adminPermission = systemSession.post(ZebedeeHost.permission, adminPermissionDefinition, String.class);
        checkOk(adminPermission, "Unable to set admin permission for " + adminUser);

        // Publisher
        PermissionDefinition publisherPermissionDefinition = permission(publisherUser, false, true, null);
        Response<String> publisherPermission = systemSession.post(ZebedeeHost.permission, publisherPermissionDefinition, String.class);
        checkOk(publisherPermission, "Unable to set editor permission for " + publisherUser);

        // Content Owner
        Response<Boolean> team = systemSession.post(ZebedeeHost.teams.addPathSegment("economy"), "", Boolean.class);

        PermissionDefinition contentOwnerPermissionDefinition = permission(contentOwnerUser, false, false, "economy");
        Response<String> contentOwnerPermission = systemSession.post(ZebedeeHost.permission, contentOwnerPermissionDefinition, String.class);
        checkOk(contentOwnerPermission, "Unable to set /economy permission for " + contentOwnerUser);
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
}
