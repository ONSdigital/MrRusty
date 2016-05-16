package com.github.onsdigital.test;

import com.github.onsdigital.junit.Teardown;

/**
 * Created by thomasridd on 03/06/15.
 */
public class TearDownAfterTesting implements Teardown {
    @Override
    public void teardown() {

    }

//    @Override
//    public void teardown() {
//        Drivers.quit();
//        try {
//            CleanUp.cleanUpAllCollectionsBeginningWithRusty();
//            CleanUp.cleanUpAllUsersBeginningWithRusty();
//            CleanUp.cleanUpAllTeamsBeginningWithRusty();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
