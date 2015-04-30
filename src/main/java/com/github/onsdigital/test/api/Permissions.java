package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.zebedee.json.PermissionDefinition;
import com.github.onsdigital.zebedee.json.Team;
import com.github.onsdigital.zebedee.json.User;
import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 29/04/15.
 */
@Api
@DependsOn(Login.class)
public class Permissions {

    /**
     *
     * Test basic POST functionality
     *
     * Test posting permissions with admin as Boolean.TRUE gives admin permissions
     */
    @POST
    @Test
    public void canAddAdminPermissions() throws IOException {
        // Given
        // a simple user
        User user = OneLineSetups.newActiveUserWithViewerPermissions();

        // When
        // admin assigns permissions
        Response<String> response = postPermission(permission(user.email, Boolean.TRUE, null), Login.httpAdministrator);

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, Login.httpAdministrator).body;
        assertTrue(BooleanUtils.isTrue(permissions.admin));
    }

    /**
     *
     * Test basic POST functionality
     *
     * Test posting permissions with admin as Boolean.FALSE revokes admin permissions
     */
    @POST
    @Test
    public void canRemoveAdminPermissions() throws IOException {
        // Given
        // an admin user
        User user = OneLineSetups.newActiveUserWithViewerPermissions();
        postPermission(permission(user.email, Boolean.TRUE, null), Login.httpAdministrator);

        // When
        // admin revokes permissions
        Response<String> response = postPermission(permission(user.email, Boolean.FALSE, null), Login.httpAdministrator);

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, Login.httpAdministrator).body;
        assertTrue(BooleanUtils.isFalse(permissions.admin));
    }

    /**
     *
     * Test basic POST functionality
     *
     * Test posting permissions with publisher as Boolean.TRUE gives publisher permissions
     */
    @POST
    @Test
    public void canAddPublisherPermissions() throws IOException {
        // Given
        // a simple user
        User user = OneLineSetups.newActiveUserWithViewerPermissions();

        // When
        // admin assigns permissions
        Response<String> response = postPermission(permission(user.email, null, Boolean.TRUE), Login.httpAdministrator);

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, Login.httpAdministrator).body;
        assertTrue(BooleanUtils.isTrue(permissions.editor));
    }

    /**
     *
     * Test basic POST functionality
     *
     * Test posting permissions with publisher as Boolean.FALSE revokes permissions
     */
    @POST
    @Test
    public void canRemovePublisherPermissions() throws IOException {
        // Given
        // an admin user
        User user = OneLineSetups.newActiveUserWithViewerPermissions();
        postPermission(permission(user.email, null, Boolean.TRUE), Login.httpAdministrator);

        // When
        // admin revokes permissions
        Response<String> response = postPermission(permission(user.email, null, Boolean.FALSE), Login.httpAdministrator);

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, Login.httpAdministrator).body;
        assertTrue(BooleanUtils.isFalse(permissions.editor));
    }


    public Response<String> postPermission(PermissionDefinition definition, Http http) throws IOException {
        return http.post(ZebedeeHost.permission, definition, String.class);
    }
    public Response<PermissionDefinition> getPermissions(String email, Http http) throws IOException {
        return http.get(ZebedeeHost.permission.setParameter("email", email), PermissionDefinition.class);
    }

    public PermissionDefinition permission(String email, Boolean admin, Boolean editor) {
        PermissionDefinition permissionDefinition = new PermissionDefinition();
        permissionDefinition.email = email;
        permissionDefinition.admin = admin;
        permissionDefinition.editor = editor;
        return permissionDefinition;
    }
}
