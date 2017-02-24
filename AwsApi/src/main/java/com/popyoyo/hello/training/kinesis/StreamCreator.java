package com.popyoyo.hello.training.kinesis;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.ResourceInUseException;
import com.popyoyo.hello.training.dynamodb.Utils;

import java.util.concurrent.TimeUnit;

// The StreamCreator class creates a Kinesis stream.
public class StreamCreator {

    // Before running the code, check that the ~/.aws/credentials file contains your credentials.

    public static final String STREAM_NAME = "KinesisLabStream";
    public static final Region REGION = Utils.getRegion();
    public static final int WAIT_TIME_MINUTES = 10;
    public static final int POLLING_INTERVAL_SECONDS = 20;

    // Kinesis Client Object
    private static AmazonKinesisClient kinesis;

    public static void main(String[] args) throws Exception {
        init();

        // Creates a stream with 10 shards.
        createStream(STREAM_NAME, 10);

    }

    private static void init() throws Exception {

        // The ProfileCredentialsProvider will return your default credential profile by reading from the ~/.aws/credentials file.
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("AmazonClientException represents an error that occurred inside the client on the local host," + "either while trying to send the request to AWS or interpret the response." + " "
                    + "For example, if no network connection is available, the client won't be able to connect to AWS to execute a request and will throw an AmazonClientException.", e);
        }

        // STUDENT TODO: Create an Amazon Kinesis client.
        kinesis = new AmazonKinesisClient();

        // STUDENT TODO: Set the region for the Kinesis client. Use the REGION constant as parameter to the setRegion method.
        kinesis.setRegion(REGION);

    }

    private static void createStream(String streamName, int shardCount) throws Exception {
        try {
            // STUDENT TODO: Create a stream request with the given stream name and shard count.
            CreateStreamRequest createStreamRequest = new CreateStreamRequest();
            createStreamRequest.setStreamName(streamName);
            createStreamRequest.setShardCount(shardCount);

            // STUDENT TODO: Create a stream with the stream request object.
            kinesis.createStream(createStreamRequest);

        } catch (ResourceInUseException re) {
            System.out.printf("Stream %s already exists %n", streamName);
        }

        waitForStreamToBecomeActive(streamName);
    }

    private static void waitForStreamToBecomeActive(String streamName) throws Exception {
        System.out.printf("Waiting for %s to become ACTIVE... %n", streamName);

        String streamStatus = null;
        boolean isActive = false;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + TimeUnit.MINUTES.toMillis(WAIT_TIME_MINUTES);

        while (System.currentTimeMillis() < endTime) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(POLLING_INTERVAL_SECONDS));
            streamStatus = getStreamStatus(streamName);
            System.out.printf("Current stream status: %s%n ", streamStatus);
            if ("ACTIVE".equals(streamStatus)) {
                isActive = true;
                break;
            }
        }

        if (!isActive) {
            throw new Exception(String.format("Stream %s never became active", streamName));
        }
    }

    private static String getStreamStatus(String streamName) {
        String streamStatus = null;
        // STUDENT TODO: Check if stream status is active.
//        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest(); // @Del
//        describeStreamRequest.setStreamName(streamName); // @Del
//        DescribeStreamResult describeStreamResponse = kinesis.describeStream(describeStreamRequest); // @Del

//        streamStatus = describeStreamResponse.getStreamDescription().getStreamStatus(); // @Del

        return streamStatus;
    }
}
