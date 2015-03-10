/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.quality;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import net.nutch.util.*;
import net.nutch.searcher.*;

/*****************************************************
 * This class modifies search parameters hill-climbing
 * style, trying to find the best set of values.  It uses
 * the results of a QualityTestTool run as a test set.
 * (That will include both a query term list and a 
 * set of results from other search engines.)
 * Since we're dynamically adjusting params, we need
 * to query a live Nutch searcher, so we'll need to
 * be provided the segment directories.
 *
 * @author Mike Cafarella
 *****************************************************/
public class SearchOptimizer {
    static final Logger LOG = LogFormatter.getLogger("net.nutch.quality.SearchOptimizer");
    private final static int NUM_RESTARTS = 10;

    final static String QUERY_LIST = "queryList.txt";
    final static String URL_INSET_SUFFIX = ".urlInset";
    final static String QUERY_RESULTS_SUFFIX = ".queryResults";
    final static String ENGINE_DESC_SUFFIX = ".src";

    PageExtractor.NutchExtractor nutch;
    Random rand;
    Vector queryTerms;
    File segmentsDir;
    File inputsDir;

    TreeMap remoteExtractors = new TreeMap();

    // 'engineResults' is a Map of engine name to a Map of
    // Query to Ordered-Result-List
    TreeMap engineResults = new TreeMap();

    // 'insetTables' and 'outsetTables' are Maps to Sets.
    // Each Set contains the appropriate URLs.
    TreeMap insetTables = new TreeMap(), outsetTables = new TreeMap();

    static float[] STEP_SIZE = {0.01f, 0.05f, 0.1f};
    
    /**
     * Give the dirs where all the segments can be found, plus
     * the dir for existing QualityTestTool results.
     */
    public SearchOptimizer(File segmentsDir, File inputsDir, String userAgent, Random rand) throws Exception {
        this.segmentsDir = segmentsDir;
        this.inputsDir = inputsDir;
        this.rand = rand;
        
        this.nutch = new PageExtractor.NutchExtractor(segmentsDir.getPath());

        //
        // Now, load in everything from the existing QualityTestTool
        // directory.  This includes:
        //   1.  Query-term list
        //   2.  Results from other engines
        //

        //
        // 1.  Query-terms
        // 
        File queryList = new File(inputsDir, QUERY_LIST);
        BufferedReader in = new BufferedReader(new FileReader(queryList));
        
        this.queryTerms = new Vector();
        try {
            String term = in.readLine();
            while (term != null) {
                queryTerms.add(term.trim());
                term = in.readLine();
            }
        } finally {
            in.close();
        }

        //
        // 2.  Other engines' results.
        //
        File contents[] = inputsDir.listFiles();
        for (int i = 0; i < contents.length; i++) {
            String filename = contents[i].getName();

            if (filename.endsWith(URL_INSET_SUFFIX)) {
                //
                // First, load whether engines found URLs to
                // be in or out of set.  If a URL is not found
                // in either table, in means that we have never
                // asked the engine for that URL.
                //

                // Compute the engine name
                int suffixStart = filename.lastIndexOf(URL_INSET_SUFFIX);
                String engineName = filename.substring(0, suffixStart);

                if (! engineName.startsWith("Nutch")) {
                    Set insetURLs = new HashSet();
                    Set outsetURLs = new HashSet();
                    insetTables.put(engineName, insetURLs);
                    outsetTables.put(engineName, outsetURLs);

                    // Load in the engine's url-inset list
                    int curInsetScore = 0;
                    DataInputStream din = new DataInputStream(new FileInputStream(contents[i]));
                    try {
                        int numItems = din.readInt();

                        // Load in whether each URL was in-set or not.
                        for (int j = 0; j < numItems; j++) {
                            String url = din.readUTF();
                            if (din.readBoolean()) {
                                insetURLs.add(url);
                            } else {
                                outsetURLs.add(url);
                            }
                        }
                    } finally {
                        din.close();
                    }
                }

            } else if (filename.endsWith(QUERY_RESULTS_SUFFIX)) {
                //
                // Second, load the actual returned-values that each
                // engine gave in response to a query.
                //

                // Compute the engine name
                int suffixStart = filename.lastIndexOf(QUERY_RESULTS_SUFFIX);
                String engineName = filename.substring(0, suffixStart);

                if (! engineName.startsWith("Nutch")) {

                    // Load in results
                    DataInputStream din = new DataInputStream(new FileInputStream(contents[i]));
                    try {
                        TreeMap resultLists = new TreeMap();
                        int numQueries = din.readInt();
                        for (int j = 0; j < numQueries; j++) {
                            String query = din.readUTF();
                            int numResults = din.readInt();
                            String resultList[] = new String[numResults];
                            for (int k = 0; k < numResults; k++) {
                                resultList[k] = din.readUTF();
                            }
                            resultLists.put(query, resultList);
                        }
                        engineResults.put(engineName, resultLists);
                    } finally {
                        din.close();
                    }
                }
            } else if (filename.endsWith(ENGINE_DESC_SUFFIX)) {
                // Third, build up the remote-extractor db
                int suffixStart = filename.lastIndexOf(ENGINE_DESC_SUFFIX);
                String engineName = filename.substring(0, suffixStart);

                PageExtractor.RemotePageExtractor extractor = new PageExtractor.RemotePageExtractor(contents[i], userAgent, false);
                remoteExtractors.put(engineName, extractor);
            }
        }
    }

