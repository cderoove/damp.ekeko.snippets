/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.net.protocols.Response;

import net.nutch.fetcher.RobotRulesParser;
import net.nutch.fetcher.RobotRulesParser.RobotRuleSet;
import net.nutch.pagedb.FetchListEntry;
import net.nutch.net.protocols.http.Http;
import net.nutch.net.protocols.http.HttpResponse;
import net.nutch.util.*;

import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

/**
 * <code>HostQueue<code>s handle all aspects of queuing requests for
 * one server, including issuing robots.txt requests as needed, adding
 * delays between subsequent requests, etc.
 *
 * <p>
 *
 * <em>Note that HostQueues are not thread-safe!</em> They require
 * external syncronization (currently provided by the {@link
 * RequestScheduler} class.
 *
 */
public class HostQueue implements FetcherConstants, 
  SoftHashMap.FinalizationNotifier {

  public static final Logger LOG=
  LogFormatter.getLogger("net.nutch.fetcher.HostQueue");

  public final static int MAX_CONSECUTIVE_FAILURES= 
    NutchConf.getInt("fetcher.host.consecutive.failures", 3);

  // when the percentage of failed requests goes above this number we 
  // declare the host dead (subject to the limit below)
  public final static float MAX_FAILERR_RATE=
    NutchConf.getFloat("fetcher.host.max.failerr.rate", 0.5f);

  // we apply the MAX_FAIL_RATE test only when we've made at least this 
  // many requests
  public final static int MIN_REQ_FAIL_RATE=
    NutchConf.getInt("fetcher.host.min.requests.rate", 10);

  // result codes for robots.txt checks
  public static final int ROBOTS_EXCLUDE= -1;
  public static final int ROBOTS_UNKNOWN= 0;  // expired, unfetched, etc.
  public static final int ROBOTS_ALLOW= 1;

  // object states:
  // state upon creation
  private static final int INITIAL_STATE= 0;

  // robots.txt request for this HostQueue is in the front of this
  // HostQueue's queue.
  private static final int QUEUED_ROBOTS= 1;

  // robots.txt request for this HostQueue has been handed out for
  // service by a FetcherThread
  private static final int FETCHING_ROBOTS= 2;

  // robots.txt request for this host has been redirected - possibly
  // to another host
  private static final int REPROCESSING_ROBOTS= 3;

  // robotRules object is ready for use
  private static final int DONE_ROBOTS= 4;  

  // host has had too many network errors- we give up
  private static final int DEAD_HOST= 5;

  // fixme: should become params?
  // How long we're willing to wait in REPROCESSING_ROBOTS state before
  // assuming deadlock.  Deadlock example case below..
  // (5 minutes)
  public final static long ROBOTS_REDIR_TIMEOUT_MS= 5 * 60 * 1000; 

  //  Logical deadlock can occur when robots.txt requests are
  //  redirected.  Here's an anonymized example we've found:
  // 
  // http://A/robots.txt -> http://A/default.html
  //                     -> http://B/default.html
  // 
  // http://B/robots.txt -> http://A/default.html
  // 
  // In this case, the first request added to the HostQueue for A
  // causes a request for A's robots.txt, and redirects are
  // followed until they are redirected to host B.
  //
  // Suppose B had not yet been seen.  The request for B's file
  // "/default.html" triggers a request for B's robots.txt file.  This
  // request redirects to A- but A's HostQueue is blocking while
  // awaiting B's "/default.html" file...  Each host needs a file from
  // the other to satisfy their own robots request.
  //
  // The same deadlock occurs if host B is seen first.
  //
  // The way we handle this is to only hold requests so long in cases
  // where we're waiting for our robots.txt file to be fetched.  After
  // a timeout, we start allowing other queues' robots.txt requests to
  // be processed by a blocked queue.  Requests from FetchListEntries 
  // are still held, though.

  // If there's no expiration time for a robots.txt file, how long (in 
  // ms) should it be considered valid?
  // (5 days)
  private static final long DEFAULT_ROBOTS_LIFETIME= 5 * 24 * 60 * 60 * 1000;

  // If the robots.txt file is supposed to expire soon, or in the
 // past, how long (in ms) should it be considered valid?
        // (5 minutes)
  private static final long MINIMUM_ROBOTS_LIFETIME= 5 * 60 * 1000;

  // the handle for looking up this HostQueue (currently key is
  // host+port)...  
  private HostQueueKey key;

  // currently where we get config from..  should we just take this
  // out when we have a properties mechanism?
  private RequestScheduler owner;

  // the requests queued up for this host
  private LinkedList queuedRequests;

  // object state (see INITIAL_STATE, etc)
  private int state;

  // used for timeout during REPROCESSING_ROBOTS
  private long robotsRedirTimer;

  // the requests we're waiting to hear back about
  private HashSet outstandingRequests;

  // the robots request initiated by this HostQueue
  private RequestRecord myOrigRobotsRequest;

  private RobotRuleSet robotRules;
  private long robotRulesExpireTime;

  // data structures for delaying...  if we never issue more than one
  // request at a time, this is total overkill.
  private int delaysUsed;
  private long[] delayTimers;
  private boolean[] delayInUse;

  // counts the number of requests and failures/errors
  private int totalRequestsIssued;
  private int totalFailErrs;
  private int consecutiveFailures;

  private int httpVersion;

  // cached InetAddress
  private InetAddress addr;

  // for logging stats about what caused DEAD_HOST status
  private String deadMsg= null;

  // for supporting FinalizationNotifier interface
  ArrayList finalizationListeners;

  /**
   * Encapsulates a <code>protocol</code> and <code>hostname</code>
   * and <code>port</code> tuple,
   * and serves as the hash key for {@link HostQueue}s.
   */
  public static class HostQueueKey {
    String protocol;
    String host;
    int port;
    int hashCode= Integer.MIN_VALUE;

    /**
     * Creates a new <code>HostQueueKey</code> with the given hostname
     * and port number.
     */
    HostQueueKey(String protocol, String host, int port) {
      this.protocol= protocol;
      this.host= host;
      this.port= port;
    }

    /**
     * Returns the protocol associated with this
     * <code>HostQueueKey</code>.
     */
    public String getProtocol() {
      return protocol;
    }

    /**
     * Returns the hostname associated with this
     * <code>HostQueueKey</code>.
     */
    public String getHost() {
      return host;
    }

    /**
     * Returns the port number associated with this
     * <code>HostQueueKey</code>.
     */
    public int getPort() {
      return port;
    }

    public String toString() {
      return protocol + "://" + host + ":" + port;
    }

    public int hashCode() {
      if (hashCode == Integer.MIN_VALUE)
        hashCode= this.toString().hashCode();
      return hashCode;
    }

    public boolean equals(Object other) {
      try {
        HostQueueKey o= (HostQueueKey) other;
        if ( (this.port == o.port)
             && (this.host.equals(o.host))
             && (this.protocol.equals(o.protocol)) ) 
          return true;
        return false;
      } catch (ClassCastException e) {
        return false;
      }
    }

  }

  /**
   *  Creates a new <code>HostQueue</code> with the given
   *  <code>key</code>.  The supplied {@link RequestScheduler} will be
   *  queried for various parameters (should these be read directly
   *  from here via properties mechanism?).
   *
   */
  protected HostQueue(HostQueueKey key, RequestScheduler owner) {
    this.key= key;
    this.owner= owner;

    this.state= INITIAL_STATE;
    this.queuedRequests= new LinkedList();
    this.outstandingRequests= new HashSet();
    this.delayTimers= 
      new long[owner.getMaxConcurrentRequests()];
    this.delayInUse= 
      new boolean[owner.getMaxConcurrentRequests()];
    for (int i= 0; i < owner.getMaxConcurrentRequests(); i++)
      this.delayInUse[i]= false;
    this.totalRequestsIssued= 0;
    this.totalFailErrs= 0;
    this.consecutiveFailures= 0;
    this.httpVersion= Http.HTTP_VER_LATEST;
    this.addr= null;
    this.robotRulesExpireTime= 0;
    this.finalizationListeners= new ArrayList();
  }

  public HostQueueKey getKey() {
    return key;
  }

  /**
   * Adds the given request to this queue's to-do list.  
   * 
   * <p>
   * 
   * Requests will be prioritized in the following way: first, this
   * <code>HostQueue</code>'s <code>robots.txt</code> request; next,
   * any redirected robots.txt requests; finally, all requests from
   * the <code>FetchList</code>.
   * 
   * <p>
   * 
   * <em>Note that the <code>HostQueue</code> object does not do any
   * checking to see if the request is to the relevant host and
   * port!</em>
   */
  public void addRequest(RequestRecord request) {
    switch (state) {

    case INITIAL_STATE:
      if (isHttpRequest(request)) {
        queueRobotsRequest();
        queueRequest(request);
        state= QUEUED_ROBOTS;
      } else {
        queueRequest(request);
        state= DONE_ROBOTS;
      }
      break;

    case QUEUED_ROBOTS:
    case FETCHING_ROBOTS:
      queueRequest(request);
      break;

    case REPROCESSING_ROBOTS:
      if (!request.isRobotsRequest()) {
        queueRequest(request);
        return;
      }

      if (request.getOriginalRequest() == myOrigRobotsRequest) {
        // it is my robots request!
        // requeue and set state
        queuedRequests.addFirst(request);
        state= QUEUED_ROBOTS;
      } else {
        // somebody else's...  just queue it.
        queueRequest(request);
      } 
      break;

    case DONE_ROBOTS:
      checkRobots(request);
      queueRequest(request);
      break;

    case DEAD_HOST:
      request.setHasFailed(true);
      request.setFailureReason(FAIL_DEAD_HOST);
      queueRequest(request);
      break;

    default:
      throw new IllegalStateException("HostQueue state is "
                                      + state);
    }

  }

  // cannot be our own robots.txt redirect
  private void queueRequest(RequestRecord request) {
    if ( (request.isRobotsRequest())  
         || (request.getNumErrors() != 0) 
         || (request.getNumRedirects() != 0) ) {

      // redirected robots request from another host, or already
      // failed (banned by robots)- we'll add it immediately after any
      // existing robots requests
      ListIterator iter= queuedRequests.listIterator();
      int index= 0;
      while (iter.hasNext()) {
        RequestRecord curr= (RequestRecord) iter.next();
        if (!curr.isRobotsRequest()) 
          break;
        index++;
      }
      // add request after last robots.txt entry
      queuedRequests.add(index, request);

    } else {

      // add at end
      queuedRequests.addLast(request);

    }

  }

  private void queueRobotsRequest() {
    URL url;
    try {
      url= new URL(key.getProtocol(), key.getHost(), key.getPort(), "/robots.txt");
      RequestRecord robotReq= new RequestRecord(url, null, this);
      queuedRequests.addFirst(robotReq);
      myOrigRobotsRequest= robotReq;
    } catch (Exception e) {
      LOG.severe("Could not generate robots URL for " + key.getHost() 
                 + ":" + key.getPort() + "!");
      LOG.severe("Winging it and allowing all requests....");
      robotRules= RobotRulesParser.getEmptyRules();
      state= DONE_ROBOTS;
    }
  }

  private int checkRobots(RequestRecord request) {
    if (state != DONE_ROBOTS) 
      return ROBOTS_UNKNOWN;

    if (robotsExpired()) {
      queueRobotsRequest();
      state= QUEUED_ROBOTS;
      return ROBOTS_UNKNOWN;
    }

    String path= request.getURL().getFile();
    if ( (path == null) || "".equals(path) )
      path= "/";

    if ( robotRules.isAllowed(path) ) {
      return ROBOTS_ALLOW;
    } else {
      request.setHasFailed(true);
      request.setFailureReason(FAIL_ROBOTS_EXCLUDED);
      return ROBOTS_EXCLUDE;
    }
  }

  private boolean robotsExpired() {
    long now= owner.getTime();
    if (now < robotRulesExpireTime)
      return false;
    else 
      return true;
  }

  /**
   * This method pops a non-<code>robots.txt</code> request off of the
   * queue, and returns it.  <code>null</code> is returned if there
   * are no suitable requests queued.  This is similar to
   * <code>getNextRequest()</code>, except that the
   * <code>HostQueue</code> will not expect any later notification of
   * the returned request's status, and will not delay.
   */
  public RequestRecord killRequest() {
    // fixme: could look for robots-disallowed requests first...
    ListIterator iter= queuedRequests.listIterator();
    RequestRecord curr= null;
    while (iter.hasNext()) {
      curr= (RequestRecord) iter.next();
      if (!curr.isRobotsRequest()){
        iter.remove();
        break;
      }
      curr= null;
    }
    return curr;
  }

  /** 
   * Pops the next request off the queue, and returns it.  The result
   * of <code>requestReady()</code> should be checked before calling
   * this.
   */
  public RequestRecord getNextRequest() {
    if (!requestReady())
      throw new IllegalStateException("Can't return request; none ready! (" 
                                      + key + ")");

    RequestRecord request;

    switch (state) {

    case INITIAL_STATE:
      throw new IllegalStateException("getNextRequest() called while"
                                      + " HostQueue in INITIAL_STATE");

    case QUEUED_ROBOTS:
      state= FETCHING_ROBOTS;
      request= (RequestRecord) queuedRequests.removeFirst();
      request.setHttpVersion(httpVersion);
      outstandingRequests.add(request);
      if (LOG.isLoggable(Level.FINER))
        LOG.finer(key + ", returning " + request.getURLString());
      request.setNotifyQueue(true);
      totalRequestsIssued++;
      return request;

    case FETCHING_ROBOTS:
      throw new IllegalStateException("getNextRequest() called while"
                                      + " HostQueue in FETCHING_ROBOTS");

    case REPROCESSING_ROBOTS:
      // see all the checks in requestReady()!
      
      request= (RequestRecord) queuedRequests.getFirst();
      queuedRequests.removeFirst();

      outstandingRequests.add(request);
      request.setNotifyQueue(true);
      totalRequestsIssued++;

      request.setHttpVersion(httpVersion);

      return request;

    case DONE_ROBOTS:
      request= (RequestRecord) queuedRequests.getFirst();

      if (isHttpRequest(request))
        checkRobots(request);

      if (state != DONE_ROBOTS)  // had to requeue robots request 
        return getNextRequest(); // due to expiry 

      request= (RequestRecord) queuedRequests.removeFirst();

      // if we exclude it due to robots.txt, we don't want to get
      // notified of completion or count it as dispatched...
      if (!request.getHasFailed()) {
        outstandingRequests.add(request);
        request.setNotifyQueue(true);
        totalRequestsIssued++;
      }

      // apply cached HTTP version and InetAddress
      request.setHttpVersion(httpVersion);
      request.setAddr(addr);

      return request;

    case DEAD_HOST:
      request= (RequestRecord) queuedRequests.removeFirst();
      request.setHasFailed(true);
      request.setFailureReason(FAIL_DEAD_HOST);
      if (deadMsg != null) {
        request.setFailureMessages(new String[] {deadMsg});
        deadMsg= null;
      }
      request.setNotifyQueue(false);
      return request;

    }

    throw new IllegalStateException("getNextRequest() missed case:"
                                    + " state=" + state);

  }

  /**
   * Returns <code>true</code> if this queue is finished processing
   * all of it's requests.  A <code>HostQueue</code> is considered
   * finised when it's queue is empty and all outstanding requests
   * have completed.
   */
  public boolean isFinished() {
    if (LOG.isLoggable(Level.FINER))
      LOG.finer(queuedRequests.size() + " queued, " 
                + outstandingRequests.size() + " outstanding,"
                + delaysInUse() + "/" + owner.getMaxConcurrentRequests() 
                + " delays in use");

    return ( (queuedRequests.size() == 0) 
             && (outstandingRequests.size() == 0) );
  }

  /**
   *  Returns <code>true</code> if there are any delay slots in use.
   */
  public boolean delaysPending() {
    return delaysInUse() > 0;
  }

  /**
   * Returns <code>true</code> if there is a request in this
   * <code>HostQueue</code> which is ready for processing.
   */
  public boolean requestReady() {
    switch (state) {

    case INITIAL_STATE: 
      return false;

    case QUEUED_ROBOTS:
      if (LOG.isLoggable(Level.FINE))
        LOG.fine("requestReady(QUEUED_ROBOTS): delaysInUse()=" + delaysInUse()
                 + " outstandingRequests.size()= " 
                 + outstandingRequests.size());
      if ( (delaysInUse() + outstandingRequests.size()) 
           < owner.getMaxConcurrentRequests() ) 
        if (queuedRequests.size() > 0)
          return true;
      return false;

    case FETCHING_ROBOTS:
      return false;

    case REPROCESSING_ROBOTS:
      if (LOG.isLoggable(Level.FINER))
        LOG.finer("REPROCESSING_ROBOTS: queuedRequests.size()= "
                  + queuedRequests.size());

      if (queuedRequests.size() == 0) 
        return false;

      if (LOG.isLoggable(Level.FINER))
        LOG.finer("REPROCESSING_ROBOTS: timer finished? "
                  + (owner.getTime() >= robotsRedirTimer));

      if (owner.getTime() < robotsRedirTimer) 
        return false;

      RequestRecord request= (RequestRecord) queuedRequests.getFirst();

      if (LOG.isLoggable(Level.FINER))
        LOG.finer("REPROCESSING_ROBOTS: first req robots? "
                  + request.isRobotsRequest());

      if (!request.isRobotsRequest()) 
        return false;

      if (LOG.isLoggable(Level.FINER))
        LOG.finer(getKey() + ": delaysInUse= " + delaysInUse() 
                  + " outstandingRequests.size()= " +
                  + outstandingRequests.size());

      if ( (delaysInUse() + outstandingRequests.size()) 
           < owner.getMaxConcurrentRequests() ) 
        return true;

    case DONE_ROBOTS:
      if (LOG.isLoggable(Level.FINER))
        LOG.finer(getKey() + ": delaysInUse= " + delaysInUse() 
                  + " outstandingRequests.size()= " +
                  + outstandingRequests.size());

      if ( (delaysInUse() + outstandingRequests.size()) 
           < owner.getMaxConcurrentRequests() ) 
        if (queuedRequests.size() > 0)
          return true;
      return false;

    case DEAD_HOST:
      return queuedRequests.size() > 0;

    default:
      throw new IllegalStateException("missed case, state == " + state);
    }

  }

  /**
   * Returns the number of requests which are queued in this
   * <code>HostQueue</code>.
   *
   */ 
  public int size() {
    return queuedRequests.size();
  }

  // returns the number of delays in use
  private int delaysInUse() {
    updateDelays();
    if (LOG.isLoggable(Level.FINE))
      LOG.fine(key + ": " + delaysUsed + "/" + delayInUse.length 
               + " delays in use");
    return delaysUsed;
  }

  // updates the delaysUsed variable
  private void updateDelays() {
    if (delaysUsed == 0)
      return;

    delaysUsed= 0;
    long now= owner.getTime();
    for (int i= 0; i < delayInUse.length; i++) 
      if (delayInUse[i]) {
        if (now > delayTimers[i]) 
          delayInUse[i] = false;
        else 
          delaysUsed++;
      }
  }

  private void removeFromOutstandingRequests(RequestRecord request) {

    // try to delete request from outstandingRequests
    RequestRecord tmp= request;
    boolean found= false;
    while (!found && (tmp != null)) {
      if (outstandingRequests.remove(tmp))
        found= true;
      else 
        tmp= tmp.getParentRequest();
    }

    if (!found) {
      if (request.getOriginalRequest() != myOrigRobotsRequest) 
        LOG.warning(key + ": Could not find outstanding request for " 
                 + request.getURLString() + ", I wasn't waiting for this, so"
                 +" I won't delay"); 
      return;
    }

    found= false;  // find empty delay slot
    int empty= -1;
    for (int i= 0;  (i < delayInUse.length) && (empty == -1);  i++)
      if (!delayInUse[i]) 
        empty= i;

    if (empty == -1) {
      LOG.warning(key + ": Could not find empty delay slot in queue " 
                  + " -- won't delay"); 
      return;
    } 

    delaysUsed++;
    delayInUse[empty]= true;
    delayTimers[empty]= owner.getTime() + owner.getMsDelay();

    if (LOG.isLoggable(Level.FINE))
      LOG.fine(key + ": delay set");

    return;

  }


  /**
   * This is called by {@link
   * RequestRecord#notifyQueuesOfCompletion()}, to notify the queue
   * that the request has been finished.  Calling this method removes
   * the request from the set of things this queue is waiting for, and
   * sets a delay timer.  If the request is our robots.txt file, the
   * result of the request is also processed.
   */
  public void requestCompleted(RequestRecord request) {
    if (LOG.isLoggable(Level.FINE))
      LOG.fine(key + " notified of completion: " + request.getURLString());

    httpVersion= Http.minHttpVersion(httpVersion, request.getHttpVersion());

    removeFromOutstandingRequests(request); 

    // now we don't care about this... unless it's our robots req, and
    // it's been redirected- see below.
    request.setNotifyQueue(false);  

    
    if (request.getHasFailed()
        && (request.getFailureReason() != FAIL_NOT_FOUND)) {
      consecutiveFailures++;
    } 

    if ( (request.getHasFailed() 
          && (request.getFailureReason() != FAIL_NOT_FOUND))
         || (request.getErrorReason() != 0) ) { // fail or error
      // fixme: getErrorReason() == 0 is currently ERR_UNKNOWN..
      // should have hasError() indicator or NO_ERROR status code.

      totalFailErrs++;
      // reset cached InetAddress on error
      addr= null;
    }

    if (request.getAddr() != null) 
      addr= request.getAddr();

    if ( (request.getResponse() != null) 
         && (request.getResponse().getCode() == 200) ) // has succeeded
      consecutiveFailures= 0;

    if (request.getOriginalRequest() == myOrigRobotsRequest) {

      // we don't want more notifications about this- tell parent
      // requests not to notify us.  if request is redirected, we'll
      // be notified by request- we don't need the parent!
      for (RequestRecord tmpReq= request.getParentRequest();
           tmpReq != null;
           tmpReq= tmpReq.getParentRequest()) 
        if (tmpReq.getHostQueue() == this)
          tmpReq.setNotifyQueue(false);

      if ( (state == REPROCESSING_ROBOTS) 
           || (state == FETCHING_ROBOTS) ) {

        if (request.getHasFailed()) { // failure

          if ( (request.getFailureReason() == FAIL_FORBIDDEN)
               || (request.getFailureReason() 
                   == FAIL_ROBOTS_EXCLUDED) ) {

            FetcherStatus.logTraceMisc(FetcherStatus.MISC_ROBOTS_FORBIDDEN,
                                       request.getURLString());

            robotRules= RobotRulesParser.getForbidAllRules();
            robotRulesExpireTime= 
              owner.getTime() + DEFAULT_ROBOTS_LIFETIME;
            state= DONE_ROBOTS;

          } else if (request.getFailureReason()
                     == FetcherStatus.FAIL_NOT_FOUND) {

            LOG.fine("Could not get robots.txt for " + key.getHost() 
                      + ":" + key.getPort() + " due to 404");
             robotRules= RobotRulesParser.getEmptyRules();
             robotRulesExpireTime= 
               owner.getTime() + DEFAULT_ROBOTS_LIFETIME;
             state= DONE_ROBOTS;


          } else {  // failed, but not FORBIDDEN or 404...

            checkDead();
            if (state != DEAD_HOST) {
              // requeue robots request- we'll try again
              queueRobotsRequest();
              state= QUEUED_ROBOTS;
            }
          } 

        } else { // success, redirect, or retry

          Response response= request.getResponse();
          if ( (response == null) 
               || ( (response.getCode() >= 300) 
                    && (response.getCode() < 400) )) {

            request.setNotifyQueue(true);  // we want to hear back
                                           // when this finishes

            if (LOG.isLoggable(Level.FINE))
              LOG.fine("Trying to reprocess robots.txt");
            state= REPROCESSING_ROBOTS;
            robotsRedirTimer= owner.getTime() + ROBOTS_REDIR_TIMEOUT_MS;

          } else {

            if (LOG.isLoggable(Level.FINE))
              LOG.fine("Trying to parse robots.txt");
            robotRules= 
              owner.getRobotRulesParser().parseRules(response.getContent());
                                                Date expireTime= request.getExpireTime();

            if (expireTime == null) {
              robotRulesExpireTime= 
                owner.getTime() + DEFAULT_ROBOTS_LIFETIME;
            } else {
              long expiresAt= expireTime.getTime();
              long now= owner.getTime();
              long min= now + MINIMUM_ROBOTS_LIFETIME;
              if (expiresAt < min)
                expiresAt= min;
              robotRulesExpireTime= expiresAt;
            }

            if (LOG.isLoggable(Level.FINE)) {
              LOG.fine("After parse of robots.txt");
              LOG.fine("got: "+robotRules);
            }
            
            state= DONE_ROBOTS;
          }
        }
      } else {
        LOG.warning(key + " got a robots request back,"
                    + " but not expecting one");
        LOG.warning(key + " state= " + state);
      }
    } 

    checkDead();

  }


  private final void checkDead() {
    int maxFailErr= (int) 
      ( ((float) totalRequestsIssued * MAX_FAILERR_RATE) + 0.5f );
    if ( (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) 
         || ( (totalRequestsIssued >= MIN_REQ_FAIL_RATE) 
              && (totalFailErrs > maxFailErr) ) ) {
      state= DEAD_HOST;
      deadMsg= consecutiveFailures + " consec, " + totalFailErrs
        + "/" + totalRequestsIssued + " err rate";

      if (LOG.isLoggable(Level.FINE))
        LOG.fine("After " + consecutiveFailures + " consecutive errors ("
                 + totalFailErrs + "/" + totalRequestsIssued + " req failed"
                 + "), " + "giving up on host " + key.getHost() + ":" 
                 + key.getPort() + "!");
    }
  }

  public int hashCode() {
    return key.hashCode();
  }

  // convinience method
  private boolean isHttpRequest(RequestRecord request) {
    return "http".equalsIgnoreCase(request.getURL().getProtocol());
  }

  // implements SoftHashMap.FinalizationNotifier interface
  public void addFinalizationListener(SoftHashMap.FinalizationListener
                                      listener) {
    finalizationListeners.add(listener);
  }

  // implements SoftHashMap.FinalizationNotifier interface
  protected void finalize() {
    for (Iterator iter= finalizationListeners.iterator();
         iter.hasNext() ; ) {
      SoftHashMap.FinalizationListener l=
        (SoftHashMap.FinalizationListener) iter.next();
      l.finalizationOccurring();
    }
  }

}

