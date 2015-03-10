/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.util.LogFormatter;
import net.nutch.util.NutchConf;
import net.nutch.util.StringUtil;

import java.net.URL;
import java.util.Date;
import java.util.HashSet;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;


/**
 * Utility class for reporting events to the Fetcher log, and for
 * generating summary reports based on event counts.
 *
 */
public final class FetcherStatus implements FetcherConstants {

  public static final Logger LOG=
    LogFormatter.getLogger("net.nutch.fetcher.FetcherStatus");

  private static final boolean LOG_SUCCESS=
    NutchConf.getBoolean("fetcher.trace.success", false);

  private static final boolean LOG_NOT_FOUND=
    NutchConf.getBoolean("fetcher.trace.not.found", false);

  private static final boolean USE_LONG_TRACE_MSGS=
    NutchConf.getBoolean("fetcher.trace.longmsg", false);

  private static final String NEWLINE_STRING=
    System.getProperty("line.separator");

  private static final String URL_UNKNOWN= "url_unknown";

  private static final long SECONDS_TO_MS_MULTIPLIER= 1000;

  /** 
   * The <code>String</code> length of the longest MISC_* constant's
   * prettyName.
   */
  public static final int MISC_PRETTY_NAME_MAX_LEN;

  /** 
   * The <code>String</code> length of the longest FAIL_* constant's
   * prettyName.
   */
  public static final int FAILURE_PRETTY_NAME_MAX_LEN;

  /** 
   * The <code>String</code> length of the longest ERR_* constant's
   * prettyName.
   */
  public static final int ERROR_PRETTY_NAME_MAX_LEN;

  /** 
   * The <code>String</code> length of the longest OUT_* constant's
   * prettyName.
   */
  public static final int OUT_PRETTY_NAME_MAX_LEN;

  static {
    int max= 0;
    for (int i= 0; i < NUM_MISC_CODES; i++)
      if (getMiscInfoPrettyName(i).length() > max)
        max= getMiscInfoPrettyName(i).length();
    MISC_PRETTY_NAME_MAX_LEN= max;
  }

  static {
    int max= 0;
    for (int i= 0; i < NUM_FAIL_REASONS; i++)
      if (getFailurePrettyName(i).length() > max)
        max= getFailurePrettyName(i).length();
    FAILURE_PRETTY_NAME_MAX_LEN= max;
  }

  static {
    int max= 0;
    for (int i= 0; i < NUM_ERR_REASONS; i++)
      if (getErrorPrettyName(i).length() > max)
        max= getErrorPrettyName(i).length();
    ERROR_PRETTY_NAME_MAX_LEN= max;
  }

  static {
    int max= 0;
    for (int i= 0; i < NUM_OUT_STATUS; i++)
      if (getOutputStatusPrettyName(i).length() > max)
        max= getOutputStatusPrettyName(i).length();
    OUT_PRETTY_NAME_MAX_LEN= max;
  }

  private long requestsIssued;
  private long fetchListRequestsIssued;
  private long robotsRequestsIssued;
  private long http11RequestsIssued;

  private long requestRetries;
  private long fetchListRequestRetries;
  private long robotsRequestRetries;

  private long requestRedirects;
  private long fetchListRequestRedirects;
  private long robotsRequestRedirects;

  private long requestsSucceeded;
  private long fetchListRequestsSucceeded;
  private long robotsRequestsSucceeded;

  private long[] requestsFailedByReason= 
    new long[NUM_FAIL_REASONS];
  private long[] fetchListRequestsFailedByReason= 
    new long[NUM_FAIL_REASONS];
  private long[] robotsRequestsFailedByReason= 
    new long[NUM_FAIL_REASONS];

  private long[] requestErrorsByReason= 
    new long[NUM_ERR_REASONS];
  private long[] fetchListRequestErrorsByReason= 
    new long[NUM_ERR_REASONS];
  private long[] robotsRequestErrorsByReason= 
    new long[NUM_ERR_REASONS];

  private long[] outputStatusCounts= 
    new long[NUM_OUT_STATUS];

  private long getRequestAttempts;
  private long getRequestAllBusy;
  private long getRequestThrottled;
  private long getRequestFoundExcluded;
  private long getRequestFoundNotReady;
  private long getRequestSuccesses;

  private long outputQueueAdds;
  private long outputQueueAdded;
  private long outputQueueAddDelays;

  private long outputQueuePopAttempts;
  private long outputQueuePopped;
  private long outputQueuePopNoDelay;
  private long outputQueueEmpty;

  private long bytesFetched;
  private long bytesTransferred;
  private long fetchListBytesFetched;
  private long robotsBytesFetched;

  private long requestsReadFromFetchList;

  private long droppedOnFloor;

  private long rawBytesSent;
  private long rawBytesRecieved;

  private long numCompressedTransfers;
  private long numContinues;

  private long startTime;
  private long endTime;

  public FetcherStatus() {
    reset();
  }

  public void reset() {
    requestsIssued= 0;
    fetchListRequestsIssued= 0;
    robotsRequestsIssued= 0;
    http11RequestsIssued= 0;

    requestRetries= 0;
    fetchListRequestRetries= 0;
    robotsRequestRetries= 0;

    requestRedirects= 0;
    fetchListRequestRedirects= 0;
    robotsRequestRedirects= 0;

    requestsSucceeded= 0;
    fetchListRequestsSucceeded= 0;
    robotsRequestsSucceeded= 0;

    for (int i= 0; i < NUM_FAIL_REASONS; i++) {
      requestsFailedByReason[i]= 0;
      fetchListRequestsFailedByReason[i]= 0;
      robotsRequestsFailedByReason[i]= 0;
    }

    for (int i= 0; i < NUM_ERR_REASONS; i++) {
      requestErrorsByReason[i]= 0;
      fetchListRequestErrorsByReason[i]= 0;
      robotsRequestErrorsByReason[i]= 0;
    }

    for (int i= 0; i < NUM_OUT_STATUS; i++) 
      outputStatusCounts[i]= 0;

    getRequestAttempts= 0;
    getRequestAllBusy= 0;
    getRequestThrottled= 0;
    getRequestFoundExcluded= 0;
    getRequestFoundNotReady= 0;
    getRequestSuccesses= 0;

    outputQueueAdds= 0;
    outputQueueAdded= 0;
    outputQueueAddDelays= 0;
    outputQueuePopAttempts= 0;
    outputQueuePopped= 0;
    outputQueuePopNoDelay= 0;
    outputQueueEmpty= 0;

    bytesFetched= 0;
    bytesTransferred= 0;
    fetchListBytesFetched= 0;
    robotsBytesFetched= 0;

    requestsReadFromFetchList= 0;

    droppedOnFloor= 0;

    rawBytesSent= 0;
    rawBytesRecieved= 0;

    numCompressedTransfers= 0;
    numContinues= 0;

    startTime= new Date().getTime();
    endTime= -1;
  }

