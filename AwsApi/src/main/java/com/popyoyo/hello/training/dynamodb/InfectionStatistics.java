package com.popyoyo.hello.training.dynamodb;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.util.Iterator;

// The InfectionStatistics class queries the infections table and reports the total number of infections in a city.
public class InfectionStatistics {

    public static final String INFECTIONS_TABLE_NAME = InfectionsTableCreator.INFECTIONS_TABLE_NAME;
    public static final String CITY_DATE_INDEX_NAME = InfectionsTableCreator.CITY_DATE_INDEX_NAME;
    public static final Region REGION = Utils.getRegion();
    private static DynamoDB dynamoDB = null;
    private static AmazonDynamoDBClient dynamoDBClient = null;
    public static int itemCount = 0;

    public static void main(String[] args) throws Exception {
        init();

        try {
            queryByCity(args[0]);
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

    public static int queryByCity(String inputCity) {
        Iterator<Item> itemIterator = null;
        Item item = null;
        Table infectionsTable = dynamoDB.getTable(INFECTIONS_TABLE_NAME);

        // STUDENT TODO: Using the CITY_DATE_INDEX_NAME constant as index name, retrieve a reference to the global secondary index for the infections table.
        Index index = infectionsTable.getIndex(CITY_DATE_INDEX_NAME); // @Del

        // STUDENT TODO: Create an instance of the QuerySpec class. Should return items where value of the City attribute matches the inputCity.
        QuerySpec spec = new QuerySpec() // @Del
                .withKeyConditionExpression("City = :v_city") // @Del
                .withValueMap(new ValueMap() // @Del
                        .withString(":v_city", inputCity)); // @Del

        // STUDENT TODO: Query the global secondary index by using the QuerySpec object defined.
        ItemCollection<QueryOutcome> items = index.query(spec); // @Del

        System.out.println("-------------------------------------------------------------");

        // STUDENT TODO: Retrieve a reference to an iterator from the ItemCollection object returned by the query.
        itemIterator = items.iterator(); // @Del

        // Iterates over the collection of items.
        while (itemIterator.hasNext()) {
            // Increments the itemCount variable to find the total number of items returned by the query.
            itemCount++;
            item = itemIterator.next();
            System.out.printf("%s - %s - %s %n", item.getString("PatientId"), item.getString("City"),
                    item.getString("Date"));
        }
        System.out.println("-------------------------------------------------------------");
        System.out.printf("Summary: Number of infections in %s city is %d %n", inputCity, itemCount);
        return itemCount;
    }

    
}
