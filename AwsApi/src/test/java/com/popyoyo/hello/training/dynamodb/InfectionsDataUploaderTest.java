package com.popyoyo.hello.training.dynamodb;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import org.junit.Test;

public class InfectionsDataUploaderTest {

    @Test
    public void test() throws Exception {
        InfectionsDataUploader.main(new String[0]);
        assertEquals(0, InfectionsDataUploader.numFailures, 0);
    }

}
