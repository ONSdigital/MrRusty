package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.PermissionDefinition;
import com.github.onsdigital.test.json.User;
import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 29/04/15.
 */
@Api
@DependsOn(com.github.onsdigital.test.api.Login.class)
public class Permissions extends ZebedeeApiTest {

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
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);

        // When
        // admin assigns permissions
        Response<String> response = postPermission(permission(user.email, Boolean.TRUE, null, false), context.getAdministrator());

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, context.getAdministrator()).body;
        assertTrue(BooleanUtils.isTrue(permissions.admin));
    }

    /**
     * Test verifies that a use can be create with Data Visualisation permission.
     */
    @Test
    public void shouldCreateUserWithDataVisualisationPermission() throws Exception {
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);

        Response<String> response = postPermission(permission(user.email, false, false, true), context.getAdministrator());
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, context.getAdministrator()).body;
        assertTrue(BooleanUtils.isTrue(permissions.dataVisPublisher));
        assertTrue(BooleanUtils.isFalse(permissions.admin));
        assertTrue(BooleanUtils.isTrue(permissions.editor));
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
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
        postPermission(permission(user.email, true, null, false), context.getAdministrator());

        // When
        // admin revokes permissions
        Response<String> response = postPermission(permission(user.email, false, null, false), context.getAdministrator());

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, context.getAdministrator()).body;
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
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);

        // When
        // admin assigns permissions
        Response<String> response = postPermission(permission(user.email, null, true, false), context.getAdministrator());

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, context.getAdministrator()).body;
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
        User user = OneLineSetups.newActiveUserWithViewerPermissions(context);
        postPermission(permission(user.email, null, true, false), context.getAdministrator());

        // When
        // admin revokes permissions
        PermissionDefinition permission = permission(user.email, null, false, false);
        Response<String> response = postPermission(permission, context.getAdministrator());

        // Expect
        // a response of 200 - success and they have admin permissions
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        PermissionDefinition permissions = getPermissions(user.email, context.getAdministrator()).body;
        assertTrue(BooleanUtils.isFalse(permissions.editor));
    }


    public static Response<String> postPermission(PermissionDefinition definition, Http http) throws IOException {
        return http.post(ZebedeeHost.permission, definition, String.class);
    }
    public Response<PermissionDefinition> getPermissions(String email, Http http) throws IOException {
        return http.get(ZebedeeHost.permission.setParameter("email", email), PermissionDefinition.class);
    }

    public static PermissionDefinition permission(String email, Boolean admin, Boolean editor, Boolean dataVisPublisher) {
        PermissionDefinition permissionDefinition = new PermissionDefinition();
        permissionDefinition.email = email;
        permissionDefinition.admin = admin;
        permissionDefinition.editor = editor;
        permissionDefinition.dataVisPublisher = dataVisPublisher;
        return permissionDefinition;
    }
}
