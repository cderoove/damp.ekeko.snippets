/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.quality;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import net.nutch.util.*;
import net.nutch.quality.dynamic.*;

/***************************************************
 * The QualityTestTool runs a bunch of tests
 * against both Nutch and external search engines.
 * The inputs for the actual quality metric computation
 * can be precomputed using ResultSetGenerator and 
 * URLInsetGenerator.  OR, QualityTestTool can run
 * those programs for you.
 *
 * Whether the values have been computed before, or
 * whether you ask QualityTestTool to do it, they
 * remain where they are.  We do not delete the results
 * of the test, in case the user wants to run computations
 * again without all the work of building the test
 * material.
 *
 * We compute two metrics that are interesting.  The
 * first compares page-coverage among the engines.
 * The second compares ranking-quality.  We emit
 * statistics for both.
 *
 * @author Mike Cafarella
 **************************************************/
public class QualityTestTool {
    final static String UNIQUE_URLS = "uniqueURLs.bin";
    final static String QUERY_LIST = "queryList.txt";
    final static String URL_INSET_SUFFIX = ".urlInset";
    final static String QUERY_RESULTS_SUFFIX = ".queryResults";
    final static String ENGINE_DESC_SUFFIX = ".src";
    final static String NUTCH_LABEL = "Nutch";
    public static final Logger LOG = LogFormatter.getLogger("net.nutch.quality.QualityTestTool");

    File inputsDir;
    TreeMap engineInsetData = null;

    /**
     * The inputs are given to us.
     */
    public QualityTestTool(File inputsDir) {
        this.inputsDir = inputsDir;
    }

    /**
     * We need to compute all the inputs ourselves.
     */
    public QualityTestTool(File externalEngines, String userAgent, String segmentsDirs[], File queryList) throws IOException, ParseException {
        this.inputsDir = new File("localInputs");
        if (inputsDir.exists()) {
            throw new IOException("Cannot run QualityTestTool.  File " + inputsDir + " already exists");
        }
        inputsDir.mkdirs();

        createInputs(externalEngines, userAgent, queryList, segmentsDirs);
    }

