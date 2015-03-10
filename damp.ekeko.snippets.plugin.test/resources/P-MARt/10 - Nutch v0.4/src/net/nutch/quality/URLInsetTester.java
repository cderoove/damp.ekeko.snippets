/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.quality;

import java.io.*;
import java.util.*;

import net.nutch.searcher.*;
import net.nutch.quality.dynamic.*;

/**********************************************************
 * The URLInsetTester takes a description of a search engine
 * and a list of URLs.  (The search engine can be an external
 * one or a local Nutch index.)  We go through every URL in
 * the list, and figure out whether the search engine in
 * question has indexed the URL.  We emit a binary file
 * that lists each URL, plus a "true" or "false" answer.
 *
 * @author Mike Cafarella
 **********************************************************/
public class URLInsetTester {
    boolean debug = false;
    PageExtractor.IExtractor extractor;

    /**
     * Take a PageExtractor for a search engine.
     */
    public URLInsetTester(PageExtractor.IExtractor extractor, boolean debug) {
        this.extractor = extractor;
        this.debug = debug;
    }

    /**
     * Load a list of urls from a binary list, then call testURLs().
     */
    public void testURLs(File urlList, TreeSet knownInset, File outputSet) throws IOException {
        // Load in the URLs whose presence we should test.
        Vector urls = new Vector();
        DataInputStream in = new DataInputStream(new FileInputStream(urlList));
        try {
            int numItems = in.readInt();
            for (int i = 0; i < numItems; i++) {
                urls.add(in.readUTF().trim());
                in.readInt();
            }
        } finally {
            in.close();
        }
        testURLs(urls, knownInset, outputSet);
    }

    /**
     * Just pass in a list of the URLs we want to test.
     * Go through the list of URLs, outputting whether each
     * one occurs in the search engine's result list.
     */
    public void testURLs(Vector urls, TreeSet knownInset, File outputSet) throws IOException {
        // Output the test results
        DataOutputStream out = new DataOutputStream(new FileOutputStream(outputSet));
        try {
            out.writeInt(urls.size());
            for (Enumeration e = urls.elements(); e.hasMoreElements(); ) {
                String url = (String) e.nextElement();
                ArrayList results = null;
                try {
                    results = extractor.applyQuery(url);
                } catch (IOException ie) {
                    System.err.println("Could not extract results for " + url);
                }
                
                boolean hasURL = false;
                if (knownInset != null && knownInset.contains(url)) {
                    hasURL = true;
                } else if (results != null) {
                    for (Iterator it = results.iterator(); it.hasNext(); ) {
                        String val = (String) it.next();
                        if (val.trim().compareTo(url) == 0) {
                            hasURL = true;
                            break;
                        } else {
                            if (debug) {
                                System.out.println("Query url " + url + " does not match result " + val);
                            }
                        }
                    }
                } else {
                    if (debug) {
                        System.out.println("Got no results when searching for " + url);
                    }
                }

                out.writeUTF(url);
                out.writeBoolean(hasURL);
                if (debug) {
                    System.out.println("For " + url + ": " + hasURL);
                }
            }
        } finally {
            out.close();
        }
    }

    /**
     * Provide this program a target search engine, a set of 
     * URLs, and a place to write the output.
     */
    public static void main(String argv[]) throws IOException, ParseException {
        if (argv.length < 4) {
            System.out.println("Usage: java net.nutch.quality.URLInsetTester [-externalengine <pageDesc> <userAgent>] [-nutchengine <segments>] <queryList> <setMembershipResults> [-debug]");
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

        // Load in plaintext urls
        Vector urls = new Vector();
        BufferedReader in = new BufferedReader(new FileReader(queryList));
        try {
            String url = in.readLine();
            while (url != null) {
                urls.add(url);
                url = in.readLine();
            }
        } finally {
            in.close();
        }

        // Get results from each, and test them!
        URLInsetTester uit = new URLInsetTester(extractor, true);
        uit.testURLs(urls, null, new File(outputSet));
    }
}
