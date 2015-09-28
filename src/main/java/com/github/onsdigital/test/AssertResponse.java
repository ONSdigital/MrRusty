package com.github.onsdigital.test;

import com.github.onsdigital.http.Response;
import org.eclipse.jetty.http.HttpStatus;

import static org.junit.Assert.assertEquals;

/**
 * Static utility methods for checking responses.
 */
public class AssertResponse {

    public static <T> Response<T> assertOk(Response<T> response) {
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        return response;
    }
}
