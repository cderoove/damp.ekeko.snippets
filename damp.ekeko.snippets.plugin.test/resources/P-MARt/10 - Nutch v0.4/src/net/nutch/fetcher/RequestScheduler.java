/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.net.protocols.Response;

import net.nutch.io.ArrayFile;
import net.nutch.fetcher.HostQueue.HostQueueKey;
import net.nutch.pagedb.FetchListEntry;
import net.nutch.net.protocols.http.Http;
import net.nutch.net.protocols.http.MiscHttpAccounting;
import net.nutch.net.protocols.ftp.Ftp;
import net.nutch.util.FibonacciHeap;
import net.nutch.util.NutchConf;
import net.nutch.util.TrieStringMatcher;
import net.nutch.util.SoftHashMap;
import net.nutch.util.StringUtil;
import net.nutch.util.SuffixStringMatcher;
import net.nutch.util.LogFormatter;

import java.io.File;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

/**
 * This class is responsible for reading from the
 * <code>fetchList</code> DB, and coordinating the activity of {@link
 * FetcherThread}s and {@link OutputThread}s. 
 * 
 * <p>
 *
 * A <code>RequestScheduler</code> reads records from the
 * <code>fetchList</code>, and parcels them out to {@link HostQueue}s.
 * <code>HostQueues</code> are polled for waiting requests when
 * <code>FetcherThread</code>s are idle, and later notified when 
 * a request is completed.
 *
 * <p>
 *
 * Completed requests are queued for output.
 * <code>OutputThread</code>s poll the <code>RequestScheduler</code>
 * for finished requests to process.
 */
public class RequestScheduler implements FetcherConstants {

  public static final Logger LOG=
    LogFormatter.getLogger("net.nutch.fetcher.RequestScheduler");

  public static final String NEWLINE_STRING=
    System.getProperty("line.separator");

  public static final int WAIT_TIMEOUT= 15 * 1000;

  // Configuration parameters- these all default to extremely
  // conservative and/or rediculous values, and should be overridden
  // in the configuration file.
  public static final int DELAY_SECONDS= 
    NutchConf.getInt("fetcher.server.delay", 60); 

  public static final int NUM_FETCHER_THREADS= 
    NutchConf.getInt("fetcher.threads.fetch", 5); 

  public static final int NUM_OUTPUT_THREADS=
    NutchConf.getInt("fetcher.threads.output", 5); 

  public static final int MAX_QUEUED_REQUESTS=
    NutchConf.getInt("fetcher.request.queue", 2000); 

  public static final int MAX_OUTPUT_QUEUE= 
    NutchConf.getInt("fetcher.output.queue", 20); 

  public static final int MAX_ACTIVE_HOSTS= 
    NutchConf.getInt("fetcher.active.servers", 400); 

  public static final int MAX_CACHED_ROBOTS= 
    NutchConf.getInt("fetcher.robots.cache", 200); 

  public static final int STATS_MINUTES= 
    NutchConf.getInt("fetcher.stats.minutes", 2); 

  public static final int MAX_QUEUED_HOSTS=
    MAX_CACHED_ROBOTS + MAX_ACTIVE_HOSTS;

  public static final int MAX_HOSTQUEUE_LENGTH=
    NutchConf.getInt("fetcher.server.maxurls", 1000); 

  public static final int LOW_ACTIVE_QUEUES= 
    NutchConf.getInt("fetcher.lowservers.threshold", 10); 

  public static final int LOW_ACTIVE_QUEUES_MAX_LENGTH=
    NutchConf.getInt("fetcher.lowservers.maxurls", 100); 

  public static final int MAX_PAGE_ERRORS=
    NutchConf.getInt("fetcher.retry.max", 3);

  public static final int MAX_PAGE_REDIRECTS= 
    NutchConf.getInt("fetcher.redirect.max", 3);

  private static final String AGENT_NAME=
    NutchConf.get("http.agent.name");

  private static final int THROTTLE_PERIOD_SECONDS= 
    NutchConf.getInt("fetcher.throttle.period", -1);

  private static final int THROTTLE_MAX_BANDWIDTH= 
    NutchConf.getInt("fetcher.throttle.bandwidth", -1);

  private static final int THROTTLE_INITIAL_THREADS= 
    NutchConf.getInt("fetcher.throttle.initial.threads", 1);

  // Setting this above one is dangerous- you are likely to draw the
  // ire of many webmasters.  You should only adjust this if you
  // really know what you're doing and have permission from the sites
  // you'll be hitting.
  private static final int MAX_CONCURRENT_REQUESTS_TO_A_SINGLE_SERVER= 1; 

  public static final long SECONDS_TO_MS_MULTIPLIER= 1000;

  // controls behavior
  private long msDelay;
  private int maxPageErrors;
  private int maxPageRedirects;
  private int numFetchThreads;
  private int numOutputThreads;
  private int maxOutputQueue;
  private int maxQueuedRequests;
  private int maxQueuedHosts;
  private int maxCachedRobots;
  private long throttlePeriod;
  private int throttleMaxBandwidth;
  private int throttleInitialThreads;

  private HashMap allHostQueues;         // contains all HostQueues

  // each HostQueue is also in exactly one of the following structures:
  // hosts with max # of requests ongoing 
  private HashSet busyHostQueues;

  // the time when we last ran checkQueues
  private long lastCheckQueues;
  // current time, reset on all calls to returnRequestAndGetNext
  private long now;

  // hosts with "< max #" of requests ongoing / in delay
  private FibonacciHeap readyHostQueues;

  // hosts with "< max #" of requests ongoing, remainder in delay
  private LinkedList delayHostQueues;

  // hosts with no URLs left (w/cached robots.txt, dead-status, etc)
  private LinkedHashSet idleHostQueues; 

  // cache of hosts which have fallen out of idle queue
  private SoftHashMap hostQueueCache; 

