/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.apache.lucene.util.PriorityQueue;
import net.nutch.util.*;

/** Support for flat files of binary key/value pairs. */
public class SequenceFile {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.io.SequenceFile");

  private SequenceFile() {}                         // no public ctor

  private static byte[] VERSION = new byte[] {
    (byte)'S', (byte)'E', (byte)'Q', 1
  };

  /** Write key/value pairs to a sequence-format file. */
  public static class Writer {
    private BufferedRandomAccessFile out;
    private DataOutputBuffer buffer = new DataOutputBuffer();

    private Class keyClass;
    private Class valClass;

    /** Create the named file. */
    public Writer(String name, Class keyClass, Class valClass)
      throws IOException {
      File file = new File(name);
      if (file.exists())
        throw new IOException("already exists: " + file);

      init(new BufferedRandomAccessFile(name, false), keyClass, valClass);
    }
    
    /** Write to an arbitrary stream using a specified buffer size. */
    private Writer(BufferedRandomAccessFile out,
                   Class keyClass, Class valClass) throws IOException {
      init(out, keyClass, valClass);
    }
    
    private void init(BufferedRandomAccessFile out,
                      Class keyClass, Class valClass) throws IOException {
      this.out = out;
      this.out.write(VERSION);

      this.keyClass = keyClass;
      this.valClass = valClass;

      new UTF8(WritableName.getName(keyClass)).write(this.out);
      new UTF8(WritableName.getName(valClass)).write(this.out);
    }
    

    /** Returns the class of keys in this file. */
    public Class getKeyClass() { return keyClass; }

    /** Returns the class of values in this file. */
    public Class getValueClass() { return valClass; }


    /** Close the file. */
    public void close() throws IOException {
      if (out != null) {
        out.close();
        out = null;
      }
    }

    /** Append a key/value pair. */
    public void append(Writable key, Writable val) throws IOException {
      if (key.getClass() != keyClass)
        throw new IOException("wrong key class: "+key+" is not "+keyClass);
      if (val.getClass() != valClass)
        throw new IOException("wrong value class: "+val+" is not "+valClass);

      buffer.reset();

      key.write(buffer);
      int keyLength = buffer.getLength();
      if (keyLength == 0)
        throw new IOException("zero length keys not allowed: " + key);

      val.write(buffer);
      //System.out.println("Appending " + key + ", " + val);
      append(buffer.getData(), 0, buffer.getLength(), keyLength);
    }

    /** Append a key/value pair. */
    public void append(byte[] data, int start, int length, int keyLength)
      throws IOException {
      if (keyLength == 0)
        throw new IOException("zero length keys not allowed");

      out.writeInt(length);                       // total record length
      out.writeInt(keyLength);                    // key portion length
      out.write(data, start, length);             // data
    }

    /** Returns the current length of the output file. */
    public long getLength() {
      return out.getFilePointer();
    }

  }

  /** Writes key/value pairs from a sequence-format file. */
  public static class Reader {
    private String file;
    private BufferedRandomAccessFile in;
    private DataOutputBuffer outBuf = new DataOutputBuffer();
    private DataInputBuffer inBuf = new DataInputBuffer();

    private Class keyClass;
    private Class valClass;

    private long end;
    private int keyLength;

    /** Open the named file. */
    public Reader(String file) throws IOException {
      this(file, 4096);
    }

    private Reader(String file, int bufferSize) throws IOException {
      this.file = file;
      this.in = new BufferedRandomAccessFile(file, bufferSize, true);
      this.end = new File(file).length();
      init();
    }
    
    private Reader(String file, int bufferSize, long start, long length)
      throws IOException {
      this.file = file;
      this.in = new BufferedRandomAccessFile(file, bufferSize, true);

      seek(start);
      init();

      this.end = in.getFilePointer() + length;
    }
    
    private void init() throws IOException {
      byte[] version = new byte[VERSION.length];
      in.readFully(version);
      if (!Arrays.equals(version, VERSION))
        throw new VersionMismatchException(VERSION[3], version[3]);

      UTF8 className = new UTF8();
      
      className.readFields(in);                   // read key class name
      this.keyClass = WritableName.getClass(className.toString());
      
      className.readFields(in);                   // read val class name
      this.valClass = WritableName.getClass(className.toString());
    }
    
