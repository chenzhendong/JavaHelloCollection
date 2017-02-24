package com.popyoyo.hello.training.cache;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

// The DynamoDBManager class sets up a DynamoDB table and loads it with pharmaceutical data. 
public class DynamoDBManager {

    public static final String PHARMA_TABLE_NAME = "PharmaceuticalsUsage";
    public static final Region REGION = Utils.getRegion();
    public static final String S3_BUCKET_NAME = Utils.LAB_S3_BUCKET_NAME;
    public static final String S3_BUCKET_REGION = Utils.LAB_S3_BUCKET_REGION;
    public static final String PHARMA_DATA_FILE_KEY = Utils.PHARMA_DATA_FILE_KEY;

    private static DynamoDB dynamoDB = null;
    private static AmazonDynamoDBClient dynamoDBClient = null;
    private static AmazonS3Client s3 = null;

    public static String getPharmaInfo(String drugName) {
        String pharmaInfo = dynamoDB.getTable(PHARMA_TABLE_NAME).getItem("DrugName", drugName).get("Usage").toString();
        return pharmaInfo;
    }

    public static void init() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file." + "Please make sure that your credentials file is at the correct" + "location (~/.aws/credentials), and is in valid format.", e);
        }

        s3 = new AmazonS3Client(credentials);
        s3.setRegion(Region.getRegion(Regions.fromName(S3_BUCKET_REGION)));

        dynamoDBClient = new AmazonDynamoDBClient(credentials);
        dynamoDBClient.setRegion(REGION);
        dynamoDB = new DynamoDB(dynamoDBClient);
        setup();

    }

    public static void setup() {
        System.out.println("Beginning DynamoDB setup.");
        if (!pharmaDataExists()) {
            createPharmaTable();
        }
        loadData();
        System.out.println("Completed DynamoDB setup.");
    }

    private static boolean pharmaDataExists() {
        boolean pharmaDataExists = false;
        try {
            dynamoDBClient.describeTable(PHARMA_TABLE_NAME);
            pharmaDataExists = true;
        } catch (ResourceNotFoundException e) {
            System.out.println(PHARMA_TABLE_NAME + " DynamoDB table does not exist.");
        }
        return pharmaDataExists;
    }

    private static void createPharmaTable() {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

        attributeDefinitions.add(new AttributeDefinition().withAttributeName("DrugName").withAttributeType("S"));

        CreateTableRequest request = new CreateTableRequest().withTableName(PHARMA_TABLE_NAME)
                .withKeySchema(new KeySchemaElement().withAttributeName("DrugName").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
                        new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(5L));

        System.out.printf("Creating %s table. %n", PHARMA_TABLE_NAME);

        Table table = dynamoDB.createTable(request);

        try {
            table.waitForActive();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Loads pharmaceutical usage data from CSV file into DynamoDB table. 
     */
    private static void loadData() {
        S3Object pharmaDataObject = null;
        BufferedReader br = null;
        String line = "";
        String splitter = ",";

        try {

            pharmaDataObject = s3.getObject(S3_BUCKET_NAME, PHARMA_DATA_FILE_KEY);

            if (pharmaDataObject != null) {

                Table table = dynamoDB.getTable(PHARMA_TABLE_NAME);

                br = new BufferedReader(new InputStreamReader(pharmaDataObject.getObjectContent()));

                while ((line = br.readLine()) != null) {

                    String[] pharmaDataAttrValues = line.split(splitter);

                    try {

                        if (!pharmaDataAttrValues[0].equals("DrugName")) {

                            // CSV attributes: DrugName, Usage
                            Item item = new Item().withPrimaryKey("DrugName", pharmaDataAttrValues[0])
                                    .withString("Usage", pharmaDataAttrValues[1]);

                            table.putItem(item);
                            System.out.println("Added item to table:" + line);

                        }
                    } catch (Exception e) {
                        System.err.println("Failed to create item in table" + line);
                        System.err.println(e.getMessage());
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("DynamoDB data upload complete.");
    }

}
