/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import net.nutch.util.LogFormatter;
import net.nutch.io.*;
import net.nutch.ipc.*;


/** Implements the search API over IPC connnections. */
public class DistributedSearch {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.searcher.DistributedSearch");

  private DistributedSearch() {}                  // no public ctor

  // op codes for IPC calls
  private static final byte OP_SEGMENTS = (byte)0;
  private static final byte OP_SEARCH = (byte)1;
  private static final byte OP_EXPLAIN = (byte)2;
  private static final byte OP_DETAILS = (byte)3;
  private static final byte OP_SUMMARY = (byte)4;
  private static final byte OP_CONTENT = (byte)5;
  private static final byte OP_ANCHORS = (byte)6;

  /** Names of the op codes. */
  private static final String[] OP_NAMES = new String[7];
  static {
    OP_NAMES[OP_SEGMENTS] = "getSegmentNames";
    OP_NAMES[OP_SEARCH] = "search";
    OP_NAMES[OP_EXPLAIN] = "getExplanation";
    OP_NAMES[OP_DETAILS] = "getDetails";
    OP_NAMES[OP_SUMMARY] = "getSummary";
    OP_NAMES[OP_CONTENT] = "getContent";
    OP_NAMES[OP_ANCHORS] = "getAnchors";
  }

  /** The parameter passed with IPC requests.  Public only so that {@link
   * Server} can construct instances. */
  public static class Param implements Writable {
    private byte op;                              // the op code
    private Writable first;                       // the first operand
    private Writable second;                      // the second operand

    public Param() {}

    Param(byte op, Writable first) {
      this(op, first, NullWritable.get());
    }

    Param(byte op, Writable first, Writable second) {
      this.op = op;
      this.first = first;
      this.second = second;
    }

    public void write(DataOutput out) throws IOException {
      out.writeByte(op);
      first.write(out);
      second.write(out);
    }

    public void readFields(DataInput in) throws IOException {
      op = in.readByte();

      switch (op) {
      case OP_SEGMENTS:
        first = NullWritable.get();
        second = NullWritable.get();
        break;
      case OP_SEARCH:
        first = new Query();
        second = new IntWritable();
        break;
      case OP_EXPLAIN:
        first = new Query();
        second = new Hit();
        break;
      case OP_DETAILS:
        first = new Hit();
        second = NullWritable.get();
        break;
      case OP_SUMMARY:
        first = new HitDetails();
        second = new Query();
        break;
      case OP_CONTENT:
      case OP_ANCHORS:
        first = new HitDetails();
        second = NullWritable.get();
        break;
      default:
        throw new RuntimeException("Unknown op code: " + op);
      }

      first.readFields(in);
      second.readFields(in);
    }
  }

  /** The parameter returned with IPC responses.  Public only so that {@link
   * Client} can construct instances. */
  public static class Result implements Writable {
    private byte op;
    private Writable value;

    public Result() {}

    Result(byte op, Writable value) {
      this.op = op;
      this.value = value;
    }

    public void write(DataOutput out) throws IOException {
      out.writeByte(op);
      value.write(out);
    }

    public void readFields(DataInput in) throws IOException {
      op = in.readByte();

      switch (op) {
      case OP_SEGMENTS:
        value = new ArrayWritable(UTF8.class);
        break;
      case OP_SEARCH:
        value = new Hits();
        break;
      case OP_EXPLAIN:
        value = new UTF8();
        break;
      case OP_DETAILS:
        value = new HitDetails();
        break;
      case OP_SUMMARY:
        value = new UTF8();
        break;
      case OP_CONTENT:
        value = new BytesWritable();
        break;
      case OP_ANCHORS:
        value = new ArrayWritable(UTF8.class);
        break;
      default:
        throw new RuntimeException("Unknown op code: " + op);
      }

      value.readFields(in);
    }
  }

  /** The search server. */
  public static class Server extends net.nutch.ipc.Server {
    private NutchBean bean;

    /** Construct a search server on the index and segments in the named
     * directory, listening on the named port. */
    public Server(File directory, int port) throws IOException {
      super(port, Param.class, 10);
      this.bean = new NutchBean(directory);
    }

