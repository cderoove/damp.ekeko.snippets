/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.net.protocols.Response;

import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.text.DateFormat;

import net.nutch.pagedb.FetchListEntry;
import net.nutch.net.protocols.http.Http;
import net.nutch.net.protocols.http.HttpResponse;
import net.nutch.util.LogFormatter;
import net.nutch.util.StringUtil;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

/**
 * <code>RequestRecord<code>s represent a URL's state in the system,
 * either encapsulating a {@link FetchListEntry} or representing a
 * <code>robots.txt<code> request and what it's status is.
 *
 * <p>
 *
 * RequestRecords are passed around between {@link HostQueue}s, the
 * {@link RequestScheduler}, {@link FetcherThread}s, and {@link
 * OutputThread}s.  They count retries and redirects, track
 * redirect chains, remember failure reasons, and hold the content 
 * from successful fetches.
 *
 */
public class RequestRecord {

  public static final Logger LOG=
  LogFormatter.getLogger("net.nutch.fetcher.RequestRecord");

  private URL url;
  private String urlString;

  // previous request (set if this RequestRecord is the result of a redirect)
  private RequestRecord redirectedFrom;

  // the FetchListEntry associated with the request (set if this is not a 
  // robots.txt request)
  private FetchListEntry fle;

  // the HostQueue that this request belongs to
  private HostQueue hostQueue;

  // whether the HostQueue wants to know when this request finishes
  private boolean hostQueueWantsNotification;

  // how many errors we've encountered fetching this URL
  private int numErrors;

  // how many redirects we've encountered fetching this URL
  private int redirects;

  // after a failure, this encodes what went wrong
  private int failureReason;

  // after a failure, this contains strings that should be logged 
  // to clarify the problem 
  private String[] failureMessages;

  // after an output failure, this encodes what went wrong
  private int outputStatus;

  // after an output failure, this contains strings that should be logged 
  // to clarify the problem 
  private String[] outputStatusMessages;

  // after an error, this encodes what went wrong
  private int errorReason;

  // after an error, this contains strings that should be logged 
  // to clarify the problem (usually just the URL string)
  private String[] errorMessages;

  // whether or not this request has failed
  private boolean hasFailed;

  // the HTTP version code; what version should we try this request with?
  // fetcher may modify this value to tell HostQueue to fall back
  private int httpVersion;

  // after a successful fetch, this points to the Http.Response we got
  private Response response;

  // after a successful fetch, this holds the date from the "Expires" header
  private Date expireTime;

  // bandwidth consumed for this request
  private long bytesTransmitted;
  private long bytesReceived;

  private InetAddress addr;

  /**
   * Creates a new <code>RequestRecord</code>, which encapsulates the
   * given {@link FetchListEntry} and {@link URL}.  If the
   * <code>hostQueue</code> field is filled in, the RequestRecord is
   * associated with the given <code>HostQueue</code>.
   */
  RequestRecord(URL url, FetchListEntry fle, HostQueue hostQueue) {
    this.redirectedFrom= null;
    this.url= url;
    this.fle= fle;
    this.hostQueue= hostQueue;
    this.hostQueueWantsNotification= false;
    this.numErrors= 0;
    this.redirects= 0;
    this.response= null;
    this.failureReason= 0;
    this.hasFailed= false;
    this.httpVersion= Http.HTTP_VER_NOTSET;
    this.bytesTransmitted= 0;
    this.bytesReceived= 0;
    this.addr= null;
  }

  /**
   * Creates a new <code>RequestRecord</code>, as a redirect from the
   * supplied <code>RequestRecord</code>.  The new target is the
   * supplied <code>URL</code>.   If the <code>hostQueue</code>
   * field is filled in, the RequestRecord is associated with the
   * given <code>HostQueue</code>.  The number of bytes transferred 
   * is initialized from the parent request.
   */
  RequestRecord(RequestRecord redirectedFrom, URL redirectedTo, 
                HostQueue hostQueue) {
    this.redirectedFrom= redirectedFrom;
    this.url= redirectedTo;
    this.fle= redirectedFrom.getFetchListEntry();
    this.hostQueue= hostQueue;
    this.hostQueueWantsNotification= false;
    this.numErrors= redirectedFrom.getNumErrors();
    this.redirects= redirectedFrom.getNumRedirects();
    this.response= null;
    this.failureReason= 0;
    this.hasFailed= false;
    this.httpVersion= Http.HTTP_VER_NOTSET;
    this.bytesTransmitted= redirectedFrom.bytesTransmitted;
    this.bytesReceived= redirectedFrom.bytesReceived;
    this.addr= null;
  }

