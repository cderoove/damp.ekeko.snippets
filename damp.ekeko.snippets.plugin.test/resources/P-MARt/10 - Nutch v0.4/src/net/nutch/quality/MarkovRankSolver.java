/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.quality;

import java.util.*;

/*********************************************
 * This finds a ranking of all known pages that
 * minimizes the Kendall Tau distance between the
 * full-ranking and each component ranking.
 * 
 * @author Mike Cafarella
 *********************************************/
public class MarkovRankSolver {

    // Remember all the ranking objects
    Vector orderings = new Vector();
    boolean solved = false;
    TreeMap fullRanking = new TreeMap();

    /**
     * The MarkovRankSolver takes a bunch of rankers.  When
     * there's a call to solveRanking(), we return an array
     * of all the results from those rankers.
     */
    public MarkovRankSolver() {
    }

    /**
     * Add an ordering of items to the MRS' working set.
     * You can call this function as much as you like
     * prior to calling "solve".
     */
    public void addOrdering(Object[] ordering) {
        orderings.add(ordering);
    }

    /**
     * Solving the Markov chain requires N^2 space,
     * where N is the number of unique items returned
     * by the list rankers.  Keep this in mind!
     */
    public void solveRanking() {
        //
        // 1.  Get all known states from the orderings.
        //     Uniquify them.  Then build a state set.
        //
        TreeSet stateSet = new TreeSet();
        Vector allPositions = new Vector();

        for (Enumeration e = orderings.elements(); e.hasMoreElements(); ) {
            Object ordering[] = (Object[]) e.nextElement();
            TreeMap curItemPositions = new TreeMap();

            for (int i = 0; i < ordering.length; i++) {
                stateSet.add(ordering[i]);
                curItemPositions.put(ordering[i], new Integer(i));
            }
            allPositions.add(curItemPositions);
        }
        int s = 0;
        Object states[] = new Object[stateSet.size()];
        for (Iterator it = stateSet.iterator(); it.hasNext(); s++) {
            states[s] = it.next();
        }

        //
        // 2.  Build connectivity matrix.  Each cell
        //     has "1" or "0" in it.
        //
        byte transitions[][] = new byte[states.length][];
        for (int i = 0; i < transitions.length; i++) {
            transitions[i] = new byte[states.length];
        }

        //
        // 3. Iterate through each elt in the lower-left triangle.
        // Also fill in value for its dual in the upper-right.
        //
        for (int i = 0; i < states.length; i++) {
            for (int j = 0; j < i; j++) {

                // Find what a majority of rankers think.
                int item1Better = 0, item2Better = 0;
                for (Enumeration e = allPositions.elements(); e.hasMoreElements(); ) {
                    TreeMap curItemPositions = (TreeMap) e.nextElement();
                    Integer pos1 = (Integer) curItemPositions.get(states[i]);
                    Integer pos2 = (Integer) curItemPositions.get(states[j]);

                    if (pos1 != null && pos2 != null) {

                        if (pos1.intValue() < pos2.intValue()) {
                            item1Better++;
                        } else if (pos1.intValue() > pos2.intValue()) {
                            item2Better++;
                        }
                    }
                }

                //
                // If there's a majority to be found, fill in the 
                // transition matrix.  We fill in a "1" when we want
                // to make the transition from i to j.  That is, when 
                // the majority thinks the rank at j is smaller than the
                // rank at i, we make the transit.
                //
                if (((item1Better > 0) || (item2Better > 0)) && 
                    (item1Better != item2Better)) {
                    transitions[i][j] = (item1Better < item2Better) ? (byte) 1 : (byte) 0;
                    transitions[j][i] = (byte) (1 - transitions[i][j]);
                }
            }
        }

        //
        // To maintain the final sorted list...
        //
        float lastStateDist[] = new float[states.length];
        final float curStateDist[] = new float[states.length];
        int numTransitions[] = new int[states.length];
        int totalStates = states.length, numSortedStates = 0;
        boolean removedState[] = new boolean[states.length];
        for (int i = 0; i < removedState.length; i++) {
            removedState[i] = false;
        }

        //
        // Loop until we rank all items
        //
        while (numSortedStates < totalStates) {
            //
            // 4.  Find the total number of nonzero transitions 
            // from each state
            //
            for (int i = 0; i < states.length; i++) {
                numTransitions[i] = 0;

                if (! removedState[i]) {
                    for (int j = 0; j < states.length; j++) {
                        if (! removedState[j]) {
                            numTransitions[i] += transitions[i][j];
                        }
                    }
                }
            }

            //
            // 5. If there are zero transitions from a given state
            // (that is, it's a sink), then give it a self-loop
            // transition entry.  This means the sink node will
            // eventually rise to stationary likelihood of 100%.
            //
            for (int i = 0; i < numTransitions.length; i++) {
                if (! removedState[i] && numTransitions[i] == 0) {
                    transitions[i][i] = 1;
                    numTransitions[i] = 1;
                }
            }

            //
            // Build likelihoods for each state
            //
            for (int i = 0; i < states.length; i++) {
                lastStateDist[i] = (1.0f / (totalStates - numSortedStates));
                curStateDist[i] = (1.0f / (totalStates - numSortedStates));
            }

            //
            // 6.  Find the stationary distribution iteratively.
            //
            // REMIND - mjc - in the future we'd like to stop
            // iterating based on convergence criteria rather than
            // a hard-coded number of loops
            //
            for (int k = 0; k < (2 * states.length); k++) {

                // For every target state....
                for (int i = 0; i < states.length; i++) {
                    // Init target state's likelihood to zero.
                    curStateDist[i] = 0;

                    // Iterate through every source state...
                    if (! removedState[i]) {
                        for (int j = 0; j < states.length; j++) {
                            //
                            // If we transit from the current source to
                            // the current target, then adjust the target
                            // to have its share of the source's likelihood.
                            //
                            if (! removedState[j] && transitions[j][i] == 1) {
                                curStateDist[i] += (lastStateDist[j] / numTransitions[j]);
                            }
                        }
                    }
                }

                // Now copy the "cur" value to "last" values.
                System.arraycopy(curStateDist, 0, lastStateDist, 0, curStateDist.length);
            }

            //
            // 7. Now detect and remove sinks.  Place in sorted
            // list.  Mark removed states in the "removedState[]"
            // array
            //
            TreeSet stateSorter = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    Integer pos1 = (Integer) o1;
                    Integer pos2 = (Integer) o2;

                    double score1 = curStateDist[pos1.intValue()];
                    double score2 = curStateDist[pos2.intValue()];
                    if (score1 > score2) {
                        return -1;
                    } else if (score1 == score2) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            }
                );

