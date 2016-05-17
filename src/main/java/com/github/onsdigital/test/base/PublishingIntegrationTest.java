package com.github.onsdigital.test.base;

import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.Setup;
import com.github.onsdigital.test.TearDownAfterTesting;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class PublishingIntegrationTest {

    private static boolean hasSetupRun = false;

    protected static Context context = new Context();

    @BeforeClass
    public synchronized static void setup() throws Exception {
        System.out.println("Running setup in PublishingIntegrationTest...");

        if (Setup.context != null) {
            context = Setup.context;
            return; // hack to skip the class teardown when running the full suite.
        }

        if (!hasSetupRun) {
            hasSetupRun = true;
            context.setup();
        }
    }

    @AfterClass
    public static void teardown() {
        System.out.println("Running test teardown code...");

        if (Setup.context != null)
            return; // hack to skip the class teardown when running the full suite.

        TearDownAfterTesting.doTeardown(context);
    }
}
