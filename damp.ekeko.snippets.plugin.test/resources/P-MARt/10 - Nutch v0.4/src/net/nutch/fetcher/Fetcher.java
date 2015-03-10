/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.net.protocols.Response;
import net.nutch.pagedb.FetchListEntry;
import net.nutch.net.protocols.http.*;
import net.nutch.net.protocols.ftp.*;
import net.nutch.io.*;
import net.nutch.db.*;
import net.nutch.util.*;
import net.nutch.util.RobotsMetaProcessor.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import org.cyberneko.html.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.apache.html.dom.*;

/***************************************
 * A simple Fetcher, now adorned with new features,
 * such as hostname bans and politeness constraints.
 *
 * @author Doug Cutting, added to by Mike Cafarella
 ***************************************/
public class Fetcher { 
    //
    // This seems like real overkill.  Time till we remove robots.txt from cache.
    // Make it 1 day.
    //
    static final long DEFAULT_ROBOTS_LIFETIME = 1 * 24 * 60 * 60 * 1000;

    //
    // Min robots lifetime
    //
    static final long MINIMUM_ROBOTS_LIFETIME = 5 * 60 * 1000;

    // delay between hitting same host
    private long serverDelay =
      NutchConf.getInt("fetcher.server.delay", 1) * 1000;

    static final String AGENT_NAME = NutchConf.get("http.agent.name");

    public static final Logger LOG =
      LogFormatter.getLogger("net.nutch.fetcher.Fetcher");

    private ArrayFile.Reader fetchList;              // the input
    private ArrayFile.Writer fetcherDb;              // the output
    private ArrayFile.Writer rawDb;
    private ArrayFile.Writer strippedDb;

    private TrieStringMatcher hostnameBans[];

    private int threadCount =                     // max number of threads
      NutchConf.getInt("fetcher.threads.fetch", 10);

    private long start;                             // start time of fetcher run
    private long bytes;                             // total bytes fetched
    private int pages;                              // total pages fetched
    private int errors;                             // total pages errored

    private ThreadGroup group = new ThreadGroup("fetcher"); // our thread group
    private int timeout = -1;
    private Http http = new Http();
    private RobotRulesParser robotRulesParser;
    private Hashtable robotRulesCache = new Hashtable();
    private TreeSet deadHosts = new TreeSet();

    /*********************************************
     * BlockedHost class keeps track of a pair
     * consisting of (hostname, timestamp).  Used for
     * sorting when a target hostname is ready to
     * hit.
     ********************************************/
    class BlockedHost {
        String hostname;
        long readyTime;

        public BlockedHost(String hostname) {
            this.hostname = hostname;
            this.readyTime = System.currentTimeMillis() + serverDelay;
        }

        public String getHostname() {
            return hostname;
        }

        public long getReadyTime() {
            return readyTime;
        }
    }

    Comparator blockedHostComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            BlockedHost bh1 = (BlockedHost) o1;
            BlockedHost bh2 = (BlockedHost) o2;

