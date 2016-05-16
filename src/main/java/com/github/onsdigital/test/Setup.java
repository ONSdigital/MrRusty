package com.github.onsdigital.test;

public class Setup implements com.github.onsdigital.junit.Setup {

    public static Context context = new Context();

    @Override
    public void setup() throws Exception {
        context.setup();
    }
}
