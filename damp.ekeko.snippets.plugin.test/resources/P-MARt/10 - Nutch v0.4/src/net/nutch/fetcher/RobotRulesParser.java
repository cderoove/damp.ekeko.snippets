/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import net.nutch.util.LogFormatter;


/**
 * This class handles the parsing of <code>robots.txt</code> files.
 * It emits RobotRules objects, which describe the download permissions
 * as described in RobotRulesParser.
 *
 * @author Tom Pierce, modified by Mike Cafarella
 */
public class RobotRulesParser {
    public static final Logger LOG=
    LogFormatter.getLogger("net.nutch.fetcher.RobotRulesParser");

    private HashMap robotNames;

    private static final String CHARACTER_ENCODING= "UTF-8";
    private static final int NO_PRECEDENCE= Integer.MAX_VALUE;
    private static final RobotRuleSet EMPTY_RULES= new RobotRuleSet();

    /**
     * This class holds the rules which were parsed from a robots.txt
     * file, and can test paths against those rules.
     */
    public static class RobotRuleSet {
        ArrayList tmpEntries;
        RobotsEntry[] entries;
        long expireTime;

        /**
         */
        private class RobotsEntry {
            String prefix;
            boolean allowed;

            RobotsEntry(String prefix, boolean allowed) {
                this.prefix= prefix;
                this.allowed= allowed;
            }
        }

        /**
         * should not be instantiated from outside RobotRulesParser
         */
        private RobotRuleSet() {
            tmpEntries= new ArrayList();
            entries= null;
        }

        /**
         */
        private void addPrefix(String prefix, boolean allow) {
            if (tmpEntries == null) {
                tmpEntries= new ArrayList();
                if (entries != null) {
                    for (int i= 0; i < entries.length; i++) 
                        tmpEntries.add(entries[i]);
                }
                entries= null;
            }

            tmpEntries.add(new RobotsEntry(prefix, allow));
        }

        /**
         */
        private void clearPrefixes() {
            if (tmpEntries == null) {
                tmpEntries= new ArrayList();
                entries= null;
            } else {
                tmpEntries.clear();
            }
        }

        /**
         * Change when the ruleset goes stale.
         */
        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        /**
         * Get expire time
         */
        public long getExpireTime() {
            return expireTime;
        }

        /** 
         *  Returns <code>false</code> if the <code>robots.txt</code> file
         *  prohibits us from accessing the given <code>path</code>, or
         *  <code>true</code> otherwise.
         */ 
        public boolean isAllowed(String path) {
            try {
                path= URLDecoder.decode(path, CHARACTER_ENCODING);
            } catch (Exception e) {
                // just ignore it- we can still try to match 
                // path prefixes
            }

            if (entries == null) {
                entries= new RobotsEntry[tmpEntries.size()];
                entries= (RobotsEntry[]) 
                    tmpEntries.toArray(entries);
                tmpEntries= null;
            }

            int pos= 0;
            int end= entries.length;
            while (pos < end) {
                if (path.startsWith(entries[pos].prefix))
                    return entries[pos].allowed;
                pos++;
            }

            return true;
        }

        /**
         */
        public String toString() {
            isAllowed("x");  // force String[] representation
            StringBuffer buf= new StringBuffer();
            for (int i= 0; i < entries.length; i++) 
                if (entries[i].allowed)
                    buf.append("Allow: " + entries[i].prefix
                               + System.getProperty("line.separator"));
                else 
                    buf.append("Disallow: " + entries[i].prefix
                               + System.getProperty("line.separator"));
            return buf.toString();
        }
    }

    /**
     *  Creates a new <code>RobotRulesParser</code> which will use the
     *  supplied <code>robotNames</code> when choosing which stanza to
     *  follow in <code>robots.txt</code> files.  Any name in the array
     *  may be matched.  The order of the <code>robotNames</code>
     *  determines the precedence- if many names are matched, only the
     *  rules associated with the robot name having the smallest index
     *  will be used.
     */
    public RobotRulesParser(String[] robotNames) {
        this.robotNames= new HashMap();
        for (int i= 0; i < robotNames.length; i++) {
            this.robotNames.put(robotNames[i].toLowerCase(), new Integer(i));
        }
        // always make sure "*" is included
        if (!this.robotNames.containsKey("*"))
            this.robotNames.put("*", new Integer(robotNames.length));
    }

