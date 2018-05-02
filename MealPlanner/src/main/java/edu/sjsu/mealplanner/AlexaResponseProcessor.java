//File Name:AlexaResponseProcessor.java
//Email Address:premaldattatray.samale@sjsu.edu
//Project Name:Personalized Cook book using Alexa
//Project Team:21
//Team Member:Pranjali Sanjay Raje(012440948),Premal Dattatray Samale(012566333),Vignesh Venkateswaran(012557324)
//Description:This program creates the Alexa response for each intents.AlexaResponseProcessor calls the methods of DyanamoDBHandler and  
//fetch the requested data from dynamo db and that data is passes inside response.
//Last Changed:May 1,2018


package edu.sjsu.mealplanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.sjsu.mealplanner.dynamodb.DynamoDBHandler;

public class AlexaResponseProcessor {
	private static final String LAUNCH_SPEECH = "Hello. I can help you prepare your meal. What ingredients do you have ?";
	private static final String INGREDIENT_PARTIAL_SPEECH = "So you have ";
	private static final String NO_MATCH_REQUEST_SPEECH = "Sorry we couldn't understand your request";
	private static final String NO_RECIPE_FOUND_SPEECH = "Sorry we couldn't find a recipe with the combination you want";
	private static final String CUISINE_SPEECH = "What cuisine do you want ? We have Chinese, Mexican, Indian and Italian cuisine";
	private static final String SPICE_LEVEL_SPEECH = "What Spice level do you want ? "
			+ "You can say I want extremely spicy, moderately spicy, medium spicy, low spicy, no spicy";
	private static final String RECIPE_PROCESS_PREFIX = "Here is how you can prepare ";
	private static final String RECIPE_PROCESS_SUFFIX = "Your delicious recipe is now ready";
	private static final String YOU_ARE_WELCOME = "You are welcome";
	private static final String OKAY = "Okay";
	private boolean shouldEndSession;

	private JsonNode createResponse(String speechText) {
		ObjectNode outputSpeech = new ObjectMapper().createObjectNode();
		outputSpeech.put("type", "SSML");
		outputSpeech.put("ssml", "<speak>" + speechText + "</speak>");
		
		ObjectNode response = new ObjectMapper().createObjectNode();
		response.put("shouldEndSession", this.shouldEndSession);
		response.set("outputSpeech", (JsonNode) outputSpeech);
		
		ObjectNode output = new ObjectMapper().createObjectNode();
		output.set("response", (JsonNode) response);
		return output;
	}

	public JsonNode respondLaunchRequest() throws IOException {
		this.setShouldEndSession(false);
		return this.createResponse(LAUNCH_SPEECH);
	}

	public JsonNode respondIngredientIntent(ArrayList<String> ingrdientList) throws IOException {
		this.setShouldEndSession(false);
		return this.createResponse(INGREDIENT_PARTIAL_SPEECH + Arrays.toString(ingrdientList.toArray()) + ". Do you have anything else?");
	}

	public JsonNode respondWithRecipies(ArrayList<String> filteredRecipeNames) throws Exception {
		if (filteredRecipeNames.size() == 0 || filteredRecipeNames == null) {
			return respondWithSpeech(NO_RECIPE_FOUND_SPEECH);
		} else if (filteredRecipeNames.size() == 1) {
			return respondForDish(filteredRecipeNames.get(0));
		} else {
			return respondWithSpeech("You can prepare " + filteredRecipeNames + ". Which one would you like?");
		}
	}

	public JsonNode respondForDish(String dish) throws Exception {
		DynamoDBHandler dynamoDBHandler = new DynamoDBHandler();
		String dishProcess = dynamoDBHandler.getDishProcess(dish);
		return respondWithSpeech(RECIPE_PROCESS_PREFIX + dish + ". " + dishProcess + ". " + RECIPE_PROCESS_SUFFIX);
	}

	public JsonNode respondWithSpeech(String speech) {
		return this.createResponse(speech);
	}

	public JsonNode respondNoMatch() {
		return this.createResponse(NO_MATCH_REQUEST_SPEECH);
	}

	public boolean isShouldEndSession() {
		return shouldEndSession;
	}

	public void setShouldEndSession(boolean shouldEndSession) {
		this.shouldEndSession = shouldEndSession;
	}

	public JsonNode askForCuisine() {
		return respondWithSpeech(CUISINE_SPEECH);
	}

	public JsonNode askForSpiceLevel() {
		return respondWithSpeech(SPICE_LEVEL_SPEECH);
	}

	public JsonNode thankYou() {
		this.setShouldEndSession(true);
		return this.respondWithSpeech(YOU_ARE_WELCOME);
	}

	public JsonNode okay() {
		this.setShouldEndSession(true);
		return this.respondWithSpeech(OKAY);
	}
}