  /**
   * Creates a new <code>RequestRecord</code>, which encapsulates the
   * given {@link FetchListEntry} and sets it's failure flag to
   * <code>hasFailed</code>.  The RequestRecord created this way is
   * suitable for handoff to an {@link OutputThread}, but not a {@link
   * FetcherThread}, since the <code>URL</code> field is not filled
   * in.  Requests may be created this way for
   * <code>FetchListEntries</code> which are not scheduled for
   * fetching (see FetchListEntry.getFetch()), or those that are
   * filtered for other reasons.
   */
  RequestRecord(URL url, FetchListEntry fle, boolean hasFailed) {
    this.redirectedFrom= null;
    this.url= url;
    this.fle= fle;
    this.hostQueue= null;
    this.hostQueueWantsNotification= false;
    this.numErrors= 0;
    this.redirects= 0;
    this.response= null;
    this.failureReason= 0;
    this.hasFailed= hasFailed;
    this.httpVersion= Http.HTTP_VER_NOTSET;
    this.bytesTransmitted= 0;
    this.bytesReceived= 0;
    this.addr= null;
  }

  /**
   * Creates a new <code>RequestRecord</code>, which encapsulates the
   * given {@link FetchListEntry} and sets it's failure flag to
   * <code>hasFailed</code>.  A RequestRecord created this way is
   * suitable for handoff to an {@link OutputThread}, but not a {@link
   * FetcherThread}, since the <code>URL</code> field is not filled
   * in.  Requests may be created this way for
   * <code>FetchListEntries</code> which are not scheduled for
   * fetching (see FetchListEntry.getFetch()), or those that are
   * filtered for other reasons.
   */
  RequestRecord(FetchListEntry fle, boolean hasFailed) {
    this(null, fle, hasFailed);
  }

  /**
   * Increment the error count for this RequestRecord
   */
  public void incrementErrors() {
    numErrors++;
  }

  /**
   * Returns the current error count for this RequestRecord
   */
  public int getNumErrors() {
    return numErrors;
  }

  /**
   * Increment the redirect count for this RequestRecord
   */
  public void incrementRedirects() {
    redirects++;
  }

  /**
   * Returns the current redirect count for this RequestRecord
   */
  public int getNumRedirects() {
    return redirects;
  }

  /**
   * Sets the response for this RequestRecord
   */
  public void setResponse(Response response) {
    this.response= response;

    if (response != null) {
      expireTime= null;
      String expireStr= response.getHeader("Expires");
      if (expireStr != null) {
        try {
          DateFormat df= DateFormat.getDateInstance(DateFormat.LONG);
          Date date= df.parse(expireStr);
          expireTime= date;
        } catch (Exception e) {
          ;
        }
      }
    }

  }

  /**
   * Returns the response for this RequestRecord
   */
  public Response getResponse() {
    return response;
  }

  /**
   * Returns the {@link FetchListEntry} associated with this
   * <code>RequestRecord</code>, or <code>null</code> if this 
   * is a <code>robots.txt</code> request.
   */
  public FetchListEntry getFetchListEntry() {
    return fle;
  }

  /**
   * Returns <code>true</code> if this is a <code>robots.txt</code> request, or
   * <code>false</code> otherwise.
   */
  public boolean isRobotsRequest() {
    return fle == null;
  }

  /**
   * Returns the {@link URL} associated with this RequestRecord (which
   * may be down a redirect path from the original request).
   */
  public URL getURL() {
    return url;
  }

