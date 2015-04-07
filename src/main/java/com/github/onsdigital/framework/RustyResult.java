package com.github.onsdigital.framework;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Result} class that collates multiple results.
 * Created by david on 31/03/2015.
 */
public class RustyResult extends Result {

    private AtomicInteger rounds = new AtomicInteger();
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger ignoreCount = new AtomicInteger();
    private final List<Failure> failures = Collections.synchronizedList(new ArrayList<Failure>());
    private final Set<Class<?>> blocked = Collections.synchronizedSet(new HashSet<Class<?>>());
    //private final Set<Class<?>> pasesed = Collections.synchronizedSet(new HashSet<Class<?>>());
    //private final List<Failure> fAssumptionFailures = Collections.synchronizedList(new ArrayList<Failure>());
    private long runTime = 0;
    private long startTime;

    /**
     * @return the number of testing rounds run
     */
    public int getRounds() {
        return rounds.get();
    }

    /**
     * @return the number of tests run
     */
    @Override
    public int getRunCount() {
        return count.get();
    }

    /**
     * @return the number of tests that failed during the run
     */
    @Override
    public int getFailureCount() {
        return failures.size();
    }

    /**
     * @return the number of milliseconds it took to run the entire suite to run
     */
    @Override
    public long getRunTime() {
        return runTime;
    }

    /**
     * @return the {@link Failure}s describing tests that failed and the problems they encountered
     */
    @Override
    public List<Failure> getFailures() {
        return failures;
    }

    /**
     * @return the number of tests ignored during the run
     */
    @Override
    public int getIgnoreCount() {
        return ignoreCount.get();
    }

    /**
     * @return <code>true</code> if all tests succeeded
     */
    @Override
    public boolean wasSuccessful() {
        return getFailureCount() == 0 && blocked.size() == 0;
    }

    public java.util.Collection<Class<?>> getBlocked() {
        return Collections.unmodifiableCollection(this.blocked);
    }

    //public java.util.Collection<Class<?>> getPassed() {
    //    return Collections.unmodifiableCollection(this.passed);
    //}

    public void setBlocked(java.util.Collection<Class<?>> blocked) {
        this.blocked.addAll(blocked);
    }

    // public void setPassed(java.util.Collection<Class<?>> passed) {
    //    this.passed.addAll(passed);
    //}


    public void add(Result result) {

        // Run count
        count.getAndAdd(result.getRunCount());

        // Failures
        failures.addAll(result.getFailures());

        // Start time
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        // Run time
        runTime += result.getRunTime();

        // End time
        long endTime = System.currentTimeMillis();
        runTime += endTime - startTime;

        // Ignore count:
        ignoreCount.getAndAdd(result.getIgnoreCount());

        // Assumption failures - not avavilable yet it seems?
        //fAssumptionFailures.addAll(result.g)

        rounds.getAndIncrement();
    }
}
