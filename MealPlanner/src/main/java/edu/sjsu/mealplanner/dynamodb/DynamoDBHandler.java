//File Name:DynamoDBHandler.java
//Project Name:Personalized Cook book using Alexa
//Project Team:21
//Team Member:Pranjali Sanjay Raje(012440948),Premal Dattatray Samale(012566333),Vignesh Venkateswaran(012557324)
//Description:This program communicates with Dyanamo DB to fetch and store recipe data like ingredients,recipe,cuisine etc.
//Last Changed:May 1,2018


package edu.sjsu.mealplanner.dynamodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

public class DynamoDBHandler {
	private static final String USER = "user";
	private static final String USER_ID = "userId";
	private static final String SESSION_ID = "sessionId";
	private static final String INGREDIENT_LIST = "ingredientList";
	private static final String CUISINE = "cuisine";
	private static final String SPICE_LEVEL = "spiceLevel";

	private static final String RECIPE = "recipe";
	private static final String RECIPE_NAME = "recipeName";
	private static final String PROCEDURE = "procedure";

	private AmazonDynamoDB dynamoDB;

	private void init() throws Exception {
		EnvironmentVariableCredentialsProvider credentialsProvider = new EnvironmentVariableCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (/Users/sgsh/.aws/credentials), and is in valid format.",
							e);
		}
		dynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withCredentials(credentialsProvider)
				.withRegion("us-east-1")
				.build();
	}

	private Map<String, AttributeValue> newItem(String userId, String sessionId, ArrayList<String> ingredientList) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put(USER_ID, new AttributeValue(userId));
		item.put(SESSION_ID, new AttributeValue(sessionId));
		item.put(INGREDIENT_LIST, new AttributeValue().withSS(ingredientList));

		return item;
	}

	private Map<String, AttributeValue> newItemWithCuisine(String userId, String sessionId, List<String> existingIngredients, String cuisine) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put(USER_ID, new AttributeValue(userId));
		item.put(SESSION_ID, new AttributeValue(sessionId));
		item.put(INGREDIENT_LIST, new AttributeValue().withSS(existingIngredients));
		item.put(CUISINE, new AttributeValue().withS(cuisine));

		return item;
	}

	private Map<String, AttributeValue> newItemWithSpiceLevel(String userId, String sessionId,
	        List<String> existingIngredients, String cuisine, int spiceLevelNumber) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put(USER_ID, new AttributeValue(userId));
		item.put(SESSION_ID, new AttributeValue(sessionId));
		item.put(INGREDIENT_LIST, new AttributeValue().withSS(existingIngredients));
		item.put(CUISINE, new AttributeValue().withS(cuisine));
		item.put(SPICE_LEVEL, new AttributeValue().withN(""+spiceLevelNumber));

		return item;
	}

	public void createTable(String userId, String sessionId) throws Exception {
		init();

		String tableName = USER;

		// Create a table with a primary hash key named 'userId', which holds a string
		CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
				.withKeySchema(new KeySchemaElement().withAttributeName(USER_ID).withKeyType(KeyType.HASH))
				.withKeySchema(new KeySchemaElement().withAttributeName(SESSION_ID).withKeyType(KeyType.RANGE))
				.withAttributeDefinitions(new AttributeDefinition().withAttributeName(USER_ID).withAttributeType(ScalarAttributeType.S))
				.withAttributeDefinitions(new AttributeDefinition().withAttributeName(SESSION_ID).withAttributeType(ScalarAttributeType.S))
				.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

		// Create table if it does not exist yet
		TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);

		// Describe our new table
		DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
		TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
		System.out.println("Table Description: " + tableDescription);
	}

	public void storeIngredients(ArrayList<String> ingredientList, String userId, String sessionId) throws Exception {
		init();

		try {
			String tableName = USER;

			HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
			Condition userIdCondition = new Condition()
					.withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(userId));
			Condition sessionIdCondition = new Condition()
					.withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(sessionId));
			scanFilter.put(USER_ID, userIdCondition);
			scanFilter.put(SESSION_ID, sessionIdCondition);
			ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			ScanResult scanResult = dynamoDB.scan(scanRequest);
			System.out.println("Scan Result: " + scanResult);

			if (scanResult.getCount() == 0) { // no item present for userId and sessionId combination
				Map<String, AttributeValue> item = newItem(userId, sessionId, ingredientList);
				PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
				dynamoDB.putItem(putItemRequest);
			} else {
				Map<String, AttributeValue> map = scanResult.getItems().get(0);
				AttributeValue atrValue = map.get(INGREDIENT_LIST);
				List<String> existingIngredients = atrValue.getSS();
				ingredientList.addAll(existingIngredients);
				Map<String, AttributeValue> item = newItem(userId, sessionId, ingredientList);
				PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
				dynamoDB.putItem(putItemRequest);
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public void storeCuisine(String cuisine, String userId, String sessionId) throws Exception {
		init();

		try {
			String tableName = USER;

			HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
			Condition userIdCondition = new Condition()
					.withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(userId));
			Condition sessionIdCondition = new Condition()
					.withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(sessionId));
			scanFilter.put(USER_ID, userIdCondition);
			scanFilter.put(SESSION_ID, sessionIdCondition);
			ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			ScanResult scanResult = dynamoDB.scan(scanRequest);
			System.out.println("Cuisine Scan Result: " + scanResult);

			Map<String, AttributeValue> map = scanResult.getItems().get(0);
			AttributeValue atrValue = map.get(INGREDIENT_LIST);
			List<String> existingIngredients = atrValue.getSS();
			Map<String, AttributeValue> item = newItemWithCuisine(userId, sessionId, existingIngredients, cuisine);
			PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
			dynamoDB.putItem(putItemRequest);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public void storeSpiceLevel(int spiceLevelNumber, String userId, String sessionId) throws Exception {
		init();

		try {
			String tableName = USER;

			HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
			Condition userIdCondition = new Condition()
					.withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(userId));
			Condition sessionIdCondition = new Condition()
					.withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(sessionId));
			scanFilter.put(USER_ID, userIdCondition);
			scanFilter.put(SESSION_ID, sessionIdCondition);
			ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			ScanResult scanResult = dynamoDB.scan(scanRequest);
			System.out.println("Spice Level Scan Result: " + scanResult);

			Map<String, AttributeValue> map = scanResult.getItems().get(0);
			AttributeValue atrValue = map.get(INGREDIENT_LIST);
			List<String> existingIngredients = atrValue.getSS();
			AttributeValue cuisineAtrValue = map.get(CUISINE);
			String cuisine = cuisineAtrValue.getS();
			Map<String, AttributeValue> item = newItemWithSpiceLevel(userId, sessionId, existingIngredients, cuisine, spiceLevelNumber);
			PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
			dynamoDB.putItem(putItemRequest);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public ArrayList<String> getTotalIngredients(String userId, String sessionId) throws Exception {
		init();

		String tableName = USER;

		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		Condition userIdCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(userId));
		Condition sessionIdCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(sessionId));
		scanFilter.put(USER_ID, userIdCondition);
		scanFilter.put(SESSION_ID, sessionIdCondition);
		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
		ScanResult scanResult = dynamoDB.scan(scanRequest);
		System.out.println("Total Ingredients Scan Result: " + scanResult);

		Map<String, AttributeValue> map = scanResult.getItems().get(0);
		AttributeValue atrValue = map.get(INGREDIENT_LIST);
		List<String> existingIngredients = atrValue.getSS();
		ArrayList<String> totalIngredients = new ArrayList<String>();
		totalIngredients.addAll(existingIngredients);

		return totalIngredients;
	}

	public String getCuisine(String userId, String sessionId) throws Exception {
		init();

		String tableName = USER;

		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		Condition userIdCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(userId));
		Condition sessionIdCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(sessionId));
		scanFilter.put(USER_ID, userIdCondition);
		scanFilter.put(SESSION_ID, sessionIdCondition);
		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
		ScanResult scanResult = dynamoDB.scan(scanRequest);
		System.out.println("Total Ingredients Scan Result: " + scanResult);

		Map<String, AttributeValue> map = scanResult.getItems().get(0);
		AttributeValue atrValue = map.get(CUISINE);
		return atrValue.getS();
	}

	public void createRecipeTable() throws Exception {
		init();

		String tableName = RECIPE;

		// Create a table with a primary hash key named 'userId', which holds a string
		CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
				.withKeySchema(new KeySchemaElement().withAttributeName(RECIPE_NAME).withKeyType(KeyType.HASH))
				.withAttributeDefinitions(new AttributeDefinition().withAttributeName(RECIPE_NAME).withAttributeType(ScalarAttributeType.S))
				.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

		// Create table if it does not exist yet
		TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);

		// Describe our new table
		DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
		TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
		System.out.println("Recipe Table Description: " + tableDescription);
	}

	public ArrayList<String> searchRecipe(ArrayList<String> totalIngredientList, String cuisine, int spiceLevelNumber) throws Exception {
		init();

		try {
			String tableName = RECIPE;

			ScanRequest scanRequest = new ScanRequest(tableName);
			ScanResult scanResult = dynamoDB.scan(scanRequest);
			ArrayList<Map<String, AttributeValue>> filteredRecipes = new ArrayList<Map<String, AttributeValue>>();
			ArrayList<String> filteredRecipeNames = new ArrayList<String>();

			for (Map<String, AttributeValue> recipe: scanResult.getItems()) {
				AttributeValue ingredientAtrValue = recipe.get(INGREDIENT_LIST);
				List<String> recipeIngredients = ingredientAtrValue.getSS();
				AttributeValue cuisineAtrValue = recipe.get(CUISINE);
				String recipeCuisine = cuisineAtrValue.getS();
				AttributeValue cuisineSpiceLevelNumberAtrValue = recipe.get(SPICE_LEVEL);
				String recipeSpiceLevelNumber = cuisineSpiceLevelNumberAtrValue.getN();
				if (containsAll(totalIngredientList, recipeIngredients)
						&& cuisine.equalsIgnoreCase(recipeCuisine)
						&& String.valueOf(spiceLevelNumber).equalsIgnoreCase(recipeSpiceLevelNumber)) {
					filteredRecipes.add(recipe);
				}
			}
			for (Map<String, AttributeValue> recipe: filteredRecipes) {
				AttributeValue atrValue = recipe.get(RECIPE_NAME);
				filteredRecipeNames.add(atrValue.getS());
			}

			return filteredRecipeNames;
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		return null;
	}

	private boolean containsAll(ArrayList<String> totalIngredientList, List<String> recipeIngredients) {
		if (recipeIngredients.containsAll(totalIngredientList)) return true;
		return false;
	}

	public String getDishProcess(String dish) throws Exception {
		init();

		String tableName = RECIPE;

		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		Condition recipeNameCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(dish));
		scanFilter.put(RECIPE_NAME, recipeNameCondition);
		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
		ScanResult scanResult = dynamoDB.scan(scanRequest);

		Map<String, AttributeValue> map = scanResult.getItems().get(0);
		AttributeValue atrValue = map.get(PROCEDURE);
		return atrValue.getS();
	}
}