  // requests which are ready for OutputThreads, but have not yet
  // reached the outputQueue
  private LinkedList pendingOutputQueue;

  // requests which are ready for OutputThreads.  
  // NOTE: This accesses to this object must be synchronized on it- 
  // if a thread needs to synchronized on "this" and the outputQueue object,
  // "this" should be synchronized first, then "outputQueue".
  private LinkedList outputQueue;

  // number of fetchList requests held by HostQueues
  private int numQueuedRequests;
  // number of fetchList requests held by FetcherThreads
  private int numOutstandingRequests;

  // The input and output DBs
  private ArrayFile.Reader fetchList;
  private ArrayFile.Writer fetcherDb;
  private ArrayFile.Writer rawDb;
  private ArrayFile.Writer strippedDb;

  // have we exhausted the fetchList?
  private boolean fetchListEmpty;

  // have we finished processing all requests from the fetchlist?
  private boolean finishedRequests;

  // Robots rules parser for our HostQueues to use
  private RobotRulesParser robotRulesParser;

  private TrieStringMatcher hostNameBans[];

  private FetcherStatus overallFetcherStatus;

  private String agentString;

  private boolean aborted;

  public RequestScheduler(ArrayFile.Reader fetchList, 
                          ArrayFile.Writer fetcherDb, 
                          ArrayFile.Writer rawDb,
                          ArrayFile.Writer strippedDb) {
    this.fetchList= fetchList;
    this.fetcherDb= fetcherDb;
    this.rawDb= rawDb;
    this.strippedDb= strippedDb;

    this.msDelay= DELAY_SECONDS * SECONDS_TO_MS_MULTIPLIER;
    this.numFetchThreads= NUM_FETCHER_THREADS;
    this.numOutputThreads= NUM_OUTPUT_THREADS;
    this.maxQueuedRequests= MAX_QUEUED_REQUESTS;
    this.maxOutputQueue= MAX_OUTPUT_QUEUE;
    this.maxQueuedHosts= MAX_QUEUED_HOSTS;
    this.maxCachedRobots= MAX_CACHED_ROBOTS;
    this.maxPageErrors= MAX_PAGE_ERRORS;
    this.maxPageRedirects= MAX_PAGE_REDIRECTS;

    this.throttlePeriod= THROTTLE_PERIOD_SECONDS;
    this.throttleMaxBandwidth= THROTTLE_MAX_BANDWIDTH;
    if (throttleMaxBandwidth >= 0) 
      this.throttleInitialThreads= THROTTLE_INITIAL_THREADS;
    else
      this.throttleInitialThreads= numFetchThreads;

    this.overallFetcherStatus= new FetcherStatus();

    this.aborted= false;

    numQueuedRequests= 0;
    numOutstandingRequests= 0;
    fetchListEmpty= false;
    finishedRequests= false;

    lastCheckQueues= 0;

    allHostQueues= new HashMap();

    busyHostQueues= new HashSet();
    readyHostQueues= new FibonacciHeap();
    delayHostQueues= new LinkedList();
    idleHostQueues= new LinkedHashSet();
    hostQueueCache= new SoftHashMap();

    pendingOutputQueue= new LinkedList();
    outputQueue= new LinkedList();

    FetcherStatus.logKeys();

    // build robotRulesParser
    String allAgentNames= NutchConf.get("http.robots.agents");
    StringTokenizer tok= new StringTokenizer(allAgentNames, ",");
    ArrayList agents= new ArrayList();
    while (tok.hasMoreTokens()) {
      agents.add(tok.nextToken().trim());
    }

    if (agents.size() == 0) {
      agents.add(AGENT_NAME);
      LOG.severe("No agents listed in 'http.robots.agents' property!");
    } else if (!((String)agents.get(0)).equalsIgnoreCase(AGENT_NAME)) {
      agents.add(0, AGENT_NAME);
      LOG.severe("Agent we advertise (" + AGENT_NAME 
                 + ") not listed first in 'http.robots.agents' property!");
    }

    String[] agentStrings= (String[])
      agents.toArray(new String[agents.size()]);
               
    robotRulesParser= new RobotRulesParser(agentStrings);

    FetcherStatus.logTraceMisc(MISC_INFORMATIONAL, 
                               "Robots.txt entries we'll obey (in order):");
    for (int i= 0; i < agentStrings.length; i++) 
      FetcherStatus.logTraceMisc(MISC_INFORMATIONAL, agentStrings[i]);

    // build agent string
    
    String agentName = NutchConf.get("http.agent.name");
    String agentVersion = NutchConf.get("http.agent.version");
    String agentDesc = NutchConf.get("http.agent.description");
    String agentURL = NutchConf.get("http.agent.url");
    String agentEmail = NutchConf.get("http.agent.email");

    if ( (agentName == null) || (agentName.trim().length() == 0) )
      LOG.severe("No User-Agent string set (http.agent.name)!");

    StringBuffer buf= new StringBuffer();

    buf.append(agentName);
    if (agentVersion != null) {
      buf.append("/");
      buf.append(agentVersion);
    }
    if ( ((agentDesc != null) && (agentDesc.length() != 0))
         || ((agentEmail != null) && (agentEmail.length() != 0))
         || ((agentURL != null) && (agentURL.length() != 0)) ) {
      buf.append(" (");

      if ((agentDesc != null) && (agentDesc.length() != 0)) {
        buf.append(agentDesc);
        if ( (agentURL != null) || (agentEmail != null) )
          buf.append("; ");
      }

      if ((agentURL != null) && (agentURL.length() != 0)) {
        buf.append(agentURL);
        if (agentEmail != null) 
          buf.append("; ");
      }

      if ((agentEmail != null) && (agentEmail.length() != 0)) 
        buf.append(agentEmail);

      buf.append(")");
    }
    this.agentString= buf.toString();

    FetcherStatus.logTraceMisc(MISC_INFORMATIONAL, 
                               "User-Agent string is: " + buf.toString());

    // load hostNameBans
    ArrayList bans= new ArrayList();

    try {
      LineNumberReader reader= 
        new LineNumberReader( 
          NutchConf.getConfResourceAsReader(
            NutchConf.get("excludehosts.suffix.file")));
                        
      ArrayList suffixStrings= new ArrayList();

      String line;
      while ( (line= reader.readLine()) != null) {
        // trim out comments and whitespace
        int hashPos= line.indexOf("#");
        if (hashPos >= 0) 
          line= line.substring(0, hashPos);
        line= line.trim();
        if (line.length() > 0) {
          line= line.toLowerCase();
          suffixStrings.add(line);
        }
      }

      bans.add(new SuffixStringMatcher(suffixStrings));
    } catch (Exception e) {
      LOG.warning("Not using hostNameSuffixBans: " + e.toString());
    }

    if (bans.size() > 0)
      hostNameBans= (TrieStringMatcher[]) 
        bans.toArray(new TrieStringMatcher[bans.size()]);
    else 
      hostNameBans= null;
  }

