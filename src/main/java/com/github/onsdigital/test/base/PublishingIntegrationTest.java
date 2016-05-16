package com.github.onsdigital.test.base;

import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.TearDownAfterTesting;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class PublishingIntegrationTest {

    private static boolean hasSetupRun = false;
    private static boolean hasTeardownRun = false;

    protected Context context = new Context();

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        System.out.println("Running test setup code...");
        hasSetupRun = true;
        //new Context().setup();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("Running test teardown code...");
        new TearDownAfterTesting().teardown();
    }
}