    public Writable call(Writable param) throws IOException {
      Param p = (Param)param;
      logRequest(p);
      Writable value;
      switch (p.op) {
      case OP_SEGMENTS:
        value = new ArrayWritable(bean.getSegmentNames());
        break;
      case OP_SEARCH:
        value = bean.search((Query)p.first, ((IntWritable)p.second).get());
        break;
      case OP_EXPLAIN:
        value = new UTF8(bean.getExplanation((Query)p.first, (Hit)p.second));
        break;
      case OP_DETAILS:
        value = bean.getDetails((Hit)p.first);
        break;
      case OP_SUMMARY:
        value = new UTF8(bean.getSummary((HitDetails)p.first,(Query)p.second));
        break;
      case OP_CONTENT:
        value = new BytesWritable(bean.getContent((HitDetails)p.first));
        break;
      case OP_ANCHORS:
        value = new ArrayWritable(bean.getAnchors((HitDetails)p.first));
        break;
      default:
        throw new RuntimeException("Unknown op code: " + p.op);
      }
      
      //LOG.info("Result: "+value);

      return new Result(p.op, value);

    }

    private static void logRequest(Param p) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(Thread.currentThread().getName());
      buffer.append(": ");
      buffer.append(OP_NAMES[p.op]);
      buffer.append("(");
      if (p.first != NullWritable.get()) {
        buffer.append(p.first);
        if (p.second != NullWritable.get()) {
          buffer.append(", ");
          buffer.append(p.second);
        }
      }
      buffer.append(")");
	  DistributedSearch.LOG.info(buffer.toString());
    }