  /**
   * Returns a {@link RobotRulesParser} with an appropriate
   * <code>robotName</code> setting.
   *
   * <p>
   *
   * This method is intended for use by {@link HostQueue}s.
   */
  public RobotRulesParser getRobotRulesParser() {
    return robotRulesParser;
  }

  /**
   * Returns a suitable User-Agent string for our robot. 
   */
  public String getAgentString() {
    return agentString;
  }

  /**
   * Returns the number of concurrent requests we allow to a given
   * server.
   *
   * <p>
   *
   * This method is intended for use by {@link HostQueue}s.
   */
  public final int getMaxConcurrentRequests() {
    return MAX_CONCURRENT_REQUESTS_TO_A_SINGLE_SERVER;
  }

  /**
   * Returns the number of milliseconds we delay between requests to
   * the same host.
   *
   * <p>
   *
   * This method is intended for use by {@link HostQueue}s.
   */
  public long getMsDelay() {
    return msDelay;
  }

  private void primeQueue() {
    while ( !fetchListEmpty
            && (allHostQueues.size() < maxQueuedHosts)
            && (numQueuedRequests < maxQueuedRequests) ) {
      addRequest();
    }
  }

  private void addRequest() {
    FetchListEntry fle= null;
    try {
      fle = (FetchListEntry)fetchList.next(new FetchListEntry());
    } catch (java.io.IOException e) {
      LOG.severe("Got exception while iterating through FetchList:");
      LOG.severe(e.toString());
      LOG.severe("Giving up and treating it as empty");
      fetchListEmpty= true;
      return;
    }

    if (fle == null) {
      fetchListEmpty= true;
      return;
    }

    overallFetcherStatus.readFromFetchlist();

    String urlString= null;
    URL url= null;
    try {
      urlString= fle.getPage().getURL().toString();
      url= new URL(urlString);
    } catch (Exception e) {
      LOG.warning("not fetching " + urlString + " due to exception:");
      LOG.warning(e.toString());
      RequestRecord request= new RequestRecord(fle, true);
      request.setFailureReason(FAIL_BAD_URL);
      request.setFailureMessages(new String[] {urlString});
      handleFailedFetch(request);
      return;
    }

    if (!fle.getFetch()) {
      if (LOG.isLoggable(Level.FINEST))
        LOG.finest("not supposed to fetch " + fle.getPage().getURL());
      enqueueOutput(new RequestRecord(url, fle, false));
      return;
    }

    if (hostNameBans != null) {
      String hostName= url.getHost();
      hostName= hostName.toLowerCase();
      for (int i= 0; i < hostNameBans.length; i++) 
        if (hostNameBans[i].matches(hostName)) {
          RequestRecord request= new RequestRecord(url, fle, true);
          request.setFailureReason(FAIL_HOSTNAME_BANNED);
          handleFailedFetch(request);
          return;
        }
    }

    queueNewRequest(new RequestRecord(url, fle, null));
  }

  private void queueNewRequest(RequestRecord request) {
    URL url= request.getURL();

    boolean newHostQueue= false;

    HostQueue queue= request.getHostQueue();  // redirs will have this set
    if (queue == null) {
      HostQueueKey key= new HostQueueKey(url.getProtocol(), url.getHost(), url.getPort());
      queue= (HostQueue) allHostQueues.get(key);

      if (queue == null) {
        queue= (HostQueue) hostQueueCache.remove(key);
        if (queue != null) {
          allHostQueues.put(key, queue);
          delayHostQueues.add(queue); //safest place to add
        }
      }

      if (queue == null) {
        queue= new HostQueue(key, this);
        allHostQueues.put(key, queue);
        readyHostQueues.add(queue, -queue.size());
        newHostQueue= true;
      }
      request.setHostQueue(queue);
    }

    // fixme: once there is a mechanism to "defer" a page, 
    // we should mark page as deferred, not drop on floor!!
    if (queue.size() >= MAX_HOSTQUEUE_LENGTH) {
      // if it's not a robots.txt request, and not a redirect (ie. no
      // other HostQueues can possibly be waiting for it), just drop
      // it on the floor
      if ( (!request.isRobotsRequest()) && 
           (request.getParentRequest() == null) ) {

        overallFetcherStatus.droppedOnFloor(request);

        return;
      }
    }

    queue.addRequest(request);
    if (!request.isRobotsRequest()) 
      // no accounting on robots.txt files
      // this is a robots redirect- requeue it if it's on the same host
      numQueuedRequests++;

    if (!newHostQueue) {
      // find it and put it in appropriate place

      if (readyHostQueues.contains(queue)) {
        readyHostQueues.decreaseKey(queue, -queue.size());
        return;
      }

      if (idleHostQueues.contains(queue)) {
        idleHostQueues.remove(queue);
        if (queue.requestReady())
          readyHostQueues.add(queue, -queue.size());
        else 
          delayHostQueues.add(queue);
        return;
      }

      // otherwise it's busy or in delay- leave it!

    }
  }

