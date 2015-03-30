package com.github.onsdigital.framework;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
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

            readySize = getReady(queue) ;
            runReady();

        } while(readySize > 0);
        System.out.println(passed);
        System.out.println(failed);
        System.out.println(errored);
    }

    private static void runReady() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for(Class<?> test:ready){

            try {
                boolean result;
                result = (boolean) test.getMethod("main").invoke(test);

                if (result) {
                    passed.add(test);
                }else{
                    failed.add(test);
                }
            } catch(Exception e){
                errored.add(test);

            }finally {
                ready.remove(test);
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
     *
     * Returns a list of all of the classes that the given class depends on.
     * @param type The Class to get the dependencies for
     * @return A List of classes that type depends on.
     */
   static  List<Class<?>> getDependencies(Class<?> type){
        DependsOn annotation = type.getAnnotation(DependsOn.class);
        return Arrays.asList(annotation.value());
    }

    static int getReady(Set<Class<?>> queue){
        ready = new HashSet<>();

        for (Class<?> element:queue){
            if(dependenciesMet(element)){
                ready.add(element);
            }
        }
        if (ready !=null){
            queue.removeAll(ready);
        }

        return ready.size();
    }

   static boolean dependenciesMet(Class<?> type){

        List<Class<?>> dependencies = getDependencies(type);
       if(dependencies == null){
           return true;
       } else if(passed == null) {
           return false;
       }

        return passed.containsAll(dependencies);
    }
}

