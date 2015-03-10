/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import net.nutch.net.protocols.Response;

import net.nutch.pagedb.FetchListEntry;
import net.nutch.io.*;
import net.nutch.db.*;
import net.nutch.util.*;
import net.nutch.util.RobotsMetaProcessor.*;
import net.nutch.util.DOMContentUtils;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

import org.cyberneko.html.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.apache.html.dom.*;

/**
 * This class is a worker thread which polls the RequestScheduler for
 * finished requests and performs the title, link and plaintext
 * extraction, then writes the results to the appropriate DBs.
 */
public class OutputThread extends Thread implements FetcherConstants {
  public static final Logger LOG =
  LogFormatter.getLogger("net.nutch.fetcher.OutputThread");

  public static final int NO_OUTPUT_DELAY_MS= 2 * 1000;

  private RequestScheduler scheduler;
  private DOMFragmentParser parser;
  private RobotsMetaIndicator robotsMeta;

  private ArrayFile.Writer fetcherDb;              // the output
  private ArrayFile.Writer rawDb;
  private ArrayFile.Writer strippedDb;

  private class UnhandledContentTypeException extends Exception {
    String contentType;
    UnhandledContentTypeException(String contentType) {
      this.contentType= contentType;
    }
    String getContentType() {
      return contentType;
    }
  }

  // used to indicate that an unexpected error came out of the DOM
  // parser
  private class DOMErrorException extends Exception {
  }

  /**
   * Creates a new <code>OutputThread</code>, which will poll the
   * given <code>scheduler</code> for output, and write output records
   * to the supplied databases.
   */
  protected OutputThread(RequestScheduler scheduler,
                         ArrayFile.Writer fetcherDb,
                         ArrayFile.Writer rawDb,
                         ArrayFile.Writer strippedDb) {
    this.scheduler= scheduler;
    this.fetcherDb= fetcherDb;
    this.rawDb= rawDb;
    this.strippedDb= strippedDb;
    this.parser = new DOMFragmentParser();
    this.robotsMeta= new RobotsMetaIndicator();
  }

  
  /**
   *  Polls the {@link RequestScheduler} for output to service,
   *  until {@link RequestScheduler#finishedOutput()} returns
   *  <code>true</code>.
   */
  public void run() {
    RequestRecord prevOutputRequest= null;
    String prevUrlString= null;

    while (!scheduler.finishedOutput()) {
      RequestRecord outputEntry= null;

      try {
        outputEntry= scheduler.returnOutputAndGetNext(prevOutputRequest, 
                                                      prevUrlString);
        prevOutputRequest= null;
        prevUrlString= null;
      } catch (Exception e) {
        e.printStackTrace();
        LOG.severe("Exception caught during call to"
                   + " RequestScheduler.getNextOutput()!");
      }
      if (outputEntry == null) {
        try {
          Thread.sleep(NO_OUTPUT_DELAY_MS);
        } catch (InterruptedException e) {
          ;
        }
        continue;
      }

      String urlString;

      if (outputEntry.getURL() != null) {
        urlString= outputEntry.getURLString();
      } else 
        urlString= 
          outputEntry.getFetchListEntry().getPage().getURL().toString();

      if (LOG.isLoggable(Level.FINER))
        LOG.finer("going to write " + urlString );

      try {
        if ( (outputEntry.getResponse() != null) 
             && (!outputEntry.getHasFailed()) ) {
          handleFetch(outputEntry.getURL(), outputEntry.getFetchListEntry(),
                      outputEntry.getResponse());
        } else {
          int status = (!outputEntry.getHasFailed() 
                        && !outputEntry.getFetchListEntry().getFetch()) ?
            FetcherOutput.SUCCESS :
            FetcherOutput.RETRY;
          handleNoFetch(outputEntry.getFetchListEntry(), status);
        }
      } catch (org.w3c.dom.DOMException e) {
        outputEntry.setOutputStatus(OUT_DOM_ERROR);
      } catch (UnhandledContentTypeException e) {
        outputEntry.setOutputStatus(OUT_UNKNOWN_CONTENT);
        outputEntry.setOutputStatusMessages(
          new String[] { e.getContentType() } );
      } catch (DOMErrorException e) {
        outputEntry.setOutputStatus(OUT_DOM_EXCEPTION);
      } catch (Exception e) {
        outputEntry.setOutputStatus(OUT_UNKNOWN);
        outputEntry.setOutputStatusMessages(
          new String[] { e.toString() } );
      }

      // fixme: ought to output an error to the DBs
      // to prevent immediate re-fetch!

      prevOutputRequest= outputEntry;
      prevUrlString= urlString;
    }

    scheduler.returnOutputAndGetNext(prevOutputRequest, 
                                     prevUrlString);
  }

  private void handleFetch(URL url, FetchListEntry fle,
                           Response response)
    throws IOException, SAXException, UnhandledContentTypeException,
    DOMErrorException {
    String contentType = response.getHeader("Content-Type");
    if (contentType != null && !contentType.startsWith("text/html"))
      throw new UnhandledContentTypeException(contentType);
      
    DocumentFragment node = new HTMLDocumentImpl().createDocumentFragment();

    try {
      parser.parse(new InputSource            // parse content
                   (new ByteArrayInputStream(response.getContent())),
                   node);
    } catch (org.w3c.dom.DOMException e) {
      // expect this, rethrow
      throw e;
    } catch (Exception e) {
      throw new DOMErrorException();
    }

    RobotsMetaProcessor.getRobotsMetaDirectives(robotsMeta, node, url);

    String text;
    String title;

    if (robotsMeta.getNoIndex() == false) {
      StringBuffer sb = new StringBuffer();
      DOMContentUtils.getText(sb, node);
      text = sb.toString();

      sb.setLength(0);
      DOMContentUtils.getTitle(sb, node);
      title = sb.toString().trim();
    } else {
      FetcherStatus.logTraceMisc(FetcherStatus.MISC_META_NOINDEX, url);
      text=  "";
      title= "";
    }

    Outlink[] outlinks;
    if (robotsMeta.getNoFollow() == false) {      
      URL baseURL= robotsMeta.getBaseHref();
      if (baseURL == null) 
        baseURL= url;

      ArrayList l = new ArrayList();
      DOMContentUtils.getOutlinks(baseURL, l, node);
      outlinks = (Outlink[])l.toArray(new Outlink[l.size()]);
      LOG.fine("found " + outlinks.length + " outlinks in " + url);
    } else {
      outlinks = new Outlink[0];
      FetcherStatus.logTraceMisc(FetcherStatus.MISC_META_NOFOLLOW, url);
    }

    byte[] content;
    if (robotsMeta.getNoCache() == false) {
      content= response.getContent();
    } else {
      FetcherStatus.logTraceMisc(FetcherStatus.MISC_META_NOCACHE, url);
      content= new byte[0];
    }

    outputPage(new FetcherOutput(fle, MD5Hash.digest(response.getContent()),
                                 FetcherOutput.SUCCESS, title, outlinks),
               new FetcherContent(content),
               new FetcherText(text));
  }

  private void handleNoFetch(FetchListEntry fle, int status) {
    outputPage(new FetcherOutput(fle, MD5Hash.digest(fle.getPage().getURL().toString()),
                                 status, "", new Outlink[0]),
               new FetcherContent(new byte[0]),
               new FetcherText(""));
  }


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
                                       
                     

}
