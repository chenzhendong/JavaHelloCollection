package com.popyoyo.hello.training.dynamodb;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

// The PatientReportLinker class updates DynamoDB items with the corresponding link to a patient's report on S3.
public class PatientReportLinker {

    public static final String INFECTIONS_TABLE_NAME = InfectionsTableCreator.INFECTIONS_TABLE_NAME;
    public static final String PATIENT_REPORT_PREFIX = Utils.PATIENT_REPORT_PREFIX;
    public static final String S3_BUCKET_NAME = Utils.LAB_S3_BUCKET_NAME;
    public static final String S3_BUCKET_REGION = Utils.LAB_S3_BUCKET_REGION;

    public static final Region REGION = Utils.getRegion();

    public static DynamoDB dynamoDB = null;
    private static AmazonDynamoDBClient dynamoDBClient = null;

    public static void main(String[] args) throws Exception {
        init();
        linkPatientReport();
    }

    private static void init() throws Exception {

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file." + "Please make sure that your credentials file is at the correct" + "location (~/.aws/credentials), and is in valid format.", e);
        }

        dynamoDBClient = new AmazonDynamoDBClient(credentials);
        dynamoDBClient.setRegion(REGION);
        dynamoDB = new DynamoDB(dynamoDBClient);
    }

    private static void linkPatientReport() {
        String reportUrl = null;
        String objectKey = null;

        // Sample reports exist for patient ids 1, 2, 3
        for (int i = 1; i < 4; i++) {
            objectKey = PATIENT_REPORT_PREFIX + i + ".txt";
            // STUDENT TODO: Construct the URL for the patient report.
            reportUrl = "https://s3-" + S3_BUCKET_REGION + ".amazonaws.com/" + S3_BUCKET_NAME + "/" + objectKey; // @Del
            updateItemWithLink(("" + i), reportUrl);
        }
    }

    private static void updateItemWithLink(String patientId, String reportUrl) {
        System.out.printf("Updating item patientId: %s, reportURL: %s %n", patientId, reportUrl);

        // STUDENT TODO: Retrieve a reference to the infections table by using the INFECTIONS_TABLE_NAME constant.
        Table table = dynamoDB.getTable(INFECTIONS_TABLE_NAME); // @Del

        // STUDENT TODO: Create an instance of the UpdateItemSpec class to add an attribute called PatientReportUrl and the attribute's value.
        // Use patientId as the primary key.
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("PatientId", patientId) // @Del
                .withUpdateExpression("set #purl = :val1").withNameMap(new NameMap().with("#purl", "PatientReportUrl")) // @Del
                .withValueMap(new ValueMap().withString(":val1", reportUrl)).withReturnValues(ReturnValue.ALL_NEW); // @Del

        // STUDENT TODO: Update the item in the table.
        UpdateItemOutcome outcome = table.updateItem(updateItemSpec); // @Del

        System.out.println("Printing item after adding attribute:");

        // STUDENT TODO: Retrieve the item from the UpdateItemOutcome object and print its contents in JSON format.
        System.out.println(outcome.getItem().toJSONPretty()); // @Del
    }
}