  // output handling

  // pushes all pendingOutputQueue items into outputQueue.
  // caller should hold lock on this
  private void enqueuePendingOutput() {
    int numAdded= pendingOutputQueue.size();

    if (numAdded == 0) 
      return;

    int prevSize;
    int newSize;
    synchronized (outputQueue) {
      prevSize= outputQueue.size();
      outputQueue.addAll(pendingOutputQueue);
      pendingOutputQueue.clear();

      overallFetcherStatus.incrementOutputQueueAdd(numAdded);
      newSize= outputQueue.size();

      if (prevSize <= MAX_HOSTQUEUE_LENGTH) {
        for (int i= 0; i < numAdded; i++) 
          outputQueue.notify();
      }

      if (newSize > MAX_HOSTQUEUE_LENGTH) {
        try {
          overallFetcherStatus.incrementOutputQueueFull();
          outputQueue.wait(WAIT_TIMEOUT);
        } catch (InterruptedException e) {
          ;
        }
      }
    }

  }

  // adds request to pendingOutputQueue- enqueuePendingOutput() must be
  // called after all calls to enqueueOutput() have been made.
  // caller should hold lock on this
  private void enqueueOutput(RequestRecord request) {
    pendingOutputQueue.addLast(request);
  }

  /**
   * Returns true if there are no remaining requests that may need to
   * be sent to an {@link OutputThread}.
   */
  public boolean finishedOutput() {
    if (LogFormatter.hasLoggedSevere()) {
      aborted= true;
      return true;
    }

    // do lightweight checks first- get lock and do final check if
    // there's a chance we're done
    if (!fetchListEmpty) 
      return false;
    if (!finishedRequests) 
      return false;

    synchronized (outputQueue) {
      if (finishedRequests && (outputQueue.size() == 0) )
        return true;
      else 
        return false;
    }
  }

  /**
   * If <code>finishedRequest</code> is not null, it is "returned" to
   * the scheduler as having been output.  The next request that is
   * ready to be output (or <code>null</code> if there are no such
   * requests) is returned.
   */
  public RequestRecord returnOutputAndGetNext(RequestRecord finishedRequest,
                                              String finishedUrlString) {
    RequestRecord nextRequest= null;
    boolean done= finishedOutput();

    synchronized (outputQueue) {

      LOG.finest("returnOutputAndGetNext: got outputQueue lock, returning"
                 + " request");

      if (finishedRequest != null) {
       // this is in the synchronized block so we can have have
       // a set of mutexes around overallFetcherStatus- needed
       // for bandwidth-throttling
        overallFetcherStatus.outputStatus(finishedRequest, finishedUrlString);
      }

      // get the next request to output

      if (LOG.isLoggable(Level.FINEST))
        LOG.finest("returnOutputAndGetNext: outputQueue: " 
                   + outputQueue.size());

      if (outputQueue.size() == 0) {
        if (!done) {
          overallFetcherStatus.incrementOutputQueueEmpty();
          try {
            LOG.finest("returnOutputAndGetNext: going to wait");
            outputQueue.wait(WAIT_TIMEOUT);
          } catch (InterruptedException e) {
            ;
          }
          LOG.finest("returnOutputAndGetNext: done wait");
        } else {
          // we are done- wake all waiters
          outputQueue.notifyAll();
        }
      } else {
        LOG.finest("returnOutputAndGetNext: popping immediately");
        overallFetcherStatus.incrementOutputQueuePopNoDelay();
        outputQueue.notify();
      }

      if (outputQueue.size() != 0) {
        nextRequest= (RequestRecord) outputQueue.removeFirst();
        overallFetcherStatus.incrementOutputQueuePopped();
        LOG.finest("returnOutputAndGetNext: popped ");
      }
    }
    return nextRequest;
  }

  /**
   * Returns true if all requests from the <code>fetchList</code> have
   * been processed by {@link FetcherThread}s, false otherwise.
   */
  public boolean finishedRequests() {
    if (LogFormatter.hasLoggedSevere()) {
      aborted= true;
      return true;
    }

    // do lightweight checks first- get locks and do more checks if
    // there's a chance we're done
    if (!fetchListEmpty) 
      return false;

    synchronized (this) {
      // fixme:
      // kill all the queues before we're so bold as to declare finished?
      if ( fetchListEmpty 
           && (numQueuedRequests == 0) 
           && (busyHostQueues.size() == 0)  // implies ready/delay q's are empty
           && (numOutstandingRequests == 0) ) {
        finishedRequests= true;
        return true;
      }
      if (LOG.isLoggable(Level.FINEST))
        LOG.finest("fetchListEmpty: " + fetchListEmpty 
                   + "  numQueuedRequests: " + numQueuedRequests);
      return false;
    }
  }

