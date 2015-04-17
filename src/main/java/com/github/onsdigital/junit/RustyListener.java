package com.github.onsdigital.junit;

import org.junit.internal.TextListener;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provides a listener that collects results from each
 * dependency round of testing and prints results to stdout.
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
     * @param rounds
     */
    public void testRunsAllFinished(Set<Class<?>> blocked, Set<Class<?>> passed, List<Set<Class<?>>> rounds) {

        this.passed = passed;
        this.blocked = blocked;
        rustyResult.setBlocked(blocked);
        printRounds(rounds);
        super.testRunFinished(rustyResult);
        printBlocked();
    }

    private void printRounds(List<Set<Class<?>>> rounds) {
        System.out.println();
        System.out.println(rustyResult.getRounds() + " rounds of testing:");
        for (Set<Class<?>> round : rounds) {
            System.out.println(" - " + list(round));
        }
    }

    private String list(Set<Class<?>> round) {
        StringBuilder result = new StringBuilder();
        for (Class<?> klass : round) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(klass.getSimpleName());
        }
        return result.toString();
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
