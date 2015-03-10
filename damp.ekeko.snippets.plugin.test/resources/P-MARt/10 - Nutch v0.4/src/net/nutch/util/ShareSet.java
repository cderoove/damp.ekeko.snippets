/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;
import java.util.*;

/****************************************************************
 * A ShareSet is a library of ShareGroup objects.  It defines
 * every other machine in the current NutchFileSystem's universe.
 *
 * @author Mike Cafarella
 *****************************************************************/
public class ShareSet {
    TreeMap shareGroups = new TreeMap();

    /**
     * Build a ShareSet out of a Vector of ShareGroup objects.
     */
    public ShareSet(File dbRoot, Vector shareList) {
        for (Enumeration e = shareList.elements(); e.hasMoreElements(); ) {
            ShareGroup sg = (ShareGroup) e.nextElement();
            shareGroups.put(sg.getName(), sg);
        }
        buildDefault(dbRoot);
    }

    /**
     * Default constructor.  Loads configuration from NutchConf.
     */
    public ShareSet(File dbRoot) {
        String groupList = NutchConf.get("nutchfs.sharegroups.names");
        if (groupList != null) {
            StringTokenizer toks = new StringTokenizer(groupList, ",");
            Vector sharenames = new Vector();
            while (toks.hasMoreTokens()) {
                sharenames.add(toks.nextToken());
            }

            for (Enumeration e = sharenames.elements(); e.hasMoreElements();) {
                String shareName = (String) e.nextElement();
                shareGroups.put(shareName, new ShareGroup(shareName));
            }        
        }
        buildDefault(dbRoot);
    }

    /**
     * Add a default ShareGroup if necessary.
     */
    void buildDefault(File dbRoot) {
        // Create a default shareGroup if necessary
        if (shareGroups.get("*") == null) {
            ShareGroup defaultSG = new ShareGroup("*", dbRoot.getPath());
            shareGroups.put(defaultSG.getName(), defaultSG);
        }
    }

    /**
     * Find the relevant ShareGroup object
     */
    ShareGroup getShareGroup(NutchFile nutchFile) {
        // Check if there is a registered ShareGroup that matches this NutchFile
        ShareGroup sg = (ShareGroup) shareGroups.get(nutchFile.getShareGroupName());

        // If not, find the default ShareGroup
        if (sg == null) {
            sg = (ShareGroup) shareGroups.get("*");
        }
        return sg;
    }

    /**
     * Return entire TreeMap of ShareGroups
     */
    TreeMap getShareGroups() {
        return shareGroups;
    }
}


