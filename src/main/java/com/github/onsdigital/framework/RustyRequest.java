package com.github.onsdigital.framework;

import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.Collection;

/**
 * Created by david on 31/03/2015.
 */
public class RustyRequest extends Request {

    Runner runner;

    public RustyRequest(Collection<Class<?>> ready) throws InitializationError {

        // Put the classes into an array for varargs parameter
        Class<?>[] classes = new Class<?>[ready.size()];
        int i = 0;
        for (Class<?> klass : classes) {
            classes[i++] = klass;
        }

        // Set up the runner
        runner = new Suite(new RunnerBuilder() {
            @Override
            public Runner runnerForClass(Class<?> testClass) throws Throwable {
                return new BlockJUnit4ClassRunner(testClass);
            }
        }, classes);

    }

    @Override
    public Runner getRunner()  {
        return runner;
    }

}