            int diff = (int) (bh1.getReadyTime() - bh2.getReadyTime());
            if (diff == 0) {
                return bh1.getHostname().compareTo(bh2.getHostname());
            } else {
                return diff;
            }
        }
    };
    TreeSet blockedHostsByOrder = new TreeSet(blockedHostComparator);
    TreeSet blockedHostsByName = new TreeSet();
    TreeMap blockedPendingQueues = new TreeMap();
    TreeMap readyPendingQueues = new TreeMap();

    /********************************************
     * Fetcher thread
     ********************************************/
    private class FetcherThread extends Thread {
        private DOMFragmentParser parser = new DOMFragmentParser();
        private RobotsMetaIndicator robotsMeta = new RobotsMetaIndicator();

        private Ftp ftp = null; // one instance per thread

        private int timeout = -1;

        /**
         */
        public FetcherThread() {
            super(group, "starting");
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        /**
         * This thread keeps looping, grabbing an item off the list
         * of URLs to be fetched (in a thread-safe way).  It checks 
         * whether the URL is OK to download.  If so, we do it.
         */
        public void run() {

            this.ftp = new Ftp();
            if (this.timeout != -1)
                this.ftp.setTimeout(this.timeout);

            boolean hasDiskItems = true;
            while (true) {
                if (LogFormatter.hasLoggedSevere())
                  break;

                FetchListEntry fle = null;
                String urlString = null;
                try {
                    setName("starting");

                    // 
                    // Unblock any hosts that might be done and past the 
                    // delay time
                    //
                    synchronized (blockedHostsByOrder) {
                        // Check to see if any hosts should be unblocked
                        while ((blockedHostsByOrder.size() > 0) &&
                               (((BlockedHost) blockedHostsByOrder.first()).getReadyTime() < System.currentTimeMillis())) {
                            BlockedHost blockedHost = (BlockedHost) blockedHostsByOrder.first();
                            blockedHostsByOrder.remove(blockedHost);
                            blockedHostsByName.remove(blockedHost.getHostname());

                            // There's now a host that's newly-unblocked.  Move its
                            // pending queue from blocked to ready.
                            synchronized (blockedPendingQueues) {
                                LinkedList readyQueue = (LinkedList) blockedPendingQueues.get(blockedHost.getHostname());
                                if (readyQueue != null) {
                                    blockedPendingQueues.remove(blockedHost.getHostname());
                                    readyPendingQueues.put(blockedHost.getHostname(), readyQueue);
                                }
                            }
                        }
                    }

                    //
                    // Grab next item.
                    //
                    // First, check if there is any work in the readyPendingQueue.
                    //
                    synchronized (blockedPendingQueues) {
                        while (fle == null && readyPendingQueues.size() > 0) {
                            String readyHost = (String) readyPendingQueues.firstKey();
                            LinkedList readyQueue = (LinkedList) readyPendingQueues.get(readyHost);
                            if (readyQueue.size() > 0) {
                                fle = (FetchListEntry) readyQueue.removeFirst();
                            }
                            if (readyQueue.size() == 0) {
                                readyPendingQueues.remove(readyHost);
                            }
                        }
                    }

                    //
                    // Second, if there was no pending work ready to be processed, 
                    // we get a URL off the fetchlist
                    //
                    if (fle == null && hasDiskItems) {
                        fle = (FetchListEntry)fetchList.next(new FetchListEntry());
                        if (fle == null) {
                            hasDiskItems = false;
                        }
                    }

                    //
                    // If we still haven't found an FLE, but there is still 
                    // stuff waiting in the delay queue, then all we can do is
                    // wait and repeat the loop.
                    //
                    // Otherwise exit.
                    //
                    if (fle == null) {
                        boolean waitAndContinue = false;
                        long targetTime = 0;

                        synchronized (blockedHostsByOrder) {
                            if (blockedHostsByOrder.size() > 0) {
                                waitAndContinue = true;
                                targetTime = ((BlockedHost) blockedHostsByOrder.first()).getReadyTime();
                            }
                        }

                        if (waitAndContinue) {
                            long waitTime = targetTime - System.currentTimeMillis();
                            if (waitTime > 0) {
                                try {
                                    Thread.sleep(waitTime);
                                } catch (InterruptedException ie) {
                                }
                            }
                            continue;
                        } else {
                            break;
                        }
                    }

                    //
                    // OK!  We now have the URL and will subject it to 
                    // a few tests
                    //
                    urlString = fle.getPage().getURL().toString();
                    URL url = new URL(urlString);

                    //
                    // 1. Check hostname
                    //
                    String hostname = url.getHost().toLowerCase();
                    if (hostnameBans != null) {
                        for (int i = 0; i < hostnameBans.length; i++) {
                            if (hostnameBans[i].matches(hostname)) {
                                LOG.fine("Hostname banned for " + urlString);
                                handleNoFetch(fle, FetcherOutput.NOT_FOUND);
                                continue;
                            }
                        }
                    }

                    //
                    // 2. Check FLE whether we should fetch at all
                    //
                    if (!fle.getFetch()) {
                        LOG.fine("not fetching " + urlString);
                        handleNoFetch(fle, FetcherOutput.SUCCESS);
                        continue;
                    }

                    //
                    // 3.  Check whether the host is dead
                    //
                    if (deadHosts.contains(hostname)) {
                        LOG.fine("host dead for " + urlString);
                        handleNoFetch(fle, FetcherOutput.RETRY);
                        continue;
                    }

                    //
                    // 4.  Make sure we there is no pending host-delay on 
                    // the host.  Otherwise this URL will need to be deferred
                    // till the host-delay expires.  This might not be an issure
                    // for large crawls, but it's very important for small ones.
                    // 
                    // (Small crawls may try to obtain several hundred URLS
                    // from the same host and little else.  These hosts will
                    // quickly shut down the fetcher unless it inserts delays
                    // between fetch attempts.)
                    //
                    synchronized (blockedHostsByOrder) {
                        synchronized (blockedPendingQueues) {
                            // If blocked, store the FLE and continue
                            if (blockedHostsByName.contains(hostname)) {
                                LinkedList blockedQueue = (LinkedList) blockedPendingQueues.get(hostname);
                                blockedQueue.add(fle);
                                continue;
                            } else {
                                // If free, move into blocked state but go on and
                                // process the FLE.
                                BlockedHost bh = new BlockedHost(hostname);
                                blockedHostsByName.add(bh.getHostname());
                                blockedHostsByOrder.add(bh);

                                LinkedList readyQueue = (LinkedList) readyPendingQueues.remove(hostname);
                                if (readyQueue == null) {
                                    readyQueue = new LinkedList();
                                }
                                blockedPendingQueues.put(hostname, readyQueue);
                            }
                        }
                    }

                    //
                    // 5.  Check robots, fetching if necessary
                    //
                    RobotRulesParser.RobotRuleSet robotRules = (RobotRulesParser.RobotRuleSet) robotRulesCache.get(hostname);
                    if (robotRules == null || (System.currentTimeMillis() > robotRules.getExpireTime())) {
                        // Remove from cache if expired
                        if (robotRules != null) {
                            robotRulesCache.remove(hostname);
                        }

                        try {
                            // Obtain robots.txt from the INTERNET!
                            URL robotURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/robots.txt");
                            Response robotResponse = null;
                            if ("http".equals(url.getProtocol())) {
                                if (this.timeout != -1)
                                    http.setTimeout(this.timeout);
                                robotResponse = http.getResponse(robotURL);
                            } else if ("ftp".equals(url.getProtocol())) {
                                robotResponse = this.ftp.getResponse(robotURL);
                            }

                            // If the robots.txt HTTP xfer worked, 
                            if (robotResponse.getCode() == 200) {
                                // Parse the file
                                robotRules = robotRulesParser.parseRules(robotResponse.getContent());
                                // Set expiration policy
                                long expireTime = System.currentTimeMillis() + DEFAULT_ROBOTS_LIFETIME;
                                String expireStr = robotResponse.getHeader("Expires");
                                if (expireStr != null) {
                                    try {
                                        Date date = DateFormat.getDateInstance(DateFormat.LONG).parse(expireStr);
                                        expireTime = date.getTime();
                                        long min = System.currentTimeMillis() + MINIMUM_ROBOTS_LIFETIME;
                                        if (expireTime < min) {
                                            expireTime = min;
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                                robotRules.setExpireTime(expireTime);
                            } else if (robotResponse.getCode() >= 400) {
                                // Robots.txt not available, but server's there.  
                                // Just use default robots.
                            } else {
                                // Robots.txt can't be loaded because server's not
                                // there.  Mark this host as kaput.
                                deadHosts.add(hostname);
                                handleNoFetch(fle, FetcherOutput.RETRY);
                                continue;
                            }
                        } catch (Exception e) {
                        }

                        // Cache the resulting robotRules object.  Create it,
                        // if it hasn't been created yet.
                        if (robotRules == null) {
                            // robots.txt cannot be loaded; anything goes, boys!
                            robotRules = robotRulesParser.getEmptyRules();
                            robotRules.setExpireTime(System.currentTimeMillis() + DEFAULT_ROBOTS_LIFETIME);
                        }
                        robotRulesCache.put(hostname, robotRules);
                    }

                    //
                    // OK, we are guaranteed to have a valid robots at this pt.
                    //
                    String path = url.getFile();
                    if ((path == null) || "".equals(path)) {
                        path= "/";
                    }
                    if (! robotRules.isAllowed(path)) {
                        handleNoFetch(fle, FetcherOutput.NOT_FOUND);
                    }

                    //
                    // FINALLY!
                    // Passed tests so let's grab it.
                    //
                    LOG.info("fetching " + url);
                    setName(urlString);

                    Response response = null;

                    if ("http".equals(url.getProtocol())) {
                        if (this.timeout != -1)
                            http.setTimeout(this.timeout);
                        response = http.getResponse(url);
                    } else if ("ftp".equals(url.getProtocol())) {
                        response = this.ftp.getResponse(url);
                    }

                    handleFetch(url, fle, response);

                    //
                    // Record the results.  A failure will throw an exception.
                    //
                    synchronized (Fetcher.this) {
                        pages++;
                        bytes += response.getContent().length;

                        // Show status every 100pp
                        if ((pages % 100) == 0) {
                            status();
                        }
                    }
                } catch (HttpError e) {
                  logError(urlString, fle, e);
                  // mostly 401's and 403's: page not found
                  handleNoFetch(fle, FetcherOutput.NOT_FOUND);

                } catch (SocketException e) {
                  logError(urlString, fle, e);
                  // timeout, dns or connect error: retry
                  handleNoFetch(fle, FetcherOutput.RETRY);

                } catch (Throwable t) {
                  if (fle != null) {
                    logError(urlString, fle, t);
                    handleNoFetch(fle, FetcherOutput.NOT_FOUND);
                  }
                }
            }

            //LOG.info("deleting ftp");
            this.ftp = null;
            System.gc();
            return;
        }

        private void logError(String urlString,
                              FetchListEntry fle, Throwable t) {
          LOG.info("fetch of " + urlString + " failed with: " + t);
          synchronized (Fetcher.this) {          // record failure
            errors++;
          }
        }

        /**
         */
        private void handleFetch(URL url, FetchListEntry fle,
                                 Response response)
            throws IOException, SAXException {
            String contentType = response.getHeader("Content-Type");

            String text;
            String title;
            Outlink[] outlinks;
            byte[] content;

            if (contentType == null || contentType.startsWith("text/html")) {
      
              DocumentFragment node =               // parse content
                new HTMLDocumentImpl().createDocumentFragment();
              parser.parse(new InputSource
                           (new ByteArrayInputStream(response.getContent())),
                           node);

              RobotsMetaProcessor.
                getRobotsMetaDirectives(robotsMeta, node, url);

              if (robotsMeta.getNoIndex()) {
                text = "";                        // ignore text and title
                title = "";
              } else {                            // extract text and title
                StringBuffer sb = new StringBuffer();
                DOMContentUtils.getText(sb, node);
                text = sb.toString();
                sb.setLength(0);
                DOMContentUtils.getTitle(sb, node);
                title = sb.toString().trim();
              }
      
              if (robotsMeta.getNoFollow()) {     // ignore outlinks
                outlinks = new Outlink[] {};
              } else {                            // extract outlinks
                URL baseURL = response.getUrl();
                ArrayList l = new ArrayList();
                DOMContentUtils.getOutlinks(baseURL, l, node);
                outlinks = (Outlink[])l.toArray(new Outlink[l.size()]);
                LOG.fine("found " + outlinks.length + " outlinks in " + url);
              }

              if (robotsMeta.getNoCache()) {
                content= new byte[0];             // ignore content
              } else {
                content = response.getContent();  // cache content
              }
            } else if (contentType.equals("text/plain")) {
              text = new String(response.getContent());
              title = "";
              outlinks = new Outlink[] {};
              content = response.getContent();
            } else {
              throw new IOException("Unknown content-type: " + contentType);
            }

            outputPage(new FetcherOutput(fle, MD5Hash.digest(response.getContent()),
                                         FetcherOutput.SUCCESS,
                                         title, outlinks),
                       new FetcherContent(content),
                       new FetcherText(text));
        }

        /**
         *
         */
        private void handleNoFetch(FetchListEntry fle, int status) {
            outputPage(new FetcherOutput(fle, MD5Hash.digest(fle.getPage().getURL().toString()),
                                         status, "", new Outlink[0]),
                       new FetcherContent(new byte[0]),
                       new FetcherText(""));
        }
    }
      
    /**
     */
    private void outputPage(FetcherOutput fo, FetcherContent raw,
                            FetcherText stripped) {
        try {
            synchronized (fetcherDb) {
                fetcherDb.append(fo);
                rawDb.append(raw);
                strippedDb.append(stripped);
            }
        } catch (Throwable t) {
            LOG.severe("error writing output:" + t.toString());
        }
    }
                                       
			
    /** 
     * Constructs a fetcher.
     */
    public Fetcher(String directory) throws IOException {
        //
        // Set up in/out streams
        //
        fetchList = new ArrayFile.Reader
            (new File(directory, FetchListEntry.DIR_NAME).toString());
        fetcherDb = new ArrayFile.Writer
            (new File(directory, FetcherOutput.DIR_NAME).toString(), FetcherOutput.class);
        rawDb = new ArrayFile.Writer
            (new File(directory, FetcherContent.DIR_NAME).toString(), FetcherContent.class);
        strippedDb = new ArrayFile.Writer
            (new File(directory, FetcherText.DIR_NAME).toString(), FetcherText.class);

        //
        // Build robot rules parser.  First, grab the agent names
        // we advertise to robots files.
        //
        String agentNames = NutchConf.get("http.robots.agents");
        StringTokenizer tok = new StringTokenizer(agentNames, ",");
        ArrayList agents = new ArrayList();
        while (tok.hasMoreTokens()) {
            agents.add(tok.nextToken().trim());
        }

        //
        // If there are no agents for robots-parsing, use our 
        // default agent-string.  If both are present, our agent-string
        // should be the first one we advertise to robots-parsing.
        // 
        if (agents.size() == 0) {
            agents.add(AGENT_NAME);
            LOG.severe("No agents listed in 'http.robots.agents' property!");
        } else if (!((String)agents.get(0)).equalsIgnoreCase(AGENT_NAME)) {
            agents.add(0, AGENT_NAME);
            LOG.severe("Agent we advertise (" + AGENT_NAME 
                       + ") not listed first in 'http.robots.agents' property!");
        }

        // Turn into string array and construct rule parser
        this.robotRulesParser = new RobotRulesParser((String[]) agents.toArray(new String[agents.size()]));

        // Load hostname bans
        ArrayList bans = new ArrayList();
        try {
            LineNumberReader reader= new LineNumberReader(NutchConf.getConfResourceAsReader(NutchConf.get("excludehosts.suffix.file")));
                        
            ArrayList suffixStrings= new ArrayList();
            String line;
            while ((line= reader.readLine()) != null) {
                // trim out comments and whitespace
                int hashPos= line.indexOf("#");
                if (hashPos >= 0) {
                    line = line.substring(0, hashPos);
                }
                line = line.trim();
                if (line.length() > 0) {
                    line = line.toLowerCase();
                    suffixStrings.add(line);
                }
            }

            bans.add(new SuffixStringMatcher(suffixStrings));
        } catch (Exception e) {
            LOG.warning("Not using hostNameSuffixBans: " + e.toString());
        }
        if (bans.size() > 0) {
            this.hostnameBans = (TrieStringMatcher[]) bans.toArray(new TrieStringMatcher[bans.size()]);
        } else {
            this.hostnameBans = null;
        }

        if (this.timeout != -1)
          this.http.setTimeout(this.timeout);
    }

    /** 
     * Set thread count
     */
    public void setThreadCount(int threadCount) {
        this.threadCount=threadCount;
    }

    // set timeout
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /** 
     * Set delay between accesses to the same host.
     */
    public void setServerDelay(long serverDelay) {
        this.serverDelay=serverDelay;
    }
    /** 
     * Return the Http implementation.
     */
    public Http getHttp() { 
        return http;
    }

    /** 
     * Set the logging level.
     */
    public void setLogLevel(Level level) {
        LOG.setLevel(level);
        Http.LOG.setLevel(level);
        Ftp.LOG.setLevel(level);
        LOG.info("logging at " + level);
    }

    /** 
     * Runs the fetcher.
     */
    public void run() throws IOException, InterruptedException {
        start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {       // spawn threads
            FetcherThread thread = new FetcherThread(); 
            if (this.timeout != -1)
                thread.setTimeout(this.timeout);
            thread.start();
        }
        do {
            Thread.sleep(1000);

            if (LogFormatter.hasLoggedSevere()) 
              throw new RuntimeException("SEVERE error logged.  Exiting fetcher.");

        } while (group.activeCount() > 0);            // wait for threads to finish

        fetchList.close();                            // close databases
        fetcherDb.close();
        rawDb.close();
        strippedDb.close();

        status();                                     // print final status
    }

    /** 
     * Display the status of the fetcher run. 
     */
    public synchronized void status() {
        long ms = System.currentTimeMillis() - start;
        LOG.info("status: "
                 + pages + " pages, "
                 + errors + " errors, "
                 + bytes + " bytes, "
                 + ms + " ms");
        LOG.info("status: "
                 + (((float)pages)/(ms/1000.0f))+" pages/s, "
                 + (((float)bytes*8/1024)/(ms/1000.0f))+" kb/s, "
                 + (((float)bytes)/pages) + " bytes/page");
    }

    /** 
     * Run the fetcher. 
     */
    public static void main(String[] args) throws Exception {
        int timeout = -1;
        int threadCount = -1;
        long delay = -1;
        boolean verbose = false;
        boolean showThreadID = false;
        String directory = null;

        String usage = "Usage: Fetcher [-verbose] [-showThreadID] [-timeout N] [-threads M] [-delay O] dir";

        if (args.length == 0) {
            System.err.println(usage);
            System.exit(-1);
        }
      
        for (int i = 0; i < args.length; i++) {       // parse command line
            if (args[i].equals("-timeout")) {		  // found -timeout option
                timeout = Integer.parseInt(args[++i]) * 1000;
            } else if (args[i].equals("-threads")) {	  // found -threads option
                threadCount =  Integer.parseInt(args[++i]);
            } else if (args[i].equals("-delay")) {	  // found -delay option
                delay =  Integer.parseInt(args[++i]);
            } else if (args[i].equals("-verbose")) {	  // found -verbose option
                verbose = true;
            } else if (args[i].equals("-showThreadID")) { // found -showThreadID option
                showThreadID = true;
            } else if (i != args.length-1) {
                System.err.println(usage);
                System.exit(-1);
            } else                                      // root is required parameter
                directory = args[i];
        }

        Fetcher fetcher = new Fetcher(directory);     // make a Fetcher
        if (timeout != -1)                            // set timeout option
            fetcher.setTimeout(timeout);
            //fetcher.getHttp().setTimeout(timeout);
        if (threadCount != -1)                        // set threadCount option
            fetcher.setThreadCount(threadCount);
        if (delay != -1)                          // set delay option
            fetcher.setServerDelay(delay * 1000); // convert seconds to milliseconds

        if (showThreadID)
             LogFormatter.setShowThreadIDs(showThreadID);

        // set log level
        fetcher.setLogLevel(verbose ? Level.FINE : Level.INFO);

        fetcher.run();                                // run the Fetcher
    }
}
