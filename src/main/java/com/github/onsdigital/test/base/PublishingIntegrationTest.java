package com.github.onsdigital.test.base;

import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.TearDownAfterTesting;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class PublishingIntegrationTest {

    private static boolean hasSetupRun = false;
    private static boolean hasTeardownRun = false;

    protected static Context context = new Context();

    @BeforeClass
    public synchronized static void setup() throws Exception {
        System.out.println("Running test setup code...");
        if (!hasSetupRun) {
            hasSetupRun = true;
            context.setup();
        }
    }

    @AfterClass
    public static void teardown() {
        System.out.println("Running test teardown code...");
        TearDownAfterTesting.doTeardown(context);
    }
}
