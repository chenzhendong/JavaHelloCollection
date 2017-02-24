package com.popyoyo.hello.training.kinesis;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.regions.Region;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.popyoyo.hello.training.dynamodb.Utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

// The SensorReadingsProducer class generates data for sensor readings and publishes records to a Kinesis stream.
public class SensorReadingsProducer {

    // Before running the code, check that the ~/.aws/credentials file contains your credentials.

    public static final String STREAM_NAME = StreamCreator.STREAM_NAME;
    public static final Region REGION = Utils.getRegion();
    public static final int TOTAL_NUM_EVENTS = 200;
    private final Random RANDOM = new Random();

    private static SensorReadingsProducer sensorReadingsProducer = null;

    private int successCounter = 0;

    public static void main(String[] args) {
        sensorReadingsProducer = getSensorReadingsProducer();
        sensorReadingsProducer.addDataToStream();
    }

    private void addDataToStream() {
        SensorData sensorData = null;

        // STUDENT TODO: Create an instance of KinesisProducerConfiguration. Set the region by using REGION.getName().
        KinesisProducerConfiguration config = new KinesisProducerConfiguration();
        config.setRegion(REGION.getName());

        // STUDENT TODO: Instantiate a KinesisProducer using the configuration defined.
        KinesisProducer kinesisProducer = new KinesisProducer(config);

        // STUDENT TODO: Create a FutureCallback to store the results of asynchronous call to add data to Kinesis stream.
        FutureCallback<UserRecordResult> myCallback = new FutureCallback<UserRecordResult>() {
            @Override
            public void onSuccess(UserRecordResult userRecordResult) {
                successCounter++;
                if(successCounter == TOTAL_NUM_EVENTS){
                    System.out.println("Number of records added records to stream. :  " + successCounter);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("Failed to add record to stream.");
                throwable.printStackTrace();
            }
        };
//        FutureCallback<UserRecordResult> myCallback = // @Del
//        new FutureCallback<UserRecordResult>() { // @Del
//            @Override // @Del
//            public void onFailure(Throwable t) { // @Del
//                System.out.println("Failed to add record to stream."); // @Del
//                t.printStackTrace(); // @Del
//            }; // @Del

//            @Override // @Del
//            public void onSuccess(UserRecordResult result) { // @Del
//                successCounter++; // @Del
//                if (successCounter == TOTAL_NUM_EVENTS) { // @Del
//                    System.out.println("Number of records added records to stream. :  " + successCounter); // @Del
//                } // @Del
//            }; // @Del
//        }; // @Del

        for (int readingCounter = 0; readingCounter < TOTAL_NUM_EVENTS; readingCounter++) {
            sensorData = generateData(readingCounter);
            // STUDENT TODO: Use sensorData.sensorId as partition key. Use sensorData.data as data for record.
            ListenableFuture<UserRecordResult> future = // @Del
            kinesisProducer.addUserRecord(STREAM_NAME, sensorData.sensorId, sensorData.data); // @Del

            // STUDENT TODO: Add callback to Futures object
            Futures.addCallback(future, myCallback); // @Del
        }
    }

    // Generates sensor data. DO NOT MODIFY THIS METHOD.
    private SensorData generateData(int readingCounter) {
        String sensorId = getSensorId(readingCounter);
        int temperature = getRandomTemperature(readingCounter);

        String data = sensorId + ":" + temperature;
        ByteBuffer dataBytes = null;

        dataBytes = ByteBuffer.wrap(data.getBytes(Charset.forName("UTF-8")));

        return this.new SensorData(sensorId, dataBytes);
    }

    private String getSensorId(int readingCounter) {
        String sensorId = null;
        String[] sensorIds = { "A12345", "Z09876" };

        if (readingCounter % 2 == 0) {
            sensorId = sensorIds[0];
        } else {
            sensorId = sensorIds[1];
        }

        return sensorId;
    }

    private int getRandomTemperature(int readingCounter) {
        int temperature = 0;
        int randomNumber = RANDOM.nextInt(10);

        if (readingCounter > 100 && readingCounter < 150 && readingCounter % 2 == 0) {
            temperature = randomNumber + 50;
        } else {
            temperature = randomNumber + 30;
        }
        return temperature;
    }

    public static SensorReadingsProducer getSensorReadingsProducer() {
        if (sensorReadingsProducer == null) {
            sensorReadingsProducer = new SensorReadingsProducer();
        }
        return sensorReadingsProducer;
    }

    public int getSuccessCounter() {
        return successCounter;
    }

    // Object used to pass around sensor id and data.
    private class SensorData {
        private String sensorId;
        private ByteBuffer data;

        private SensorData(String id, ByteBuffer sData) {
            sensorId = id;
            data = sData;
        }
    }
}
