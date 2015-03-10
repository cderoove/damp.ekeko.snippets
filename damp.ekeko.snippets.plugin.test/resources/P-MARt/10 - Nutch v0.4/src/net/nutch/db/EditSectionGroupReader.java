/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.db;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import net.nutch.io.*;
import net.nutch.util.*;

/*********************************************************
 * The EditSectionGroupReader will read in an edits-file that
 * was built in a distributed way.  It acts as a "meta-SequenceFile",
 * incorporating knowledge of Section numbering as well as
 * process-synchronization.  If you had different ideas
 * about how to make the db-edits distributed (apart from using
 * NFS), you'd implement them here.
 *
 * @author Mike Cafarella
 *********************************************************/
public class EditSectionGroupReader {
    static final Logger LOG = LogFormatter.getLogger("net.nutch.db.EditSectionGroupReader");
    private final static String MERGED_EDITS = "merged_edits";
    private final static int SLEEP_INTERVAL = 3000;
    private final static int WORRY_INTERVALS = 5;

    NutchFileSystem nutchfs;
    String dbName, label;
    int readerNum = -1, totalMachines = -1, numEdits = 0;
    boolean sectionComplete = false;

    /**
     * Open the EditSectionGroupReader for the appropriate file.
     */
    public EditSectionGroupReader(NutchFileSystem nutchfs, String dbName, String label, int readerNum, int totalMachines) {
        this.nutchfs = nutchfs;
        this.dbName = dbName;
        this.label = label;
        this.readerNum = readerNum;
        this.totalMachines = totalMachines;
    }

    /**
     * Block until all contributions to the EditSection are present
     * and complete.  To figure out how many contributors there are,
     * we load the meta-info first (which is written at section-create
     * time).
     */
    private synchronized void sectionComplete() throws IOException { 
        if (! sectionComplete) {
            //
            // Make sure that every contributor's file is present.
            // When all are present, we know this section is complete.
            //
            for (int i = 0; i < totalMachines; i++) {
                // Create the files we're interested in
                NutchFile allEditsDir = new NutchFile(nutchfs, dbName, "editsection." + readerNum, new File("editsdir." + i));
                NutchFile editsDir = new NutchFile(allEditsDir, label);

                NutchFile editsList = new NutchFile(editsDir, "editslist");
                NutchFile editsInfo = new NutchFile(editsDir, "editsinfo");

                // Block until the editsInfo file appears
                File editsInfoFile = nutchfs.get(editsInfo);

                // Read in edit-list info
                DataInputStream in = new DataInputStream(new FileInputStream(editsInfoFile));
                try {
                    in.read();                 // version
                    this.numEdits += in.readInt();  // numEdits
                } finally {
                    in.close();
                }
            }
            sectionComplete = true;
        }
    }


    /**
     * Return how many edits there are in this section.  This
     * method requires total section-completion before executing.
     */
    public int numEdits() throws IOException {
        sectionComplete();
        return numEdits;
    }

    /**
     * Merge all the components of the Section into a single file
     * and return the location.  This method requires total section-
     * completion before executing.
     */
    public File mergeSectionComponents() throws IOException {
        // Wait till all edit-contributors are done.
        sectionComplete();

        // The merged destination file for this section
        File mergedEditsFile = nutchfs.getWorkingFile();

        //
        // Figure out the keyclass
        //
        NutchFile allEdits0 = new NutchFile(nutchfs, dbName, "editsection." + readerNum, new File("editsdir." + 0));
        NutchFile editsDir0 = new NutchFile(allEdits0, label);
        NutchFile editsList0 = new NutchFile(editsDir0, "editslist");
        File editsListFile0 = nutchfs.get(editsList0);

        SequenceFile.Reader test = new SequenceFile.Reader(editsListFile0.getPath());
        Class keyClass = null;
        try {
            keyClass = test.getKeyClass();
        } finally {
            test.close();
        }

        //
        // Now write out contents of each contributor's file
        //
        try {
            Writable key = (Writable) keyClass.newInstance();
            SequenceFile.Writer out = new SequenceFile.Writer(mergedEditsFile.getPath(), keyClass, NullWritable.class);

            try {
                for (int i = 0; i < totalMachines; i++) {
                    NutchFile allEditsDir = new NutchFile(nutchfs, dbName, "editsection." + readerNum, new File("editsdir." + i));
                    NutchFile editsDir = new NutchFile(allEditsDir, label);
                    NutchFile editsList = new NutchFile(editsDir, "editslist");
                    File editsListFile = nutchfs.get(editsList);

                    SequenceFile.Reader in = new SequenceFile.Reader(editsListFile.getPath());
                    try {
                        while (in.next(key)) {
                            out.append(key, NullWritable.get());
                        }
                    } finally {
                        in.close();
                    }
                }
            } finally {
                out.close();
            }
        } catch (InstantiationException ie) {
            throw new IOException("Could not create instance of " + keyClass);
        } catch (IllegalAccessException iae) {
            throw new IOException("Could not create instance of " + keyClass);
        }

        return mergedEditsFile;
    }

    /**
     * Get rid of the edits encapsulated by this file.
     */
    public void delete() throws IOException {
        for (int i = 0; i < totalMachines; i++) {
            // Delete the files we're interested in
            NutchFile editsDir = new NutchFile(nutchfs, dbName, "editsection." + readerNum, new File("editsdir." + i));
            NutchFile consumedEdits = new NutchFile(editsDir, label);
            nutchfs.delete(consumedEdits);
        }
    }
}