    /**
     * Returns a {@link RobotRuleSet} object which encapsulates the
     * rules parsed from the supplied <code>robotContent</code>.
     */
    RobotRuleSet parseRules(byte[] robotContent) {
        if (robotContent == null) 
            return EMPTY_RULES;

        String content= new String (robotContent);

        StringTokenizer lineParser= new StringTokenizer(content, "\n\r");

        RobotRuleSet bestRulesSoFar= null;
        int bestPrecedenceSoFar= NO_PRECEDENCE;

        RobotRuleSet currentRules= new RobotRuleSet();
        int currentPrecedence= NO_PRECEDENCE;

        boolean addRules= false;    // in stanza for our robot
        boolean doneAgents= false;  // detect multiple agent lines

        while (lineParser.hasMoreTokens()) {
            String line= lineParser.nextToken();

            // trim out comments and whitespace
            int hashPos= line.indexOf("#");
            if (hashPos >= 0) 
                line= line.substring(0, hashPos);
            line= line.trim();

            if ( (line.length() >= 11) 
                 && (line.substring(0, 11).equalsIgnoreCase("User-agent:")) ) {

                if (doneAgents) {
                    if (currentPrecedence < bestPrecedenceSoFar) {
                        bestPrecedenceSoFar= currentPrecedence;
                        bestRulesSoFar= currentRules;
                        currentPrecedence= NO_PRECEDENCE;
                        currentRules= new RobotRuleSet();
                    }
                    addRules= false;
                }
                doneAgents= false;

                String agentNames= line.substring(line.indexOf(":") + 1);
                agentNames= agentNames.trim();
                StringTokenizer agentTokenizer= new StringTokenizer(agentNames);

                while (agentTokenizer.hasMoreTokens()) {
                    // for each agent listed, see if it's us:
                    String agentName= agentTokenizer.nextToken().toLowerCase();

                    Integer precedenceInt= (Integer) robotNames.get(agentName);

                    if (precedenceInt != null) {
                        int precedence= precedenceInt.intValue();
                        if ( (precedence < currentPrecedence)
                             && (precedence < bestPrecedenceSoFar) )
                            currentPrecedence= precedence;
                    }
                }

                if (currentPrecedence < bestPrecedenceSoFar) 
                    addRules= true;

            } else if ( (line.length() >= 9)
                        && (line.substring(0, 9).equalsIgnoreCase("Disallow:")) ) {

                doneAgents= true;
                String path= line.substring(line.indexOf(":") + 1);
                path= path.trim();
                try {
                    path= URLDecoder.decode(path, CHARACTER_ENCODING);
                } catch (Exception e) {
                    LOG.warning("error parsing robots rules- can't decode path: "
                                + path);
                }

                if (path.length() == 0) { // "empty rule"
                    if (addRules)
                        currentRules.clearPrefixes();
                } else {  // rule with path
                    if (addRules)
                        currentRules.addPrefix(path, false);
                }

            } else if ( (line.length() >= 6)
                        && (line.substring(0, 6).equalsIgnoreCase("Allow:")) ) {

                doneAgents= true;
                String path= line.substring(line.indexOf(":") + 1);
                path= path.trim();

                if (path.length() == 0) { 
                    // "empty rule"- treat same as empty disallow
                    if (addRules)
                        currentRules.clearPrefixes();
                } else {  // rule with path
                    if (addRules)
                        currentRules.addPrefix(path, true);
                }
            }
        }

        if (currentPrecedence < bestPrecedenceSoFar) {
            bestPrecedenceSoFar= currentPrecedence;
            bestRulesSoFar= currentRules;
        }

        if (bestPrecedenceSoFar == NO_PRECEDENCE) 
            return EMPTY_RULES;
        return bestRulesSoFar;
    }

    /**
     *  Returns a <code>RobotRuleSet</code> object appropriate for use
     *  when the <code>robots.txt</code> file is empty or missing; all
     *  requests are allowed.
     */
    static RobotRuleSet getEmptyRules() {
        return EMPTY_RULES;
    }

    /**
     *  Returns a <code>RobotRuleSet</code> object appropriate for use
     *  when the <code>robots.txt</code> file is not fetched due to a
     *  <code>403/Forbidden</code> response; all requests are
     *  disallowed.
     */
    static RobotRuleSet getForbidAllRules() {
        RobotRuleSet rules= new RobotRuleSet();
        rules.addPrefix("", false);
        return rules;
    }

    private final static int BUFSIZE= 2048;

    /** command-line main for testing */
    public static void main(String[] argv) {
        if (argv.length != 3) {
            System.out.println("Usage:");
            System.out.println("   java <robots-file> <url-file> <agent-name>+");
            System.out.println("");
            System.out.println("The <robots-file> will be parsed as a robots.txt file,");
            System.out.println("using the given <agent-name> to select rules.  URLs ");
            System.out.println("will be read (one per line) from <url-file>, and tested");
            System.out.println("against the rules.");
            System.exit(-1);
        }
        try { 
            FileInputStream robotsIn= new FileInputStream(argv[0]);
            LineNumberReader testsIn= new LineNumberReader(new FileReader(argv[1]));
            String[] robotNames= new String[argv.length - 1];

            for (int i= 0; i < argv.length - 2; i++) 
                robotNames[i]= argv[i+2];

            ArrayList bufs= new ArrayList();
            byte[] buf= new byte[BUFSIZE];
            int totBytes= 0;

            int rsize= robotsIn.read(buf);
            while (rsize >= 0) {
                totBytes+= rsize;
                if (rsize != BUFSIZE) {
                    byte[] tmp= new byte[rsize];
                    System.arraycopy(buf, 0, tmp, 0, rsize);
                    bufs.add(tmp);
                } else {
                    bufs.add(buf);
                    buf= new byte[BUFSIZE];
                }
                rsize= robotsIn.read(buf);
            }

            byte[] robotsBytes= new byte[totBytes];
            int pos= 0;

            for (int i= 0; i < bufs.size(); i++) {
                byte[] currBuf= (byte[]) bufs.get(i);
                int currBufLen= currBuf.length;
                System.arraycopy(currBuf, 0, robotsBytes, pos, currBufLen);
                pos+= currBufLen;
            }

            RobotRulesParser parser= 
                new RobotRulesParser(robotNames);
            RobotRuleSet rules= parser.parseRules(robotsBytes);
            System.out.println("Rules:");
            System.out.println(rules);
            System.out.println();

            String testPath= testsIn.readLine().trim();
            while (testPath != null) {
                System.out.println( (rules.isAllowed(testPath) ? 
                                     "allowed" : "not allowed")
                                    + ":\t" + testPath);
                testPath= testsIn.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
