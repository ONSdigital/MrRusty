package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.CollectionDescription;
import com.github.onsdigital.test.json.SimpleZebedeeResponse;
import com.github.onsdigital.test.json.Visualisation;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Tests verify the behaviour of the Zebedee cms DataVisualisationZip endpoint.
 */
@Api
@DependsOn(com.github.onsdigital.test.api.Permissions.class)
public class DataVisualisationZip extends ZebedeeApiTest {

    private static final String TEST_ZIP_PATH = "src/main/resources/dummy_dataVis/zip/dataVisualisation.zip";
    private static final String DATA_VIS_JSON_PATH = "/visualisations/{0}/data.json";
    private static final String DATA_JSON_PATH = "/visualisations/{0}/data";
    private static final String DATA_VIS_ZIP_CONTENT_PATH = "/visualisations/{0}/content/dataVisualisation.zip";

    private String dataVisId;
    private String zipContentPath;
    private String dataJsonPath;
    private String dataPath;
    private Visualisation visualisation;

    /**
     *
     */
    public static CollectionDescription uploadDataVisualisationZipFile(String zipPath, String jsonPath, Visualisation dataJson) throws IOException {
        CollectionDescription collectionDescription = OneLineSetups.publishedCollection(context.getDataVis());
        File zip = getDataVisualisationZip();

        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionDescription.id).setParameter("uri", zipPath);
        context.getDataVis().post(contentEndpoint, zip, String.class);

        contentEndpoint = ZebedeeHost.content.addPathSegment(collectionDescription.id).setParameter("uri", jsonPath);
        context.getDataVis().post(contentEndpoint, dataJson, String.class);
        return collectionDescription;
    }

    public static File getDataVisualisationZip() throws IOException {
        return new File(TEST_ZIP_PATH);
    }

    @Before
    public void setUp() {
        dataVisId = Random.id();
        visualisation = new Visualisation().setUid(dataVisId);
        zipContentPath = MessageFormat.format(DATA_VIS_ZIP_CONTENT_PATH, dataVisId);
        dataJsonPath = MessageFormat.format(DATA_VIS_JSON_PATH, dataVisId);
        dataPath = MessageFormat.format(DATA_JSON_PATH, dataVisId);
    }

    /**
     * Test DataVisualisationZip unzip API endpoint works as expected.
     * <ul>
     *     <li>{@link javax.ws.rs.POST} the data vis zip file to the content endpoint.</li>
     *     <li>{@link javax.ws.rs.POST} the data.json for the uploaded file.</li>
     *     <li>{@link javax.ws.rs.POST} request to unpack the zip.</li>
     *     <li>{@link javax.ws.rs.GET} request for the page json file to verify the filenames match the content
     *     of the uploaded zip.</li>
     * </ul>
     */
    @Test
    public void shouldUnpackDataVisualisationZipfile() throws Exception {
        CollectionDescription collectionDescription = uploadDataVisualisationZipFile(zipContentPath, dataJsonPath, visualisation);

        Endpoint unzipEndpoint = ZebedeeHost.dataVisualisationZip.addPathSegment(collectionDescription.id)
                .setParameter("zipPath", zipContentPath);

        Response<SimpleZebedeeResponse> response = context.getDataVis().post(unzipEndpoint, null, SimpleZebedeeResponse.class);
        assertThat(response.statusLine.getStatusCode(), is(200));

        Endpoint dataEndpoint = ZebedeeHost.data.addPathSegment(collectionDescription.id).setParameter("uri", dataPath);
        Response<Visualisation> dataResponse = context.getDataVis().get(dataEndpoint, Visualisation.class);

        Set<String> expectedFiles = getExpectedFilenames();
        assertThat(dataResponse.statusLine.getStatusCode(), is(200));
        assertThat(dataResponse.body.getFilenames().size(), is(expectedFiles.size()));
        assertThat(dataResponse.body.getFilenames(), equalTo(expectedFiles));
    }

    private static Set<String> getExpectedFilenames() throws Exception {
        Set<String> expectedFiles = new HashSet<>();
        try (InputStream in = new FileInputStream(getDataVisualisationZip())) {
            ZipInputStream zipInputStream = new ZipInputStream(in);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                expectedFiles.add(zipEntry.getName());
                zipEntry = zipInputStream.getNextEntry();
            }
        }
        return expectedFiles;
    }
}
