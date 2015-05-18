package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.converter.ChartObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.omg.CORBA.NameValuePair;

import javax.ws.rs.POST;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 14/05/15.
 */
@Api
@DependsOn(Permissions.class) // Doesn't really but I am using the http.post function for convenience
public class DataServices {

    /**
     * Test basic functionality for a ChartObject file
     */
    @POST
    @Test
    public void shouldUploadFile() throws IOException {

        // Given
        // A chart file
        File file = new File("src/main/resources/dummy_chart.json");

        // When
        // We attempt to upload the file and download
        Response<Path> response = upload(file, "chart", "csv");


        // Then
        // A filepath to a temporary file should be returned
        assertNotNull(response.body);
        assertTrue(Files.size(response.body) > 0);
    }


    public static Response<Path> upload(File file, String input, String output) throws IOException {
        // Get an http (which one doesn't matter)
        Http http = Login.httpPublisher;

        // Get the content endpoint
        Endpoint contentEndpoint = ZebedeeHost.dataservices.setParameter("input", input).setParameter("output", output);

        try(FileInputStream fis = new FileInputStream(file.getPath());){
            ChartObject object = Serialiser.deserialise(fis, ChartObject.class);
            return http.postAndReturn(contentEndpoint, object);
        }
    }

}
