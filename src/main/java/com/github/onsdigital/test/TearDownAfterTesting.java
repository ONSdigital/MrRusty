package com.github.onsdigital.test;

import com.github.onsdigital.junit.Teardown;
import com.github.onsdigital.selenium.Drivers;
import com.github.onsdigital.test.api.CleanUp;

/**
 * Created by thomasridd on 03/06/15.
 */
public class TearDownAfterTesting implements Teardown {

    @Override
    public void teardown() {
        Drivers.quit();
        try {
            CleanUp.cleanUpAllCollectionsBeginningWithRusty();
            CleanUp.cleanUpAllUsersBeginningWithRusty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
