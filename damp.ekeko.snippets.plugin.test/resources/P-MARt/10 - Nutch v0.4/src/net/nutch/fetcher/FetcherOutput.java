/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

import net.nutch.io.*;
import net.nutch.pagedb.FetchListEntry;
import net.nutch.tools.UpdateDatabaseTool;

/*********************************************
 * An entry in the fetcher's output.  This includes all of the fetcher output
 * except the raw and stripped versions of the content, which are placed in
 * separate files.
 *
 * @author Doug Cutting
 *********************************************/
public class FetcherOutput implements Writable {
  public static final String DIR_NAME = "fetcher";
  public static final String DONE_NAME = "fetcher.done";
  public static final String ERROR_NAME = "fetcher.error";

  private final static byte VERSION = 3;

  public final static byte RETRY = 0;
  public final static byte SUCCESS = 1;
  public final static byte NOT_FOUND = 2;

  private FetchListEntry fetchListEntry;
  private MD5Hash md5Hash;
  private int status;
  private String title = "";
  private Outlink[] outlinks;
  private long fetchDate;

  public FetcherOutput() {}

  public FetcherOutput(FetchListEntry fetchListEntry,
                       MD5Hash md5Hash, int status, String title,
                       Outlink[] outlinks) {
    this.fetchListEntry = fetchListEntry;
    this.md5Hash = md5Hash;
    this.status = status;
    this.title = title != null ? title : "";
    this.outlinks = outlinks;
    this.fetchDate = System.currentTimeMillis();
  }

  public byte getVersion() { return VERSION; }

  public void readFields(DataInput in) throws IOException {
    byte version = in.readByte();                 // read version
    fetchListEntry = FetchListEntry.read(in);
    md5Hash = MD5Hash.read(in);
    status = in.readByte();
    title = UTF8.readString(in);
    int totalOutlinks = in.readInt();
    int outlinksToRead = Math.min(UpdateDatabaseTool.MAX_OUTLINKS_PER_PAGE,
                                  totalOutlinks);
    outlinks = new Outlink[outlinksToRead];
    for (int i = 0; i < outlinksToRead; i++) {
      outlinks[i] = Outlink.read(in);
    }
    for (int i = outlinksToRead; i < totalOutlinks; i++) {
      Outlink.skip(in);
    }

    fetchDate = (version > 1) ? in.readLong() : 0; // added in version=2
  }

  public void write(DataOutput out) throws IOException {
    out.writeByte(VERSION);                       // store current version
    fetchListEntry.write(out);
    md5Hash.write(out);
    out.writeByte(status);
    UTF8.writeString(out, title);
    out.writeInt(outlinks.length);
    for (int i = 0; i < outlinks.length; i++) {
      outlinks[i].write(out);
    }
    out.writeLong(fetchDate);
  }

  public static FetcherOutput read(DataInput in) throws IOException {
    FetcherOutput fetcherOutput = new FetcherOutput();
    fetcherOutput.readFields(in);
    return fetcherOutput;
  }

  //
  // Accessor methods
  //
  public FetchListEntry getFetchListEntry() { return fetchListEntry; }
  public MD5Hash getMD5Hash() { return md5Hash; }
  public int getStatus() { return status; }
  public String getTitle() { return title; }
  public Outlink[] getOutlinks() { return outlinks; }
  public long getFetchDate() { return fetchDate; }
  public void setFetchDate(long fetchDate) { this.fetchDate = fetchDate; }


  public boolean equals(Object o) {
    if (!(o instanceof FetcherOutput))
      return false;
    FetcherOutput other = (FetcherOutput)o;
    return
      this.fetchListEntry.equals(other.fetchListEntry) &&
      this.md5Hash.equals(other.md5Hash) &&
      (this.status == other.status) &&
      this.title.equals(other.title) &&
      Arrays.equals(this.outlinks, other.outlinks);
  }


  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("FetchListEntry: " + fetchListEntry + "Fetch Result:\n" );
    buffer.append("MD5Hash: " + md5Hash + "\n" );
    buffer.append("Status: " + status + "\n" );
    buffer.append("Title: " + title + "\n" );
    buffer.append("Outlinks: " + outlinks.length + "\n" );
    for (int i = 0; i < outlinks.length; i++) {
       buffer.append("  outlink: " + outlinks[i] + "\n");
    }
    buffer.append("FetchDate: " + new Date(fetchDate) + "\n" );
    return buffer.toString();
  }

  public static void main(String argv[]) throws Exception {
    String usage = "FetcherOutput (-recno <recno> | -dumpall) [-filename <filename>]";
    if (argv.length == 0 || argv.length > 4) {
      System.out.println("usage:" + usage);
      return;
    }

    // Process the args
    String filename = FetcherOutput.DIR_NAME;
    boolean dumpall = false;
    int recno = -1;
    for (int i = 0; i < argv.length; i++) {
        if ("-recno".equals(argv[i])) {
            recno = Integer.parseInt(argv[i+1]);
            i++;
        } else if ("-dumpall".equals(argv[i])) {
            dumpall = true;
        } else if ("-filename".equals(argv[i])) {
            filename = argv[i+1];
            i++;
        }
    }

    // Now carry out the command
    ArrayFile.Reader fetcher = new ArrayFile.Reader(filename);
    try {
      FetcherOutput fo = new FetcherOutput();

      if (dumpall) {
        while ((fo = (FetcherOutput) fetcher.next(fo)) != null) {
          recno++;
          System.out.println("Retrieved " + recno + " from file " + filename);
          System.out.println(fo);
        }
      } else if (recno >= 0) {
        fetcher.get(recno, fo);
        System.out.println("Retrieved " + recno + " from file " + filename);
        System.out.println(fo);
      }
    } finally {
      fetcher.close();
    }
  }
}
