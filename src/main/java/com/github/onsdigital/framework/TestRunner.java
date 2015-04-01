package com.github.onsdigital.framework;

import org.junit.runner.JUnitCore;
import org.junit.runners.model.InitializationError;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kanemorgan on 30/03/2015.
 */
public class TestRunner {

    static JUnitCore jUnitCore = new JUnitCore();
    static RustyListener listener = new RustyListener();

    static Set<Class<?>> queue;
    static Set<Class<?>> ready;
    static Set<Class<?>> passed = new HashSet<>();

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InitializationError {

        listener = new RustyListener();
        jUnitCore.addListener(listener);

        queue = getQueue();

        int readySize;
        do {

            readySize = getReady(queue);
            if (readySize > 0) {
                RustyRequest rustyRequest = new RustyRequest(ready);
                jUnitCore.run(rustyRequest);
            }

        } while (readySize > 0);

        // Now trigger printing out the results:
        listener.testRunsAllFinished();
    }


    static Set<Class<?>> getQueue() {
        Reflections reflections = new Reflections("com.github.onsdigital");
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

