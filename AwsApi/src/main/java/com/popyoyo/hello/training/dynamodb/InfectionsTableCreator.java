package com.popyoyo.hello.training.dynamodb;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

import java.util.ArrayList;

// The InfectionsTableCreator class creates a table and global secondary index to store data about infections.
public class InfectionsTableCreator {

    public static final String INFECTIONS_TABLE_NAME = "Infections";
    public static final String CITY_DATE_INDEX_NAME = "InfectionsByCityDate";
    public static final Region REGION = Utils.getRegion();
    public static DynamoDB dynamoDB = null;
    public static AmazonDynamoDBClient dynamoDBClient = null;

    public static void main(String[] args) throws Exception {
        init();

        // In this sample, we will use the Document API to create our designed DynamoDB table.
        try {
            removeInfectionsTableIfExists();
            createInfectionsTable();

        } catch (AmazonServiceException ase) {
            /*
             * AmazonServiceException represents an error response from an AWS service.
             * 
             * AWS service received the request but either found it invalid or encountered an error trying to execute it.
             */
            System.out.println("Error Message:" + ase.getMessage());
            System.out.println("HTTP Status Code:" + ase.getStatusCode());
            System.out.println("AWS Error Code:" + ase.getErrorCode());
            System.out.println("Error Type:" + ase.getErrorType());
            System.out.println("Request ID:" + ase.getRequestId());
        } catch (AmazonClientException ace) {
            /*
             * AmazonClientException represents an error that occurred inside the client on the local host,
             * 
             * either while trying to send the request to AWS or interpret the response.
             * 
             * For example, if no network connection is available, the client won't be able to connect to AWS to execute a request and will throw an AmazonClientException.
             */
            System.out.println("Error Message:" + ace.getMessage());
        }
    }

    private static void init() throws Exception {

        // The ProfileCredentialsProvider will return your default credential profile by reading from the ~/.aws/credentials file.
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file." + 
                    "Please make sure that your credentials file is at the correct" + "location (~/.aws/credentials), and is in valid format.", e);
        }

        // STUDENT TODO: Create an instance of AmazonDynamoDBClient class.
        dynamoDBClient = new AmazonDynamoDBClient(credentials); // @Del
        // STUDENT TODO: Set AmazonDynamoDBClient object's region using the REGION constant.
        dynamoDBClient.setRegion(REGION); // @Del
        // STUDENT TODO: Create an instance of DynamoDB class.
        dynamoDB = new DynamoDB(dynamoDBClient); // @Del
    }

    private static void removeInfectionsTableIfExists() {
        try {
            Table table = dynamoDB.getTable(INFECTIONS_TABLE_NAME);
            DescribeTableResult descTableResult = dynamoDBClient.describeTable(INFECTIONS_TABLE_NAME);
            if (descTableResult != null && descTableResult.getTable().getTableStatus().equals("ACTIVE")) {
                System.out.println("Attempting to delete DynamoDB table if one already exists.");
                table.delete();
                table.waitForDelete();
            }
        } catch (ResourceNotFoundException e) {
            System.out.printf("%s table does not exist. Do not need to remove it. \n", INFECTIONS_TABLE_NAME);
        } catch (InterruptedException ie) {
        }
    }

    private static void createInfectionsTable() {
        // STUDENT TODO: Create an instance of a GlobalSecondaryIndex class.
        GlobalSecondaryIndex gsi = new GlobalSecondaryIndex() // @Del
                .withIndexName(CITY_DATE_INDEX_NAME) // @Del
                .withKeySchema(new KeySchemaElement("City", KeyType.HASH), new KeySchemaElement("Date", KeyType.RANGE)) // @Del
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L)) // @Del
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL)); // @Del

        // STUDENT TODO: Create attribute definitions for PatientId, City, Date.
        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>(); // @Del

        attributeDefinitions.add(new AttributeDefinition().withAttributeName("PatientId").withAttributeType("S")); // @Del
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("City").withAttributeType("S")); // @Del
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("Date").withAttributeType("S")); // @Del

        // STUDENT TODO: Create an instance of CreateTableRequest class.
        CreateTableRequest request = new CreateTableRequest() // @Del
                .withTableName(INFECTIONS_TABLE_NAME) // @Del
                .withKeySchema(new KeySchemaElement().withAttributeName("PatientId").withKeyType(KeyType.HASH)) // @Del
                .withAttributeDefinitions(attributeDefinitions) // @Del
                .withProvisionedThroughput(new ProvisionedThroughput() // @Del
                        .withReadCapacityUnits(5L) // @Del
                        .withWriteCapacityUnits(10L)) // @Del
                .withGlobalSecondaryIndexes(gsi); // @Del

        System.out.println("Creating DynamoDB table.");

        // STUDENT TODO: Create table using the request defined.
        Table table = dynamoDB.createTable(request); // @Del

        // STUDENT TODO: Invoke the method to wait for table to become active.
        try { // @Del
            table.waitForActive(); // @Del
        } catch (InterruptedException ie) { // @Del
            ie.printStackTrace(); // @Del
        } // @Del
    }

}