    /** Close the file. */
    public synchronized void close() throws IOException {
      in.close();
    }

    /** Returns the class of keys in this file. */
    public Class getKeyClass() { return keyClass; }

    /** Returns the class of values in this file. */
    public Class getValueClass() { return valClass; }

    /** Read the next key in the file into <code>key</code>, skipping its
     * value.  True if another entry exists, and false at end of file. */
    public synchronized boolean next(Writable key) throws IOException {
      if (key.getClass() != keyClass)
        throw new IOException("wrong key class: "+key+" is not "+keyClass);

      outBuf.reset();

      keyLength = next(outBuf);
      if (keyLength < 0)
        return false;

      inBuf.reset(outBuf.getData(), outBuf.getLength());

      key.readFields(inBuf);
      if (inBuf.getPosition() != keyLength)
        throw new IOException(key + " read " + inBuf.getPosition()
                              + " bytes, should read " + keyLength);
      return true;
    }

    /** Read the next key/value pair in the file into <code>key</code> and
     * <code>val</code>.  Returns true if such a pair exists and false when at
     * end of file */
    public synchronized boolean next(Writable key, Writable val)
      throws IOException {
      if (val.getClass() != valClass)
        throw new IOException("wrong value class: "+val+" is not "+valClass);

      boolean more = next(key);

      if (more) {
        val.readFields(inBuf);
        if (inBuf.getPosition() != outBuf.getLength())
          throw new IOException(val+" read "+(inBuf.getPosition()-keyLength)
                                + " bytes, should read " +
                                (outBuf.getLength()-keyLength));
      }

      return more;
    }

    /** Read the next key/value pair in the file into <code>buffer</code>.
     * Returns the length of the key read, or -1 if at end of file.  The length
     * of the value may be computed by calling buffer.getLength() before and
     * after calls to this method. */
    public synchronized int next(DataOutputBuffer buffer) throws IOException {
      if (in.getFilePointer() >= end)
        return -1;

      int length = in.readInt();
      int keyLength = in.readInt();
      buffer.write(in, length);

      return keyLength;
    }

    /** Set the current byte position in the input file. */
    public synchronized void seek(long position) throws IOException {
      in.seek(position);
    }

    /** Return the current byte position in the input file. */
    public synchronized long getPosition() {
      return in.getFilePointer();
    }

    /** Returns the name of the file. */
    public String toString() {
      return file;
    }

  }

  /** Adds buffering to {@link RandomAccessFile}, which is not an InputStream
   * or an OutputStream, so BufferedInputStream and BufferredOutputStream
   * cannot be used. */
  private static class BufferedRandomAccessFile extends RandomAccessFile {
    private byte[] buf;
    private int pos;
    private int count;
    private long filePointer;
    private boolean isReadOnly;                   // if false, then writeOnly
    
    public BufferedRandomAccessFile(String file, boolean isReadOnly)
      throws IOException {
      this(file, 4096, isReadOnly);
    }

    public BufferedRandomAccessFile(String file, int bufLen,
                                    boolean isReadOnly) throws IOException {
      super(file, isReadOnly ? "r" : "rw");
      this.buf = new byte[bufLen];
      this.isReadOnly = isReadOnly;
    }

    /** Override unbuffered implementation. */
    public int read() throws IOException {
      if (pos >= count) {
        fill();
        if (pos >= count)
          return -1;
      }
      return buf[pos++] & 0xff;
    }

    private void fill() throws IOException {
      if (!isReadOnly) throw new IOException("can't read write-only file");
      pos = 0;
      count = pos;
      int n = super.read(buf, 0, buf.length);
      if (n > 0) {
        count = n;                                // update count
        filePointer += n;                         // update pointer
      }
    }

    /** Override unbuffered implementation. */
    public int read(byte[] b, int off, int len) throws IOException {
      int avail = count - pos;
      if (avail <= 0) {
        if (len >= buf.length) {
          int n = super.read(b, off, len);
          if (n > 0)
            filePointer += n;                     // update pointer
          return n;
        }
        fill();
        avail = count - pos;
        if (avail <= 0) return -1;
      }
      int cnt = (avail < len) ? avail : len;
      System.arraycopy(buf, pos, b, off, cnt);
      pos += cnt;
      return cnt;
    }