  private void checkQueues() {

    while (delayHostQueues.size() > 0) {
      HostQueue queue= (HostQueue) delayHostQueues.getFirst();
      if (queue.requestReady()) {
        delayHostQueues.removeFirst();
        readyHostQueues.add(queue, -queue.size());
      } else if (queue.isFinished()) {
        delayHostQueues.removeFirst();
        idleHostQueues.add(queue);
      } else if (!queue.delaysPending()) {
        // must be waiting for redirected robots or somesuch
        delayHostQueues.removeFirst();
        delayHostQueues.add(queue); 
        // LOG.fine("requeueing host: " + queue.getKey().toString());
        break;
      } else 
        // LOG.fine("blocked on delay host: " + queue.getKey().toString());
      // delays are pending
      break;
    }

    // do this once to see if we re-populate an 'idle' queue
    primeQueue();

    // kill some idle queues
    Iterator iter= idleHostQueues.iterator();
    while (idleHostQueues.size() > maxCachedRobots) {
      HostQueue queue= (HostQueue) iter.next();
      iter.remove();
      hostQueueCache.put(queue.getKey(), queue);
      if (!queue.isFinished()) {
        LOG.warning("Warning: queue " + queue.getKey() + " in idleQueue"
                    + " but is not finished!");
        // safest place to add...
        delayHostQueues.add(queue);
      } else {
        if (LOG.isLoggable(Level.FINEST))
          LOG.finest("disposing of idle queue " + queue.getKey());
                                
        if (allHostQueues.remove(queue.getKey()) != queue) {
          LOG.warning("Warning: queue " + queue.getKey() + " in idleQueue"
                      + " but not in allHostQueues!");
        }
      }
    }

    // prime again to replace any idle queues we threw out
    primeQueue();

    if ( ( readyHostQueues.size() + idleHostQueues.size() 
           + delayHostQueues.size() + busyHostQueues.size())
         != allHostQueues.size()) 
      LOG.warning("    BAD allHostQueues.size() is: " + 
                  allHostQueues.size() + ", should be: "
                  + ( readyHostQueues.size() + idleHostQueues.size() 
                      + delayHostQueues.size() + busyHostQueues.size()) );

  }

  /**
   * Returns the next request waiting for processing by a {@link
   * FetcherThread}, or <code>null</code> if no such request exists.
   */
  private synchronized RequestRecord getNextRequest() {
    
    overallFetcherStatus.incrementGetRequestAttempts();    

    if (LOG.isLoggable(Level.FINE))
      LOG.fine("ready: " + readyHostQueues.size()
               + " idle: " + idleHostQueues.size()
               + " delay: " + delayHostQueues.size()
               + " busy: " + busyHostQueues.size()
               + " total: " + allHostQueues.size());

    // fixme: remove this sometime..
    if ( ( readyHostQueues.size() + idleHostQueues.size() 
           + delayHostQueues.size() + busyHostQueues.size())
         != allHostQueues.size()) 
      LOG.severe("ready: " + readyHostQueues.size()
                 + " idle: " + idleHostQueues.size()
                 + " delay: " + delayHostQueues.size()
                 + " busy: " + busyHostQueues.size()
                 + " BADTOTAL: " + allHostQueues.size());

    // clean up queues and read more requests if there are no 
    // ready queues or a second has passed
    if ( (readyHostQueues.size() == 0) 
         || ((lastCheckQueues - now) < SECONDS_TO_MS_MULTIPLIER) ) {
      lastCheckQueues= now;
      checkQueues();
    }

    // check if we have anything that seems ready
    if (readyHostQueues.size() == 0) {
      if ( (busyHostQueues.size() != 0)
           || (delayHostQueues.size() != 0) )

        overallFetcherStatus.incrementGetRequestAllBusy();

      return null;
    }

    return getNextRequestHelper();
  }
    
  private RequestRecord getNextRequestHelper() {
    while (readyHostQueues.size() > 0) {

      HostQueue queue= (HostQueue) readyHostQueues.popMin();

      // fixme: once there is a mechanism to "defer" a page, 
      // we should mark page as deferred, not drop on floor!!
      if (readyHostQueues.size() + busyHostQueues.size() 
          + delayHostQueues.size() < LOW_ACTIVE_QUEUES) {
        while (queue.size() > LOW_ACTIVE_QUEUES_MAX_LENGTH) {
          RequestRecord request= queue.killRequest();
          if (request == null) 
            break;
          numQueuedRequests--;

          overallFetcherStatus.droppedOnFloor(request);
          // drop request on floor
        }
      }

      if (!queue.requestReady()) {
        LOG.warning("queue " + queue.getKey() + " in readyQueue"
                    + " but is not ready!");
        if (queue.isFinished()) {
          idleHostQueues.add(queue);
        } else {
          delayHostQueues.add(queue); // safest place to add
        }

        overallFetcherStatus.incrementGetRequestFoundNotReady();
        return null;
      }
                
      RequestRecord request= queue.getNextRequest();

      if (request == null) {  
        LOG.warning("queue " + queue.getKey() + " in ready queue, but not"
                    + " ready!");
        if (!queue.isFinished()) { // robots.txt expired?
          delayHostQueues.add(queue);
        } else {
          LOG.warning("Warning: finished queue " + queue.getKey() 
                      + " in ready queue");
          idleHostQueues.add(queue);
        }

        overallFetcherStatus.incrementGetRequestFoundNotReady();

        return null;
      }

      overallFetcherStatus.dispatchingToFetcherThread(request);
      if (!request.isRobotsRequest()) {
        numQueuedRequests--;
      }

      if (request.getHasFailed()) {
        // robots.txt excluded it, make host ready immediately if we can
        if (queue.requestReady()) {
          readyHostQueues.add(queue, -queue.size());
        } else if (queue.isFinished()) {
          idleHostQueues.add(queue);
        } else {
          // always safe to add to delay q
          delayHostQueues.add(queue); 
        }

        handleFailedFetch(request);

        overallFetcherStatus.incrementGetRequestFoundExcluded();

        continue;
      }

      if (LOG.isLoggable(Level.FINE))
        LOG.fine("got " + request.getURLString() + ", ready= " 
                 + queue.requestReady());

      if (queue.requestReady()) 
        readyHostQueues.add(queue, -queue.size());
      else if (queue.delaysPending()) 
        delayHostQueues.add(queue);
      else 
        busyHostQueues.add(queue);

      if (LOG.isLoggable(Level.FINE))
        LOG.fine("numOutstandingRequests: " + numOutstandingRequests);
      if (!request.isRobotsRequest()) {
        numOutstandingRequests++;
        if (LOG.isLoggable(Level.FINE))
          LOG.fine("incremented numOutstandingRequests (" 
                   + numOutstandingRequests + "): " + request.getURLString());
      }

      overallFetcherStatus.incrementGetRequestSuccesses();

      return request;
    }

    return null;
  }

