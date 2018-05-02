//File Name:AlexaRequestHandler.java
//Project Name:Personalized Cook book using Alexa
//Project Team:21
//Team Member:Pranjali Sanjay Raje(012440948),Premal Dattatray Samale(012566333),Vignesh Venkateswaran(012557324)
//Description:This program handles the Alexa request.It gets the slot value of intents from input request which is in the form of JsonNode. 
//Last Changed:May 1,2018


package edu.sjsu.mealplanner;

import com.fasterxml.jackson.databind.JsonNode;

public class AlexaRequestHandler {
	private String userId;
	private String sessionId;

	public String getRequestType(JsonNode input) {
        JsonNode request = input.get("request");
        return request.get("type").textValue();
    }

    public String getIngredients(JsonNode intent) {
        JsonNode slots = intent.get("slots");
        JsonNode food = slots.get("food");
        if (food.has("value")) return food.get("value").textValue();
        return null;
    }

	public String getDish(JsonNode intent) {
		JsonNode slots = intent.get("slots");
        JsonNode dish = slots.get("dish");
        if (dish.has("value")) return dish.get("value").textValue();
		return null;
	}

	public String getCuisine(JsonNode intent) {
		JsonNode slots = intent.get("slots");
        JsonNode cuisine = slots.get("cuisine");
        if (cuisine.has("value")) return cuisine.get("value").textValue();
		return null;
	}

	public String getSpiceLevel(JsonNode intent) {
		JsonNode slots = intent.get("slots");
        JsonNode spiceLevel = slots.get("spiceLevel");
        if (spiceLevel.has("value")) return spiceLevel.get("value").textValue();
		return null;
	}

	public void setUserIdAndSessionId(JsonNode input) {
		JsonNode session = input.get("session");
		setSessionId(session.get("sessionId").textValue());
		JsonNode user = session.get("user");
		setUserId(user.get("userId").textValue());
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}