/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.linkdb;

import java.io.*;
import java.util.*;
import net.nutch.io.*;

/**********************************************
 * An entry in the LinkAnalysisTool's output.  Consists
 * of a single float for every entry in a table administered
 * by LinkAnalysisTool.
 *
 * @author Mike Cafarella
 *********************************************/
public class LinkAnalysisEntry extends VersionedWritable {
    private final static byte VERSION = 1;

    float score;

    /**
     */
    public LinkAnalysisEntry() {
        score = 0.0f;
    }

    public byte getVersion() { return VERSION; }

    /**
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     */
    public void readFields(DataInput in) throws IOException {
        super.readFields(in);

        score = in.readFloat();
    }

    /**
     */
    public void write(DataOutput out) throws IOException {
        super.write(out);

        out.writeFloat(score);
    }

    /**
     */
    public static LinkAnalysisEntry read(DataInput in) throws IOException {
        LinkAnalysisEntry lae = new LinkAnalysisEntry();
        lae.readFields(in);
        return lae;
    }

    //
    // Accessors
    //
    public float getScore() {
        return score;
    }

    /**
     */
    public boolean equals(Object o) {
        LinkAnalysisEntry other = (LinkAnalysisEntry) o;

        if (score == other.score) {
            return true;
        }
        return false;
    }

}