    /** Override unbuffered implementation. */
    public int read(byte b[]) throws IOException {
	return read(b, 0, b.length);
    }

    /** Override unbuffered implementation. */
    public void write(int b) throws IOException {
      if (count >= buf.length) {
        flushBuffer();
      }
      buf[count++] = (byte)b;
    }

    /** Override unbuffered implementation. */
    public void write(byte b[], int off, int len) throws IOException {
      if (len >= buf.length) {
        flushBuffer();
        super.write(b, off, len);
        filePointer += len;
        return;
      }
      if (len > buf.length - count) {
        flushBuffer();
      }
      System.arraycopy(b, off, buf, count, len);
      count += len;
    }

    /** Override unbuffered implementation. */
    public void write(byte b[]) throws IOException {
      write(b, 0, b.length); 
    }

    private void flushBuffer() throws IOException {
      if (count > 0) {
        super.write(buf, 0, count);
        filePointer += count;
        count = 0;
      }
    }

    /** Override unbuffered implementation. */
    public void seek(long desired) throws IOException {
      if (!isReadOnly) throw new IOException("can't seek write-only file");
      long current = getFilePointer();
      long start = (current - pos);
      if (desired >= start && desired < start + count) {
        // can position within buffer
        pos += (desired - current);
      } else {
        count = 0;                                // invalidate buffer
        pos = 0;
        super.seek(desired);                      // seek underlying stream
        filePointer = desired;                    // update pointer
      }
    }

    /** Override unbuffered implementation. */
    public long getFilePointer() {
      if (isReadOnly)
        return filePointer - (count - pos);
      else
        return filePointer + count;
    }

    /** Override unbuffered implementation. */
    public void close() throws IOException {
      if (!isReadOnly)
        flushBuffer();
      super.close();
    }
  }

  /** Sorts key/value pairs in a sequence-format file.
   *
   * <p>For best performance, applications should make sure that the {@link
   * Writable#readFields(DataInput)} implementation of their keys is
   * very efficient.  In particular, it should avoid allocating memory.
   */
  public static class Sorter {
    private static final int FACTOR = NutchConf.getInt("io.sort.factor", 100); 
    private static final int MEGABYTES = NutchConf.getInt("io.sort.mb", 100); 

    private WritableComparator comparator;

    private String inFile;                        // when sorting
    private String[] inFiles;                     // when merging

    private String outFile;

    private int memory = MEGABYTES * 1024*1024;   // bytes
    private int factor = FACTOR;                  // merged per pass

    private Class keyClass;
    private Class valClass;

    /** Sort and merge files containing the named classes. */
    public Sorter(Class keyClass, Class valClass)  {
      this(new WritableComparator(keyClass), valClass);
    }

    /** Sort and merge using an arbitrary {@link WritableComparator}. */
    public Sorter(WritableComparator comparator, Class valClass) {
      this.comparator = comparator;
      this.keyClass = comparator.getKeyClass();
      this.valClass = valClass;
    }

    /** Set the number of streams to merge at once.*/
    public void setFactor(int factor) { this.factor = factor; }

    /** Get the number of streams to merge at once.*/
    public int getFactor() { return factor; }

    /** Set the total amount of buffer memory, in bytes.*/
    public void setMemory(int memory) { this.memory = memory; }

    /** Get the total amount of buffer memory, in bytes.*/
    public int getMemory() { return memory; }

    /** Perform a file sort.*/
    public void sort(String inFile, String outFile) throws IOException {
      this.inFile = inFile;
      this.outFile = outFile;

      File file = new File(outFile);
      if (file.exists())
        throw new IOException("already exists: " + file);

      int segments = sortPass();
      int pass = 1;
      while (segments > 1) {
        segments = mergePass(pass, segments <= factor);
        pass++;
      }
    }

    private int sortPass() throws IOException {
      LOG.fine("running sort pass");
      SortPass sortPass = new SortPass();         // make the SortPass
      try {
        return sortPass.run();                    // run it
      } finally {
        sortPass.close();                         // close it
      }
    }