  /**
   * Returns the {@link URL} associated with this RequestRecord (which
   * may be down a redirect path from the original request), as a
   * <code>String</code>.
   */
  public String getURLString() {
    if (urlString == null) {
      if (url == null) 
        urlString= "null";
      else 
        urlString= url.toString();
    }
    return urlString;
  }

  /**
   * Returns the original {@link URL} associated with this
   * RequestRecord (ie the beginning of any redirect path that has
   * been followed).
   */
  public URL getOriginalURL() {
    RequestRecord tmp= this;
    while (tmp.redirectedFrom != null) {
      tmp= tmp.redirectedFrom;
    }
    return tmp.url;
  }

  /**
   * Returns the HTTP version code associated with this RequestRecord.
   * HostQueues should set this value to determine the protocol used
   * for a fetch attempt, and FetcherThreads may reset it to tell the
   * HostQueue to use a different version.
   */
  public int getHttpVersion() {
    return httpVersion;
  }


  /**
   * Sets the HTTP version code associated with this RequestRecord.
   * HostQueues should set this value to determine the protocol used
   * for a fetch attempt, and FetcherThreads may reset it to tell the
   * HostQueue to use a different version.
   */
  public void setHttpVersion(int httpVersion) {
    this.httpVersion= httpVersion;
  }

  /**
   * Gets the {@link HostQueue} that is responsible for queueing this
   * request.
   */
  public HostQueue getHostQueue() {
    return hostQueue;
  }

  /**
   * Sets the {@link HostQueue} that is responsible for queueing this
   * request.  The current <code>HostQueue<code> must be
   * <code>null</code>; it may not be re-set.
   *
   * @throws IllegalStateException if the <code>hostQueue</code> is already set
   */
  public void setHostQueue(HostQueue hostQueue) {
    if (this.hostQueue != null) 
      throw new IllegalStateException("Can't reset hostQueue!");
    this.hostQueue= hostQueue;
  }

  /**
   * Sets the <code>hasFailed</code> code.
   */
  public void setHasFailed(boolean hasFailed) {
    this.hasFailed= hasFailed;
  }

  /**
   * Returns the <code>hasFailed</code> code.
   */
  public boolean getHasFailed() {
    return hasFailed;
  }

  /**
   * Sets the <code>FailureReason</code> to <code>code</code>.  The
   * failure reason explains why we didn't to get a page (redirect
   * loop detected, host is dead, etc), but not necessarily what
   * specific errors were encountered (refused connection, read
   * timeout, etc).
   */
  public void setFailureReason(int reasonCode) {
    this.failureReason= reasonCode;
  }

  /**
   * Returns the  <code>FailureReason</code> code.
   */ 
 public int getFailureReason() {
    return failureReason;
  }

  /**
   * Associates additional <code>Strings</code> (which should help explain
   * the problem) with the last <code>failureReason</code> that was set.
   */ 
  public void setFailureMessages(String[] msgs) {
    failureMessages= msgs;
  }

  /**
   * Returns additional <code>Strings</code> which may help explain
   * the failure, or <code>null</code> if no such messages have been
   * set.
   */ 
  public String[] getFailureMessages() {
    return failureMessages;
  }

  /**
   * Sets the <code>ErrorReason</code> to <code>code</code>.  The
   * error reason explains why we didn't get the page on this attempt.
   * When this method is called, the current
   * <code>errorMessages</code> is reset (see {@link
   * #setErrorMessages(String[])}).
   */
  public void setErrorReason(int reasonCode) {
    this.errorReason= reasonCode;
    this.errorMessages= null;
  }

  /**
   * Returns the <code>ErrorReason</code> code.
   */ 
 public int getErrorReason() {
    return errorReason;
  }

  /**
   * Associates additional <code>Strings</code> (which should help explain
   * the problem) with the last errorReason that was set.
   */ 
  public void setErrorMessages(String[] msgs) {
    errorMessages= msgs;
  }

  /**
   * Returns additional <code>Strings</code> which may help explain
   * the error, or <code>null</code> if no such messages have been
   * set.
   */ 
  public String[] getErrorMessages() {
    return errorMessages;
  }