    /**
     * Run Nutch against the test query set, and compute a
     * score.  We try to adjust search params so that this
     * score is maximized.
     */
    double testNutch() {
        Map nutchResults = new TreeMap();

        //
        // Loop through all the query terms, and ask Nutch for
        // responses.  Record them all.
        //
        for (Enumeration e = queryTerms.elements(); e.hasMoreElements(); ) {
            String query = (String) e.nextElement();
            Object results[] = null;
            try {
                results = nutch.applyQuery(query).toArray();
                nutchResults.put(query, results);
            } catch (IOException ex) {
                continue;
            }
        }

        //
        // Iterate through all of Nutch's responses to make sure
        // we have tested for the URLs' presence in other engines.
        //
        String sampleKey = (String) insetTables.firstKey();
        Set testInset = (Set) insetTables.get(sampleKey);
        Set testOutset = (Set) outsetTables.get(sampleKey);

        for (Iterator it = nutchResults.keySet().iterator(); it.hasNext(); ) {
            String query = (String) it.next();
            Object results[] = (Object[]) nutchResults.get(query);
            for (int i = 0; i < results.length; i++) {
                if (!testInset.contains(results[i]) && 
                    !testOutset.contains(results[i])) {
                    performInsetTest((String) results[i]);
                }
            }
        }

        //
        // Go through all the queries and compute a score for
        // the Nutch results.
        //
        int orderingScore = 0, normalizer = 0;
        for (Enumeration e = queryTerms.elements(); e.hasMoreElements(); ) {
            String query = (String) e.nextElement();
            Object results[] = (Object[]) nutchResults.get(query);

            for (int i = 0; i < results.length; i++) {
                String result1 = (String) results[i];
                for (int j = i+1; j < results.length; j++) {
                    String result2 = (String) results[j];

                    int numVoters = countStatements(query, result1, result2);
                    int term1Votes = countVotes(query, result1, result2);
                    
                    if ((numVoters > 1) && (term1Votes != (numVoters / 2))) {
                        if (term1Votes > (numVoters / 2.0)) {
                            orderingScore++;
                        }
                        normalizer++;
                    }
                }
            }
        }
        return (orderingScore / (normalizer * 1.0));
    }

