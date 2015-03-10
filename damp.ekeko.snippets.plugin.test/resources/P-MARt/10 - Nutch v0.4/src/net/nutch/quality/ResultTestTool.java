/************************************************
 *  Copyright (c) 2003 Michael Cafarella
 ***********************************************/
package net.nutch.quality;

import java.io.*;
import java.util.*;

import net.nutch.html.*;
import net.nutch.searcher.*;

/*****************************************************
 * The ResultTestTool lets us test the quality of our
 * search engine.  It uses a list of queries and runs
 * them against Nutch.  It then runs the same list of
 * queries against some authoritative source, the results
 * of which are found in a flat file.  This source might
 * be hand or machine generated - this tool just needs
 * a list of results.
 *
 * We compute various stats based on how Nutch compares.
 *
 * This lets us tell, roughly, how much of a difference
 * our improvements make.
 *
 * @author Mike Cafarella
 *****************************************************/
public class ResultTestTool {
    //
    // Interfaces and inner classes for the different
    // search result sources
    //

    /**
     * This interface provides simple access to a search engine's
     * query/result set.  It returns a Vector of URL Strings
     * that represent the top hits against the engine.  All
     * search engines we're interested in can implement this
     * basic interface.
     */
    interface SearchEngine {
        public Vector search(String query, int maxResults) throws IOException;
    }

    /**
     * Implement the SearchEngine interface with our Nutch
     * system.  We create a LuceneSegmentSearcher out of some
     * given segments, and query it.
     */
    class NutchEngine implements SearchEngine {
        NutchBean searcher;

        /**
         * Give the location of the segments dir.
         */
        public NutchEngine(String dir) throws IOException {
            searcher = new NutchBean(new File(dir));
        }

        /**
         * Search for the given term and return no more than
         * maxResults URL Strings in the Vector.
         */
        public Vector search(String queryStr, int maxResults) throws IOException {
            Vector results = new Vector();
            Query query = Query.parse(queryStr);
            
            Hits hits = searcher.search(query, maxResults);
            long max = Math.min(hits.getTotal(), maxResults);
            for (int i = 0; i < max; i++) {
                HitDetails details = searcher.getDetails(hits.getHit(i));
                results.add(details.getValue("url"));
            }
            return results;
        }
    }
 
    /**
     * Implement the SearchEngine interface for a different 
     * system.  For now this is just a flat file of results, 
     * not a dynamic search.
     */
    class ResultsList implements SearchEngine {
        Hashtable resultTable = new Hashtable();

        /**
         * Load in a results list.  We will compare queries
         * against this flat list.
         */
        public ResultsList(File resultsList) throws IOException {
            DataInputStream in = new DataInputStream(new FileInputStream(resultsList));
            try {
                int numQueries = in.readInt();
                System.out.println("Number queries: " + numQueries);
                for (int i = 0; i < numQueries; i++) {
                    String curQuery = in.readUTF();
                    int numResults = in.readInt();
                    if (verbose) {
                        System.out.println("For " + curQuery + ": " + numResults);
                    }

                    // Extract all the results
                    Vector resultList = new Vector();
                    for (int j = 0; j < numResults; j++) {
                        String str = in.readUTF();
                        resultList.add(str);
                    }
                    resultTable.put(curQuery, resultList);
                }
            } finally {
                in.close();
            }
        }

        /**
         * Grab a set of search results from the table
         */
        public Vector search(String queryStr, int maxResults) {
            Vector results = new Vector();
            Vector hits = (Vector) resultTable.get(queryStr);
            if (hits != null) {
                for (Enumeration e = hits.elements(); e.hasMoreElements() && maxResults > 0; maxResults--) {
                    results.add(e.nextElement());
                }
            }
            return results;
        }
    }

    //
    // Interfaces and Inner classes for measuring quality
    //

    /**
     * QualityMetric computes a single value for many calls to
     * computeMetric().
     */
    interface QualityMetric {
        public void computeMetric(String query, Vector testResults, Vector answerResults);
        public double getScore();
        public long scoredPoints();
        public long maxPoints();
        public String getName();

    }

    /**
     * The PerfectPage metric works as follows:
     *
     * For the purposes of our metric, we assume that the first answer
     * given in "answerResults" is the "Perfect Page" for that query.
     *
     * If we find the PP within the first topChunk of testResults, then we
     * give a point.
     *
     * If we find a page from the PP's domain within the first topChunk of
     * testResults, then we give a half-point.  (Not yet implemented!)
     *
     * If there are no results from answerResults, it's a no-op.
     *
     * Scores are computed across many queries.  We divide the
     * actual points by possible points, and give a score
     * between 0 and 1.0.
     *
     */
    class PerfectPageMetric implements QualityMetric {
        long points = 0, possiblePoints = 0;
        /**
         * The PerfectPageMetric takes the best result from answerResults.
         * If it's found in testResults, we award a point.
         * Soon, we will award a half-point for getting the domain right.
         */
        public void computeMetric(String query, Vector testResults, Vector answerResults) {
            // Get the best result
            if (answerResults != null && answerResults.size() > 0) {
                possiblePoints++;
                String perfectPage = (String) answerResults.elementAt(0);
                
                // Look for it in the test set
                if (testResults != null) {
                    if (verbose) {
                        System.out.println("PerfectPage: " + perfectPage);
                    }
                    for (Enumeration e = testResults.elements(); e.hasMoreElements(); ) {
                        String curTest = (String) e.nextElement();
                        if (curTest.equals(perfectPage)) {
                            points++;
                            if (verbose) {
                                System.out.println("  MATCHED: " + curTest);
                            }
                            break;
                        } else {
                            if (verbose) {
                                System.out.println("  failed: " + curTest);
                            }
                        }
                    }
                }
            }
        }

