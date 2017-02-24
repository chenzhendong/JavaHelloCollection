package com.popyoyo.hello.training.sqns;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class Utils {
    public static Region getRegion() {
        Region region = Regions.getCurrentRegion();

        // For local testing only.
        if (region == null) {
            region = Region.getRegion(Regions.US_WEST_2);
        }

        System.out.printf("Get region returns: %s%n ", region.getName());
        return region;
    }
}