    /**
     * Given a directory full of search engine descriptions, 
     * a directory of Nutch segments, and a list of queries,
     * we need to build the necessary files for a Quality Test.
     *
     * Everything is placed in the directory at "inputsDir".
     */
    private void createInputs(File externalEngines, String userAgent, File queryList, String segmentsDirs[]) throws IOException, ParseException {
        //
        // 1st, just copy the query list
        //
        LOG.info("CreateInputs, 1 of 6:  Copying query list...");
        File targetQueryList = new File(inputsDir, QUERY_LIST);
        FileUtil.copyContents(queryList, targetQueryList, true);

        //
        // 2nd, test the queryList against all the remote 
        // search engines.  Use ResultSetGenerator for this.
        //
        LOG.info("CreateInputs, 2 of 6:  Testing queries against remote engines...");
        File engineDescs[] = externalEngines.listFiles();
        for (int i = 0; i < engineDescs.length; i++) {
            String filename = engineDescs[i].getName();
            if (filename.endsWith(ENGINE_DESC_SUFFIX)) {
                // Compute the engine name
                int suffixStart = filename.lastIndexOf(ENGINE_DESC_SUFFIX);
                String engineName = filename.substring(0, suffixStart);

                PageExtractor.IExtractor extractor = new PageExtractor.RemotePageExtractor(engineDescs[i], userAgent, false);
                ResultSetGenerator rsg = new ResultSetGenerator(extractor, false);
                rsg.processQueries(targetQueryList, new File(inputsDir, engineName + QUERY_RESULTS_SUFFIX));
            }
        }


        //
        // 3rd, test the queryList against the Nutch segments.
        //
        LOG.info("CreateInputs, 3 of 6:  Testing queries against local Nutch segments...");
        for (int i = 0; i < segmentsDirs.length; i++) {
            PageExtractor.IExtractor extractor = new PageExtractor.NutchExtractor(segmentsDirs[i]);
            ResultSetGenerator rsg = new ResultSetGenerator(extractor, false);
            rsg.processQueries(targetQueryList, new File(inputsDir, NUTCH_LABEL + "." + i + QUERY_RESULTS_SUFFIX));
        }
        
        
        //
        // 4th, now that we have all the queryResults, we 
        // compute the uniquified URL list.  This is written
        // to a file in inputsDir.
        //
        // Note we may have a value "minSupport".  If the percentage
        // of engines that include a given term in the top-10 list
        // is >= minSupport, then it is included in the uniquified
        // list.  Otherwise, the term doesn't make it.  This approach
        // lets us remove wholly "idiosyncratic" URLs from the test
        // pool.
        //
        // Of course, if minSupport is 0 then all items will pass 
        // the guard.
        //
        LOG.info("CreateInputs, 4 of 6:  Computing unique URL set...");
        TreeMap returnedURLSets = new TreeMap();
        SortedMap uniqueMap = new TreeMap();

        File resultLists[] = inputsDir.listFiles();
        for (int i = 0; i < resultLists.length; i++) {
            String filename = resultLists[i].getName();
            if (filename.endsWith(QUERY_RESULTS_SUFFIX)) {
                // Compute the engine name
                int suffixStart = filename.lastIndexOf(QUERY_RESULTS_SUFFIX);
                String engineName = filename.substring(0, suffixStart);

                // Store all the URLs returned by this engine
                SortedSet returnedURLSet = new TreeSet();
                returnedURLSets.put(engineName, returnedURLSet);

                // Load in the engine's result set
                DataInputStream in = new DataInputStream(new FileInputStream(resultLists[i]));
                try {
                    int numQueries = in.readInt();
                    for (int j = 0; j < numQueries; j++) {
                        String query = in.readUTF();
                        int numResults = in.readInt();
                        for (int k = 0; k < numResults; k++) {
                            String foundURL = in.readUTF();
                            // Remember all the URLs from this engine
                            returnedURLSet.add(foundURL);
                        }
                    }
                } finally {
                    in.close();
                }
            }
        }

        //
        // Figure out whether each term enjoys enough support
        // to make it into the unique set.
        //
        // Go through all known sets...
        for (Iterator it = returnedURLSets.values().iterator(); it.hasNext(); ) {
            SortedSet curSet = (SortedSet) it.next();

            // And iterate through the terms of each set...
            for (Iterator it2 = curSet.iterator(); it2.hasNext(); ) {
                String term = (String) it2.next();

                // Testing each term to make sure it is common enough...
                int containsCount = 0;
                for (Iterator it3 = returnedURLSets.values().iterator(); it3.hasNext(); ) {
                    SortedSet testSet = (SortedSet) it3.next();
                    if (testSet.contains(term)) {
                        containsCount++;
                    }
                }

                // Before inserting the term into the uniquified pool.
                uniqueMap.put(term, new Integer(containsCount));
            }
        }

        // Now write out the unique URL set
        File uniqueURLs = new File(inputsDir, UNIQUE_URLS);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(uniqueURLs));
        try {
            out.writeInt(uniqueMap.size());
            for (Iterator it = uniqueMap.keySet().iterator(); it.hasNext(); ) {
                String url = (String) it.next();
                Integer count = (Integer) uniqueMap.get(url);
                out.writeUTF(url);
                out.writeInt(count.intValue());
            }
        } finally {
            out.close();
        }

        //
        // 5th, we test each remote search engine to see
        // if it contains each unique URL.  We write the
        // results of each test to inputsDir.  Use URLInsetTester
        // for this.
        //
        LOG.info("CreateInputs, 5 of 6:  Test membership of each URL in every remote engine...");
        for (int i = 0; i < engineDescs.length; i++) {
            String filename = engineDescs[i].getName();
            if (filename.endsWith(ENGINE_DESC_SUFFIX)) {
                // Compute the engine name
                int suffixStart = filename.lastIndexOf(ENGINE_DESC_SUFFIX);
                String engineName = filename.substring(0, suffixStart);

                // Test the URLs to see if they are in-set
                PageExtractor.RemotePageExtractor extractor = new PageExtractor.RemotePageExtractor(engineDescs[i], userAgent, false);
                URLInsetTester uit = new URLInsetTester(extractor, false);
                uit.testURLs(uniqueURLs, (TreeSet) returnedURLSets.get(engineName), new File(inputsDir, engineName + URL_INSET_SUFFIX));
            }
        }