    /**
     * countStatements() returns how many engines make some kind
     * of statement about the query and result-pair given.
     */
    int countStatements(String query, String url1, String url2) {
        int engineCount = 0;

        // Iterate through all non-Nutch engines
        for (Iterator it = engineResults.keySet().iterator(); it.hasNext(); ) {

            String engineName = (String) it.next();
            if (! engineName.startsWith("Nutch")) {

                // The engine must have both items in-set
                if (inset(engineName, url1) && inset(engineName, url2)) {
                    // Now make sure at least one is in the top-10
                    Map resultLists = (Map) engineResults.get(engineName);
                    String results[] = (String[]) resultLists.get(query);

                    for (int i = 0; i < results.length; i++) {
                        if (results[i].equals(url1) || results[i].equals(url2)) {
                            engineCount++;
                        }
                    }               
                }
            }
        }
        return engineCount;
    }

    /**
     * countVotes() returns how many votes are cast for the
     * given ordering of terms (in response to the query).
     */
    int countVotes(String query, String url1, String url2) {
        int url1Votes = 0;

        // Iterate through all non-Nutch engines
        for (Iterator it = engineResults.keySet().iterator(); it.hasNext(); ) {

            String engineName = (String) it.next();
            if (! engineName.startsWith("Nutch")) {

                // The engine must have both items in-set
                if (inset(engineName, url1) && inset(engineName, url2)) {
                    // Now make sure at least one is in the top-10
                    Map resultLists = (Map) engineResults.get(engineName);
                    String results[] = (String[]) resultLists.get(query);

                    int url1Pos = Integer.MAX_VALUE, url2Pos = Integer.MAX_VALUE;
                    for (int i = 0; i < results.length; i++) {
                        if (results[i].equals(url1)) {
                            url1Pos = i;
                        }
                        if (results[i].equals(url2)) {
                            url2Pos = i;
                        }
                    }

                    if ((url1Pos < Integer.MAX_VALUE || 
                         url2Pos < Integer.MAX_VALUE) &&
                        (url1Pos < url2Pos)) {
                        url1Votes++;
                    }
                }
            }
        }
        return url1Votes;
    }

    /**
     * Return whether the given engine has indexed the url.
     * Every URL should be known to be in-set or out-set by
     * the time we reach this point.  We will have to contact
     * live search engines during optimization, because modifying
     * Nutch parameters might reveal novel URLs.
     *
     * However, we should have already found those URLs and
     * made the necessary modifications to "insetURLs" and "outsetURLs"
     * by the time we get here.
     */
    boolean inset(String engineName, String url) {
        Set insetURLs = (Set) insetTables.get(engineName);
        if (insetURLs.contains(url)) {
            return true;
        }

        Set outsetURLs = (Set) outsetTables.get(engineName);
        if (outsetURLs.contains(url)) {
            return false;
        }

        throw new IllegalArgumentException("For engine " + engineName + ", a URL is always in-set or out-set.");
    }

    /**
     * Our inset records are lacking a URL.  Test all the remote
     * engines and find out whether it's inset or not.
     *
     * REMIND - mjc
     * I know, I know, we should be writing down the results of 
     * this test for future runs.  This is coming soon.
     */
    void performInsetTest(String query) {
        //
        // For now, we just add any unknown URL to the "out-of-set" list
        //
        for (Iterator it = engineResults.keySet().iterator(); it.hasNext(); ) {
            String engineName = (String) it.next();
            Set outsetURLs = (Set) outsetTables.get(engineName);
            outsetURLs.add(query);
        }

        //
        // Ask every remote service to test for the presence of the URL.
        //
        for (Iterator it = remoteExtractors.keySet().iterator(); it.hasNext(); ) {
            String engineName = (String) it.next();
            PageExtractor.RemotePageExtractor remoteExtractor = (PageExtractor.RemotePageExtractor) remoteExtractors.get(engineName);

            boolean inSet = false;
            ArrayList results = null;
            try {
                results = remoteExtractor.applyQuery(query);
            } catch (IOException ie) {
                // Count query as out-of-set
                LOG.info("Could not contact " + engineName + " to test " + query);
            }

            if (results != null) {
                for (Iterator it2 = results.iterator(); it2.hasNext(); ) {
                    String val = (String) it2.next();
                    if (val.trim().compareTo(query) == 0) {
                        inSet = true;
                        break;
                    }
                }
            }

            if (inSet) {
                Set insetURLs = (Set) insetTables.get(engineName);
                insetURLs.add(query);
                System.err.println("Engine " + engineName + " has " + query);
            } else {
                Set outsetURLs = (Set) outsetTables.get(engineName);
                outsetURLs.add(query);
                System.err.println("Engine " + engineName + " lacks " + query);
            }
        }
    }


