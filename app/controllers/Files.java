package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Document;
import models.DynamoDBSample;
import models.S3BucketManager;
import models.videoFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.ListTopicsRequest;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;

import play.libs.MimeTypes;
//import play.modules.s3blobs.S3Blob;
import play.mvc.Controller;

public class Files extends Controller
{

	public static void uploadForm()
	{
		render();
	}

	public static void doUpload(File file, String comment, String ParentId ) throws Exception
	{
		java.util.Date date= new java.util.Date();
		Timestamp t1 = new Timestamp(date.getTime());
		String FileId = 	t1.toString() + file.getName();  
		FileId = FileId.replaceAll("\\s+","");
		String fileName = file.getName() ; 
		String isParent = "n" ; 

		S3BucketManager s3BM = new S3BucketManager(); 
		s3BM.putObject(FileId, file) ; 
		DynamoDBSample dbInstance = new DynamoDBSample() ;
		dbInstance.createEntry( fileName, FileId,ParentId, isParent);
		AmazonSNSClient snsService = new AmazonSNSClient(
				new PropertiesCredentials(Notifications.class.getResourceAsStream("AwsCredentials.properties")));


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

		// Send Notification to the topic selected
		for (Topic topic : topics) {
			if (topic.getTopicArn().endsWith(ParentId)) {
				snsService.publish(new PublishRequest(topic.getTopicArn(),
						"New Notification! Message from " + topic.getTopicArn()));
			}
		}	


		listUploadsVideos(ParentId);
	}

	public static void doUploadNewChat(File file, String comment) throws Exception
	{
		java.util.Date date= new java.util.Date();
		Timestamp t1 = new Timestamp(date.getTime());
		String FileId = 	t1.toString() + file.getName();  
		FileId = FileId.replaceAll("\\s+","");
		FileId = FileId.replaceAll("[^a-zA-Z0-9-_]","");
		System.out.println(FileId);
		String ParentId = "none" ; 
		String isParent = "y" ;
		String fileName = file.getName() ; 
		S3BucketManager s3BM = new S3BucketManager(); 
		s3BM.putObject(FileId, file) ; 
		DynamoDBSample dbInstance = new DynamoDBSample() ;
		dbInstance.createEntry( fileName, FileId,ParentId, isParent);
		AmazonSNSClient snsService = new AmazonSNSClient(
				new PropertiesCredentials(Notifications.class.getResourceAsStream("AwsCredentials.properties")));

		CreateTopicRequest createReq = new CreateTopicRequest()
		.withName(FileId) ; 
		CreateTopicResult createRes = snsService.createTopic(createReq) ;

		listUploads();
	}

	public static void listUploadsVideos(String ParentId) throws Exception
	{
		DynamoDBSample dbInstance = new DynamoDBSample() ;
		List<videoFile> replyThreads = dbInstance.getVideoByParentId(ParentId) ;	
		//List<videoFile> replyThreads  = null ;
		System.out.println("***************************"  + ParentId) ;
		videoFile mainThread = dbInstance.getVideoById(ParentId) ; 
		//List<Document> docs = Document.findAll();
		
		render(ParentId, replyThreads, mainThread);

	}
	public static void listUploads() throws Exception
	{
		DynamoDBSample dbInstance = new DynamoDBSample() ;
		List<videoFile> names = dbInstance.getVideoForHomePage() ;	



		//List<Document> docs = Document.findAll();
		render(names);
	}
	public static void downloadFile(long id)
	{
		final Document doc = Document.findById(id);
		notFoundIfNull(doc);
		//response.setContentTypeIfNotSet(doc.file.type());
		//renderBinary(doc.file.get(), doc.fileName);
	}
}
