package com.github.onsdigital.framework;

import com.github.onsdigital.test.*;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by kanemorgan on 30/03/2015.
 */
public class TestRunner {


    static Set<Class<?>> queue;

    static Set<Class<?>> ready;

    static Set<Class<?>> passed = new HashSet<>();

    static Set<Class<?>> failed = new HashSet<>();

    static Set<Class<?>> errored = new HashSet<>();

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        queue = getQueue();

        int readySize;
        do {

            readySize = getReady(queue);
            runReady();

        } while (readySize > 0);
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Errored: " + errored);
    }

    private static void runReady() {
        for (Class<?> test : ready) {
            runTestClass(test);
//            try {
//                boolean result;
//                result = (boolean) test.getMethod("main").invoke(test);
//
//                if (result) {
//                    passed.add(test);
//                } else {
//                    failed.add(test);
//                }
//            } catch (Exception e) {
//                errored.add(test);
//
//            } finally {
//                ready.remove(test);
//            }
        }
    }


    private static void runTestClass(Class<?> testClass) {

        // Get test methods
        List<Method> tests = new ArrayList<>();
        for (Method method : testClass.getMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                tests.add(method);
            }
        }

        // run tests
        for (Method testMethod : tests) {
            try {

                testMethod.invoke(testClass);
                // Deal with success.
                passed.add(testClass);

            } catch (IllegalAccessException e) {

                // Develop fail
                System.out.println("You have an issue with class " + testClass + ": unable to access all test methods.");

            } catch (InvocationTargetException e) {

                // Inspect the exeption that was thrown when invoking the method
                // to determine whether it's a failed assertion or an error.
                Throwable cause = e.getCause();
                if (cause != null && AssertionError.class.isAssignableFrom(cause.getClass())) {

                    // Deal with test failure
                    failed.add(testClass);

                } else {

                    // Deal with test error
                    errored.add(testClass);
                }

            } finally {

                // Remove the test from the pool of tests to run this time
                ready.remove(testClass);

            }
        }

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

