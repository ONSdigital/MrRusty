package com.github.onsdigital.test;

import com.github.onsdigital.http.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Common assertions for HTTP responses.
 */
public class AssertResponse {

    public static <T> Response<T> assertOk(Response<T> response) {
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        return response;
    }

    public static <T> Response<T> assertBodyNotEmpty(Response<T> response) {
        assertTrue(StringUtils.isNotBlank(response.body.toString()));
        return response;
    }
}