            for (int i = 0; i < states.length; i++) {
                if (! removedState[i]) {
                    stateSorter.add(new Integer(i));
                }
            }

            //
            // 8. Put the top-ranked items into the sorted result list
            // until we find an "end-of-sinks" break.  At that point
            // we restart the Markov-solving.
            //
            int numStatesMoved = 0;

            for (Iterator it = stateSorter.iterator(); it.hasNext(); ) {
                int index = ((Integer) it.next()).intValue();
                float rating = curStateDist[index];
                
                // 
                // When we notice a very large drop in ratings, we 
                // assume it's because we've found a sink region.
                // Remove the items in the sink and restart computation.
                //
                if ((numStatesMoved > 0) &&
                    (rating == 0 || rating < ((1.0 / (totalStates - numSortedStates)) / 10000.0))) {
                    break;
                } else {
                    fullRanking.put(states[index], new Integer(numSortedStates));
                    numSortedStates++;
                    removedState[index] = true;
                    numStatesMoved++;
                }
            }
        }
        solved = true;
    }

    /**
     * Find the position in the full list for the given
     * object.
     */
    public int getPos(Object obj) {
        if (! solved) {
            throw new IllegalArgumentException("Must call solveRanking() first.");
        }
        Integer pos = (Integer) fullRanking.get(obj);
        if (pos == null) {
            throw new IllegalArgumentException("Unknown item obj.");            
        }
        return pos.intValue();
    }

    /**
     * Return total number of states in final ranking
     */
    public int getNumStates() {
        return fullRanking.size();
    }

    /**
     * Compute the Kendall Tau distance between a given list
     * of ListItem objects and the current full ranking.
     * Must be called after solveRanking().
     */
    public double getKendallTauDistance(Object testList[], boolean normalized) {
        if (! solved) {
            throw new IllegalArgumentException("Must call solveRanking() first.");
        }

        int misOrderings = 0, maxOrderings = 0;
        //
        // Go through all pairs of elts in the testList.  See
        // if they are consistent with the fullList.
        //
        for (int i = 0; i < testList.length; i++) {
            for (int j = i + 1; j < testList.length; j++) {
                int pos1 = getPos(testList[i]);
                int pos2 = getPos(testList[j]);
                if (pos1 > pos2) {
                    misOrderings++;
                }
                maxOrderings++;
            }
        }

        if (normalized) {
            if (maxOrderings == 0) {
                return 0;
            }
            return misOrderings / (1.0 * maxOrderings);
        } else {
            return misOrderings;
        }
    }

    /**
     * Test the rank-solver
     */
    public static void main(String argv[]) throws NumberFormatException {
        if (argv.length < 1) {
            System.out.println("Usage: java net.nutch.quality.MarkovRankSolver <maxStates> [-seed <seed>]");
            return;
        }

        int maxStates = Integer.parseInt(argv[0]);
        long seed = new Random().nextInt();
        boolean usedSeed = false;

        if (argv.length > 1) {
            for (int i = 1; i < argv.length; i++) {
                if ("-seed".equals(argv[i])) {
                    seed = Long.parseLong(argv[i + 1]);
                    usedSeed = true;
                    i++;                    
                }
            }
        }

        if (usedSeed) {
            System.out.println("Using seed: " + seed);
        } else {
            System.out.println("Seed: " + seed);
        }

        //
        // Finally, create the markov-model solver
        //
        MarkovRankSolver solver = new MarkovRankSolver();
        Random rand = new Random(seed);

        // Build state set
        Integer states[] = new Integer[maxStates];
        for (int i = 0; i < states.length; i++) {
            states[i] = new Integer(i);
        }

        // Build 10 slightly-different orderings
        for (int i = 0; i < 10; i++) {
            Integer ordering[] = new Integer[maxStates];
            for (int j = 0; j < ordering.length; j++) {
                ordering[j] = states[(j + (Math.abs(rand.nextInt()) % 2)) % ordering.length];
                System.out.print(ordering[j] + " ");
            }
            System.out.println();
            solver.addOrdering(ordering);
        }

        // And a very boring one that contains each item
        Integer ordering[] = new Integer[states.length];
        for (int i = 0; i < ordering.length; i++) {
            ordering[i] = states[i];
        }
        solver.addOrdering(ordering);

        System.out.println("About to solve problem...");
        solver.solveRanking();

        System.out.println("-----------------------------------");
        for (int i = 0; i < states.length; i++) {
            System.out.println(states[i] + "\t\t" + solver.getPos(states[i]));
        }
    }
}
