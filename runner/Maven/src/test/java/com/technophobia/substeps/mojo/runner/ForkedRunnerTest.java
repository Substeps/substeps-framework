package com.technophobia.substeps.mojo.runner;

import com.technophobia.substeps.runner.ForkedRunner;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.mockito.Mockito;
import org.apache.maven.plugin.logging.Log;
import org.substeps.config.SubstepsConfigLoader;
import scala.Int;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


//import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.empty;
//import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.*;
import static org.mockito.Mockito.mock;


public class ForkedRunnerTest {

    private Method commandBuilderMethod;
    private static String previousEnvironment = null;

    @BeforeClass
    public static void preClassSetup(){

        previousEnvironment = SubstepsConfigLoader.getEnvironmentName();
    }

    @AfterClass
    public static void postClassTearDown(){
        System.clearProperty("environment");
        if (previousEnvironment != null) {
            System.setProperty("environment", previousEnvironment);
        }
    }


    @Before
    public void setup() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
        commandBuilderMethod = ForkedRunner.class.getDeclaredMethod("buildSubstepsRunnerCommand", Log.class, String.class, Integer.TYPE, String.class);
        commandBuilderMethod.setAccessible(true);

        System.clearProperty("environment");
        System.clearProperty("ENVIRONMENT");

    }

    private List<String> invoke(String cp, int port, String vmArgs) throws InvocationTargetException, IllegalAccessException{
        Log log = mock(Log.class);
        List<String> results =
        (List<String>) commandBuilderMethod.invoke(ForkedRunner.class, log, cp, port, vmArgs);

        for (String r : results){
            System.out.println(r);
        }

        return results;
    }

    @Test
    public void testCommandConstructionWithSingleVMArgNoEnv () throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        int port = 9292;
        List<String> results = invoke("myclasspath", port, "-XWhatever");

        Assert.assertThat(results, is(not(empty())));

        Assert.assertThat(results.get(0), endsWith("/java"));

        Assert.assertThat(results.get(2), is("-Dcom.sun.management.jmxremote.port=" + port));

        Assert.assertThat(results.get(6), is("-XWhatever"));

        Assert.assertThat(results.get(8), is("myclasspath"));

        Assert.assertThat(results.get(9), is("com.technophobia.substeps.jmx.SubstepsJMXServer"));
    }

    @Test
    public void testChainingOfEnvironmentParamWhenNoConflictingVmArg() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        int port = 9292;

        System.setProperty("environment", "travis");

        List<String> results = invoke("myclasspath", port, null);

        Assert.assertThat(results.get(6), is("-Denvironment=travis"));

    }

    @Test
    public void testChainingOfEnvironmentParamWhenConflictingVmArg() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        int port = 9292;

        System.setProperty("ENVIRONMENT", "travis");

        List<String> results = invoke("myclasspath", port, "-DENVIRONMENT=jenkins");

        Assert.assertThat(results.get(6), is("-DENVIRONMENT=jenkins"));

    }
}
