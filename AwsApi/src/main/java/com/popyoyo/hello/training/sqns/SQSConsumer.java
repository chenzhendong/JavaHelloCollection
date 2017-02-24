package com.popyoyo.hello.training.sqns;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// The SQSConsumer class retrieves messages from an SQS queue.
public class SQSConsumer implements Runnable {

    public static final Region REGION = Utils.getRegion();
    public static final String QUEUE_NAME = "MySQSQueue_A";
    public static final long SLEEP = 500;
    private AmazonSQSClient sqsClient = null;

    public static void main(String[] args) throws Exception {
        SQSConsumer sqsConsumer = new SQSConsumer();
        sqsConsumer.init();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture <?> future = executor.scheduleWithFixedDelay(sqsConsumer, 0, SLEEP,TimeUnit.MILLISECONDS);
        Thread.sleep(20 * SLEEP);
        future.cancel(false);
        executor.shutdown();
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

        // STUDENT TODO: Create an instance of the AmazonSQSClient class and set its region using the REGION constant.
        sqsClient = new AmazonSQSClient(credentials); // @Del
        sqsClient.setRegion(REGION); // @Del
    }

    public void run() {
        System.out.println("SQSConsumer ...Thread running.");
        consumeMessages();
    }

    private void consumeMessages() {
        try {
            Order order = null;
            String queueUrl = null;
            Map<String, String> messageAttributes = null;
            // Should contain result returned by the receiveMessage call.
            ReceiveMessageResult receiveMessageResult = null;

            // Should contain messages retrieved from the ReceiveMessageResult object.
            List <Message> messages = null;

            // STUDENT TODO: Retrieve the URL of the SQS queue by using the AmazonSQSClient object and QUEUE_NAME constant for SQS queue name.
            GetQueueUrlResult queueUrlResult = sqsClient.getQueueUrl(QUEUE_NAME);   // @Del
            queueUrl = queueUrlResult.getQueueUrl();   // @Del

            // STUDENT TODO: Create an instance of the ReceiveMessageRequest class.
            ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);   // @Del

            // STUDENT TODO: Enable long polling and set maximum number of messages to 10.
            request.setWaitTimeSeconds(20);   // @Del
            request.setMaxNumberOfMessages(10);   // @Del

            // STUDENT TODO: Receive messages from the SQS queue by using the ReceiveMessageRequest object.
            receiveMessageResult = sqsClient.receiveMessage(request);   // @Del

            // STUDENT TODO: Retrieve all messages in the ReceiveMessageResult object.
            messages = receiveMessageResult.getMessages();   // @Del

            System.out.printf("Number of messages received this time: %d%n", messages.size());

            ObjectMapper mapper = new ObjectMapper();

            for (Message message: messages) {
                messageAttributes = message.getAttributes();
                order = mapper.readValue(message.getBody(), Order.class);
                // Adds message metadata to Order object.
                order.setSenderId(messageAttributes.get("SenderId"));
                order.setSentTimestamp(messageAttributes.get("SentTimestamp"));

                System.out.printf("Order received from SQS queue:%s%n", order);

                deleteMessage(queueUrl, message);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void deleteMessage(String queueUrl, Message message) {
        String messageReceiptHandle = message.getReceiptHandle();

        // STUDENT TODO: Delete the message from the queue.
        sqsClient.deleteMessage(queueUrl, messageReceiptHandle);  // @Del
    }


}
