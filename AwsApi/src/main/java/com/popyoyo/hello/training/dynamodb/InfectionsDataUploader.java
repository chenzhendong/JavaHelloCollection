package com.popyoyo.hello.training.dynamodb;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

// The InfectionsDataUploader class reads infections data from a file and uploads each item to the infections table.
public class InfectionsDataUploader {

    public static final String INFECTIONS_TABLE_NAME = InfectionsTableCreator.INFECTIONS_TABLE_NAME;
    public static final Region REGION = Utils.getRegion();
    public static final String S3_BUCKET_NAME = Utils.LAB_S3_BUCKET_NAME;
    public static final String S3_BUCKET_REGION = Utils.LAB_S3_BUCKET_REGION;
    public static final String INFECTIONS_DATA_FILE_KEY = Utils.INFECTIONS_DATA_FILE_KEY;

    private static DynamoDB dynamoDB = null;
    private static AmazonDynamoDBClient dynamoDBClient = null;
    private static AmazonS3Client s3 = null;

    public static int numFailures = 0;

    public static void main(String[] args) throws Exception {
        init();
        loadInfectionsData();

    }

    private static void init() throws Exception {

        // The ProfileCredentialsProvider will return your default credential profile by reading from the ~/.aws/credentials file.
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file." + "Please make sure that your credentials file is at the correct" + "location (~/.aws/credentials), and is in valid format.", e);
        }

        dynamoDBClient = new AmazonDynamoDBClient(credentials);
        dynamoDBClient.setRegion(REGION);
        dynamoDB = new DynamoDB(dynamoDBClient);
        // STUDENT TODO: Create an instance of AmazonS3Client class.
        s3 = new AmazonS3Client(credentials); // @Del
        s3.setRegion(Region.getRegion(Regions.fromName(S3_BUCKET_REGION))); // @Del
    }

    /**
     * Loads infection data from CSV file.
     */
    private static void loadInfectionsData() {
        S3Object infectionsDataObject = null;
        BufferedReader br = null;
        String line = "";
        String splitter = ",";

        try {

            // STUDENT TODO: Using the AmazonS3Client object, retrieve the infections data file from the S3 bucket.
            infectionsDataObject = s3.getObject(S3_BUCKET_NAME, INFECTIONS_DATA_FILE_KEY); // @Del

            if (infectionsDataObject != null) {

                Table table = dynamoDB.getTable(INFECTIONS_TABLE_NAME);

                br = new BufferedReader(new InputStreamReader(infectionsDataObject.getObjectContent()));
                // Skip first line because it has the attribute names only.
                br.readLine();

                while ((line = br.readLine()) != null) {

                    // separate CSV values by comma
                    String[] infectionsDataAttrValues = line.split(splitter);

                    try {
                        if (!infectionsDataAttrValues[0].equals("PatientId")) {

                            // STUDENT TODO: Create an instance of a DynamoDB item with attribute values read from the infections data file.
                            // CSV attributes: PatientId, City, Date
                            Item item = new Item() // @Del
                                    .withPrimaryKey("PatientId", infectionsDataAttrValues[0]) // @Del
                                    .withString("City", infectionsDataAttrValues[1]) // @Del
                                    .withString("Date", infectionsDataAttrValues[2]); // @Del

                            // STUDENT TODO: Add the item to the infections data table.
                            table.putItem(item); // @Del
                            System.out.println("Added item:" + line);

                        }
                    } catch (Exception e) {
                        System.err.println("Failed to create item in" + INFECTIONS_TABLE_NAME);
                        System.err.println(e.getMessage());
                        numFailures++;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            numFailures = 9999;
        } catch (IOException e) {
            e.printStackTrace();
            numFailures = 9999;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Infections data upload complete.");
    }

}
