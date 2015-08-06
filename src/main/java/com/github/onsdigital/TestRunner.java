package com.github.onsdigital;

import com.github.onsdigital.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.reflections.Reflections;

import java.util.*;

/**
 * This is the main class and the entry point for testing.
 * <p/>
 * This is the class which will be set as the JAR main class in the manifest when running <code>mvn assembly:assembly</code>.
 * <p/>
 * Created by kanemorgan on 30/03/2015.
 */
public class TestRunner {

    static JUnitCore jUnitCore = new JUnitCore();
    static RustyListener listener = new RustyListener();

    static Set<Class<?>> queue;
    static Set<Class<?>> ready;
    static Set<Class<?>> passed = new HashSet<>();
    static List<Set<Class<?>>> rounds = new ArrayList<>();

    static Reflections reflections = new Reflections("com.github.onsdigital");

    public static void main(String[] args) throws Exception {

        listener = new RustyListener();
        jUnitCore.addListener(listener);

        setup();
        queue = getQueue();

        int readySize;
        do {

            readySize = getReady(queue);
            if (readySize > 0) {

                // Run the tests
                RustyRequest rustyRequest = new RustyRequest(ready);
                jUnitCore.run(rustyRequest);

                // Add passed classes to the passed Set to unlock more tests
                List<String> failedClassNames = new ArrayList<>();
                for (Failure failure : listener.rustyResult.getFailures()) {
                    failedClassNames.add(failure.getDescription().getClassName());
                }
                Set<Class<?>> round = new HashSet<>();
                for (Class<?> klass : ready) {
                    if (!failedClassNames.contains(klass.getName())) {
                        passed.add(klass);
                        round.add(klass);
                    }
                }
                rounds.add(round);
            }

        } while (readySize > 0);

        teardown();

        // Now trigger printing out the results:
        listener.testRunsAllFinished(queue, passed, rounds);
    }

    private static void setup() throws Exception {
        Set<Class<? extends Setup>> setupClasses = reflections.getSubTypesOf(Setup.class);
        for (Class<? extends Setup> setupClass : setupClasses) {
            Setup setup = setupClass.newInstance();
            setup.setup();
        }
    }

    private static void teardown() throws Exception {
        Set<Class<? extends Teardown>> setupClasses = reflections.getSubTypesOf(Teardown.class);
        for (Class<? extends Teardown> setupClass : setupClasses) {
            Teardown teardown = setupClass.newInstance();
            teardown.teardown();
        }
    }


    static Set<Class<?>> getQueue() {

        Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(DependsOn.class);
        System.out.println(annotated);
        return annotated;
    }

    /**
     * Returns a list of all of the classes that the given class depends on.
     *
     * @param type The Class to get the dependencies for
     * @return A List of classes that type depends on.
     */
    static List<Class<?>> getDependencies(Class<?> type) {
        DependsOn annotation = type.getAnnotation(DependsOn.class);
        return Arrays.asList(annotation.value());
    }

    static int getReady(Set<Class<?>> queue) {
        ready = new HashSet<>();

        for (Class<?> element : queue) {
            if (dependenciesMet(element)) {
                ready.add(element);
            }
        }
        if (ready != null) {
            queue.removeAll(ready);
        }

        return ready.size();
    }

    static boolean dependenciesMet(Class<?> type) {

        List<Class<?>> dependencies = getDependencies(type);
        if (dependencies == null) {
            return true;
        } else if (passed == null) {
            return false;
        }

        return passed.containsAll(dependencies);
    }
}