        //
        // 6th, we test Nutch to see if it contains each
        // unique URL.  Write the results to inputsDir.
        //
        LOG.info("CreateInputs, 6 of 6:  Test membership of each URL in local Nutch segments...");
        for (int i = 0; i < segmentsDirs.length; i++) {
            PageExtractor.NutchExtractor extractor = new PageExtractor.NutchExtractor(segmentsDirs[i]);
            URLInsetTester uit = new URLInsetTester(extractor, false);
            uit.testURLs(uniqueURLs, (TreeSet) returnedURLSets.get(NUTCH_LABEL + "." + i), new File(inputsDir, NUTCH_LABEL + "." + i + URL_INSET_SUFFIX));
        }
    }

    /**
     * This assumes we have a directory full of all the information
     * we need.  We look in inputsDir for files of this format:
     *
     *  "queryList.txt"
     *  "uniqueURLs.txt"
     *  "searchEngineNameA.queryResults"
     *  "searchEngineNameA.urlInset"
     *  "searchEngineNameB.queryResults"
     *  "searchEngineNameB.urlInset"
     *  ...
     *
     * We assume that queryList.txt has only a few hundred
     * items in it, tops.  If that assumption doesn't hold,
     * then this code might not be efficent enough.
     */
    public void runTests(boolean testCoverage, boolean testOrdering, double coverageConsensus) throws IOException {
        //
        // Part I.  Compute the coverage numbers.
        //
        if (testCoverage) {
            computeCoverageScore(coverageConsensus);

            System.out.println();
            System.out.println();
        }

        //
        // Part I.5.  Compute the 'top-10 eccentric' score
        //
        if (testCoverage && (coverageConsensus > 0.0)) {
            computeEccentricScore(coverageConsensus);
            System.out.println();
            System.out.println();
        }

        //
        // Part II.  Compute the ordering scores.
        //
        //
        if (testOrdering) {
            computeOrderingScore();
        }
    }

    /**
     * Compute page-coverage over all the engines.
     * Uses information stored in "inputsDir".
     */
    private void computeCoverageScore(double coverageConsensus) throws IOException {
        //
        // 1.  Figure out how many engines we're testing, and how
        // many times a term needs to appear to satisfy "coverageConsensus"
        //
        int numEngines = 0;
        File contents[] = inputsDir.listFiles();
        for (int i = 0; i < contents.length; i++) {
            String filename = contents[i].getName();
            if (filename.endsWith(URL_INSET_SUFFIX)) {
                // Compute the engine name
                numEngines++;
            }
        }
        int requiredCount = (int) Math.ceil(numEngines * coverageConsensus);
        System.out.println("URL must be present in at least " + requiredCount + " (" + coverageConsensus + ") item(s)");

        //
        // 2.  Load in the complete uniquified URL list, along
        //    with counts of how many engines have the URL.
        //    Don't include terms that fail to satisfy coverageConsensus
        //
        TreeMap uniqueURLs = new TreeMap();
        DataInputStream in = new DataInputStream(new FileInputStream(new File(inputsDir, UNIQUE_URLS)));
        try {
            int numItems = in.readInt();
            for (int i = 0; i < numItems; i++) {
                String url = in.readUTF();
                int count = in.readInt();
                if (count >= requiredCount) {
                    uniqueURLs.put(url, new Integer(count));
                }
            }
        } finally {
            in.close();
        }

        //
        // 3.  Go through each engine and load in the list
        // of inset-URLs.
        //
        TreeMap urlInsetScores = new TreeMap();
        int maxInsetScore = uniqueURLs.size();
        for (int i = 0; i < contents.length; i++) {
            String filename = contents[i].getName();
            if (filename.endsWith(URL_INSET_SUFFIX)) {
                // Compute the engine name
                int suffixStart = filename.lastIndexOf(URL_INSET_SUFFIX);
                String engineName = filename.substring(0, suffixStart);

                // Load in the engine's url-inset list
                int insetScore = 0;
                DataInputStream din = new DataInputStream(new FileInputStream(contents[i]));
                try {
                    int numItems = din.readInt();

                    //
                    // Load in whether each URL was in-set or not.
                    // If it was in-set for the engine, and is in
                    // the qualified unique set overall, then the
                    // engine gets a point.
                    //
                    for (int j = 0; j < numItems; j++) {
                        String url = din.readUTF();
                        if (din.readBoolean() && uniqueURLs.get(url) != null) {
                            insetScore++;
                        }
                    }
                } finally {
                    din.close();
                }
                
                // When done processing this file, store the score
                urlInsetScores.put(engineName, new Integer(insetScore));
            }
        }

        //
        // Third, output the coverage statistics
        //
        System.out.println("Engine\t\tCoverage Score");
        System.out.println("--------------------------------");
        for (Iterator it = urlInsetScores.keySet().iterator(); it.hasNext(); ) {
            String engineName = (String) it.next();
            int score = ((Integer) urlInsetScores.get(engineName)).intValue();

            System.out.println(engineName + "\t\t" + score + " of " + maxInsetScore + "\t(" + ((score / (1.0 * maxInsetScore)) * 100) + "%)");
        }
    }

    /**
     * Figure out how many of an engine's URLs are
     * not "eccentric".  That is, the URL WILL appear
     * in at least "coverageConsensus" percentage of
     * the engines' results.
     */
    private void computeEccentricScore(double coverageConsensus) throws IOException {
        //
        // 1.  Figure out how many engines we're testing, and how
        // many times a term needs to appear to satisfy "coverageConsensus"
        //
        int numEngines = 0;
        File contents[] = inputsDir.listFiles();
        for (int i = 0; i < contents.length; i++) {
            String filename = contents[i].getName();
            if (filename.endsWith(URL_INSET_SUFFIX)) {
                // Compute the engine name
                numEngines++;
            }
        }
        int requiredCount = (int) Math.ceil(numEngines * coverageConsensus);
        System.out.println("URL must be present in at least " + requiredCount + " (" + coverageConsensus + ") item(s)");

        //
        // 2.  Load in the complete uniquified URL list, along
        //    with counts of how many engines have the URL.
        //    Don't include terms that fail to satisfy coverageConsensus
        //
        TreeMap sharedURLs = new TreeMap();
        DataInputStream in = new DataInputStream(new FileInputStream(new File(inputsDir, UNIQUE_URLS)));
        try {
            int numItems = in.readInt();
            for (int i = 0; i < numItems; i++) {
                String url = in.readUTF();
                int count = in.readInt();
                if (count >= requiredCount) {
                    sharedURLs.put(url, new Integer(count));
                }
            }
        } finally {
            in.close();
        }

        //
        // 3.  Go through each engine and load in its
        // top-10 list.  Check to see if each URL in
        // this set is also present in the "sharedURLs"
        // table.  The ratio of in-top-10 vs in-shared-set
        // is the value we're after for each engine.
        //
        TreeMap engineURLs = new TreeMap(), engineSharedURLs = new TreeMap();
        File resultFiles[] = inputsDir.listFiles();
        for (int i = 0; i < resultFiles.length; i++) {
            String filename = resultFiles[i].getName();
            if (filename.endsWith(QUERY_RESULTS_SUFFIX)) {
                // Compute engine name
                int suffixStart = filename.lastIndexOf(QUERY_RESULTS_SUFFIX);
                String engineName = filename.substring(0, suffixStart);
                
                // Load in results
                in = new DataInputStream(new FileInputStream(resultFiles[i]));
                int engineTopURLs = 0, inSharedSet = 0;
                try {
                    int numQueries = in.readInt();
                    for (int j = 0; j < numQueries; j++) {
                        String query = in.readUTF();
                        int numResults = in.readInt();
                        for (int k = 0; k < numResults; k++) {
                            String result = in.readUTF();

                            engineTopURLs++;
                            if (sharedURLs.containsKey(result)) {
                                inSharedSet++;
                            }
                        }
                    }
                    engineURLs.put(engineName, new Integer(engineTopURLs));
                    engineSharedURLs.put(engineName, new Integer(inSharedSet));
                } finally {
                    in.close();
                }
            }
        }

        //
        // 4.  Output stats
        //
        System.out.println("Engine\t\tIn-shared-set score");
        System.out.println("--------------------------------");
        for (Iterator it = engineURLs.keySet().iterator(); it.hasNext(); ) {
            String engineName = (String) it.next();
            int urlScore = ((Integer) engineURLs.get(engineName)).intValue();
            int sharedScore = ((Integer) engineSharedURLs.get(engineName)).intValue();

            System.out.println(engineName + "\t\t" + sharedScore + " of " + urlScore + "\t(" + ((sharedScore / (1.0 * urlScore)) * 100) + "%)");
        }
    }

    /**
     * Compute numbers that tell us how good the orderings are.
     *
     * Part of this test involves using the MarkovRankSolver to
     * compute a "best group-contribution ranking" that minimizes
     * the overall Kendall Tau distance between the complete
     * ranking and each contributing sublist.
     */
    private void computeOrderingScore() throws IOException {
        //
        // For an engine to say anything about two items,
        // both must be in-set, and at least one must be in
        // the top-10 list.
        //
        // Before we do anything, load in the result lists 
        // and the URL-inset data.
        //
        
        TreeMap engineResults = new TreeMap();
        engineInsetData = new TreeMap();
        File resultFiles[] = inputsDir.listFiles();
        for (int i = 0; i < resultFiles.length; i++) {
            String filename = resultFiles[i].getName();
            if (filename.endsWith(QUERY_RESULTS_SUFFIX)) {
                // Compute engine name
                int suffixStart = filename.lastIndexOf(QUERY_RESULTS_SUFFIX);
                String engineName = filename.substring(0, suffixStart);
                
                // Load in results
                DataInputStream in = new DataInputStream(new FileInputStream(resultFiles[i]));
                try {
                    TreeMap resultLists = new TreeMap();
                    int numQueries = in.readInt();
                    for (int j = 0; j < numQueries; j++) {
                        String query = in.readUTF();
                        int numResults = in.readInt();
                        String resultList[] = new String[numResults];
                        for (int k = 0; k < numResults; k++) {
                            resultList[k] = in.readUTF();
                        }
                        resultLists.put(query, resultList);
                    }
                    engineResults.put(engineName, resultLists);
                } finally {
                    in.close();
                }

                // Next, load in the inset-data
                in = new DataInputStream(new FileInputStream(new File(inputsDir, engineName + URL_INSET_SUFFIX)));
                try {
                    TreeSet insetURLs = new TreeSet();
                    int numItems = in.readInt();
                    for (int j = 0; j < numItems; j++) {
                        String url = in.readUTF();
                        if (in.readBoolean()) {
                            insetURLs.add(url);
                        }
                    }
                    engineInsetData.put(engineName, insetURLs);
                } finally {
                    in.close();
                }
            }
        }

        //
        // We now have two large useful structures.
        //
        // A. engineResults is a Map that maps engine names
        // to another Map.  The value Map maps Queries to
        // String Arrays of results.
        //
        // B. engineInsetData is a Map that maps engine names
        // to a Set.  This Set contains all the relevant URLs
        // that the engine has indexed.
        //
        
        //
        // Figure out all the pairwise statements that
        // come from an engine's top-10 list (not including
        // the ones from position 11 and lower).  
        //


        //
        // Each engine should have identical keys listed in
        // its Map from query terms to Arrays of results.  So
        // just pick the first one from 'engineResults'.
        //
        Map defaultQueryMap = (Map) engineResults.get((String) engineResults.firstKey());
        Map overallDistances = new TreeMap(), bestPageScores = new TreeMap();
        for (Iterator it = engineResults.keySet().iterator(); it.hasNext(); ) {
            String engineName = (String) it.next();
            overallDistances.put(engineName, new Double(0.0));
            bestPageScores.put(engineName, new Double(0.0));
        }

        // Iterate through every query.
        for (Iterator it = defaultQueryMap.keySet().iterator(); it.hasNext(); ) {
            String query = (String) it.next();
            //
            // Go through every engine, finding the results for the query.
            // Build a good full-ordering using the Markov solver
            //
            MarkovRankSolver fullOrdering = new MarkovRankSolver();
            for (Iterator it2 = engineResults.keySet().iterator(); it2.hasNext(); ) {
                String engineName = (String) it2.next();
                Map queryMap = (Map) engineResults.get(engineName);

                // Get results from this engine for the current query
                String results[] = (String[]) queryMap.get(query);
                
                fullOrdering.addOrdering(results);
            }

            fullOrdering.solveRanking();
            int numMarkovStates = fullOrdering.getNumStates();

            //
            // For each engine, compute the DT distance to the full-ordering
            //
            for (Iterator it2 = engineResults.keySet().iterator(); it2.hasNext(); ) {
                String engineName = (String) it2.next();
                Map queryMap = (Map) engineResults.get(engineName);
                String results[] = (String[]) queryMap.get(query);

                // Find how many binary misorderings there are between
                // the results list and the full markov list
                double curDistance = 0.0;
                if (results.length > 1) {
                    curDistance = fullOrdering.getKendallTauDistance(results, true);
                }
                double oldScore = ((Double) overallDistances.get(engineName)).doubleValue();
                overallDistances.put(engineName, new Double(oldScore + curDistance));
            }

            //
            // For each engine, compute the "Best Pages score", which
            // measures how close each engine's top-10 list matches the
            // Markov model's top-10 list.
            //
            for (Iterator it2 = engineResults.keySet().iterator(); it2.hasNext(); ) {
                String engineName = (String) it2.next();
                Map queryMap = (Map) engineResults.get(engineName);
                String results[] = (String[]) queryMap.get(query);

                //
                // Assign a score for each item in our results
                // list.
                double newScore = 0.0;
                for (int i = 0; i < results.length; i++) {
                    int markovPos = fullOrdering.getPos(results[i]);
                    newScore += (numMarkovStates - markovPos);
                }

                double oldScore = ((Double) bestPageScores.get(engineName)).doubleValue();
                bestPageScores.put(engineName, new Double(oldScore + newScore));
            }
        }

        // Emit score to stdout
        System.out.println("Engine\t\tNormalized Kendall Tau Distance");
        System.out.println("--------------------------------");
        for (Iterator it = overallDistances.keySet().iterator(); it.hasNext(); ) {
            String engineName = (String) it.next();
            Double fullDistance = (Double) overallDistances.get(engineName);
            System.out.println(engineName + "\t\t" + fullDistance);
        }

        System.out.println();
        System.out.println();
        System.out.println("Engine\t\t'Best-Page Score'");
        System.out.println("--------------------------------");
        for (Iterator it = bestPageScores.keySet().iterator(); it.hasNext(); ) {
            String engineName = (String) it.next();
            Double score = (Double) bestPageScores.get(engineName);
            System.out.println(engineName + "\t\t" + score);
        }
    }

    /**
     * Take all the file-args we need to compute test results.
     */
    public static void main(String argv[]) throws IOException, ParseException {
        if (argv.length < 2) {
            System.out.println("Usage: java net.nutch.quality.QualityTestTool (-initTest <externalEngineDirectory> <userAgent> <queryList> [-nutchSegment <segmentsDirectory0>] [-nutchSegment <segmentsDirectory1>] ... [-nutchSegmentSet <segmentDir>]) | (-repeatTest <existingWorkDir>) [-coverageConsensus <double>] [-noCoverageTest] [-noOrderingTest]");
            System.out.println();
            System.out.println("Note that 'coverageConsensus' should be a value between 0.0 and 1.0");
            return;
        }

        // vars for parsing command-line options
        File extEngineDescs = null, queryList = null;
        File existingWorkDir = null;
        String userAgent = null;
        Vector nutchSegments = new Vector();
        boolean initTest = false, repeatTest = false;
        boolean testCoverage = true, testOrdering = true;
        double coverageConsensus = 0.0;

        // loop through cmd args
        for (int i = 0; i < argv.length; i++) {
            if ("-initTest".equals(argv[i])) {
                extEngineDescs = new File(argv[i + 1]);
                userAgent = argv[i + 2];
                queryList = new File(argv[i + 3]);
                i += 3;
                initTest = true;
            } else if ("-repeatTest".equals(argv[i])) {
                existingWorkDir = new File(argv[i+1]);
                repeatTest = true;
                i++;
            } else if ("-nutchSegment".equals(argv[i])) {
                nutchSegments.add(new File(argv[i + 1]).getPath());
                i++;
            } else if ("-nutchSegmentSet".equals(argv[i])) {
                File segmentSet = new File(argv[i+1]);
                File segmentSubdirs[] = segmentSet.listFiles();
                for (int j = 0; i < segmentSubdirs.length; j++) {
                    if (segmentSubdirs[j].isDirectory()) {
                        nutchSegments.add(segmentSubdirs[i].getPath());
                    }
                }
                i++;
            } else if ("-coverageConsensus".equals(argv[i])) {
                coverageConsensus = Double.parseDouble(argv[i+1]);
                i++;
            } else if ("-noCoverageTest".equals(argv[i])) {
                testCoverage = false;
            } else if ("-noOrderingTest".equals(argv[i])) {
                testOrdering = false;
            } else {
                System.out.println("Unknown arg: " + argv[i]);
                return;
            }
        }

        // Know what kind of test to run
        if ((initTest && repeatTest) ||
            (! initTest && ! repeatTest)) {
            System.out.println("Must either 'initTest' or 'repeatTest'");
            return;
        }

        // Make sure something's being run
        if (! testCoverage && ! testOrdering) {
            System.out.println("Must run at least one test.");
            return;
        }

        // Build the QTT
        QualityTestTool qtt = null;
        if (initTest) {
            int i = 0;
            String segments[] = new String[nutchSegments.size()];
            for (Enumeration e = nutchSegments.elements(); e.hasMoreElements(); i++) {
                segments[i] = (String) e.nextElement();
            }
            qtt = new QualityTestTool(extEngineDescs, userAgent, segments, queryList);
        } else {
            qtt = new QualityTestTool(existingWorkDir);
        }

        // Kick it off.
        qtt.runTests(testCoverage, testOrdering, coverageConsensus);
    }
}
