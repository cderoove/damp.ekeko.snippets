/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.quality;

import java.io.*;
import java.util.*;

import net.nutch.searcher.*;
import net.nutch.quality.dynamic.*;

/******************************************
 * The QueryGenerator will process a list of
 * query terms, and apply them to a search engine.
 * Using a sherlock-format description of the
 * HTML output, we'll then extract the results.
 * We emit the query/result set in a format that
 * can be used by ResultTestTool
 *
 * @author Mike Cafarella
 ******************************************/
public class ResultSetGenerator {
    PageExtractor.IExtractor extractor;
    boolean debug = false;

    /**
     * A query generator needs to know about the item
     * it is interrogating.
     */
    public ResultSetGenerator(PageExtractor.IExtractor extractor, boolean debug) throws IOException, ParseException {
        this.extractor = extractor;
        this.debug = debug;
    }

    /**
     * Iterate through all the queries.  Obtain results for each
     * one, and write the results out to the indicated file.
     */
    public void processQueries(File queryList, File outputResultSet) throws IOException {
        // Load in the query terms
        Vector queries = new Vector();
        BufferedReader in = new BufferedReader(new FileReader(queryList));
        try {
            String term = in.readLine();
            while (term != null) {
                queries.add(term.trim());
                term = in.readLine();
            }
        } finally {
            in.close();
        }


        // Output the search results
        DataOutputStream out = new DataOutputStream(new FileOutputStream(outputResultSet));
        try {
            out.writeInt(queries.size());
            for (Enumeration e = queries.elements(); e.hasMoreElements(); ) {
                String term = (String) e.nextElement();

                ArrayList results = null;
                try {
                    results = extractor.applyQuery(term);
                } catch (IOException ie) {
                    System.err.println("Could not extract results for " + term);
                }
                
                int numResults = 0;
                if (results != null) {
                    numResults = Math.min(10, results.size());
                }

                out.writeUTF(term);
                out.writeInt(numResults);
                if (debug) {
                    System.out.println("For "+ term + ", " + numResults);
                }

                for (int i = 0; i < numResults; i++) {
                    String str = (String) results.get(i);
                    out.writeUTF(str);
                }
            }
        } finally {
            out.close();
        }
    }

    /**
     * Give a set of queries, and generate a set of responses from the
     * given query target
     */
    public static void main(String argv[]) throws IOException, ParseException {
        if (argv.length < 4) {
            System.out.println("Usage: java net.nutch.quality.ResultSetGenerator [-externalengine <pageDesc> <userAgent>] [-nutchengine <segments>] <queryList> <outputResultSet> [-debug]");
            return;
        }

        int pos = argv.length;
        boolean debug = false;
        String pageDesc = null, userAgent = null, segments = null, queryList = null, outputSet = null;

        // Parse command
        if ("-externalengine".equals(argv[0])) {
            pageDesc = argv[1];
            userAgent = argv[2];
            pos = 3;
        } else if ("-nutchengine".equals(argv[0])) {
            segments = argv[1];
            pos = 2;
        } else {
            System.out.println("Must use command -externalengine or -nutchengine");
            return;
        }

        // Get rest of args
        queryList = argv[pos++];
        outputSet = argv[pos++];
        if (argv.length > pos && "-debug".equals(argv[pos])) {
            debug = true;
        }

        // Prepare the extractor
        PageExtractor.IExtractor extractor = null;
        if ("-externalengine".equals(argv[0])) {
            extractor = new PageExtractor.RemotePageExtractor(new File(pageDesc), userAgent, debug);
        } else if ("-nutchengine".equals(argv[0])) {
            extractor = new PageExtractor.NutchExtractor(segments);
        }

        // Extract the results!
        ResultSetGenerator rsg = new ResultSetGenerator(extractor, true);
        rsg.processQueries(new File(queryList), new File(outputSet));
    }
}
