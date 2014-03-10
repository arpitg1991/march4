package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import play.mvc.Controller;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.ListTopicsRequest;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Topic;

public class Notifications extends Controller {
	static AmazonSNS snsService; 
	public static void createSNSService(String emailId, String topicName) throws Exception{
		try {
			snsService = new AmazonSNSClient(
					new PropertiesCredentials(Notifications.class.getResourceAsStream("AwsCredentials.properties")));

			List<Topic> topics = listTopics(snsService);
			// Subscribe to the topic selected
			for (Topic topic : topics) {
				if (topic.getTopicArn().endsWith(topicName)) {
					SubscribeResult sr = snsService.subscribe(new SubscribeRequest(topic.getTopicArn(), "email", emailId));
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

		redirect("/chat/" + topicName);
	}

	public static void sendNotification(AmazonSNS snsService, String topicName) {
		System.out.println("In send");
		
		List<Topic> topics = listTopics(snsService);
		// Subscribe to the topic selected
		for (Topic topic : topics) {
			if (topic.getTopicArn().endsWith(topicName)) {
				snsService.publish(new PublishRequest(topic.getTopicArn(),
						"New Notification! Message from " + topic.getTopicArn()));
			}
		}	
	}

	public static void createTopic( String topicName)
	{
		System.out.println("In create");
		
		if (snsService == null)
			System.out.println("AKSDAKJDKAJSDKAJSDKAJDKASSJDKASSJD");
		// Create a topic
		CreateTopicRequest createReq = new CreateTopicRequest()
		.withName(topicName);
		CreateTopicResult createRes = snsService.createTopic(createReq);
	}

	public static List<Topic> listTopics(AmazonSNS snsService) {
		List<Topic> topics = new ArrayList<Topic>();
		String nextToken = null;

		do {

			// create the request, with nextToken if not empty
			ListTopicsRequest request = new ListTopicsRequest();
			if (nextToken != null) request = request.withNextToken(nextToken);

			// call the web service
			ListTopicsResult result = snsService.listTopics(request);

			nextToken = result.getNextToken();

			// get that list of topics
			topics.addAll(result.getTopics());

			// go on if there are more elements    
		} while (nextToken != null);

		System.out.println("Topics: " + topics);

		// show the list of topics...

		return topics;
	}
}