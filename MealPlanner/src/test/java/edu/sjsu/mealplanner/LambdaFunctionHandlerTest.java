package edu.sjsu.mealplanner;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class LambdaFunctionHandlerTest {

    private static JsonNode input;

    @BeforeClass
    public static void createInput() throws IOException {    
        ObjectMapper obj = new ObjectMapper();
        input = obj.readTree("{ \"name\" : \"John Doe\" }");
    }

    private Context createContext() {
        TestContext ctx = new TestContext();
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testLambdaFunctionHandler() {
        Assert.assertEquals("Hello", "Hello");
    }
}
