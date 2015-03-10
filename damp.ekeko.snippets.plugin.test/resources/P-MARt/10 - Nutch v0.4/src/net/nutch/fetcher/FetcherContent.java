/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.*;
import java.util.*;
import net.nutch.io.*;

/*********************************************
 * The raw contents of a page stored using GZIP compression.
 *
 * @author Doug Cutting
 * @author Winton Davies
 *********************************************/

public class FetcherContent extends VersionedWritable {
  public static final String DIR_NAME = "fetcher_content";

  private final static byte VERSION = 1;

  public FetcherContent() {}
  private byte[] content;
    
  public FetcherContent(byte[] content){
    this.content = content;
  }

  public byte getVersion() { return VERSION; }

  public void readFields(DataInput in) throws IOException {
    super.readFields(in);                         // check version
    content = WritableUtils.readCompressedByteArray(in);
    return;
  }

  public void write(DataOutput out) throws IOException {
    super.write(out);                             // write version
    WritableUtils.writeCompressedByteArray(out, content);
    return;
  }

  public static FetcherContent read(DataInput in) throws IOException {
    FetcherContent fetcherRaw = new FetcherContent();
    fetcherRaw.readFields(in);
    return fetcherRaw;
  }

  //
  // Accessor methods
  //
  public byte[] getContent()  { return content; }

  public boolean equals(Object o) {
    if (!(o instanceof FetcherContent)){
      return false;
    }
    FetcherContent other = (FetcherContent)o;
    return Arrays.equals(this.getContent(), other.getContent());
  }

  public String toString() {
    return new String(content);                   // try default encoding
  }

  public static void main(String argv[]) throws Exception {

    String filename = FetcherContent.DIR_NAME;
    String usage = "FetcherContent recno [filename]";
    
    if (argv.length == 0 || argv.length > 2) {
      System.out.println("usage:" + usage);
      return;
    }

    int recno = Integer.parseInt(argv[0]);

    if (argv.length == 2) {
      filename = argv[1];
    }

    ArrayFile.Reader fetcher;

    FetcherContent fr1 = new FetcherContent();
    System.out.println("Reading from file: " + filename);

    fetcher = new ArrayFile.Reader(filename);

    fetcher.get(recno,fr1);
    System.out.println("Retrieved " + recno + " from file " + filename);

    System.out.println(fr1);

    fetcher.close();
  }
}

