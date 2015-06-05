package com.github.onsdigital.test;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.Teardown;
import com.github.onsdigital.test.api.CleanUp;
import com.github.onsdigital.test.api.Login;
import com.github.onsdigital.test.api.ZebedeeHost;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.CollectionDescriptions;

import java.io.IOException;

/**
 * Created by thomasridd on 03/06/15.
 */
public class TearDownAfterTesting implements Teardown {

    @Override
    public void teardown() {
        try {
            CleanUp.cleanUpAllCollectionsBeginningWithRusty();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
