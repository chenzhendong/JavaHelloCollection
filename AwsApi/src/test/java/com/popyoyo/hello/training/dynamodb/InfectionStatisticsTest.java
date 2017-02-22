package com.popyoyo.hello.training.dynamodb;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import org.junit.Test;

public class InfectionStatisticsTest {

    @Test
    public void test() throws Exception {
        InfectionStatistics.main(new String[] { "Reno" });
        assertEquals(178, InfectionStatistics.itemCount, 0);
    }

}
