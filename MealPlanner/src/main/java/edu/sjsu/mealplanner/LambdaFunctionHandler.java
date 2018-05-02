//File Name:LambdaFunctionHandler.java
//Project Name:Personalized Cook book using Alexa
//Project Team:21
//Team Member:Pranjali Sanjay Raje(012440948),Premal Dattatray Samale(012566333),Vignesh Venkateswaran(012557324)
//Description:This program is the entry point for Lambda function, and it will be invoked by Lambda in response to input from the Alexa.
//Last Changed:May 1,2018

package edu.sjsu.mealplanner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class LambdaFunctionHandler {
	private AlexaResponseProcessor alexaResponseProcessor;
	private AlexaRequestHandler alexaRequestHandler;
	private LambdaLogger logger;
	private OutputStreamWriter writer;
	private JsonNode input;

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws Exception {
		LambdaFunctionHandler lambdaFunctionHandler = new LambdaFunctionHandler();
		lambdaFunctionHandler.processVoiceInput(inputStream, outputStream, context);
	}

	private void processVoiceInput(InputStream inputStream, OutputStream outputStream, Context context) throws Exception {
		setup(inputStream, outputStream, context);

		JsonNode response = null;
		String requestType = alexaRequestHandler.getRequestType(input);
		if (requestType.equals("LaunchRequest")) {
			response = alexaResponseProcessor.respondLaunchRequest();
		} else if (requestType.equals("IntentRequest")) {
			IntentRequestHandler intentRequestHandler = new IntentRequestHandler(input);
			response = intentRequestHandler.handleIntentRequest();
		}
	    writer.write(response.toString());
		writer.close();
	}

	private void setup(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		alexaResponseProcessor = new AlexaResponseProcessor();
		alexaRequestHandler = new AlexaRequestHandler();
		logger = context.getLogger();
        StringWriter stWriter = new StringWriter();
		IOUtils.copy(inputStream, stWriter, "UTF-8");
		logger.log("Input: " + stWriter.toString());
        writer = new OutputStreamWriter(outputStream, "UTF-8");
        ObjectMapper obj = new ObjectMapper();
        input = obj.readTree(stWriter.toString());
	}
}