    private class SortPass {
      private int limit = memory/4;
      private DataOutputBuffer buffer = new DataOutputBuffer();
      private byte[] rawBuffer;

      private int[] starts = new int[1024];
      private int[] pointers = new int[starts.length];
      private int[] pointersCopy = new int[starts.length];
      private int[] keyLengths = new int[starts.length];
      private int[] lengths = new int[starts.length];
      
      private Reader in;
      private BufferedRandomAccessFile out;

      public SortPass() throws IOException {
        in = new Reader(inFile);
      }
      
      public int run() throws IOException {
        int segments = 0;
        boolean atEof = false;
        while (!atEof) {
          int count = 0;
          buffer.reset();
          while (!atEof && buffer.getLength() < limit) {

            int start = buffer.getLength();       // read an entry into buffer
            int keyLength = in.next(buffer);
            int length = buffer.getLength() - start;

            if (keyLength == -1) {
              atEof = true;
              break;
            }

            if (count == starts.length)
              grow();

            starts[count] = start;                // update pointers
            pointers[count] = count;
            lengths[count] = length;
            keyLengths[count] = keyLength;

            count++;
          }

          // buffer is full -- sort & flush it
          LOG.finer("flushing segment " + segments);
          rawBuffer = buffer.getData();
          sort(count);
          flush(count, segments==0 && atEof);
          segments++;
        }
        return segments;
      }

      public void close() throws IOException {
        in.close();

        if (out != null)
          out.close();
      }

      private void grow() {
        int newLength = starts.length * 3 / 2;
        starts = grow(starts, newLength);
        pointers = grow(pointers, newLength);
        pointersCopy = new int[newLength];
        keyLengths = grow(keyLengths, newLength);
        lengths = grow(lengths, newLength);
      }

      private int[] grow(int[] old, int newLength) {
        int[] result = new int[newLength];
        System.arraycopy(old, 0, result, 0, old.length);
        return result;
      }

      private void flush(int count, boolean done) throws IOException {
        if (out == null) {
          String outName = done ? outFile : outFile+".0";
          out = new BufferedRandomAccessFile(outName, false);
        }

        if (!done) {                              // an intermediate file
          long length = buffer.getLength() + count*8;
          out.writeLong(length);                  // write size
        }

        Writer writer = new Writer(out, keyClass, valClass);

        for (int i = 0; i < count; i++) {         // write in sorted order
          int p = pointers[i];
          writer.append(rawBuffer, starts[p], lengths[p], keyLengths[p]);
        }
      }

      private void sort(int count) {
        System.arraycopy(pointers, 0, pointersCopy, 0, count);
        mergeSort(pointersCopy, pointers, 0, count);
      }

      private int compare(int i, int j) {
        return comparator.compare(rawBuffer, starts[i], keyLengths[i],
                                  rawBuffer, starts[j], keyLengths[j]);
      }

      private void mergeSort(int src[], int dest[], int low, int high) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7) {
          for (int i=low; i<high; i++)
            for (int j=i; j>low && compare(dest[j-1], dest[j])>0; j--)
              swap(dest, j, j-1);
          return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high) >> 1;
        mergeSort(dest, src, low, mid);
        mergeSort(dest, src, mid, high);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (compare(src[mid-1], src[mid]) <= 0) {
          System.arraycopy(src, low, dest, low, length);
          return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = low, p = low, q = mid; i < high; i++) {
          if (q>=high || p<mid && compare(src[p], src[q]) <= 0)
            dest[i] = src[p++];
          else
            dest[i] = src[q++];
        }
      }

