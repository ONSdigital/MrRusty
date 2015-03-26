package com.github.onsdigital.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by david on 25/03/2015.
 */
public class EndpointTest {

    @Test
    public void shouldIncludeHostUrl() {

        // Given
        String baseUrl = "http://beta.ons.gov.uk";
        Host host = new Host(baseUrl);

        // When
        Endpoint endpoint = new Endpoint(host, "/path");

        // Then
        assertTrue(StringUtils.startsWith(endpoint.toString(), baseUrl));
    }

    @Test
    public void shouldIncludeHostPath() {

        // Given
        String baseUrl = "http://beta.ons.gov.uk/base";
        Host host = new Host(baseUrl);

        // When
        Endpoint endpoint = new Endpoint(host, "/path");

        // Then
        assertEquals("http://beta.ons.gov.uk/base/path", endpoint.toString());
    }

    @Test
    public void shouldDealWithSlashes() {

        // Given
        String baseUrl = "http://beta.ons.gov.uk";
        Host host1 = new Host(baseUrl);
        Host host2 = new Host(baseUrl+'/');
        String path1 = "path";
        String path2 = "/path";

        // When
        Endpoint endpoint1 = new Endpoint(host1, path1);
        Endpoint endpoint2 = new Endpoint(host2, path2);

        // Then
        assertEquals("http://beta.ons.gov.uk/path", endpoint1.toString());
        assertEquals("http://beta.ons.gov.uk/path", endpoint2.toString());
    }

    @Test
    public void shouldAddParameter() throws URISyntaxException {

        // Given
        String baseUrl = "http://beta.ons.gov.uk";
        Host host = new Host(baseUrl);
        String name = "cat";
        String value = "Finchley";

        // When
        Endpoint endpoint = new Endpoint(host, "/").setParameter(name, value);

        // Then
        System.out.println(endpoint);
        URIBuilder builder = new URIBuilder(endpoint.toString());
        List<NameValuePair> parameters = builder.getQueryParams();
        assertEquals(1, parameters.size());
        assertEquals(name, parameters.get(0).getName());
        assertEquals(value, parameters.get(0).getValue());
    }

}