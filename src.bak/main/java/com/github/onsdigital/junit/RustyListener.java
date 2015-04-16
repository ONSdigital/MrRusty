package com.github.onsdigital.junit;

import org.junit.internal.TextListener;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by david on 31/03/2015.
 */
public class RustyListener extends TextListener {

    public final RustyResult rustyResult = new RustyResult();
    Set<Class<?>> blocked;
    Collection<Class<?>> passed;

    public RustyListener() {
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
     *
     * @param blocked Classes that were unable to run because of failed dependencies.
     */
    public void testRunsAllFinished(Set<Class<?>> blocked, Set<Class<?>> passed) {

        this.passed = passed;
        this.blocked = blocked;
        rustyResult.setBlocked(blocked);
        printRounds();
        super.testRunFinished(rustyResult);
        printBlocked();
    }

    private void printRounds() {
        System.out.println();
        System.out.println(rustyResult.getRounds() + " rounds of tests ran.");
    }

    private void printBlocked() {

        if (blocked.size() > 0) {

            // List the classes that failed:
            List<Failure> failures = rustyResult.getFailures();
            Set<String> failedClassNames = new TreeSet<>();
            for (Failure failure : failures) {
                failedClassNames.add(failure.getDescription().getClassName());
            }
            System.out.println("The following classes failed:");
            for (String className : failedClassNames) {
                try {
                    System.out.println(" > " + Class.forName(className).getSimpleName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // List the blocked classes and their dependencies:
            System.out.println();
            System.out.println("The following tests could not be run:");
            for (Class<?> blockedClass : blocked) {
                DependsOn dependsOn = blockedClass.getAnnotation(DependsOn.class);
                Class<?>[] dependencies = dependsOn.value();
                System.out.println(" > \"" + blockedClass.getSimpleName() + "\" depends on:");
                for (Class<?> dependency : dependencies) {
                    if (!passed.contains(dependency)) {
                        System.out.println("   - " + dependency.getSimpleName());
                    }
                }
                System.out.println();
            }

        }
    }

}