  /**
   * Sets the <code>outputStatus</code> to <code>status</code>.  When
   * this method is called, the current
   * <code>outputStatusMessages</code> are reset (see {@link
   * #setOutputStatusMessages(String[])}).
   */
  public void setOutputStatus(int status) {
    this.outputStatus= status;
    this.outputStatusMessages= null;
  }

  /**
   * Returns the <code>outputStatus</code> code.
   */ 
 public int getOutputStatus() {
    return outputStatus;
  }

  /**
   * Associates additional <code>Strings</code> (which should help explain
   * the status) with the last <code>outputStatus</code> that was set.
   */ 
  public void setOutputStatusMessages(String[] msgs) {
    outputStatusMessages= msgs;
  }

  /**
   * Returns additional <code>Strings</code> which may help explain
   * the outputStatus, or <code>null</code> if no such messages have
   * been set.
   */ 
  public String[] getOutputStatusMessages() {
    return outputStatusMessages;
  }

  /**
   * Returns a {@link Date} Object which represents the HTTP
   * <code>Expire</code> time associated with the {@link
   * net.nutch.net.Http.Response} ({see @link #getResponse()}.  The
   * expire time is set from the response, when {@link
   * #setResponse(Http.Response)} is called.
   */
  public Date getExpireTime() {
    return expireTime;
  }

  /**
   * Sets the cached InetAddress for the host this request belongs to.
   */
  public void setAddr(InetAddress addr) {
    this.addr= addr;
  }

  /**
   * Gets the cached InetAddress for the host this request belongs to.
   * This returns <code>null</code> if it has not been set (or has 
   * previously been set to <code>null</code>).
   */
  public InetAddress getAddr() {
    return addr;
  }

  /**
   * Returns the immediate parent request of this
   * <code>RequestRecord</code>, or <code>null</code> if this request
   * is not the result of a redirect.
   */
  public RequestRecord getParentRequest() {
    return redirectedFrom;
  }

  /**
   * Returns the original RequestRecord that lead to this request (via
   * redirects).  If this request was not triggered by redirects,
   * <code>this</code> will be returned.
   */
  public RequestRecord getOriginalRequest() {
    RequestRecord tmp= this;
    while (tmp.redirectedFrom != null)
      tmp= tmp.redirectedFrom;
    return tmp;
  }

  /**
   *  If set to <code>true</code>, the {@link HostQueue} associated
   *  with this RequestRecord will be notified when the request is
   *  finished.  This defaults to <code>false</code> when a
   *  RequestRecord is first created.
   */ 
  public void setNotifyQueue(boolean notify) {
    this.hostQueueWantsNotification= notify;
  }

  /**
   *  Returns <code>true</code> if the {@link HostQueue} associated
   *  with this RequestRecord will be notified when the request is
   *  finished.  This defaults to <code>false</code> when a
   *  RequestRecord is first created.
   */ 
  public boolean getNotifyQueue() {
    return hostQueueWantsNotification;
  }

  /**
   * Propagates results (<code>hasFailed</code>,
   * <code>response</code>, <code>failureReason</code>, and
   * <code>expireTime</code>) to any parent requests.
   */
  private void updateParent() {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.finer("updating parent of " + url);
    }
    if (redirectedFrom != null) {
      redirectedFrom.hasFailed= hasFailed;
      redirectedFrom.response= response;
      redirectedFrom.failureReason= failureReason;
      redirectedFrom.expireTime= expireTime;
    }
  }

  /**
   * Calls {@link HostQueue#requestCompleted(RequestRecord)} on all
   * {@link HostQueue} Objects that are waiting for notification on this
   * request or any parent requests.
   */
  public void notifyQueuesOfCompletion() {
    updateParent();
    if (hostQueueWantsNotification) {
      if (LOG.isLoggable(Level.FINER)) {
        LOG.finer("notifying " + hostQueue.getKey() + " about " + url);
        LOG.finer("   orig url was " + getOriginalURL() );
      }
      hostQueue.requestCompleted(this);
    }
    
    if (redirectedFrom != null) 
      redirectedFrom.notifyQueuesOfCompletion();
    
    if (LOG.isLoggable(Level.FINE))
      LOG.fine("notified queues about " + url);
  }

}