    /*********************************************************
     * Describes the current parameter settings and how well
     * they perform.
     *********************************************************/
    class ParameterLocation {
        float scoreParams[] = new float[3];
        float phraseParam;
        double score;

        /**
         * Initialize the ParameterLocation with standard
         * start-point values.
         */
        public ParameterLocation() {
            // We have three scores to balance: url, anchor, and content.
            // It's best to think of these as summing to 1, so that
            // during our work we realize adjusting one value
            // means we also adjust the others.  Later we
            // convert them into a form that QueryTranslator wants.

            // PhraseBoost is a value between 0 and 1.  Its counterpart,
            // UnorderedBoost is equal to (1 - PhraseBoost).

            randomizePosition();
        }

        /**
         * Inititalize with given values
         */
        public ParameterLocation(float scoreParams[], float phraseParam) {
            System.arraycopy(scoreParams, 0, this.scoreParams, 0, this.scoreParams.length);
            this.phraseParam = phraseParam;
            evaluate();
        }

        /**
         */
        public void getScoreParam(float newScoreParams[]) {
            System.arraycopy(scoreParams, 0, newScoreParams, 0, scoreParams.length);
        }

        /**
         */
        public float getPhraseParam() {
            return phraseParam;
        }

        /**
         */
        public double getScore() {
            return score;
        }

        /**
         * This is used for random restarts of the parameter-space
         * search.
         */
        public void randomizePosition() {
            scoreParams[0] = rand.nextFloat();
            scoreParams[1] = (rand.nextFloat() * (1 - scoreParams[0]));
            scoreParams[2] = 1 - scoreParams[0] - scoreParams[1];

            this.phraseParam = rand.nextFloat();
            evaluate();
        }

        /**
         * Finds the score for this parameter setting.  Need
         * to convert these values into the style that 
         * QueryTranslator wants.
         */
        private void evaluate() {
            QueryTranslator.setUrlBoost(scoreParams[0] / scoreParams[2]);
            QueryTranslator.setAnchorBoost(scoreParams[1] / scoreParams[2]);
            QueryTranslator.setPhraseBoost(phraseParam / (1 - phraseParam));
            this.score = testNutch();
        }

        /**
         */
        public String toString() {
            return "PL  " +
                "url:" + scoreParams[0] +
                ", anchor:" + scoreParams[1] +
                ", phrase:" + phraseParam + 
                ", score:" + score;
        }
    }

    /**
     * Find the best possible setting for search parameters,
     * using hillclimbing.  When we have reached a local 
     * maximum, we do a random restart.  We always remember
     * the best settings.
     */
    public void optimizeParams() {
        float startingPoint[] = {1.0f/3f, 1.0f/3f, 1.0f/3f};
        ParameterLocation curLoc = new ParameterLocation(startingPoint, 0.5f);
        ParameterLocation newLoc = null, bestLoc = null;

        //
        // We want several randomized restarts of hillclimbing
        // so we avoid getting trapped in local maxima.
        //
        LOG.info("Now starting search-parameter optimization");
        for (int restart = 0; restart < NUM_RESTARTS; restart++) {

            LOG.info("Parameter-hillclimb, starting from " + curLoc);
            //
            // Take repeated best-steps until we hit a local maximum
            //
            while ((newLoc = takeBestStep(curLoc)).getScore() > curLoc.getScore()) {
                curLoc = newLoc;
                LOG.info("Ascended to " + curLoc);
            }

            //
            // Remember this one if it's the best we've seen
            //
            if ((bestLoc == null) || (curLoc.getScore() > bestLoc.getScore())) {
                bestLoc = curLoc;
                LOG.info("New best setting is " + bestLoc);
            } else {
                LOG.info("Best setting is still " + bestLoc);
            }

            //
            // Randomize for the next step, if there is one.
            //
            if (restart + 1 < NUM_RESTARTS) {
                LOG.info("Randomized parameters and restarting...");
                curLoc = new ParameterLocation();
            }
        }

        LOG.info("Found best parameter settings: " + bestLoc);
    }
    
