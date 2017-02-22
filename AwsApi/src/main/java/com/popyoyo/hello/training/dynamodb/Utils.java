package com.popyoyo.hello.training.dynamodb;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class Utils {

    public static final String LAB_S3_BUCKET_NAME = "us-west-2-aws-training";
    public static final String LAB_S3_BUCKET_REGION = "us-west-2";
    public static final String INFECTIONS_DATA_FILE_KEY = "awsu-ilt/developing/v2.0/lab-3-dynamodb/static/SampleInputFiles/InfectionsData.csv";
    public static final String PATIENT_REPORT_PREFIX = "awsu-ilt/developing/v2.0/lab-3-dynamodb/static/SampleInputFiles/PatientReports/PatientRecord";

    public static Region getRegion() {
        Region region = Regions.getCurrentRegion();

        // This code is for local testing only.
        if (region == null) {
            region = Region.getRegion(Regions.US_WEST_1);
        }

        System.out.printf("Utils.getRegion returned the region %s. %n ", region.getName());
        return region;
    }
}