  /**
   * Notifies this <code>RequestScheduler</code> that an attempt has
   * been made to fetch the supplied<code>request</code>.  FetcherThreads
   * must call this once for each <code>RequestRecord</code> they 
   * obtain from a call to {@link #getNextRequest()}.  The 
   * <code>Http.BytesTransferredCounter</code> should include
   * transfer counts for just the last fetch attempt made.
   */
  public void returnRequest(
    RequestRecord request, MiscHttpAccounting httpAccounting ) {

    synchronized (this) {
      unsyncReturnRequest(request, httpAccounting);
    }

  }

  // a private version of returnRequest, which requires external
  // synchronization on this.
  private void unsyncReturnRequest(
    RequestRecord request, MiscHttpAccounting httpAccounting ) {

    if (request.getResponse() != null) {
      if (LOG.isLoggable(Level.FINE))
        LOG.fine("FetcherThread returned: " + request.getURLString() 
                 + "  completed: true  code:" 
                 + request.getResponse().getCode());
    } else {
      if (LOG.isLoggable(Level.FINE))
        LOG.fine("FetcherThread returned: " + request.getURLString()
                 + "  completed: false" );
    }

    HostQueue queue= request.getHostQueue();
    if (busyHostQueues.contains(queue)) {
      // could also be in delay queue or ready queue already
      busyHostQueues.remove(queue);
      delayHostQueues.add(queue);
    }

    Response response= request.getResponse();

    if (!request.isRobotsRequest())
      numOutstandingRequests--;

    if (request.getHasFailed()) {
      handleFailedFetch(request);
      return;
    }

    if (response == null) {
      // fetch failed, can retry
      handleUnsuccessfulFetchAttempt(request);
      return;
    }

    overallFetcherStatus.incrementRawBytes(httpAccounting.getBytesSent(),
                                          httpAccounting.getBytesRead());
    overallFetcherStatus.incrementContinues(response.getNumContinues());

    int code= response.getCode();
    if (code == 200) {
      handleSuccessFetch(request);
      return;
    }

    if (code >= 300 && code < 400) {     // handle redirect
      handleRedirectedFetch(request);
      return;
    }

    if (code == 404) {                   // handle doesn't exist
      request.setFailureReason(FAIL_NOT_FOUND);
      handleFailedFetch(request);
      return;
    }

    if (code >= 400 && code < 500) {     // handle permission error
      request.setFailureReason(FAIL_FORBIDDEN);
      handleFailedFetch(request);
      return;
    }

    request.setFailureReason(FAIL_UNKNOWN_RESP_CODE);
    request.setFailureMessages(new String[] { Integer.toString(code) });

    // fetch failed, won't retry
    handleFailedFetch(request);
                
  }

  /**
   * Returns the time of the last call to getNextRequest()- this is
   * useful for calculating delays, etc.  This method can be called in
   * place of repeated <code>new Date().getTime()</code> incantations.
   * This time is guaranteed to be in the past, and after the last
   * request was returned.
   */
  public long getTime() {
    return now;
  }

  /**
   * Notifies this <code>RequestScheduler</code> that an attempt has
   * been made to fetch the supplied<code>request</code>.  FetcherThreads
   * must call this once for each <code>RequestRecord</code> they 
   * obtain from a call to {@link #getNextRequest()}.  The 
   * <code>Http.BytesTransferredCounter</code> should include
   * transfer counts for just the last fetch attempt made.
   * Returns the next request waiting for processing by a {@link
   * FetcherThread}, or <code>null</code> if no such request exists.
   */
  public RequestRecord returnRequestAndGetNext(
    RequestRecord retRequest, MiscHttpAccounting httpAccounting ) {

    now= new Date().getTime();

    RequestRecord nextRequest= null;

    synchronized (this) {

      // return the request
      if (retRequest != null) 
        unsyncReturnRequest(retRequest, httpAccounting);

     
      // now get next request
      if (!finishedRequests())
        nextRequest= getNextRequest();

      // push output items into output queue
      enqueuePendingOutput();
    }

    return nextRequest;
  }

  private void handleFailedFetch(RequestRecord request) {
    // tell HostQueue this request is done
    request.setHasFailed(true);
    if (LOG.isLoggable(Level.FINEST))
      LOG.finest("notifyQueuesOfCompletion: ");
    request.notifyQueuesOfCompletion();

    overallFetcherStatus.requestFailed(request);

    // queue output 
    if (!request.isRobotsRequest())
      enqueueOutput(request);
  }

  private void handleUnsuccessfulFetchAttempt(RequestRecord request) {

    overallFetcherStatus.requestError(request);
    request.incrementErrors();

    if  (request.getNumErrors() >= maxPageErrors) {
      request.setFailureReason(FAIL_TOO_MANY_ERRORS);
      handleFailedFetch(request);
      return;
    }

    overallFetcherStatus.retry(request);
    request.notifyQueuesOfCompletion();

    // reset
    request.setErrorReason(ERR_UNKNOWN);

    // fixme: should have better re-enqueue strategy
    queueNewRequest(request);           

  }

  private void handleSuccessFetch(RequestRecord request) {
    // tell HostQueue this request is done
    request.notifyQueuesOfCompletion();

    overallFetcherStatus.succeeded(request);

    // queue output 
    if (!request.isRobotsRequest()) {
      enqueueOutput(request);
    }
  }