    /**
     * Go through a number of loops that iterate through
     * all reasonable parameter steps from this point.
     * In the innermost loop, test Nutch's performance,
     * and remember the best score seen so far.
     *
     * Before a loop begins, be sure to restore its
     * values.
     */
    private ParameterLocation takeBestStep(ParameterLocation startingPoint) {
        float scoreParam[] = new float[3];
        float phraseParam = 0;
        ParameterLocation curParams = startingPoint;

        //
        // Make steps of varying size from the current location
        // and find the best adjustment.
        //
        for (int g = 0; g < STEP_SIZE.length; g++) {
            for (int scoreSign = -1; scoreSign <= 1; scoreSign+=2) {
                // Compute our step size for "score"
                float scoreStep = scoreSign * STEP_SIZE[g];

                // Now make each step in each of the 3 directions
                // from the current "score" settings.
                for (int i = 0; i < scoreParam.length; i++) {
                    // 1.  Restore the values for "score"
                    curParams.getScoreParam(scoreParam);

                    // 2.  Adjust one step
                    for (int j = 0; j < scoreParam.length; j++) {
                        if (j == i) {
                            scoreParam[j] += scoreStep;
                        } else {
                            scoreParam[j] -= (scoreStep / 2);
                        }
                    }

                    // Make sure we adjust values within legal limits
                    if (scoreParam[0] < 0 || scoreParam[0] > 1 ||
                        scoreParam[1] < 0 || scoreParam[1] > 1 ||
                        scoreParam[2] < 0 || scoreParam[2] > 1) {
                        continue;
                    }

                    //
                    // OK, now we do the same for "phrase"
                    //
                    for (int k = 0; k < STEP_SIZE.length; k++) {
                        for (int phraseSign = -1; phraseSign <= 1; phraseSign+=2) {
                            float phraseStep = phraseSign * STEP_SIZE[k];
                            // 1. Restore the value for phrase
                            phraseParam = curParams.getPhraseParam();

                            // 2. Adjust one step
                            phraseParam += ((phraseSign) * STEP_SIZE[k]);

                            if (phraseParam < 0 || phraseParam > 1) {
                                continue;
                            }

                            //
                            // Finally, test Nutch here!
                            //
                            ParameterLocation newParams = new ParameterLocation(scoreParam, phraseParam);
                            LOG.info("Now testing position: " + newParams);
                            if (newParams.getScore() > curParams.getScore()) {
                                curParams = newParams;
                            }
                        }
                    }
                }
            }
        }
        return curParams;
    }


    /**
     * Start hillclimbing on parameters, tested on 
     * the given set.
     */
    public static void main(String argv[]) throws Exception {
        if (argv.length < 3) {
            System.out.println("Usage: java net.nutch.quality.SearchOptimizer <localInputsDir> <segmentsDir> <userAgent> [-seed <seed>]");
            return;
        }
        
        File existingDir = new File(argv[0]);
        File segmentsDir = new File(argv[1]);
        String userAgent = argv[2];
        long seed = new Random().nextLong();

        if (argv.length > 3 && "-seed".equals(argv[3])) {
            try {
                seed = Long.parseLong(argv[4]);
            } catch (NumberFormatException nfe) {
                System.out.println("Seed is badly-formatted: " + argv[4]);
                return;
            }
        }

        SearchOptimizer so = new SearchOptimizer(segmentsDir, existingDir, userAgent, new Random());
        so.optimizeParams();
    }
}
