//File Name:IntentRequestHandler.java
//Project Name:Personalized Cook book using Alexa
//Project Team:21
//Team Member:Pranjali Sanjay Raje(012440948),Premal Dattatray Samale(012566333),Vignesh Venkateswaran(012557324)
//Description:This program handles the Intent Request.It reads the intent name from input and accordigly process the request and create a response using alexaReponseProcessor
//Last Changed:May 1,2018

package edu.sjsu.mealplanner;

import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;

import edu.sjsu.mealplanner.dynamodb.DynamoDBHandler;

public class IntentRequestHandler {
	private AlexaRequestHandler alexaRequestHandler = new AlexaRequestHandler();
	private AlexaResponseProcessor alexaResponseProcessor = new AlexaResponseProcessor();
	private JsonNode input;

	public IntentRequestHandler(JsonNode input) {
		this.input = input;
	}

	public JsonNode handleIntentRequest() throws Exception {
		JsonNode request = input.get("request");
		JsonNode intent = request.get("intent");
		JsonNode response = null;
		DynamoDBHandler dynamoDBHandler = new DynamoDBHandler();
		if (intent.get("name").textValue().equals("ingredientIntent")) {
			String ingredients = alexaRequestHandler.getIngredients(intent);
			if (ingredients !=null) {
				alexaRequestHandler.setUserIdAndSessionId(input);
				ArrayList<String> ingredientList = prepareIngredientList(ingredients);
				System.out.println("ingredientList in the input: " + Arrays.toString(ingredientList.toArray()));
				dynamoDBHandler.storeIngredients(ingredientList, alexaRequestHandler.getUserId(), alexaRequestHandler.getSessionId());
				response = alexaResponseProcessor.respondIngredientIntent(ingredientList);
			} else response = alexaResponseProcessor.respondNoMatch();
		} else if (intent.get("name").textValue().equals("ingredientFinishIntent") || intent.get("name").textValue().equals("AMAZON.NoIntent")) {
			response = alexaResponseProcessor.askForCuisine();
		} else if (intent.get("name").textValue().equals("cuisineIntent")) {
			alexaRequestHandler.setUserIdAndSessionId(input);
			String cuisine = alexaRequestHandler.getCuisine(intent);
			dynamoDBHandler.storeCuisine(cuisine, alexaRequestHandler.getUserId(), alexaRequestHandler.getSessionId());
			response = alexaResponseProcessor.askForSpiceLevel();
		} else if (intent.get("name").textValue().equals("spiceLevelIntent")) {
			alexaRequestHandler.setUserIdAndSessionId(input);
			String spiceLevel = alexaRequestHandler.getSpiceLevel(intent);
			int spiceLevelNumber = getSpiceLevelNumber(spiceLevel);
			dynamoDBHandler.storeSpiceLevel(spiceLevelNumber, alexaRequestHandler.getUserId(), alexaRequestHandler.getSessionId());
			ArrayList<String> totalIngredientList = dynamoDBHandler.getTotalIngredients(alexaRequestHandler.getUserId(), alexaRequestHandler.getSessionId());
			String cuisine = dynamoDBHandler.getCuisine(alexaRequestHandler.getUserId(), alexaRequestHandler.getSessionId());
			ArrayList<String> filteredRecipeNames = dynamoDBHandler.searchRecipe(totalIngredientList, cuisine, spiceLevelNumber);
			response = alexaResponseProcessor.respondWithRecipies(filteredRecipeNames);
		} else if (intent.get("name").textValue().equals("dishIntent")) {
			String dish = alexaRequestHandler.getDish(intent);
			response = alexaResponseProcessor.respondForDish(dish);
		} else if (intent.get("name").textValue().equals("thankYouIntent")) {
			response = alexaResponseProcessor.thankYou();
		} else if (intent.get("name").textValue().equals("AMAZON.StopIntent")) {
			response = alexaResponseProcessor.okay();
		}
		return response;
	}

	private int getSpiceLevelNumber(String spiceLevel) {
		if (spiceLevel.equalsIgnoreCase("extreme") || spiceLevel.equalsIgnoreCase("extremely")) return 5;
		if (spiceLevel.equalsIgnoreCase("moderate") || spiceLevel.equalsIgnoreCase("moderately")) return 4;
		if (spiceLevel.equalsIgnoreCase("medium")) return 3;
		if (spiceLevel.equalsIgnoreCase("low")) return 2;
		if (spiceLevel.equalsIgnoreCase("no")) return 1;
		return 0;
	}

	private ArrayList<String> prepareIngredientList(String ingredients) {
		ingredients = ingredients.replaceAll("and", "");
		ingredients = ingredients.replaceAll(",", " ");
        ingredients = ingredients.replaceAll("( )+", " ");
        ingredients = ingredients.trim();
		String[] ingredientArray = ingredients.split(" ");
		ArrayList<String> ingredientList = new ArrayList<String>();
		for(int i=0; i<ingredientArray.length; i++) {
			ingredientList.add(ingredientArray[i]);
		}
		return ingredientList;
	}
}