    /** Runs a search server. */
    public static void main(String[] args) throws Exception {
      String usage = "DistributedSearch$Server <port> <index dir>";

      if (args.length == 0 || args.length > 2) {
        System.err.println(usage);
        System.exit(-1);
      }

      int port = Integer.parseInt(args[0]);
      File directory = new File(args[1]);

      Server server = new Server(directory, port);
      //server.setTimeout(Integer.MAX_VALUE);
      server.start();
      server.join();
    }

  }

  /** The search client. */
  public static class Client extends net.nutch.ipc.Client
    implements Searcher, HitDetailer, HitSummarizer, HitContent {

    private InetSocketAddress[] addresses;
    private HashMap segmentToAddress = new HashMap();

    /** Construct a client talking to servers listed in the named file.
     * Each line in the file lists a server hostname and port, separated by
     * whitespace. 
     */

    public Client(File file) throws IOException {
      this(readConfig(file));
    }

    private static InetSocketAddress[] readConfig(File config)
      throws IOException {
      BufferedReader reader = new BufferedReader(new FileReader(config));
      ArrayList addrs = new ArrayList();
      String line;
      while ((line = reader.readLine()) != null) {
        StringTokenizer tokens = new StringTokenizer(line);
        if (tokens.hasMoreTokens()) {
          String host = tokens.nextToken();
          if (tokens.hasMoreTokens()) {
            String port = tokens.nextToken();
            addrs.add(new InetSocketAddress(host, Integer.parseInt(port)));
			DistributedSearch.LOG.info("Client adding server "  + host + ":" + port);
          }
        }
      }
      return (InetSocketAddress[])
        addrs.toArray(new InetSocketAddress[addrs.size()]);
    }

    /** Construct a client talking to the named servers. */
    public Client(InetSocketAddress[] addresses) throws IOException {
      super(Result.class);
      
      this.addresses = addresses;

      // build segmentToAddress map
      Param param = new Param(OP_SEGMENTS, NullWritable.get());
      Writable[] params = new Writable[addresses.length];
      for (int i = 0; i < params.length; i++) {
        params[i] = param;                     // build param for parallel call
      }
      Writable[] results = call(params, addresses); // make parallel call

      for (int i = 0; i < results.length; i++) {  // process results of call
        Result result = (Result)results[i];
        if (result == null) {
			DistributedSearch.LOG.warning("Client: no segments from: " + addresses[i]);
          continue;
        }
        String[] segments = ((ArrayWritable)result.value).toStrings();
        for (int j = 0; j < segments.length; j++) {
			DistributedSearch.LOG.info("Client: segment "+segments[j]+" at "+addresses[i]);
          segmentToAddress.put(segments[j], addresses[i]);
        }
      }
    }

    /** Return the names of segments searched. */
    public String[] getSegmentNames() {
      return (String[])segmentToAddress.keySet().toArray(new String[segmentToAddress.size()]);
    }

    public Hits search(Query query, int numHits) throws IOException {
      long totalHits = 0;
      Hits[] segmentHits = new Hits[addresses.length];

      Param param = new Param(OP_SEARCH, query, new IntWritable(numHits));
      Writable[] params = new Writable[addresses.length];
      for (int i = 0; i < params.length; i++) {
        params[i] = param;                     // build param for parallel call
      }
      Writable[] results = call(params, addresses); // make parallel call

      TreeSet queue = new TreeSet();              // cull top hits from results
      float minScore = 0.0f;
      for (int i = 0; i < results.length; i++) {
        Result result = (Result)results[i];
        if (result == null) continue;
        Hits hits = (Hits)result.value;
        totalHits += hits.getTotal();
        for (int j = 0; j < hits.getLength(); j++) {
          Hit hit = hits.getHit(j);
          if (hit.getScore() >= minScore) {
            queue.add(new Hit(i, hit.getIndexDocNo(), hit.getScore()));
            if (queue.size() > numHits) {         // if hit queue overfull
              queue.remove(queue.last());         // remove lowest in hit queue
              minScore = ((Hit)queue.last()).getScore(); // reset minScore
            }
          }
        }
      }
      return new Hits(totalHits, (Hit[])queue.toArray(new Hit[queue.size()]));
    }
    
    public String getExplanation(Query query, Hit hit) throws IOException {
      Param param = new Param(OP_EXPLAIN, query, hit);
      Result result = (Result)call(param, addresses[hit.getIndexNo()]);
      return result.value.toString();
    }
    
    public HitDetails getDetails(Hit hit) throws IOException {
      Param param = new Param(OP_DETAILS, hit);
      Result result = (Result)call(param, addresses[hit.getIndexNo()]);
      return (HitDetails)result.value;
    }
    
    public HitDetails[] getDetails(Hit[] hits) throws IOException {
      Writable[] params = new Writable[hits.length];
      InetSocketAddress[] addrs = new InetSocketAddress[hits.length];
      for (int i = 0; i < hits.length; i++) {
        params[i] = new Param(OP_DETAILS, hits[i]);
        addrs[i] = addresses[hits[i].getIndexNo()];
      }
      Writable[] writables = call(params, addrs);
      HitDetails[] results = new HitDetails[writables.length];
      for (int i = 0; i < results.length; i++) {
        results[i] = (HitDetails)((Result)writables[i]).value;
      }
      return results;
    }


    public String getSummary(HitDetails hit, Query query) throws IOException {
      Param param = new Param(OP_SUMMARY, hit, query);
      InetSocketAddress address =
        (InetSocketAddress)segmentToAddress.get(hit.getValue("segment"));
      Result result = (Result)call(param, address);
      return result.value.toString();
    }

    public String[] getSummary(HitDetails[] hits, Query query)
      throws IOException {
      Writable[] params = new Writable[hits.length];
      InetSocketAddress[] addrs = new InetSocketAddress[hits.length];
      for (int i = 0; i < hits.length; i++) {
        HitDetails hit = hits[i];
        params[i] = new Param(OP_SUMMARY, hit, query);
        addrs[i] =
          (InetSocketAddress)segmentToAddress.get(hit.getValue("segment"));
      }
      Writable[] results = call(params, addrs);
      String[] strings = new String[results.length];
      for (int i = 0; i < results.length; i++) {
        if (results[i] != null)
          strings[i] = ((Result)results[i]).value.toString();
      }
      return strings;
    }
    
    public byte[] getContent(HitDetails hit) throws IOException {
      Param param = new Param(OP_CONTENT, hit);
      InetSocketAddress address =
        (InetSocketAddress)segmentToAddress.get(hit.getValue("segment"));
      Result result = (Result)call(param, address);
      return ((BytesWritable)result.value).get();
    }
    
    public String[] getAnchors(HitDetails hit) throws IOException {
      Param param = new Param(OP_ANCHORS, hit);
      InetSocketAddress address =
        (InetSocketAddress)segmentToAddress.get(hit.getValue("segment"));
      Result result = (Result)call(param, address);
      return ((ArrayWritable)result.value).toStrings();
    }

    public static void main(String[] args) throws Exception {
      String usage = "DistributedSearch$Client query <host> <port> ...";

      if (args.length == 0) {
        System.err.println(usage);
        System.exit(-1);
      }

      Query query = Query.parse(args[0]);
      
      InetSocketAddress[] addresses = new InetSocketAddress[(args.length-1)/2];
      for (int i = 0; i < (args.length-1)/2; i++) {
        addresses[i] =
          new InetSocketAddress(args[i*2+1], Integer.parseInt(args[i*2+2]));
      }

      Client client = new Client(addresses);
      //client.setTimeout(Integer.MAX_VALUE);

      Hits hits = client.search(query, 10);
      System.out.println("Total hits: " + hits.getTotal());
      for (int i = 0; i < hits.getLength(); i++) {
        System.out.println(" "+i+" "+ client.getDetails(hits.getHit(i)));
      }

    }


  }

}
