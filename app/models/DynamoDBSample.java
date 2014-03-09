package models;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

public class DynamoDBSample {
	
	
    public static AmazonDynamoDBClient client;
    public static void main () {
    	
    }
    
    public DynamoDBSample() throws Exception {
            
        
        createClient();
        //System.out.println("helow");
        //getVideoById("vid1");
        //getVideoByParentId("vid1") ;
        //getVideoForHomePage() ; 
        
        // Query replies posted in the past 15 days for a forum thread.
        //findRepliesInLast15DaysWithConfig("Reply", forumName, threadSubject);
          
    }
    
    public static void createClient() throws IOException {
        
        AWSCredentials credentials = new PropertiesCredentials(
                DynamoDBSample.class.getResourceAsStream("AwsCredentials.properties"));

        client = new AmazonDynamoDBClient(credentials);
    }
    public void createEntry (String name , String id, String parentId, String isParent) {
    	
    	String tableName = "twit-tube-db" ; 
    	Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
    	item.put("id", new AttributeValue().withS(id));
    	item.put("name", new AttributeValue().withS(name));
    	item.put("parentId", new AttributeValue().withS(parentId));
    	item.put("isParent", new AttributeValue().withS(isParent));
    	
    	PutItemRequest putItemRequest = new PutItemRequest()
    	  .withTableName(tableName)
    	  .withItem(item);
    	PutItemResult result = client.putItem(putItemRequest);
    	
    }
    public static videoFile getVideoById(String id) {
    	String tableName = "twit-tube-db" ;
    	Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("id", new AttributeValue().withS(id));
        
        GetItemRequest getItemRequest = new GetItemRequest()
            .withTableName(tableName)
            .withKey(key); 
            //.withAttributesToGet(Arrays.asList("id","name", "parentId", "isParent"))
            
        	
        GetItemResult result = client.getItem(getItemRequest);

        // Check the response.
        System.out.println("Printing item after retrieving it....");
        return getName(result.getItem());            
    }

    public static List<videoFile> getVideoByParentId(String id) {
    	String tableName = "twit-tube-db" ;
    	
    	Condition hashKeyCondition = new Condition()
        .withComparisonOperator(ComparisonOperator.EQ)
        .withAttributeValueList(new AttributeValue().withS(id));

    	HashMap<String, Condition> keyConditions = new HashMap<String, Condition>();
    	keyConditions.put("parentId", hashKeyCondition);
    		
    	
    	QueryRequest queryRequest = new QueryRequest()
        	.withTableName(tableName)
        	.withIndexName("parentId-index")
        	.withSelect("ALL_ATTRIBUTES")
        	.withScanIndexForward(true);
    	queryRequest.setKeyConditions(keyConditions);
    		

    QueryResult result = client.query(queryRequest);
    ArrayList<videoFile> names = new ArrayList<videoFile> ();
    for (Map<String, AttributeValue> item : result.getItems()) {
        names.add(getName(item));
    }
    return names ; 
    
   }

        public static List<videoFile> getVideoForHomePage() {
    	String tableName = "twit-tube-db" ;
    	
    	Condition hashKeyCondition = new Condition()
        .withComparisonOperator(ComparisonOperator.EQ)
        .withAttributeValueList(new AttributeValue().withS("y"));

    	HashMap<String, Condition> keyConditions = new HashMap<String, Condition>();
    	keyConditions.put("isParent", hashKeyCondition);
    		
    	
    	QueryRequest queryRequest = new QueryRequest()
        	.withTableName(tableName)
        	.withIndexName("isParent-index")
        	.withSelect("ALL_ATTRIBUTES")
        	.withScanIndexForward(true);
    	queryRequest.setKeyConditions(keyConditions);
    		

    QueryResult result = client.query(queryRequest);
    ArrayList<videoFile> names = new ArrayList<videoFile>() ;
    for (Map<String, AttributeValue> item : result.getItems()) {
        names.add(getName(item));
    }
    return names;
            
    }
    
    private static videoFile getName(Map<String, AttributeValue> attributeList) {
        String fileName = "" ;
        String fileId = "" ; 
    	for (Map.Entry<String, AttributeValue> item : attributeList.entrySet()) {
            String attributeName = item.getKey();
            AttributeValue value = item.getValue();
            if ( attributeName.equals("name")){
            	fileName = value.getS() ;
            }
            if ( attributeName.equals("id")){
            	fileId = value.getS() ;
            }
    	}
    	
    	videoFile vf = new videoFile(fileName, fileId) ; 
		return vf;
    }
}
   