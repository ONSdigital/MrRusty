package com.github.onsdigital.framework;

import org.junit.internal.TextListener;
import org.junit.runner.Result;

/**
 * Created by david on 31/03/2015.
 */
public class RustyListener extends TextListener {

    RustyResult rustyResult = new RustyResult();

    RustyListener() {
        super(System.out);
    }

    /**
     * Collates the results of each testing round.
     *
     * @param result the summary of the test run for this round, including all the tests that failed
     */
    @Override
    public void testRunFinished(Result result) {
        rustyResult.add(result);
    }

    /**
     * Called when all rounds have completed.
     */
    public void testRunsAllFinished() {
        super.testRunFinished(rustyResult);
    }

}