      private void swap(int x[], int a, int b) {
	int t = x[a];
	x[a] = x[b];
	x[b] = t;
      }
    }

    private int mergePass(int pass, boolean last) throws IOException {
      LOG.fine("running merge pass=" + pass);
      MergePass mergePass = new MergePass(pass, last);
      try {                                       // make a merge pass
        return mergePass.run();                  // run it
      } finally {
        mergePass.close();                       // close it
      }
    }

    private class MergePass {
      private int pass;
      private boolean last;

      private MergeQueue queue;
      private RandomAccessFile in;
      private String inName;

      public MergePass(int pass, boolean last) throws IOException {
        this.pass = pass;
        this.last = last;

        this.queue = new MergeQueue(factor, last ? outFile : outFile+"."+pass);

        this.inName = outFile+"."+(pass-1);
        this.in = new RandomAccessFile(inName, "r");
      }

      public void close() throws IOException {
        in.close();                               // close and delete input
        new File(inName).delete();

        queue.close();                            // close queue
      }

      public int run() throws IOException {
        int segments = 0;
        long end = in.length();

        while (in.getFilePointer() < end) {
          LOG.finer("merging segment " + segments);
          long totalLength = 0;
          while (in.getFilePointer() < end && queue.size() < factor) {
            long length = in.readLong();
            totalLength += length;
            Reader reader = new Reader(inName, memory/(factor+1),
                                       in.getFilePointer(), length);
            MergeStream ms = new MergeStream(reader); // add segment to queue
            if (ms.next())
              queue.put(ms);
            in.seek(reader.end);
          }

          if (!last)                              // intermediate file
            queue.out.writeLong(totalLength);     // write sizes

          queue.merge();                          // do a merge

          segments++;
        }

        return segments;
      }
    }

    /** Merge the provided files.*/
    public void merge(String[] inFiles, String outFile) throws IOException {
      this.inFiles = inFiles;
      this.outFile = outFile;
      this.factor = inFiles.length;

      File file = new File(outFile);
      if (file.exists())
        throw new IOException("already exists: " + file);

      MergeFiles mergeFiles = new MergeFiles();
      try {                                       // make a merge pass
        mergeFiles.run();                         // run it
      } finally {
        mergeFiles.close();                       // close it
      }

    }

    private class MergeFiles {
      private MergeQueue queue;

      public MergeFiles() throws IOException {
        this.queue = new MergeQueue(factor, outFile);
      }

      public void close() throws IOException {
        queue.close();
      }

      public void run() throws IOException {
        LOG.finer("merging files=" + inFiles.length);
        for (int i = 0; i < inFiles.length; i++) {
          String inFile = inFiles[i];
          MergeStream ms =
            new MergeStream(new Reader(inFile, memory/(factor+1)));
          if (ms.next())
            queue.put(ms);
        }

        queue.merge();
      }
    }

    private class MergeStream {
      private Reader in;

      private DataOutputBuffer buffer = new DataOutputBuffer();
      private int keyLength;
      
      public MergeStream(Reader reader) throws IOException {
        if (reader.keyClass != keyClass)
          throw new IOException("wrong key class: " + reader.getKeyClass() +
                                " is not " + keyClass);
        if (reader.valClass != valClass)
          throw new IOException("wrong value class: "+reader.getValueClass()+
                                " is not " + valClass);
        this.in = reader;
      }

      public boolean next() throws IOException {
        buffer.reset();
        keyLength = in.next(buffer);
        return keyLength >= 0;
      }
    }

    private class MergeQueue extends PriorityQueue {
      private BufferedRandomAccessFile out;

      public MergeQueue(int size, String outName) throws IOException {
        initialize(size);
        this.out =
          new BufferedRandomAccessFile(outName, memory/(factor+1), false);
      }

      protected boolean lessThan(Object a, Object b) {
        MergeStream msa = (MergeStream)a;
        MergeStream msb = (MergeStream)b;
        return comparator.compare(msa.buffer.getData(), 0, msa.keyLength,
                                  msb.buffer.getData(), 0, msb.keyLength) < 0;
      }

      public void merge() throws IOException {
        Writer writer = new Writer(out, keyClass, valClass);

        while (size() != 0) {
          MergeStream ms = (MergeStream)top();
          DataOutputBuffer buffer = ms.buffer;    // write top entry
          writer.append(buffer.getData(), 0, buffer.getLength(), ms.keyLength);
          
          if (ms.next()) {                        // has another entry
            adjustTop();
          } else {
            pop();                                // done with this file
            ms.in.close();
          }
        }
      }

      public void close() throws IOException {
        MergeStream ms;                           // close inputs
        while ((ms = (MergeStream)pop()) != null) {
          ms.in.close();
        }
        out.close();                              // close output
      }
    }
  }

}
