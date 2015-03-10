/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import net.nutch.io.*;
import net.nutch.util.LogFormatter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;

import java.io.*;
import java.util.Vector;
import java.util.logging.Logger;
import java.security.MessageDigest;

/** Deletes duplicate documents in a set of Lucene indexes.
 * Duplicates have either the same contents (via MD5 hash) or the same URL.
 */
public class DeleteDuplicates {
  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.indexer.DeleteDuplicates");

  /** The key used in sorting for duplicates. */
  public static class IndexedDoc implements WritableComparable {
    private MD5Hash hash = new MD5Hash();
    private float score;
    private int index;                            // the segment index
    private int doc;                              // within the index
    private int urlLen;

    public void write(DataOutput out) throws IOException {
      hash.write(out);
      out.writeFloat(score);
      out.writeInt(index);
      out.writeInt(doc);
      out.writeInt(urlLen);
    }

    public void readFields(DataInput in) throws IOException {
      hash.readFields(in);
      this.score = in.readFloat();
      this.index = in.readInt();
      this.doc = in.readInt();
      this.urlLen = in.readInt();
    }

    public int compareTo(Object o) {
      throw new RuntimeException("this is never used");
    }

    /** Order equal hashes by decreasing score and increasing urlLen. */
    public static class ByHashScore extends WritableComparator {
      public ByHashScore() { super(IndexedDoc.class); }
      
      public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2){
        int c = compareBytes(b1, s1, MD5Hash.MD5_LEN, b2, s2, MD5Hash.MD5_LEN);
        if (c != 0)
          return c;

        float thisScore = readFloat(b1, s1+MD5Hash.MD5_LEN);
        float thatScore = readFloat(b2, s2+MD5Hash.MD5_LEN);

        if (thisScore < thatScore)
          return 1;
        else if (thisScore > thatScore)
          return -1;
        
        int thisUrlLen = readInt(b1, s1+MD5Hash.MD5_LEN+12);
        int thatUrlLen = readInt(b2, s2+MD5Hash.MD5_LEN+12);

        return thisUrlLen - thatUrlLen;
      }
    }

    /** Order equal hashes by decreasing index and document. */
    public static class ByHashDoc extends WritableComparator {
      public ByHashDoc() { super(IndexedDoc.class); }
      
