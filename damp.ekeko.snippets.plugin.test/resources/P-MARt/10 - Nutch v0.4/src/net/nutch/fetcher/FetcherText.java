/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.*;
import net.nutch.io.*;

/*********************************************
 * The text conversion of page's content, stored using GZIP compression.
 *
 * @author Doug Cutting
 * @author Winton Davies
 *********************************************/

public class FetcherText extends VersionedWritable {
  public static final String DIR_NAME = "fetcher_text";

  private final static byte VERSION = 1;


  public FetcherText() {}
  private String text;
    
  public FetcherText(String text){
    this.text = text;
  }

  public byte getVersion() { return VERSION; }

  public void readFields(DataInput in) throws IOException {
    super.readFields(in);                         // check version
    text = WritableUtils.readCompressedString(in);
    return;
  }

  public void write(DataOutput out) throws IOException {
    super.write(out);                             // write version
    WritableUtils.writeCompressedString(out, text);
    return;
  }

  public static FetcherText read(DataInput in) throws IOException {
    FetcherText fetcherStripped = new FetcherText();
    fetcherStripped.readFields(in);
    return fetcherStripped;
  }

  //
  // Accessor methods
  //
  public String getText()  { return text; }

  public boolean equals(Object o) {
    if (!(o instanceof FetcherText))
      return false;
    FetcherText other = (FetcherText)o;
    return this.text.equals(other.text);
  }

  public String toString() {
    return text;
  }

  public static void main(String argv[]) throws Exception {

    String filename = FetcherText.DIR_NAME;
    String usage = "FetcherText recno [filename]";
    
    if (argv.length == 0 || argv.length > 2) {
      System.out.println("usage:" + usage);
      return;
    }

    int recno = Integer.parseInt(argv[0]);

    if (argv.length == 2) {
      filename = argv[1];
    } 
    

    ArrayFile.Reader fetcher;

    FetcherText fs1 = new FetcherText();
    fetcher = new ArrayFile.Reader(filename);

    fetcher.get(recno,fs1);
    System.out.println("Retrieved " + recno + " from file " + filename);
    System.out.println(fs1);
    fetcher.close();
  }

}