  private void handleRedirectedFetch(RequestRecord request) {
    Response response= request.getResponse();

    URL target= null;
    try {
      target = new URL(request.getURL(), response.getHeader("Location"));
    } catch (Exception e) {
      ;
    }

    // too many redirects?
    if (LOG.isLoggable(Level.FINE))
      LOG.fine("code is 3xx, target is " + target);

    if ( (request.getNumRedirects() == maxPageRedirects) 
         || (target == null) ) {

      if (request.getNumRedirects() == maxPageRedirects) {
        request.setFailureReason(FAIL_TOO_MANY_REDIRECTS);
      } else if (target == null) {
        request.setFailureReason(FAIL_REDIRECT_MISSING_TARGET);
      }

      handleFailedFetch(request);

      return;
    }

    // redirect loop?
    RequestRecord tmp= request;
    while (tmp != null) {
      if (target.toString().equals(tmp.getURLString())) {
        // loop!
        request.setFailureReason(FAIL_REDIRECT_LOOP_DETECTED);
        // request.setFailureMessages(new String[] {
        //   tmp.getURL().toString(), target.toString() } );
        handleFailedFetch(request);
        return;
      }
      tmp= tmp.getParentRequest();
    }

    // LOG.fine("redirecting " + request.getURLString() + " to " + target);

    overallFetcherStatus.redirected(request);

    request.incrementRedirects();

    // fixme: should have better re-enqueue strategy
    request.notifyQueuesOfCompletion();
    request= new RequestRecord(request, target, null);
    queueNewRequest(request);           
  }

  /**
   *  Logs current state information, such as HostQueue queue sizes
   *  (readyQueue, delayQueue, etc), the number of queued requests,
   *  etc.  This information is aquired asynchronously, so all counts
   *  may not be consistent.
   */
  public void logState() {
    int code= MISC_STATS;
    FetcherStatus.logTraceMisc(code, "HostQueue sizes:");
    FetcherStatus.logTraceMisc(code, "\tready: " + readyHostQueues.size());
    FetcherStatus.logTraceMisc(code, "\tidle:  " + idleHostQueues.size());
    FetcherStatus.logTraceMisc(code, "\tdelay: " + delayHostQueues.size());
    FetcherStatus.logTraceMisc(code, "\tbusy:  " + busyHostQueues.size());
    FetcherStatus.logTraceMisc(code, "\ttotal: " + allHostQueues.size());
    FetcherStatus.logTraceMisc(code, "\tcached:" + hostQueueCache.size());

    
    FetcherStatus.logTraceMisc(code, "HostQueues contain " + numQueuedRequests
      + " fetchList entries");
                
    FetcherStatus.logTraceMisc(code,"FetchList is" + 
                               (fetchListEmpty ? "" : " not") 
                               + " empty");
  }

  /**
   * This method starts processing the <code>fetchList</code>, and
   * does not return until processing is complete.  The return value
   * indicates error status; a return value of <code>false</code>
   * means no errors were encountered, <code>true</code> means that
   * the fetch was aborted.
   */
  public boolean run() {
    try {
      primeQueue();

      FetcherThread[] fetchers= new FetcherThread[numFetchThreads];

      long now= new Date().getTime();

      long lastStats= now;
      long nextStats= lastStats 
        + (STATS_MINUTES * 60 * SECONDS_TO_MS_MULTIPLIER);

      long lastThrottle= now;
      long nextThrottle= lastThrottle
        + (throttlePeriod * SECONDS_TO_MS_MULTIPLIER);
      FetcherStatus lastStatus= null;
      int curNumThreadsThrottled= 0;
      int lastKbitsPerThread= 0;

      for (int i= 0; i < numFetchThreads; i++) {
        fetchers[i]= new FetcherThread(this);
        if (throttleInitialThreads + i < numFetchThreads) {
          fetchers[i].throttle();
          curNumThreadsThrottled++;
        }
        fetchers[i].start();
      }
      overallFetcherStatus.logTraceMisc(
        MISC_INFORMATIONAL, 
        "Starting with " + (numFetchThreads - curNumThreadsThrottled)
        + "/" + numFetchThreads + " fetcher threads active");

      OutputThread[] outputers= new OutputThread[numOutputThreads];
      for (int i= 0; i < numOutputThreads; i++) {
        outputers[i]= new OutputThread(this, fetcherDb, rawDb, strippedDb);
        outputers[i].start();
      }

      long nextSleep;

      while (!finishedRequests() && !aborted) {

        now= new Date().getTime();

        if ( (nextStats < nextThrottle) || (throttlePeriod <= 0) || ( (throttleMaxBandwidth < 0))) 
          nextSleep= nextStats - now;
        else 
          nextSleep= nextThrottle - now;

        if (nextSleep < 0)
          nextSleep = 0;

        try {
          Thread.sleep(nextSleep);
        } catch (InterruptedException e) { 
        }

        now= new Date().getTime();

        if ( (now >= nextThrottle) && (throttlePeriod > 0) 
             && (throttleMaxBandwidth > 0) ){
          FetcherStatus currentFetcherStatus;
          
          synchronized (this) {
            synchronized (outputQueue) {
              currentFetcherStatus= overallFetcherStatus.cloneStatus();
            }
          }

          // get bandwidth over last period, kbits/s
          int recentBandwidth;
          if (lastStatus == null) {
            recentBandwidth= currentFetcherStatus.getRawBandwidth();
          } else {
            FetcherStatus diffStatus= 
              currentFetcherStatus.getDelta(lastStatus);
            recentBandwidth= diffStatus.getRawBandwidth();
            if (LOG.isLoggable(Level.FINEST)) {
              currentFetcherStatus.logStats();
              lastStatus.logStats();
              diffStatus.logStats();
            }
          }

          if (recentBandwidth < 1) 
            recentBandwidth= 1;

          // decide how many threads to throttle
          int kbitsPerThread= recentBandwidth
            / (numFetchThreads - curNumThreadsThrottled);

          int newNumThreadsThrottled= 
            numFetchThreads - (throttleMaxBandwidth / kbitsPerThread);

          if (lastStatus != null) {
            // smooth it with our last decision
            newNumThreadsThrottled= (newNumThreadsThrottled
                                     + curNumThreadsThrottled) / 2;
          }

          if (lastKbitsPerThread < 1)
            lastKbitsPerThread= 1;

          int percentChangeInBandwidth= 
            (100 * (kbitsPerThread - lastKbitsPerThread) )
            / lastKbitsPerThread;

          /*
            Uncommenting this will cause the fetcher to increase
            threads pretty conservatively- you will rarely go over
            desired bandwidth in a period, but will average less, too.

          // don't increase number of running threads if bandwidth
          // per thread has dropped more than 10%!
          if ( (curNumThreadsThrottled > newNumThreadsThrottled)
               && (percentChangeInBandwidth < -10) )
            newNumThreadsThrottled= curNumThreadsThrottled;
          */

          if (newNumThreadsThrottled >= numFetchThreads) 
            newNumThreadsThrottled= numFetchThreads - 1;

          if (newNumThreadsThrottled < 0) 
            newNumThreadsThrottled= 0;

          overallFetcherStatus.logTraceMisc(
            MISC_INFORMATIONAL, "Current bandwidth: "
            + recentBandwidth + " kbits/s (" + kbitsPerThread 
            + "kbits/s/thread )");
          overallFetcherStatus.logTraceMisc(
            MISC_INFORMATIONAL, "Adjusting the number of active fetcher"
            + " threads to " + (numFetchThreads - newNumThreadsThrottled)
            + "/" + numFetchThreads);

          // throttle / unthrottle
          while (curNumThreadsThrottled > newNumThreadsThrottled) {
            curNumThreadsThrottled--;
            fetchers[curNumThreadsThrottled].unthrottle();
          }

          while (curNumThreadsThrottled < newNumThreadsThrottled) {
            fetchers[curNumThreadsThrottled].throttle();
            curNumThreadsThrottled++;
          }

          // set up for next time
          curNumThreadsThrottled= newNumThreadsThrottled;
          lastStatus= currentFetcherStatus;
          lastKbitsPerThread= kbitsPerThread;
          lastThrottle= now;
          nextThrottle+=
            (THROTTLE_PERIOD_SECONDS * SECONDS_TO_MS_MULTIPLIER);
        }

        if (now >= nextStats) {
          try {
            overallFetcherStatus.logStats();
            logState();
          } catch (Exception e) {
            e.printStackTrace();
          }
          lastStats= now;
          nextStats+= (STATS_MINUTES * 60 * SECONDS_TO_MS_MULTIPLIER);
        }

      }

      LOG.fine("Done requests");
      // unthrottle any throttled FetcherThreads
      while (curNumThreadsThrottled > 0) {
        curNumThreadsThrottled--;
        fetchers[curNumThreadsThrottled].unthrottle();
      }


      for (int i= 0; i < numFetchThreads; i++) {
        fetchers[i].join();
      }

      while (!finishedOutput() && !aborted) {
        Thread.sleep(1000);
      }
      LOG.fine("Done output");
      for (int i= 0; i < numOutputThreads; i++) {
        outputers[i].join();
      }

      overallFetcherStatus.logStats();
      logState();

      fetchList.close();
      fetcherDb.close();
      rawDb.close();
      strippedDb.close();
    } catch (Exception e) {
      LOG.severe(e.toString());
      e.printStackTrace();
    }
    return aborted;
  }