      public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2){
        int c = compareBytes(b1, s1, MD5Hash.MD5_LEN, b2, s2, MD5Hash.MD5_LEN);
        if (c != 0)
          return c;

        int thisIndex = readInt(b1, s1+MD5Hash.MD5_LEN+4);
        int thatIndex = readInt(b2, s2+MD5Hash.MD5_LEN+4);

        if (thisIndex != thatIndex)
          return thatIndex - thisIndex;

        int thisDoc = readInt(b1, s1+MD5Hash.MD5_LEN+8);
        int thatDoc = readInt(b2, s2+MD5Hash.MD5_LEN+8);

        return thatDoc - thisDoc;
      }
    }
  }

  private interface Hasher {
    void updateHash(MD5Hash hash, Document doc);
  }

  private IndexReader[] readers;
  private String tempFile;

  /** Constructs a duplicate detector for the provided indexes. */
  public DeleteDuplicates(IndexReader[] readers, String tempFile) {
    this.readers = readers;
    this.tempFile = tempFile;
  }

  /** Closes the indexes, saving changes. */
  public void close() throws IOException {
    for (int i = 0; i < readers.length; i++)
      readers[i].close();
  }

  /** Delete pages with duplicate content hashes.  Of those with the same
   * content hash, keep the page with the highest score. */
  public void deleteContentDuplicates() throws IOException {
    LOG.info("Reading content hashes...");
    computeHashes(new Hasher() {
        public void updateHash(MD5Hash hash, Document doc) {
          hash.setDigest(doc.get("digest"));
        }
      });

    LOG.info("Sorting content hashes...");
    SequenceFile.Sorter byHashScoreSorter =
      new SequenceFile.Sorter(new IndexedDoc.ByHashScore(),NullWritable.class);
    byHashScoreSorter.sort(tempFile, tempFile + ".sorted");
    
    LOG.info("Deleting content duplicates...");
    int duplicateCount = deleteDuplicates();
    LOG.info("Deleted " + duplicateCount + " content duplicates.");
  }

  /** Delete pages with duplicate URLs.  Of those with the same
   * URL, keep the most recently fetched page. */
  public void deleteUrlDuplicates() throws IOException {
    final MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (Exception e) {
      throw new RuntimeException(e.toString());
    }

    LOG.info("Reading url hashes...");
    computeHashes(new Hasher() {
        public void updateHash(MD5Hash hash, Document doc) {
          try {
            digest.update(UTF8.getBytes(doc.get("url")));
            digest.digest(hash.getDigest(), 0, MD5Hash.MD5_LEN);
          } catch (Exception e) {
            throw new RuntimeException(e.toString());
          }
        }
      });

    LOG.info("Sorting url hashes...");
    SequenceFile.Sorter byHashDocSorter =
      new SequenceFile.Sorter(new IndexedDoc.ByHashDoc(), NullWritable.class);
    byHashDocSorter.sort(tempFile, tempFile + ".sorted");
    
    LOG.info("Deleting url duplicates...");
    int duplicateCount = deleteDuplicates();
    LOG.info("Deleted " + duplicateCount + " url duplicates.");
  }

  private void computeHashes(Hasher hasher) throws IOException {
    IndexedDoc indexedDoc = new IndexedDoc();

    SequenceFile.Writer writer =
      new SequenceFile.Writer(tempFile, IndexedDoc.class, NullWritable.class);
    try {
      for (int index = 0; index < readers.length; index++) {
        IndexReader reader = readers[index];
        int readerMax = reader.maxDoc();
        indexedDoc.index = index;
        for (int doc = 0; doc < readerMax; doc++) {
          if (!reader.isDeleted(doc)) {
            Document document = reader.document(doc);
            hasher.updateHash(indexedDoc.hash, document);
            indexedDoc.score = Float.parseFloat(document.get("boost"));
            indexedDoc.doc = doc;
            indexedDoc.urlLen = document.get("url").length();
            writer.append(indexedDoc, NullWritable.get());
          }
        }
      }
    } finally {
      writer.close();
    }
  }

  private int deleteDuplicates() throws IOException {
    if (new File(tempFile).exists())
      new File(tempFile).delete();
    if (!new File(tempFile + ".sorted").renameTo(new File(tempFile)))
      throw new IOException("Couldn't rename!");

    IndexedDoc indexedDoc = new IndexedDoc();
    SequenceFile.Reader reader = new SequenceFile.Reader(tempFile);
    try {
      int duplicateCount = 0;
      MD5Hash prevHash = null;                    // previous hash
      while (reader.next(indexedDoc, NullWritable.get())) {
        if (prevHash == null) {                   // initialize prevHash
          prevHash = new MD5Hash();
          prevHash.set(indexedDoc.hash);
          continue;
        }
        if (indexedDoc.hash.equals(prevHash)) {   // found a duplicate
          readers[indexedDoc.index].delete(indexedDoc.doc); // delete it
          duplicateCount++;
        } else {
          prevHash.set(indexedDoc.hash);          // reset prevHash
        }
      }
      return duplicateCount;
    } finally {
      reader.close();
      new File(tempFile).delete();
    }
  }

  /** Delete duplicates in the indexes in the named directory. */
  public static void main(String[] args) throws Exception {
    String usage = "DeleteDuplicates <segmentsDir> <tempFile>";

    if (args.length != 2) {
      System.err.println("Usage: " + usage);
      return;
    } 

    String segmentsDir = args[0];
    String tempFile = args[1];

    File[] directories = new File(segmentsDir).listFiles();
    Vector vReaders=new Vector();
    //IndexReader[] readers = new IndexReader[directories.length];
    int maxDoc = 0;
    for (int i = 0; i < directories.length; i++) {
      File indexDone = new File(directories[i], IndexSegment.DONE_NAME);
      if(indexDone.exists() && indexDone.isFile()){
        File indexDir = new File(directories[i], "index");

      	IndexReader reader = IndexReader.open(indexDir);
        if (reader.hasDeletions()) {
          LOG.info("Clearing old deletions in " + indexDir);
          reader.undeleteAll();
        }
        maxDoc += reader.maxDoc();
        vReaders.add(reader);
      }
    }

    IndexReader[] readers=new IndexReader[vReaders.size()];
    for(int i = 0; vReaders.size()>0; i++) {
      readers[i]=(IndexReader)vReaders.remove(0);
    }

    DeleteDuplicates dd = new DeleteDuplicates(readers, tempFile);
    dd.deleteUrlDuplicates();
    dd.deleteContentDuplicates();
    dd.close();
  }
}
