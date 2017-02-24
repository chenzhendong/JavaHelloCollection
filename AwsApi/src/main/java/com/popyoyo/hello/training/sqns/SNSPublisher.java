package com.popyoyo.hello.training.sqns;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// The SNSPublisher class publishes messages to SNS topics.
public class SNSPublisher {

    private static final Region REGION = Utils.getRegion();
    // STUDENT TODO: Set ARN for SNS topic for email messages.
    private static final String TOPIC_ARN_EMAIL = "arn:aws:sns:us-west-2:267344717512:EmailSNSTopic";
    // STUDENT TODO: Set ARN for SNS topic for order messages.
    private static final String TOPIC_ARN_ORDER = "arn:aws:sns:us-west-2:267344717512:OrderSNSTopic";
    
    private static final String EMAIL_SUBJECT = "Status of pharmaceuticals order.";
    private static final String EMAIL_MESSAGE = "Your pharmaceutical supplies will be shipped 5 business days from the date of order.";
    private static final String ORDER_DETAILS = "Ibuprofen, Acetaminophen"; 
    
    public static final int NUM_MESSAGES = 10;
    
    private AmazonSNSClient snsClient = null;
    
    public static void main(String[] args) throws Exception {
        SNSPublisher snsPublisher = new SNSPublisher();
        snsPublisher.init();
        snsPublisher.publishMessages();
    }
    
    private void init() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file." +
                            "Please make sure that your credentials file is at the correct" +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        //STUDENT TODO: Create an instance of the AmazonSNSClient class and set its region using the REGION constant.
        snsClient = new AmazonSNSClient(credentials);   // @Del
        snsClient.setRegion(REGION);   // @Del
    }

    private void publishMessages() throws Exception {
        publishEmailMessage();
        publishOrderMessages();
    }
   
    private void publishEmailMessage() {
        // STUDENT TODO: Publish a message to the SNS topic for email messages. Use the EMAIL_MESSAGE and EMAIL_SUBJECT constants as email content.
        snsClient.publish(TOPIC_ARN_EMAIL, EMAIL_MESSAGE, EMAIL_SUBJECT);   // @Del  
    }
    
    private void publishOrderMessages() throws JsonProcessingException {
        // STUDENT TODO: Create an instance of the com.fasterxml.jackson.databind.ObjectMapper class.
        ObjectMapper mapper = new ObjectMapper(); // @Del
        Order order = null;
        String jsonOrder = null; // Order in JSON format.
        for (int i = 1; i < (NUM_MESSAGES + 1); i++) {
            order = new Order(i,"2015/10/" + i, ORDER_DETAILS);
            System.out.println("Publishing order to SNS topic: " + order);
            // STUDENT TODO: Invoke the ObjectMapper object's writeValueAsString method to convert the Order object to a JSON string.
            jsonOrder = mapper.writeValueAsString(order);   // @Del     
            
            // STUDENT TODO: Publish the JSON-formatted order to the SNS topic for orders.
            snsClient.publish(TOPIC_ARN_ORDER, jsonOrder);  // @Del
        }
    }

}
