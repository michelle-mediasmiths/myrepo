package com.mediasmiths.foxtel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: shanefox
 * Date: 21/10/13
 * Time: 11:30 AM
 */
@Ignore
public class TestUtil {

    public static File prepareTempFolder(String description) throws IOException {
        //create a random folder
        //String path = FileUtils.getTempDirectoryPath() + IOUtils.DIR_SEPARATOR + RandomStringUtils.randomAlphabetic(10) + IOUtils.DIR_SEPARATOR + description;
        String path="/tmp/mediaTestData/"+ IOUtils.DIR_SEPARATOR  + description;

        path = path.replace("//", "/"); //on some systems  FileUtils.getTempDirectoryPath() returns a trailing slash and on some it does not

        File dir = new File(path);

        if(dir.exists()){
            FileUtils.cleanDirectory(dir);
        }
        else{
            dir.mkdirs();
        }

        return dir;
    }

}
