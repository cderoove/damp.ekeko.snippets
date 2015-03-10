/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;
import java.util.*;

/****************************************************************
 * A ShareGroup combines the name of a group with where the Nutch
 * filesystem can find members of that group.  Used by NutchFileSystem
 * to help resolve NutchFile objects.
 *
 * @author Mike Cafarella
 *****************************************************************/
public class ShareGroup {
    String name;
    String locations[];

    /**      
     * Make a named ShareGroup, to be found at the given location.
     * locationDesc is a semicolon-separated list of the form 
     * "machinename:dbroot;machinename2:dbroot2...".  The leading
     * "machinename:" part is optional, in which case the location
     * is a locally-(probably NFS)-mounted disk.
     */
    public ShareGroup(String name, String locationDescs) {
        this.name = name;

        Vector v = new Vector();
        StringTokenizer toks = new StringTokenizer(locationDescs, ";");
        while (toks.hasMoreTokens()) {
            v.add(toks.nextToken());
        }

        this.locations = new String[v.size()];
        v.copyInto(this.locations);
    }

    /**
     * Create a ShareGroup as above, but assume the location description
     * can be found via NutchConf.
     */
    public ShareGroup(String name) {
        this(name, NutchConf.get("nutchfs.sharegroup." + name));
    }
    
    /**
     * ShareGroup name.
     */
    public String getName() {
        return name;
    }

    /**
     * Locations for the ShareGroup (machinename:path)
     */
    public String[] getLocations() {
        return locations;
    }
}
