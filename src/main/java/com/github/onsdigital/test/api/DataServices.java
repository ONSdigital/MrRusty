package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.ChartObject;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 14/05/15.
 */
@Api
@DependsOn(Permissions.class) // Doesn't really but I am using the http.post function for convenience
public class DataServices extends ZebedeeApiTest {

    /**
     * Test basic functionality for a ChartObject file
     */
    @POST
    @Test
    public void shouldUploadChartToCSV() throws IOException {

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

    /**
     * Test basic functionality for a ChartObject file
     */
    @POST
    @Test
    public void shouldUploadChartToXLSX() throws IOException {

        // Given
        // A chart file
        File file = new File("src/main/resources/dummy_chart.json");

        // When
        // We attempt to upload the file and download
        Response<Path> response = upload(file, "chart", "xlsx");


        // Then
        // A filepath to a temporary file should be returned
        assertNotNull(response.body);
        assertTrue(Files.size(response.body) > 0);
    }


    public Response<Path> upload(File file, String input, String output) throws IOException {
        // Get an http (which one doesn't matter)
        Http http = context.getPublisher();

        // Get the content endpoint
        Endpoint contentEndpoint = ZebedeeHost.dataservices.setParameter("input", input).setParameter("output", output);

        try(FileInputStream fis = new FileInputStream(file.getPath());){
            ChartObject object = Serialiser.deserialise(fis, ChartObject.class);
            return http.postAndReturn(contentEndpoint, object);
        }
    }

}
