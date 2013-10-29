package com.mediasmiths.foxtel.agent.queue;

import com.google.inject.name.Named;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Extends SingleFilePickup to add the ability to prioritise one file over another.
 * These are matched using regular expressions, both of which must provide a single group
 * that gives an ID. The ID from both of the files will be equal.
 *
 * An example of a regexp is:
 *     \p{Upper}{4}_\d{17}_(\d{5})_CI.xml
 */
public class PrioritySingleFilePickup extends SingleFilePickUp {

    private Logger logger = Logger.getLogger(PrioritySingleFilePickup.class);

    /**
     * Group 1 matches the assetID
     */
    private Pattern lowPriorityPattern;

    /**
     * Group 1 matches the assetID
     */
    private Pattern highPriorityPattern;

    public PrioritySingleFilePickup(@Named("filepickup.watched.directories") File[] pickupDirectories,
                                    @Named("filepickup.single.extension") String extension,
                                    @Named("filepickup.lowPriorityPattern") String lowPriorityPatternStr,
                                    @Named("filepickup.highPriorityPattern") String highPriorityPatternStr) {
        super(pickupDirectories, extension);

        this.lowPriorityPattern = Pattern.compile(lowPriorityPatternStr);
        this.highPriorityPattern = Pattern.compile(highPriorityPatternStr);

    }

    @Override
    protected PickupPackage selectCandidate() {
        File[] candidateFiles = getTimeOrderedPendingFiles();

        if (candidateFiles == null || candidateFiles.length == 0){
            clearDiscoveredTimes(); //no files waiting to be processed so remove any existing discovery times
            return null;
        }

        for (File f: candidateFiles) {
            if (isStableFile(f)) {
                String lowPriorityAssetId = findLowPriorityAssetID(f);
                if (lowPriorityAssetId != null) {
                    // This is a low priority asset, so we will check if the matching high priority sibling is available.
                    File highPriorityFile = findHighPriorityMatch(candidateFiles, lowPriorityAssetId);
                    if (highPriorityFile != null) {
                        // There was a hi priority file
                        if (isStableFile(highPriorityFile)) {
                            return getPickUpPackageForFile(highPriorityFile);
                        }
                    } else {
                        return getPickUpPackageForFile(f);
                    }
                } else {
                    return getPickUpPackageForFile(f);
                }
            }
        }

        return null;
    }

    private String findLowPriorityAssetID(File f) {
        Matcher matcher = lowPriorityPattern.matcher(f.getName());
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }

    private File findHighPriorityMatch(File[] candidateFiles, String assetId) {
        for (File f : candidateFiles) {
            Matcher matcher = highPriorityPattern.matcher(f.getName());
            if (matcher.matches() && assetId.equals(matcher.group(1))) {
                return f;
            }
        }
        return null;
    }

}
