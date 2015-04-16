package com.github.onsdigital.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by david on 25/03/2015.
 */
public class HostTest {

    @Test
    public void shouldParseHost() {

        // Given
        String url = "http://localhost:8080/";

        // When
        Host host = new Host(url);

        // Then
        assertEquals("localhost",host.url.getHost());
    }

    @Test
    public void shouldParsePort() {

        // Given
        String url = "http://localhost:8080/";

        // When
        Host host = new Host(url);

        // Then
        assertEquals(8080,host.url.getPort());
    }

    @Test
    public void shouldParseScheme() {

        // Given
        String url = "http://localhost:8080/";

        // When
        Host host = new Host(url);

        // Then
        assertEquals("http",host.url.getScheme());
    }

    @Test
    public void shouldParseEmptyPath() {

        // Given
        String url = "http://localhost:8080/";

        // When
        Host host = new Host(url);

        // Then
        assertEquals("/",host.url.getPath());
    }

    @Test
    public void shouldParsePath() {

        // Given
        String url = "http://localhost:8080/rest/";

        // When
        Host host = new Host(url);

        // Then
        assertEquals("/rest/",host.url.getPath());
    }

}