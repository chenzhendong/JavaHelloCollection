package com.popyoyo.hello.training.cache;
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import net.spy.memcached.MemcachedClient;

import java.net.InetSocketAddress;

// The CacheManager class implements the lazy caching approach to populate the cache and retrieve items from cache. 
public class CacheManager {

    // STUDENT TODO: Set the endpoint for the ElastiCache node cluster.
    public static final String CLUSTER_CONFIG_ENDPOINT = "qls-el-4qdctj2namoj.neb8b2.0001.usw2.cache.amazonaws.com";

    // STUDENT TODO: Check that the CLUSTER_CONFIG_ENDPOINT_PORT constant is set to 11211.
    public static final int CLUSTER_CONFIG_ENDPOINT_PORT = 11211;

    public static MemcachedClient memcachedClient = null;

    public static void main(String[] args) throws Exception {
        String drugName = args[0];
        init();
        String pharmaInfo = getPharmaInfo(drugName);
        System.out.printf("Retrieved pharmaceutical information for drug %s: %s%n", drugName, pharmaInfo);
    }

    public static void init() throws Exception {
        // Sets up the DynamoDB table with sample data about pharmaceutical usage.
        DynamoDBManager.init();

        // STUDENT TODO: Create an instance of the MemcachedClient class.
        // Use the CLUSTER_CONFIG_ENDPOINT and CLUSTER_CONFIG_ENDPOINT_PORT constants as arguments.
        memcachedClient = new MemcachedClient( // @Del
                new InetSocketAddress(CLUSTER_CONFIG_ENDPOINT, CLUSTER_CONFIG_ENDPOINT_PORT)); // @Del
    }

    /*
     * Returns pharmaceutical usage information for the given drug. 
     * 
     * Attempts to retrieve item from cache. 
     * 
     * If the item is not found in cache, retrieves the information from DynamoDB and updates cache.
     */
    public static String getPharmaInfo(String drugName) {
        String pharmaInfoStr = null;
        Object pharmaInfo = getCacheItem(drugName);
        if (pharmaInfo == null) {
            pharmaInfo = DynamoDBManager.getPharmaInfo(drugName);
            setCacheItem(drugName, pharmaInfo);
        }
        if (pharmaInfo != null) {
            pharmaInfoStr = pharmaInfo.toString();
        }

        return pharmaInfoStr;
    }

    private static Object getCacheItem(String key) {
        Object valueFromCache = null;
        // STUDENT TODO: For the given key, retrieve value from cache.
        valueFromCache = memcachedClient.get(key); // @Del

        return valueFromCache;
    }

    private static void setCacheItem(String key, Object value) {
        // STUDENT TODO: Store the given key-value pair in cache with an expiration value of 3600 seconds.
        memcachedClient.set(key, 3600, value);
    }

}