  void dispatchingToFetcherThread(RequestRecord request) {
    requestsIssued++;
    if (request.isRobotsRequest()) 
      robotsRequestsIssued++;
    else 
      fetchListRequestsIssued++;
  }

  void readFromFetchlist() {
    requestsReadFromFetchList++;
  }

  void requestFailed(RequestRecord request) {
    logTraceReqFailure(request);
    requestsFailedByReason[request.getFailureReason()]++;
    if (request.isRobotsRequest()) 
      robotsRequestsFailedByReason[request.getFailureReason()]++;
    else 
      fetchListRequestsFailedByReason[request.getFailureReason()]++;
  }

  void requestError(RequestRecord request) {
    logTraceReqError(request);
    requestErrorsByReason[request.getErrorReason()]++;
    if (request.isRobotsRequest()) 
      robotsRequestErrorsByReason[request.getErrorReason()]++;
    else 
      fetchListRequestErrorsByReason[request.getErrorReason()]++;
  }

  void succeeded(RequestRecord request) {

    if (LOG_SUCCESS) 
      logTraceMisc(MISC_FETCH_SUCCESS, 
                   request.getOriginalRequest().getURLString());

    long bytes= request.getResponse().getContent().length;
    long tBytes= (request.getResponse().getCompressedContent() != null) ?
      request.getResponse().getCompressedContent().length
      : request.getResponse().getContent().length;

    requestsSucceeded++;
    bytesFetched+= bytes;
    bytesTransferred+= tBytes;

    if (request.isRobotsRequest()) {
      robotsRequestsSucceeded++;
      robotsBytesFetched+= bytes;
    } else {
      fetchListRequestsSucceeded++;
      fetchListBytesFetched+= bytes;
    }
  }

  void retry(RequestRecord request) {
    requestRetries++;
    if (request.isRobotsRequest())
      robotsRequestRetries++;
    else
      fetchListRequestRetries++;
  }

  void redirected(RequestRecord request) {
    requestRedirects++;
    if (request.isRobotsRequest())
      robotsRequestRedirects++;
    else
      fetchListRequestRedirects++;
  }

  void droppedOnFloor(RequestRecord request) {
    droppedOnFloor++;
  }

  void incrementOutputQueueAdd(int numAdded) {
    outputQueueAdds++;
    outputQueueAdded+= numAdded;
  }

  void incrementOutputQueueFull() {
    outputQueueAddDelays++;
  }

  void incrementOutputQueuePopNoDelay() {
    outputQueuePopAttempts++;
    outputQueuePopNoDelay++;
  }

  void incrementOutputQueuePopped() {
    outputQueuePopped++;
  }
  
  void incrementOutputQueueEmpty() {
    outputQueuePopAttempts++;
    outputQueueEmpty++;
  }

  void outputStatus(RequestRecord request, String urlString) {
    logTraceOutputStatus(request, urlString);
    outputStatusCounts[request.getOutputStatus()]++;
  }

  void incrementGetRequestAttempts() {
    getRequestAttempts++;
  }

  void incrementGetRequestAllBusy() {
    getRequestAllBusy++;
  }

  void incrementGetRequestThrottled() {
    getRequestThrottled++;
  }

  void incrementGetRequestSuccesses() {
    getRequestSuccesses++;  
  }

  void incrementGetRequestFoundExcluded() {
    getRequestFoundExcluded++;  
  }

  void incrementGetRequestFoundNotReady() {
    getRequestFoundNotReady++;  
  }

  void incrementRawBytes(long sent, long received) {
    rawBytesSent+= sent;
    rawBytesRecieved+= received;
  }

  void incrementContinues(int continues) {
    numContinues+= continues;
  }

