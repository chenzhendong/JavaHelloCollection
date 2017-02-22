package com.popyoyo.hello.training;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

import java.util.List;

// The ReadySetGo class lists the number of buckets in your account.
public class ReadySetGo {

    // Before running the code, check that the ~/.aws/credentials file contains your credentials.

    static AmazonS3 s3;
    public static final Region BUCKET_REGION = Region.getRegion(Regions.US_EAST_1);

    private static void init() throws Exception {

        // The ProfileCredentialsProvider will return your default credential profile by reading from the ~/.aws/credentials file.

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new Exception("Cannot load AWS credentials.", e);
        }

        s3 = new AmazonS3Client(credentials);
        s3.setRegion(BUCKET_REGION);
    }

    public static void main(String[] args) throws Exception {

        System.out.println("============================================");
        System.out.println("Welcome to the AWS Java SDK! Ready, Set, Go!");
        System.out.println("============================================");

        init();

        // The Amazon S3 client allows you to manage buckets and objects programmatically.
        try {
            List<Bucket> buckets = s3.listBuckets();
            System.out.println("You have " + buckets.size() + " Amazon S3 bucket(s)");

            for(Bucket b : s3.listBuckets()){
                System.out.println(b.getName()+"::"+b.getOwner().getDisplayName());
            }
        } catch (AmazonServiceException ase) {
            // AmazonServiceException represents an error response from an AWS service.
            // AWS service received the request but either found it invalid or encountered an error trying to execute it.
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            // AmazonClientException represents an error that occurred inside the client on the local host,
            // either while trying to send the request to AWS or interpret the response.
            // For example, if no network connection is available, the client won't be able to connect to AWS to execute a request and will throw an AmazonClientException.
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
