package com.popyoyo.hello.training.dynamodb;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import org.junit.Test;

public class PatientReportLinkerTest {

    @Test
    public void test() throws Exception {
        PatientReportLinker.main(new String[0]);
        for (int i = 1; i < 4; i++) {
            // Checks if PatientReportUrl attribute is present.
            boolean patientReportUrlAttrPresent = PatientReportLinker.dynamoDB
                    .getTable(PatientReportLinker.INFECTIONS_TABLE_NAME).getItem("PatientId", ("" + i))
                    .isPresent("PatientReportUrl");
            System.out.printf("Test - PatientId: %s, patientReportUrlAttrPresent: %b %n", i,
                    patientReportUrlAttrPresent);
            assertTrue(patientReportUrlAttrPresent);
        }
    }

}
