/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.net.protocols.Response;

import net.nutch.net.protocols.http.BadHeaderLineException;
import net.nutch.net.protocols.http.BadStatusLineException;
import net.nutch.net.protocols.http.ChunkEOFException;
import net.nutch.net.protocols.http.ChunkLengthParseException;
import net.nutch.net.protocols.http.ContentLengthParseException;
import net.nutch.net.protocols.http.DecompressionException;
import net.nutch.net.protocols.http.Http;
import net.nutch.net.protocols.http.HttpVersionException;
import net.nutch.net.protocols.http.MiscHttpAccounting;
import net.nutch.net.protocols.http.HttpResponse;

import net.nutch.net.protocols.ftp.Ftp;
import net.nutch.net.protocols.ftp.FtpResponse;

import net.nutch.io.*;
import net.nutch.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

/**
 * This class is a worker thread which polls the RequestScheduler for
 * requests and actually performs the fetch.
 */
public class FetcherThread extends Thread implements FetcherConstants { 
  public static final Logger LOG =
  LogFormatter.getLogger("net.nutch.fetcher.FetcherThread");

  public static final int DELAY_MS= 2 * 1000;

  private RequestScheduler scheduler;

  private boolean throttle;

  // 20040404, xing, do the same for http?
  private Ftp ftp;

  /**
   *  Creates a new <code>FetcherThread</code> which will service
   *  requests from the supplied <code>scheduler</code>.
   */
  FetcherThread(RequestScheduler scheduler) {
    this.scheduler= scheduler;
    this.throttle= false;
  }
  