  public void logStats() {
    int code= MISC_STATS;
    long end= endTime;
    if (end == -1) 
      end= new Date().getTime();
    long totSecs= (end - startTime) / SECONDS_TO_MS_MULTIPLIER;
    long totMinutes= totSecs / 60;
    long hours= totSecs / 3600;
    long partialMinutes= (totSecs % 3600) / 60;
    long partialSecs= (totSecs % 3600) % 60;

    long tmp;

    if (totSecs == 0) 
      totSecs= 1;

    if (totMinutes == 0) 
      totMinutes= 1;

    StringBuffer buf= new StringBuffer();

    if (endTime == -1) 
      buf.append("RequestScheduler running for ");
    else 
      buf.append("Diff stats over ");

    if (hours > 0)
      buf.append(hours).append(":");

    if (partialMinutes < 10) 
      buf.append("0");
    buf.append(partialMinutes).append(":");

    if (partialSecs < 10) 
      buf.append("0");
    buf.append(partialSecs);

    buf.append(" (");
    buf.append(totSecs).append(" seconds)");

    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    if (endTime != -1) {
      buf.append(" from ").append(new Date(startTime).toString());
      buf.append(" to ").append(new Date(endTime).toString());
      FetcherStatus.logTraceMisc(code, buf.toString());
      buf.setLength(0);
    }

    // Requests
    buf.append(" Requests (rate): ").append(requestsIssued);
    buf.append("\t(").append(requestsIssued/totSecs).append(" req/sec)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);
        
    buf.append("       fetchList: ").append(fetchListRequestsIssued);
    buf.append("\t(").append(fetchListRequestsIssued/totSecs);
    buf.append(" req/sec)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("      robots.txt: ").append(robotsRequestsIssued);
    buf.append("\t(").append(robotsRequestsIssued/totSecs);
    buf.append(" req/sec)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    // Retries
    tmp= requestsIssued == 0 ? 0 : (requestRetries * 100 / requestsIssued);
    buf.append("  Retries (rate): ").append(requestRetries);
    buf.append("\t(").append(tmp);
    buf.append("% retry/tot)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= fetchListRequestsIssued == 0 ? 
      0 : (fetchListRequestRetries * 100 / fetchListRequestsIssued);
    buf.append("       fetchList: ").append(fetchListRequestRetries);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= robotsRequestsIssued == 0 ?
      0 : (robotsRequestRetries * 100 / robotsRequestsIssued);
    buf.append("      robots.txt: ").append(robotsRequestRetries);
    buf.append("\t(").append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    // Redirects
    tmp= requestsIssued == 0 ?
      0 : (requestRedirects * 100 / requestsIssued);
    buf.append("Redirects (rate): ").append(requestRedirects);
    buf.append("\t(").append(tmp);
    buf.append("% redirect/tot)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= fetchListRequestsIssued == 0 ?
      0 : (fetchListRequestRedirects * 100 / fetchListRequestsIssued);
    buf.append("       fetchList: ").append(fetchListRequestRedirects);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= robotsRequestsIssued == 0 ?
      0 : (robotsRequestRedirects * 100 / robotsRequestsIssued);
    buf.append("      robots.txt: ").append(robotsRequestRedirects);
    buf.append("\t(").append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    // Succeeded
    tmp= requestsIssued == 0 ?
      0 : (requestsSucceeded * 100 / requestsIssued);
    buf.append("Succeeded (rate): ").append(requestsSucceeded);
    buf.append("\t(").append(tmp);
    buf.append("% succ/req)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= fetchListRequestsIssued == 0 ?
      0 : (fetchListRequestsSucceeded * 100 / fetchListRequestsIssued);
    buf.append("       fetchList: ").append(fetchListRequestsSucceeded);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= robotsRequestsIssued == 0 ?
      0 : (robotsRequestsSucceeded * 100 / robotsRequestsIssued);
    buf.append("      robots.txt: ").append(robotsRequestsSucceeded);
    buf.append("\t(").append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    // Failures
    long allFail= 0;
    long fetchListAllFail= 0;
    long robotsAllFail= 0;

    for (int i= 0; i < NUM_FAIL_REASONS; i++) {
      allFail+= requestsFailedByReason[i];
      fetchListAllFail+= fetchListRequestsFailedByReason[i];
      robotsAllFail+= robotsRequestsFailedByReason[i];
    }

    // Failures
    buf.append("Failures (not retryable):");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("                     \t");
    buf.append("All \tfetchList\trobots");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    for (int errCode= 0;
         errCode < NUM_FAIL_REASONS; 
         errCode++) {

      String errDesc= StringUtil.rightPad(getFailurePrettyName(errCode), 
                                          FAILURE_PRETTY_NAME_MAX_LEN);
      buf.append(errDesc).append("\t");
      buf.append(requestsFailedByReason[errCode]).append("\t");
      buf.append(fetchListRequestsFailedByReason[errCode]).append("\t");
      buf.append(robotsRequestsFailedByReason[errCode]);
      FetcherStatus.logTraceMisc(code, buf.toString());
      buf.setLength(0);
    }
                
    buf.append(
      StringUtil.rightPad("Total", FAILURE_PRETTY_NAME_MAX_LEN));
    buf.append("\t");
    buf.append(allFail).append("\t");
    buf.append(fetchListAllFail).append("\t");
    buf.append(robotsAllFail);
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    // Errors
    long allErr= 0;
    long fetchListAllErr= 0;
    long robotsAllErr= 0;

    for (int i= 0; i < NUM_ERR_REASONS; i++) {
      allErr+= requestErrorsByReason[i];
      fetchListAllErr+= fetchListRequestErrorsByReason[i];
      robotsAllErr+= robotsRequestErrorsByReason[i];
    }

    // Errors
    buf.append("Errors (retryable):");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);
    buf.append("                   \t");
    buf.append("All \tfetchList\trobots");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    for (int errCode= 0; errCode < NUM_ERR_REASONS; errCode++) {

      String errDesc= StringUtil.rightPad(getErrorPrettyName(errCode), 
                                          ERROR_PRETTY_NAME_MAX_LEN);
      buf.append(errDesc).append("\t");
      buf.append(requestErrorsByReason[errCode]).append("\t");
      buf.append(fetchListRequestErrorsByReason[errCode]).append("\t");
      buf.append(robotsRequestErrorsByReason[errCode]);
      FetcherStatus.logTraceMisc(code, buf.toString());
      buf.setLength(0);
    }
                
    buf.append(StringUtil.rightPad("Total", ERROR_PRETTY_NAME_MAX_LEN));
    buf.append("\t");
    buf.append(allErr).append("\t");
    buf.append(fetchListAllErr).append("\t");
    buf.append(robotsAllErr); 
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    // Output
    long allOut= 0;

    for (int i= 0; i < NUM_OUT_STATUS; i++) {
      allOut+= outputStatusCounts[i];
    }

    // Output
    buf.append("Output stats:");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    for (int statusCode= 0; statusCode < NUM_OUT_STATUS; statusCode++) {

      String statDesc= StringUtil.rightPad(
        getOutputStatusPrettyName(statusCode), 
        OUT_PRETTY_NAME_MAX_LEN);
      buf.append(statDesc).append("\t");
      buf.append(outputStatusCounts[statusCode]);
      FetcherStatus.logTraceMisc(code, buf.toString());
      buf.setLength(0);
    }
                
    buf.append(
      StringUtil.rightPad("Total", OUT_PRETTY_NAME_MAX_LEN));
    buf.append("\t");
    buf.append(allOut);
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);
                
    // getRequests
    buf.append("Fetcher polling (all but Succeed cause delays):");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("           Polls: ");
    buf.append(getRequestAttempts);
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= getRequestAttempts == 0 ?
      0 : (getRequestSuccesses * 100 / getRequestAttempts);
    buf.append("       Succeeded: ");
    buf.append(getRequestSuccesses);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= getRequestAllBusy == 0 ?
      0 : (getRequestAllBusy * 100 / getRequestAttempts);
    buf.append("    Host Qs Busy: ");
    buf.append(getRequestAllBusy);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    // outputQueue
    tmp= outputQueueAdds == 0 ?
      0 : (outputQueueAddDelays / outputQueueAdds);
    buf.append("Fetcher delays due to Output Q Full: ");
    buf.append(outputQueueAddDelays);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= outputQueueAdds == 0 ?
      0 : (outputQueueAdded * 100 / outputQueueAdds);
    buf.append("Requests added to Output Q (per add): ");
    buf.append(outputQueueAdded);
    buf.append("\t(");
    buf.append( (float) tmp / 100.0f );
    buf.append(")");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("Output polling:");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);
    buf.append("           Polls: ");
    buf.append(outputQueuePopAttempts);
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("            Pops: ");
    buf.append(outputQueuePopped);
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= outputQueuePopAttempts == 0 ?
      0 : (outputQueuePopNoDelay * 100 / outputQueuePopAttempts);
    buf.append("  Pops w/o delay: ");
    buf.append(outputQueuePopNoDelay);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= outputQueuePopAttempts == 0 ?
      0 : (outputQueueEmpty * 100 / outputQueuePopAttempts);
    buf.append("  Output Q empty: ");
    buf.append(outputQueueEmpty);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);
                
    // content bandwidth
    buf.append("actual content bytes fetched: ");
    buf.append(bytesTransferred);
    buf.append("\t(");
    buf.append(bytesTransferred * 8 / 1024 / totSecs);
    buf.append(" kbits/s avg)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("effective content bytes:      ");
    buf.append(bytesFetched);
    buf.append("\t(");
    buf.append(bytesFetched * 8 / 1024 / totSecs);
    buf.append(" kbits/s)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= bytesFetched == 0 ? 
      0 : ((bytesFetched - bytesTransferred) * 1000) / bytesFetched;

    buf.append("content bandwidth savings (compression): ");
    buf.append(bytesFetched - bytesTransferred);
    buf.append("\t(");
    buf.append( ( (float) tmp ) / 10.0f);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= bytesFetched == 0 ?
      0 : (fetchListBytesFetched * 100 / bytesFetched);
    buf.append("effective fetchlist bytes fetched: ");
    buf.append(fetchListBytesFetched);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    tmp= bytesFetched == 0 ?
      0 : (robotsBytesFetched * 100 / bytesFetched);
    buf.append("effective robots bytes fetched:    ");
    buf.append(robotsBytesFetched);
    buf.append("\t(");
    buf.append(tmp);
    buf.append("%)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("raw bytes read: ");
    buf.append(rawBytesRecieved);
    buf.append("\t(");
    buf.append(rawBytesRecieved * 8 / 1024 / totSecs);
    buf.append(" kbits/s)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append("raw bytes sent: ");
    buf.append(rawBytesSent);
    buf.append("\t(");
    buf.append(rawBytesSent * 8 / 1024 / totSecs);
    buf.append(" kbits/s)");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append(requestsReadFromFetchList);
    buf.append(" requests have been read from the FetchList");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append(outputQueuePopped);
    buf.append(" requests have been dispatched for output");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

    buf.append(droppedOnFloor);
    buf.append(" requests have been dropped on the floor");
    FetcherStatus.logTraceMisc(code, buf.toString());
    buf.setLength(0);

  }

  /**
   * Returns an approximation of the raw bandwidth used, in kbits/s,
   * for the period of time this FetcherStatus was measured over.
   *
   * <p>
   *
   * This is meant to include all bandwidth the fetcher used except
   * for DNS and TCP/IP overhead.
   */
  public int getRawBandwidth() {
    long end= endTime;
    if (end == -1)
      end= new Date().getTime();

    long totSecs= (end - startTime) / SECONDS_TO_MS_MULTIPLIER;
    if (totSecs == 0)
      totSecs= 1;

    return (int) (rawBytesRecieved * 8 / 1024 / totSecs);
  }

  /**
   * Returns a copy of this FetcherStatus object that reflects a
   * "snapshot"; it is identical with the possible exception of the
   * "end time", which is set to the current time if <code>this</code>
   * does not have an end time.
   *
   * <p>
   *
   * It is assumed that the returned object will never be updated.
   */ 
  public FetcherStatus cloneStatus() {
    FetcherStatus other= new FetcherStatus();

    other.requestsIssued= this.requestsIssued;
    other.fetchListRequestsIssued= this.fetchListRequestsIssued;
    other.robotsRequestsIssued= this.robotsRequestsIssued;
    other.http11RequestsIssued= this.http11RequestsIssued;

    other.requestRetries= this.requestRetries;
    other.fetchListRequestRetries= this.fetchListRequestRetries;
    other.robotsRequestRetries= this.robotsRequestRetries;

    other.requestRedirects= this.requestRedirects;
    other.fetchListRequestRedirects= this.fetchListRequestRedirects;
    other.robotsRequestRedirects= this.robotsRequestRedirects;

    other.requestsSucceeded= this.requestsSucceeded;
    other.fetchListRequestsSucceeded= this.fetchListRequestsSucceeded;
    other.robotsRequestsSucceeded= this.robotsRequestsSucceeded;

    for (int i= 0; i < NUM_FAIL_REASONS; i++) {
      other.requestsFailedByReason[i]= 
        this.requestsFailedByReason[i]; 
      other.fetchListRequestsFailedByReason[i]= 
        this.fetchListRequestsFailedByReason[i];
      other.robotsRequestsFailedByReason[i]= 
        this.robotsRequestsFailedByReason[i];
    }

    for (int i= 0; i < NUM_ERR_REASONS; i++) {
      other.requestErrorsByReason[i]=
        this.requestErrorsByReason[i];
      other.fetchListRequestErrorsByReason[i]=
        this.fetchListRequestErrorsByReason[i]; 
      other.robotsRequestErrorsByReason[i]=
        this.robotsRequestErrorsByReason[i];
    }

    for (int i= 0; i < NUM_OUT_STATUS; i++) {
      other.outputStatusCounts[i]= this.outputStatusCounts[i];
    }

    other.getRequestAttempts= this.getRequestAttempts;
    other.getRequestAllBusy= this.getRequestAllBusy;
    other.getRequestThrottled= this.getRequestThrottled;
    other.getRequestFoundExcluded= this.getRequestFoundExcluded;
    other.getRequestFoundNotReady= this.getRequestFoundNotReady;
    other.getRequestSuccesses= this.getRequestSuccesses;
   
    other.outputQueueAdds= this.outputQueueAdds;
    other.outputQueueAdded= this.outputQueueAdded;
    other.outputQueueAddDelays= this.outputQueueAddDelays;

    other.outputQueuePopAttempts= this.outputQueuePopAttempts;
    other.outputQueuePopped= this.outputQueuePopped;
    other.outputQueuePopNoDelay= this.outputQueuePopNoDelay;
    other.outputQueueEmpty= this.outputQueueEmpty;

    other.bytesFetched= this.bytesFetched;
    other.bytesTransferred= this.bytesTransferred;
    other.fetchListBytesFetched= this.fetchListBytesFetched;
    other.robotsBytesFetched= this.robotsBytesFetched;

    other.requestsReadFromFetchList= this.requestsReadFromFetchList;

    other.droppedOnFloor= this.droppedOnFloor;
   
    other.rawBytesSent= this.rawBytesSent;
    other.rawBytesRecieved= this.rawBytesRecieved;

    other.numCompressedTransfers= this.numCompressedTransfers;
    other.numContinues= this.numContinues;

    other.startTime= this.startTime;
    if (endTime == -1)
      other.endTime= new Date().getTime();
    else
      other.endTime= endTime;

    return other;
  }

  /**
   * Returns a FetcherStatus object that reflects a "delta" 
   * between this status, and an <code>earlierStatus</code>.
   *
   * <p>
   *
   * Almost all fields are simply set to <code>(this.field -
   * earlierStatus.field)</code>.  The exception being the time
   * boundaries- the start time value is taken from
   * <code>earlierStatus</code>'s end time and the end time is taken
   * from <code>this</code>'s end time.
   *
   * <p>
   *
   * For the resulting FetcherStatus to be meaningful, the start times
   * of <code>this</code> and <code>earlierStatus</code> should be the
   * same, but this is not enforced.
   */ 
  public FetcherStatus getDelta(FetcherStatus earlierStatus) {
    FetcherStatus delta= new FetcherStatus();

    delta.requestsIssued= 
      this.requestsIssued - earlierStatus.requestsIssued;
    delta.fetchListRequestsIssued= 
      this.fetchListRequestsIssued - earlierStatus.fetchListRequestsIssued;
    delta.robotsRequestsIssued= 
      this.robotsRequestsIssued - earlierStatus.robotsRequestsIssued;
    delta.http11RequestsIssued= 
      this.http11RequestsIssued - earlierStatus.http11RequestsIssued;

    delta.requestRetries= 
      this.requestRetries - earlierStatus.requestRetries;
    delta.fetchListRequestRetries= 
      this.fetchListRequestRetries - earlierStatus.fetchListRequestRetries;
    delta.robotsRequestRetries= 
      this.robotsRequestRetries - earlierStatus.robotsRequestRetries;

    delta.requestRedirects= 
      this.requestRedirects - earlierStatus.requestRedirects;
    delta.fetchListRequestRedirects= 
      this.fetchListRequestRedirects - earlierStatus.fetchListRequestRedirects;
    delta.robotsRequestRedirects= 
      this.robotsRequestRedirects - earlierStatus.robotsRequestRedirects;

    delta.requestsSucceeded= 
      this.requestsSucceeded - earlierStatus.requestsSucceeded;
    delta.fetchListRequestsSucceeded= 
      this.fetchListRequestsSucceeded - earlierStatus.fetchListRequestsSucceeded;
    delta.robotsRequestsSucceeded= 
      this.robotsRequestsSucceeded - earlierStatus.robotsRequestsSucceeded;

    for (int i= 0; i < NUM_FAIL_REASONS; i++) {
      delta.requestsFailedByReason[i]= 
        this.requestsFailedByReason[i] - earlierStatus.requestsFailedByReason[i];
      delta.fetchListRequestsFailedByReason[i]= 
        this.fetchListRequestsFailedByReason[i] - earlierStatus.fetchListRequestsFailedByReason[i];
      delta.robotsRequestsFailedByReason[i]= 
        this.robotsRequestsFailedByReason[i] - earlierStatus.robotsRequestsFailedByReason[i];
    }

    for (int i= 0; i < NUM_ERR_REASONS; i++) {
      delta.requestErrorsByReason[i]= 
        this.requestErrorsByReason[i] - earlierStatus.requestErrorsByReason[i];
      delta.fetchListRequestErrorsByReason[i]= 
        this.fetchListRequestErrorsByReason[i] - earlierStatus.fetchListRequestErrorsByReason[i];
      delta.robotsRequestErrorsByReason[i]= 
        this.robotsRequestErrorsByReason[i] - earlierStatus.robotsRequestErrorsByReason[i];
    }

    for (int i= 0; i < NUM_OUT_STATUS; i++) {
      delta.outputStatusCounts[i]= 
        this.outputStatusCounts[i] - earlierStatus.outputStatusCounts[i];
    }

    delta.getRequestAttempts= 
      this.getRequestAttempts - earlierStatus.getRequestAttempts;
    delta.getRequestAllBusy= 
      this.getRequestAllBusy - earlierStatus.getRequestAllBusy;
    delta.getRequestThrottled= 
      this.getRequestThrottled - earlierStatus.getRequestThrottled;
    delta.getRequestFoundExcluded= 
      this.getRequestFoundExcluded - earlierStatus.getRequestFoundExcluded;
    delta.getRequestFoundNotReady= 
      this.getRequestFoundNotReady - earlierStatus.getRequestFoundNotReady;
    delta.getRequestSuccesses= 
      this.getRequestSuccesses - earlierStatus.getRequestSuccesses;

    delta.outputQueueAdds= 
      this.outputQueueAdds - earlierStatus.outputQueueAdds;
    delta.outputQueueAdded= 
      this.outputQueueAdded - earlierStatus.outputQueueAdded;
    delta.outputQueueAddDelays= 
      this.outputQueueAddDelays - earlierStatus.outputQueueAddDelays;

    delta.outputQueuePopAttempts= 
      this.outputQueuePopAttempts - earlierStatus.outputQueuePopAttempts;
    delta.outputQueuePopped= 
      this.outputQueuePopped - earlierStatus.outputQueuePopped;
    delta.outputQueuePopNoDelay= 
      this.outputQueuePopNoDelay - earlierStatus.outputQueuePopNoDelay;
    delta.outputQueueEmpty= 
      this.outputQueueEmpty - earlierStatus.outputQueueEmpty;

    delta.bytesFetched= 
      this.bytesFetched - earlierStatus.bytesFetched;
    delta.bytesTransferred= 
      this.bytesTransferred - earlierStatus.bytesTransferred;
    delta.fetchListBytesFetched= 
      this.fetchListBytesFetched - earlierStatus.fetchListBytesFetched;
    delta.robotsBytesFetched= 
      this.robotsBytesFetched - earlierStatus.robotsBytesFetched;

    delta.requestsReadFromFetchList= 
      this.requestsReadFromFetchList - earlierStatus.requestsReadFromFetchList;

    delta.droppedOnFloor= 
      this.droppedOnFloor - earlierStatus.droppedOnFloor;

    delta.rawBytesSent= 
      this.rawBytesSent - earlierStatus.rawBytesSent;
    delta.rawBytesRecieved= 
      this.rawBytesRecieved - earlierStatus.rawBytesRecieved;

    delta.numCompressedTransfers= 
      this.numCompressedTransfers - earlierStatus.numCompressedTransfers;
    delta.numContinues= 
      this.numContinues - earlierStatus.numContinues;

    delta.startTime= earlierStatus.endTime;

    if (endTime == -1) 
      delta.endTime= new Date().getTime();
    else 
      delta.endTime= endTime;

    return delta;
  }

  /**
   * Logs a mapping of "terse names" to "pretty names" for all
   * error/failure/status messages.
   */
  public static void logKeys() {
    HashSet dedup= new HashSet();

    logTraceMisc(MISC_KEY, "Fetch Error Codes:");

    for (int i= 0; i < NUM_ERR_REASONS; i++) {

      if (dedup.contains(getErrorTerseName(i)))
        LOG.severe("duplicate terse name: " + getErrorTerseName(i));

      dedup.add(getErrorTerseName(i));

      logTraceMisc(MISC_KEY, getErrorTerseName(i) + "\t" +
                   getErrorPrettyName(i));

    }

    logTraceMisc(MISC_KEY, "Fetch Failure Codes:");
    for (int i= 0; i < NUM_FAIL_REASONS; i++) {

      if (dedup.contains(getFailureTerseName(i)))
        LOG.severe("duplicate terse name: " + getErrorTerseName(i));

      dedup.add(getFailureTerseName(i));

      logTraceMisc(MISC_KEY, getFailureTerseName(i) + "\t" 
                   + getFailurePrettyName(i));
    }

    logTraceMisc(MISC_KEY, "Fetch Output Error Codes:");
    for (int i= 0; i < NUM_OUT_STATUS; i++) {

      if (dedup.contains(getOutputStatusTerseName(i)))
        LOG.severe("duplicate terse name: " + getErrorTerseName(i));

      dedup.add(getOutputStatusTerseName(i));

      logTraceMisc(MISC_KEY, getOutputStatusTerseName(i) + "\t"
                   + getOutputStatusPrettyName(i));
    }

    logTraceMisc(MISC_KEY, "Fetch Misc Event Codes:");
    for (int i= 0; i < NUM_MISC_CODES; i++) {

      if (dedup.contains(getMiscInfoTerseName(i)))
        LOG.severe("duplicate terse name: " + getErrorTerseName(i));

      dedup.add(getMiscInfoTerseName(i));

      logTraceMisc(MISC_KEY, getMiscInfoTerseName(i) + "\t" +
                   getMiscInfoPrettyName(i));
    }
  }


  /**
   *  Prints a "trace" line to the log at <code>Level.INFO</code>.
   *  Only one of <code>longMsg</code> or <code>shortMsg</code> will
   *  be printed, followed by the <code>urlString</code> and the
   *  additional messages, if any.
   */
  public static void logTrace(String longMsg, String shortMsg, 
                              String urlString, String[] msgs) {
    String mainMsg= USE_LONG_TRACE_MSGS ? longMsg : shortMsg;

    if (urlString.startsWith("http://")) 
      urlString= urlString.substring(7);

    StringBuffer logMsg= new StringBuffer(mainMsg);
    logMsg.append(" ").append(urlString);
    if (msgs != null) 
      for (int i= 0; i < msgs.length; i++) 
        logMsg.append(" ").append(msgs[i]);

    LOG.info(logMsg.toString());
  }


  /**
   *  Prints a "trace" line to the log at <code>Level.INFO</code>.
   *  Only one of <code>longMsg</code> or <code>shortMsg</code> will
   *  be printed, followed by the <code>url</code> and the
   *  additional messages, if any.
   */
  public static void logTrace(String longMsg, String shortMsg, 
                              URL url, String[] msgs) {
    if (url != null)
      logTrace(longMsg, shortMsg, url.toString(), msgs);
    else 
      logTrace(longMsg, shortMsg, URL_UNKNOWN, msgs);
  }


  /**
   *  Prints a "trace" line to the log at <code>Level.INFO</code>.
   *  Only one of <code>longMsg</code> or <code>shortMsg</code> will
   *  be printed, followed by the <code>urlString</code>.
   */
  private static void logTrace(String longMsg, String shortMsg, 
                              String urlString) {
    logTrace(longMsg, shortMsg, urlString, null);
  }


  /**
   *  Prints a "trace" line to the log at <code>Level.INFO</code>.
   *  Only one of <code>longMsg</code> or <code>shortMsg</code> will
   *  be printed, followed by the <code>url</code>.
   */
  private static void logTrace(String longMsg, String shortMsg, 
                              URL url) {
    if (url != null)
      logTrace(longMsg, shortMsg, url.toString(), null);
    else 
      logTrace(longMsg, shortMsg, URL_UNKNOWN, null);
  }

  /**
   *  Prints an appropriate "trace" line to the log, reflecting the
   *  failure code in the supplied <code>request</code>.
   */
  public static void logTraceReqFailure(RequestRecord request) {
    int failCode= request.getFailureReason();

    if ( (failCode == FAIL_NOT_FOUND) && (!LOG_NOT_FOUND) )
      return;

    String longMsg= getFailurePrettyName(failCode);
    String shortMsg= getFailureTerseName(failCode);
    logTrace(longMsg, shortMsg, request.getOriginalRequest().getURLString(), 
             request.getFailureMessages());
  }

  /**
   *  Prints an appropriate "trace" line to the log, reflecting the
   *  error code in the supplied <code>request</code>.
   */
  public static void logTraceReqError(RequestRecord request) {
    int errCode= request.getErrorReason();
    String longMsg= getErrorPrettyName(errCode);
    String shortMsg= getErrorTerseName(errCode);
    logTrace(longMsg, shortMsg, request.getOriginalRequest().getURLString(), 
             request.getErrorMessages());
  }

  /**
   *  Prints an appropriate "trace" line to the log, reflecting the
   *  failure code in the supplied <code>request</code>.
   */
  public static void logTraceOutputStatus(RequestRecord request, 
                                          String urlString) {
    int statusCode= request.getOutputStatus();

    if ( (statusCode != OUT_OK) || LOG_SUCCESS) {
      String longMsg= getOutputStatusPrettyName(statusCode);
      String shortMsg= getOutputStatusTerseName(statusCode);
      logTrace(longMsg, shortMsg, urlString, 
               request.getOutputStatusMessages());
    }
  }

  /**
   *  Prints an appropriate "trace" line to the log, reflecting the
   *  miscellaneous event code (see the <code>MISC_*<code> constants)
   *  and the given URL.
   */
  public static void logTraceMisc(int miscCode, URL url) {
    String longMsg= getMiscInfoPrettyName(miscCode);
    String shortMsg= getMiscInfoTerseName(miscCode);
    logTrace(longMsg, shortMsg, url.toString());
  }

  /**
   *  Prints an appropriate "trace" line to the log, reflecting the
   *  miscellaneous event code (see the <code>MISC_*<code> constants)
   *  and the given message String.
   */
  public static void logTraceMisc(int miscCode, String msg) {
    String longMsg= getMiscInfoPrettyName(miscCode);
    String shortMsg= getMiscInfoTerseName(miscCode);
    logTrace(longMsg, shortMsg, msg);
  }

  private static String getMiscInfoPrettyName(int miscCode) {
    switch (miscCode) {
    case MISC_STATS:
      return "Stats";
    case MISC_KEY:
      return "Logging code key";
    case MISC_ROBOTS_FORBIDDEN:
      return "Robots Req Forbidden";
    case MISC_META_NOINDEX:
      return "META NOINDEX";
    case MISC_META_NOFOLLOW:
      return "META NOFOLLOW";
    case MISC_META_NOCACHE:
      return "META NOCACHE";
    case MISC_FETCH_SUCCESS:
      return "Successful Fetch";
    case MISC_INFORMATIONAL:
      return "Informational";
    default:
      return "*MISC: No Displayable Form* (" + miscCode + ")";
    }
  }

  private static String getMiscInfoTerseName(int miscCode) {
    switch (miscCode) {
    case MISC_STATS:
      return "STS";
    case MISC_KEY:
      return "KEY";
    case MISC_ROBOTS_FORBIDDEN:
      return "RFB";
    case MISC_META_NOINDEX:
      return "NIN";
    case MISC_META_NOFOLLOW:
      return "NFO";
    case MISC_META_NOCACHE:
      return "NCA";
    case MISC_FETCH_SUCCESS:
      return "SUC";
    case MISC_INFORMATIONAL:
      return "INF";
    default:
      return "MISC" + miscCode;
    }
  }


  /** 
   * Returns a displayable form for the <code>failureReason</code>.
   * The result is meant for human consumption.
   */ 
  public static String getFailurePrettyName(int failureReason) {
    switch (failureReason) {
    case FAIL_UNKNOWN: 
      return "Unknown Failure";
    case FAIL_BAD_URL:
      return "Bad URL";
    case FAIL_ROBOTS_EXCLUDED:
      return "Robots Excluded";
    case FAIL_TOO_MANY_ERRORS:
      return "Max Errors";
    case FAIL_TOO_MANY_REDIRECTS:
      return "Max Redirects";
    case FAIL_REDIRECT_MISSING_TARGET:
      return "Redirect Missing Target";
    case FAIL_NOT_FOUND:
      return "Not Found";
    case FAIL_FORBIDDEN:
      return "Forbidden";
    case FAIL_REDIRECT_LOOP_DETECTED:
      return "Redirect Loop";
    case FAIL_HOSTNAME_BANNED:
      return "Hostname Banned";
    case FAIL_DEAD_HOST:
      return "Dead Host";
    case FAIL_UNKNOWN_RESP_CODE:
      return "Unknown Response Code";
    case FAIL_UNKNOWN_HOST:
      return "Unknown Host";
    case FAIL_CONNECTION_REFUSED:
      return "Connection Refused";
    default:
      return "*FAIL: No Displayable Form* (" + failureReason + ")";
    }
  }

  /** 
   * Returns a terse displayable form for the
   * <code>failureReason</code>.  The result contains no whitespace
   * and is suitable for consumption by log analysers.
   */ 
  public static String getFailureTerseName(int failureReason) {
    switch (failureReason) {
    case FAIL_UNKNOWN: 
      return "UNF";
    case FAIL_BAD_URL:
      return "URL";
    case FAIL_ROBOTS_EXCLUDED:
      return "XCL";
    case FAIL_TOO_MANY_ERRORS:
      return "ERR";
    case FAIL_TOO_MANY_REDIRECTS:
      return "RDR";
    case FAIL_REDIRECT_MISSING_TARGET:
      return "TGT";
    case FAIL_NOT_FOUND:
      return "FND";
    case FAIL_FORBIDDEN:
      return "FBD";
    case FAIL_REDIRECT_LOOP_DETECTED:
      return "LOP";
    case FAIL_HOSTNAME_BANNED:
      return "BAN";
    case FAIL_DEAD_HOST:
      return "DED";
    case FAIL_UNKNOWN_RESP_CODE:
      return "COD";
    case FAIL_UNKNOWN_HOST:
      return "DNS";
    case FAIL_CONNECTION_REFUSED:
      return "REF";
    default:
      return "FAIL"+failureReason;
    }
  }

  /** 
   * Returns a displayable form for the <code>errorReason</code>.
   * The result is meant for human consumption.
   */ 
  public static String getErrorPrettyName(int errorReason) {
    switch (errorReason) {
    case ERR_UNKNOWN: 
      return "Unknown Error";
    case ERR_CONNECTION_TIMED_OUT:
      return "Connection Timed Out";
    case ERR_BAD_HEADER_LINE:
      return "Bad Header Line";
    case ERR_RESET_BY_PEER:
      return "Reset By Peer";
    case ERR_BAD_STATUS_LINE:
      return "Bad Status Line";
    case ERR_EOF_DURING_READ:
      return "EOF During Read";
    case ERR_NO_ROUTE:
      return "No Route to Host";
    case ERR_SOCKET_TIMEOUT:
      return "Socket Timeout";
    case ERR_NETWORK_UNREACHABLE:
      return "Network Unreachable";
    case ERR_BAD_CONTENT_LENGTH:
      return "Bad Content-Length";
    case ERR_CHUNKLEN_PARSE:
      return "Bad Chunk Length";
    case ERR_CHUNK_EOF:
      return "EOF in chunk";
    case ERR_DECOMPRESS:
      return "Unzip Failed ";
    default:
      return "*ERR: No Displayable Form* (" + errorReason + ")";
    }
  }

  /** 
   * Returns a terse displayable form for the
   * <code>errorReason</code>.  The result contains no whitespace
   * and is suitable for consumption by log analysers.
   */ 
  public static String getErrorTerseName(int errorReason) {
    switch (errorReason) {
    case ERR_UNKNOWN:
      return "UNE";
    case ERR_CONNECTION_TIMED_OUT:
      return "CTO";
    case ERR_BAD_HEADER_LINE:
      return "HED";
    case ERR_RESET_BY_PEER:
      return "RST";
    case ERR_BAD_STATUS_LINE:
      return "STT";
    case ERR_EOF_DURING_READ:
      return "EOF";
    case ERR_NO_ROUTE:
      return "NRT";
    case ERR_SOCKET_TIMEOUT:
      return "STO";
    case ERR_NETWORK_UNREACHABLE:
      return "NUN";
    case ERR_BAD_CONTENT_LENGTH:
      return "CLN";
    case ERR_CHUNKLEN_PARSE:
      return "BCL";
    case ERR_CHUNK_EOF:
      return "CEF";
    case ERR_DECOMPRESS:
      return "ZIP ";
    default:
      return "ERR"+errorReason;
    }
  }

  /** 
   * Returns a displayable form for the <code>errorReason</code>.
   * The result is meant for human consumption.
   */ 
  public static String getOutputStatusPrettyName(int outputStatus) {
    switch (outputStatus) {
    case OUT_OK: 
      return "Output OK";
    case OUT_UNKNOWN:
      return "Unknown output error";
    case OUT_DOM_ERROR:
      return "DOM parse error";
    case OUT_DOM_EXCEPTION:
      return "DOM parser failed";
    case OUT_UNKNOWN_CONTENT:
      return "Unknown Content-Type";
    case OUT_ENCODING_ERR:
      return "Character Encoding Error";
    default:
      return "*OUT: No Displayable Form* (" + outputStatus + ")";
    }
  }

  /** 
   * Returns a terse displayable form for the
   * <code>outputStatus</code>.  The result contains no whitespace and
   * is suitable for consumption by log analysers.
   */ 
  public static String getOutputStatusTerseName(int outputStatus) {
    switch (outputStatus) {
    case OUT_OK:
      return "OOK";
    case OUT_UNKNOWN:
      return "UNO";
    case OUT_DOM_ERROR:
      return "DME";
    case OUT_DOM_EXCEPTION:
      return "DMF";
    case OUT_UNKNOWN_CONTENT:
      return "UCT";
    case OUT_ENCODING_ERR:
      return "CEE";
    default:
      return "OUT"+outputStatus;
    }
  }
  
}
