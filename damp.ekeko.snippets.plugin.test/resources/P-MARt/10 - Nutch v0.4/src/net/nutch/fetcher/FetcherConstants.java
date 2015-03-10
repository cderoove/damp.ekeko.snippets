/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

/**
 * Constants for Fetcher status codes, etc.
 */
public interface FetcherConstants {

  // Logging Constants
  /** Misc logging code for stats dumps */
  public static final int MISC_STATS= 0;

  /** Misc logging code for logging code key */
  public static final int MISC_KEY= 1;

  /** Misc logging code for robots.txt forbidden events */
  public static final int MISC_ROBOTS_FORBIDDEN= 2;

  /** Misc logging code for META robots/noindex events */
  public static final int MISC_META_NOINDEX= 3;

  /** Misc logging code for META robots/nofollow events */
  public static final int MISC_META_NOFOLLOW= 4;

  /** Misc logging code for META robots/nocache events */
  public static final int MISC_META_NOCACHE= 5;

  /** Misc logging code for successful fetches */
  public static final int MISC_FETCH_SUCCESS= 6;

  /** Misc logging code for random informational messages */
  public static final int MISC_INFORMATIONAL= 7;

  /** 
   *  The number of different misc codes we track (misc codes 
   *  run from <code>0</code> through <code>NUM_MISC_CODES - 1</code>
   */
  public static final int NUM_MISC_CODES= 8;

  // Failures are non-retryable things that prevent fetches (404, etc).
  // Failure reason-codes- values must run [0..NUM_FAIL_REASONS]
  /** Catch-all failure reason **/
  public static final int FAIL_UNKNOWN= 0; 

  /** The URL (or a redirect URL) was invalid **/
  public static final int FAIL_BAD_URL= 1; 

  /** A robots.txt file forbids us from accessing this URL **/
  public static final int FAIL_ROBOTS_EXCLUDED= 2;

  /** The maximum number of failed attempts has been reached **/
  public static final int FAIL_TOO_MANY_ERRORS= 3;

  /** The maximum number of failed attempts has been reached **/
  public static final int FAIL_TOO_MANY_REDIRECTS= 4;

  /** Got 3xx status code, but not a new location **/
  public static final int FAIL_REDIRECT_MISSING_TARGET= 5;

  /** Page does not exist (404/not found) **/
  public static final int FAIL_NOT_FOUND= 6;

  /** Got 4xx/Forbidden response code **/
  public static final int FAIL_FORBIDDEN= 7;

  /** Found a redirect loop **/
  public static final int FAIL_REDIRECT_LOOP_DETECTED= 8;

  /** Hostname matches a ban pattern **/
  public static final int FAIL_HOSTNAME_BANNED= 9;

  /** Host declared dead aftertoo many failed fetches **/
  public static final int FAIL_DEAD_HOST= 10;

  /** We didn't recognize the HTTP response code */
  public static final int FAIL_UNKNOWN_RESP_CODE= 11;

  /** Unknown host */
  public static final int FAIL_UNKNOWN_HOST= 12;

  /** Connection refused */
  public static final int FAIL_CONNECTION_REFUSED= 13;

  /** 
   *  The number of different failure codes we track (failure codes 
   *  run from <code>0</code> through <code>NUM_FAIL_REASONS - 1</code>
   */
  public static final int NUM_FAIL_REASONS= 14;

  // Retryable errors- explain why didn't we get the page on this try
  // Error reason-codes must run [0..NUM_ERR_REASONS]
  /** Catch-all error reason **/
  public static final int ERR_UNKNOWN= 0; 

  /** Connection timed out */
  public static final int ERR_CONNECTION_TIMED_OUT= 1;

  /** Bad header line */
  public static final int ERR_BAD_HEADER_LINE= 2;

  /** Connection reset by peer */
  public static final int ERR_RESET_BY_PEER= 3;

  /** Bad status line */
  public static final int ERR_BAD_STATUS_LINE= 4;

  /** EOF encountered during read */
  public static final int ERR_EOF_DURING_READ= 5;

  /** No route to host */
  public static final int ERR_NO_ROUTE= 6;

  /** Socket timeout */
  public static final int ERR_SOCKET_TIMEOUT= 7;

  /** Network unreachable */
  public static final int ERR_NETWORK_UNREACHABLE= 8;

  /** Bad Content-Length header */
  public static final int ERR_BAD_CONTENT_LENGTH= 9;

  /** Error parsing chunk length */
  public static final int ERR_CHUNKLEN_PARSE= 10;

  /** EOF in chunk */
  public static final int ERR_CHUNK_EOF= 11;

  /** Error uncompressing content  */
  public static final int ERR_DECOMPRESS= 12;

  /** The number of different error codes we track (error codes 
   *  run from <code>0</code> through <code>NUM_ERR_REASONS - 1</code>
   */
  public static final int NUM_ERR_REASONS= 13;

  /** output status code indicating success */
  public static final int OUT_OK= 0;

  /** output status code indicating an unknown failure */
  public static final int OUT_UNKNOWN= 1;

  /** output status code indicating DOM parse failure */
  public static final int OUT_DOM_ERROR= 2;

  /** output status code indicating an unexpected DOM parse exception */
  public static final int OUT_DOM_EXCEPTION= 3;

  /** output status code indicating an unhandled content type */
  public static final int OUT_UNKNOWN_CONTENT= 4;

  /** Output status code indicating a character encoding problem.
   * (ie. sun.io.MalformedInputException) */
  public static final int OUT_ENCODING_ERR= 5;

  /** The number of different output error codes we track (error codes 
   *  run from <code>0</code> through <code>NUM_OUT_STATUS - 1</code>
   */
  public static final int NUM_OUT_STATUS= 6;

}
