package com.github.onsdigital.test.json;

import javax.ws.rs.core.Response;

/**
 * Created by dave on 5/25/16.
 */
public class SimpleZebedeeResponse {

    private String message;
    private int statusCode;

    public SimpleZebedeeResponse(String message, Response.Status statusCode) {
        this.message = message;
        this.statusCode = statusCode.getStatusCode();
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
