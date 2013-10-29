package com.mediasmiths.foxtel.agent.queue;

import com.mediasmiths.foxtel.TestUtil;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: shanefox
 * Date: 21/10/13
 * Time: 9:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestPrioritySingleFilePickup {

    @Test
    public void simpleTest() {
        try {
            File tempFolder = TestUtil.prepareTempFolder("testPrioritySingleFilePickup");

            File file1 = new File(tempFolder, "PRDM_20131021124930100_12345_CI.xml");
            File file2 = new File(tempFolder, "PRDM_20131021124930100_12345_CP.xml");

            file1.createNewFile();file2.createNewFile();

            PrioritySingleFilePickup pickUp = new PrioritySingleFilePickup(new File[]{tempFolder}, "xml",
                    "\\p{Upper}{4}_\\d{17}_(\\d{5})_CI.xml",
                    "\\p{Upper}{4}_\\d{17}_(\\d{5})_CP.xml");
            pickUp.WAIT_TIME = 10;
            pickUp.setState(true);
            pickUp.stabilityTimes = new HashMap<File, Long>();
            pickUp.stabilityTimes.put(tempFolder, 0l);

            PickupPackage pickupPackage = pickUp.take();
            
            assertNotNull(pickupPackage);
            assertEquals(1, pickupPackage.count());
            File file = pickupPackage.getPickUp("xml");
            assertEquals(file2.getName(), file.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