        /**
         */
        public double getScore() {
            return points / (possiblePoints * 1.0);
        }

        public long scoredPoints() {
            return points;
        }

        public long maxPoints() {
            return possiblePoints;
        }

        /**
         */
        public String getName() {
            return "PerfectPage";
        }
    }

    /**
     * The GoodEnough metric works as follows:
     *
     * Take both testResults and answerResults.
     *
     * For every URL in testResults that also appears in answerResults,
     * we award a point.
     *
     * We divide the actual points by the possible points, and give
     * a score between 0 and 1.0.
     */
    class GoodEnoughMetric implements QualityMetric {
        long points = 0, possiblePoints = 0;

        /**
         * The GoodEnoughMetric looks for each answer in the given test set.
         * Every time it's present, we award a point.
         */
        public void computeMetric(String query, Vector testResults, Vector answerResults) {
            // Go through all the answers
            if (answerResults != null && answerResults.size() > 0) {
                possiblePoints += Math.min(answerResults.size(), topChunk);

                if (testResults != null) {
                    int count = 0;
                    for (Enumeration e = testResults.elements(); e.hasMoreElements() && count < topChunk; count++) {
                        String testItem = (String) e.nextElement();

                        // Does the testItem appear in the answers?
                        int count2 = 0;
                        for (Enumeration e2 = answerResults.elements(); e2.hasMoreElements() && count2 < topChunk; count2++) {
                            String answer = (String) e2.nextElement();
                            if (testItem.equals(answer)) {
                                points++;
                            }
                        }
                    }
                }
            }
        }

        /**
         * Get the score, normalized to 0 .. 1.0
         */
        public double getScore() {
            return points / (possiblePoints * 1.0);
        }

        public long scoredPoints() {
            return points;
        }

        public long maxPoints() {
            return possiblePoints;
        }

        /**
         */
        public String getName() {
            return "GoodEnough";
        }
    }


    //
    // ResultTestTool members
    //

    SearchEngine testEngine = null, answerEngine = null;
    boolean verbose = false;
    int topChunk = 0;

    /**
     * Build ResultTestTool
     */
    public ResultTestTool(String segments, String results, boolean verbose, int topChunk) throws IOException {
        testEngine = new NutchEngine(segments);
        answerEngine = new ResultsList(new File(results));
        this.verbose = verbose;
        this.topChunk = topChunk;
    }

    /**
     * Run testQueries with all the metrics we know about.
     */
    public void testAllMetrics(File queryFile) throws IOException {
        // Build the metrics
        QualityMetric metrics[] = new QualityMetric[2];
        metrics[0] = new PerfectPageMetric();
        metrics[1] = new GoodEnoughMetric();

        // Run our long test suite
        System.out.println("Running test suite");
        testQueries(queryFile, metrics);

        // Emit the results
        System.out.println("Metric Results");
        System.out.println("-------------------------------");
        for (int i = 0; i < metrics.length; i++) {
            System.out.println(metrics[i].getName() + ": " + metrics[i].scoredPoints() + " of " + metrics[i].maxPoints() + " (" + metrics[i].getScore() + ")");
        }
    }

    /**
     * Run a battery of tests against the Nutch search engine.
     * We also run the tests against the otherEngine.  We then
     * compute a number based on the test.
     */
    public void testQueries(File queryFile, QualityMetric metrics[]) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(queryFile));
        try {
            String queryStr = null;
            while ((queryStr = reader.readLine()) != null) {
                queryStr = queryStr.trim();

                // First, execute our own search
                Vector testResults = testEngine.search(queryStr, topChunk);

                // Second, search against other results
                Vector answerResults = answerEngine.search(queryStr, topChunk);

                // Compute stats
                if (verbose) {
                    System.out.println("Running test on " + queryStr);
                }
                for (int i = 0; i < metrics.length; i++) {
                    metrics[i].computeMetric(queryStr, testResults, answerResults);
                }
            }
        } finally {
            reader.close();
        }
    }

    /**
     * Run the ResultTestTool
     */
    public static void main(String argv[]) throws IOException {
        if (argv.length < 3) {
            System.out.println("Usage: java net.nutch.quality.ResultTestTool <segments> <resultSet> <queryList> [-verbose] [-topChunk chunkSize]");
            return;
        }
        boolean verbose = false;
        int topChunk = 10;
        for (int i = 3; i < argv.length; i++) {
            if ("-verbose".equals(argv[i])) {
                verbose = true;
            }
            if ("-topChunk".equals(argv[i])) {
                topChunk = Integer.parseInt(argv[i + 1]);
                i++;
            }
        }

        ResultTestTool rtt = new ResultTestTool(argv[0], argv[1], verbose, topChunk);
        rtt.testAllMetrics(new File(argv[2]));
    }
}