  /**
   * Sets the log level to <code>level</code>.
   */
  public void setLogLevel(Level level) {
    LOG.setLevel(level);
    Http.LOG.setLevel(level);
    Ftp.LOG.setLevel(level);
    RequestRecord.LOG.setLevel(level);
    HostQueue.LOG.setLevel(level);
    FetcherThread.LOG.setLevel(level);
    OutputThread.LOG.setLevel(level);
  }
  

  /** Run the fetcher. */
  public static void main(String[] args) throws Exception {
    boolean verbose = false;
    boolean showThreadID = false;
    String directory = null;

    String usage = "Usage: RequestScheduler [-verbose] [-showThreadID] dir";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }
      
    for (int i = 0; i < args.length; i++) {       // parse command line
      if (args[i].equals("-verbose")) {           // found -verbose option
        verbose = true;
      } else if (args[i].equals("-showThreadID")) {
        showThreadID = true;
      } else if (i != args.length-1) {
        System.err.println(usage);
        System.exit(-1);
      } else                                      // root is required parameter
        directory = args[i];
    }

    File doneFile = new File(directory, FetcherOutput.DONE_NAME);
    if (doneFile.exists())                        // check done file
      throw new RuntimeException("already fetched: " + doneFile + " exists");

    ArrayFile.Reader fetchList = new ArrayFile.Reader
      (new File(directory, FetchListEntry.DIR_NAME).toString());
    ArrayFile.Writer fetcherDb = new ArrayFile.Writer
      (new File(directory, FetcherOutput.DIR_NAME).toString(),
       FetcherOutput.class);
    ArrayFile.Writer rawDb = new ArrayFile.Writer
      (new File(directory, FetcherContent.DIR_NAME).toString(),
       FetcherContent.class);
    ArrayFile.Writer strippedDb = new ArrayFile.Writer
      (new File(directory, FetcherText.DIR_NAME).toString(),
       FetcherText.class);

    RequestScheduler scheduler = new RequestScheduler(fetchList, fetcherDb, 
                                                      rawDb, strippedDb);
    // 20040405, xing
    if (showThreadID)
      LogFormatter.setShowThreadIDs(showThreadID);

    scheduler.setLogLevel(verbose ? Level.FINER : Level.INFO);

    boolean aborted= scheduler.run();               // run the Fetcher

    if (aborted) 
      // create the error file
      new File(directory, FetcherOutput.ERROR_NAME).createNewFile();
    else
      // create the done file
      doneFile.createNewFile();
  }

}
