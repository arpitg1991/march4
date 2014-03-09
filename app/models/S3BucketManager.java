package models;
import java.io.File;
import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;


public class S3BucketManager {
	public static AmazonS3Client s3;
	public String bucket_name = "ag3418";
	
	public S3BucketManager() throws IOException
	{
		AWSCredentials	credentials = new PropertiesCredentials(S3BucketManager.class.getResourceAsStream("AwsCredentials.properties"));
		s3 = new AmazonS3Client(credentials);				
	}

	public void createBucket()
	{
		//create bucket
		s3.createBucket(bucket_name);
	}
	
	public void putObject(String key, File file)
	{		
		try {
			//put object - bucket, key, value(file)
			System.out.println("Putting object on S3");
			s3.putObject(new PutObjectRequest(bucket_name, key, file).withCannedAcl(CannedAccessControlList.PublicRead));
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
