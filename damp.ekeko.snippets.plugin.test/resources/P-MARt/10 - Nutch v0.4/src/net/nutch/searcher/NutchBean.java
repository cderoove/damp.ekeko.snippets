/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;
import java.io.File;

import java.net.InetSocketAddress;

import java.util.Vector;
import net.nutch.indexer.IndexSegment;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import net.nutch.util.LogFormatter;
import net.nutch.util.NutchConf;


/** One stop shopping for search-related functionality. */   
public class NutchBean
  implements Searcher, HitDetailer, HitSummarizer, HitContent {

  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.searcher.NutchBean");

  static {
    LogFormatter.setShowThreadIDs(true);
  }

  private String[] segmentNames;

  private Searcher searcher;
  private HitDetailer detailer;
  private HitSummarizer summarizer;
  private HitContent content;

  /** Cache in servlet context. */
  public static NutchBean get(ServletContext app) throws IOException {
    NutchBean bean = (NutchBean)app.getAttribute("nutchBean");
    if (bean == null) {
      LOG.info("creating new bean");
      bean = new NutchBean();
      app.setAttribute("nutchBean", bean);
    }
    return bean;
  }

  /** Construct reading from connected directory. */
  public NutchBean() throws IOException {
    this(new File(NutchConf.get("searcher.dir", ".")));
  }

  /** Construct in a named directory. */
  public NutchBean(File dir) throws IOException {
    File servers = new File(dir, "search-servers.txt");
    if (servers.exists()) {
      LOG.info("searching servers in " + servers.getCanonicalPath());
      init(new DistributedSearch.Client(servers));
    } else {
      init(new File(dir, "index"), new File(dir, "segments"));
    }
  }

  private void init(File indexDir, File segmentsDir) throws IOException {
    IndexSearcher indexSearcher;
    if (indexDir.exists()) {
      LOG.info("opening merged index in " + indexDir.getCanonicalPath());
      indexSearcher = new IndexSearcher(indexDir.toString());
    } else {
      LOG.info("opening segment indexes in " + segmentsDir.getCanonicalPath());
      
      Vector vDirs=new Vector();
      File [] directories = segmentsDir.listFiles();
      for(int i = 0; i < segmentsDir.listFiles().length; i++) {
        File indexdone = new File(directories[i], IndexSegment.DONE_NAME);
        if(indexdone.exists() && indexdone.isFile()) {
          vDirs.add(directories[i]);
        }
      }
      
      directories = new File[ vDirs.size() ];
      for(int i = 0; vDirs.size()>0; i++) {
        directories[i]=(File)vDirs.remove(0);
      }
      
      indexSearcher = new IndexSearcher(directories);
    }

    FetchedSegments segments = new FetchedSegments(segmentsDir.toString());
    
    this.segmentNames = segments.getSegmentNames();
    
    this.searcher = indexSearcher;
    this.detailer = indexSearcher;
    this.summarizer = segments;
    this.content = segments;
  }

  private void init(DistributedSearch.Client client) throws IOException {
    this.segmentNames = client.getSegmentNames();
    this.searcher = client;
    this.detailer = client;
    this.summarizer = client;
    this.content = client;
  }


  public String[] getSegmentNames() {
    return segmentNames;
  }

  public Hits search(Query query, int numHits) throws IOException {
    return searcher.search(query, numHits);
  }

  public String getExplanation(Query query, Hit hit) throws IOException {
    return searcher.getExplanation(query, hit);
  }

  public HitDetails getDetails(Hit hit) throws IOException {
    return detailer.getDetails(hit);
  }

  public HitDetails[] getDetails(Hit[] hits) throws IOException {
    return detailer.getDetails(hits);
  }

  public String getSummary(HitDetails hit, Query query) throws IOException {
    return summarizer.getSummary(hit, query);
  }

  public String[] getSummary(HitDetails[] hits, Query query)
    throws IOException {
    return summarizer.getSummary(hits, query);
  }

  public byte[] getContent(HitDetails hit) throws IOException {
    return content.getContent(hit);
  }

  public String[] getAnchors(HitDetails hit) throws IOException {
    return content.getAnchors(hit);
  }

  /** For debugging. */
  public static void main(String[] args) throws Exception {
    String usage = "NutchBean query";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }

    NutchBean bean = new NutchBean();
    Query query = Query.parse(args[0]);

    Hits hits = bean.search(query, 10);
    System.out.println("Total hits: " + hits.getTotal());
    int length = (int)Math.min(hits.getTotal(), 10);
    Hit[] show = hits.getHits(0, length);
    HitDetails[] details = bean.getDetails(show);
    String[] summaries = bean.getSummary(details, query);

    for (int i = 0; i < hits.getLength(); i++) {
      System.out.println(" "+i+" "+ details[i]);// + "\n" + summaries[i]);
    }
  }



}