  /**
   *  Polls the {@link RequestScheduler} for requests to service,
   *  until {@link RequestScheduler#finishedRequests()} returns
   *  <code>true</code>.
   */
  public void run() {
    MiscHttpAccounting httpAccounting= 
      new MiscHttpAccounting();

    RequestRecord prevRequest= null;

    // 20040404, xing, do the same for http?
    //LOG.info("creating ftp");
    ftp = new Ftp();

    while (!scheduler.finishedRequests()) {
      RequestRecord request= null;

      // could also do Thread.stop() trick w/volatile 
      synchronized (this) {
        if (throttle) {
          try {
            if (prevRequest != null) {
              scheduler.returnRequest(prevRequest, httpAccounting);
              prevRequest = null;
            }
          } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("Exception caught during call to"
                       + " RequestScheduler.returnRequest()!");
          }

          while (throttle)
            try {
              this.wait();
            } catch (InterruptedException e) {
            }

        }
      }

      try {
        request= scheduler.returnRequestAndGetNext(prevRequest, 
                                                   httpAccounting);
        prevRequest= null;
      } catch (Exception e) {
        e.printStackTrace();
        LOG.severe("Exception caught during call to"
                   + " RequestScheduler.returnRequestAndGetNext()!");
      }

      if (request == null) {
        try {
          Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
          ;
        }
        continue;
      }

      URL url= request.getURL();
      if (LOG.isLoggable(Level.FINE))
        LOG.fine("Trying to fetch: " + url.toString());

      InetAddress addr= request.getAddr();

      Response response= null;
      httpAccounting.reset();
      int httpVersion= request.getHttpVersion();

      try {

        if ("http".equals(url.getProtocol())) {
          Http http = new Http();
          http.setAgentString(scheduler.getAgentString());
          // get HTTP response, don't follow redirects
          response = http.getRawResponse(url, addr, httpAccounting,
                                      httpVersion);
        } else if ("ftp".equals(url.getProtocol())) {
          //Ftp ftp = new Ftp();
          response = ftp.getRawResponse(url, addr, httpAccounting,
                                      Http.HTTP_VER_NOTSET);
        } else {
          throw new IOException("Not an HTTP or FTP url:" + url);
        }

      } catch (ConnectException e) {
         request.setFailureReason(FAIL_CONNECTION_REFUSED);
         request.setHasFailed(true);
      } catch (SocketTimeoutException e) {
         request.setErrorReason(ERR_SOCKET_TIMEOUT);
      } catch (UnknownHostException e) {
        request.setFailureReason(FAIL_UNKNOWN_HOST);
        request.setHasFailed(true);
      } catch (NoRouteToHostException e) {
        request.setErrorReason(ERR_NO_ROUTE);
      } catch (SocketException e) {
        String msg= e.getMessage();
        if (msg != null) {
          if (msg.indexOf("reset by peer") >= 0) {
            request.setErrorReason(ERR_RESET_BY_PEER);
          } else if (msg.indexOf("Network is unreachable") >= 0) {
            request.setErrorReason(ERR_NETWORK_UNREACHABLE);
          } else {
            request.setErrorReason(ERR_UNKNOWN);
            request.setErrorMessages(new String[] { e.toString()} );
          }
        } else {
          request.setErrorReason(ERR_UNKNOWN);
          request.setErrorMessages(new String[] { e.toString()} );
        }
      } catch (EOFException e) {
        request.setErrorReason(ERR_EOF_DURING_READ);
      } catch (IOException e) {
        String msg= e.getMessage();
        if (msg != null) {
          if (msg.indexOf("Connection timed out") >= 0) {
            request.setErrorReason(ERR_CONNECTION_TIMED_OUT);
          } else if (msg.indexOf("Connection refused") >= 0) {
            request.setFailureReason(FAIL_CONNECTION_REFUSED);
            request.setHasFailed(true);
          } else {
            request.setErrorReason(ERR_UNKNOWN);
            request.setErrorMessages(new String[] { e.toString() } );
          }
        } else {
          request.setErrorReason(ERR_UNKNOWN);
          request.setErrorMessages(new String[] { e.toString() } );
        }
      } catch (BadStatusLineException e) {
        request.setErrorReason(ERR_BAD_STATUS_LINE);
      } catch (BadHeaderLineException e) {
        request.setErrorReason(ERR_BAD_HEADER_LINE);
      } catch (ContentLengthParseException e) {
        request.setErrorReason(ERR_BAD_CONTENT_LENGTH);
      } catch (ChunkLengthParseException e) { // HttpVersionException
        request.setErrorReason(ERR_CHUNKLEN_PARSE);
        // very conservative!  always fall back to 1.0!
        httpVersion= Http.HTTP_VER_1_0;
      } catch (ChunkEOFException e) {         // HttpVersionException
        request.setErrorReason(ERR_CHUNK_EOF);
        // very conservative!  always fall back to 1.0!
        httpVersion= Http.HTTP_VER_1_0;
      } catch (DecompressionException e) {    // HttpVersionException
        request.setErrorReason(ERR_DECOMPRESS);
        // very conservative!  always fall back to 1.0!
        httpVersion= Http.HTTP_VER_1_0;
      } catch (HttpVersionException e) {
        request.setErrorReason(ERR_UNKNOWN);
        request.setErrorMessages(new String[] { e.toString()} );
        // try to fall back by 1 http version
        httpVersion= Http.minHttpVersion(httpVersion - 1, Http.HTTP_VER_1_0);
      } catch (Exception e) {
        request.setErrorReason(ERR_UNKNOWN);
        request.setErrorMessages(new String[] { e.toString()} );
      }

      request.setResponse(response);

      if (httpAccounting.getAddr() != null) {
        // set addr for HostQueue to cache
        request.setAddr(httpAccounting.getAddr());
      }

      int servVers= httpAccounting.getServHttpVersion();
      request.setHttpVersion( Http.minHttpVersion(httpVersion, servVers) );

      if (LOG.isLoggable(Level.FINE))
        LOG.fine("done request: " + url.toString());
      prevRequest= request;

    }

    // return last request
    scheduler.returnRequestAndGetNext(prevRequest, 
                                      httpAccounting);

    // 20040404, xing, do the same for http?
    //LOG.info("deleting ftp");
    ftp = null;
    // force garbage collection?
    System.gc();
  }

  /** 
   * Causes this FetcherThread to stop working, after it finishes 
   * any work it has outstanding and returns it.
   *
   *  <p>
   *   
   *  <em>Note:</em>The thread must be <code>unthrottle()</code>'d
   *  before it's <code>run()</code> method will complete.
   * 
   */
  public void throttle() {
    synchronized (this) {
      throttle= true;
    }
  }


  /** 
   * Causes this FetcherThread to resume work after a call to
   * <code>throttle()</code>.
   */
  public void unthrottle() {
    synchronized (this) {
      throttle= false;
      this.notify();
    }
  }


}
