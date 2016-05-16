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
        doTeardown(Setup.context);
    }

    public static void doTeardown(Context context) {
        Drivers.quit();
        try {
            CleanUp.cleanUpAllCollectionsBeginningWithRusty(context);
            CleanUp.cleanUpAllUsersBeginningWithRusty(context);
            CleanUp.cleanUpAllTeamsBeginningWithRusty(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
