/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.examples.modules.minicomposer;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
public class LineInFromScore implements TargetDataLine {
    private AudioFormat format;
    private float sampleRate;
    private static class MyInputStream extends AudioInputStream {
        public MyInputStream (LineInFromScore me) throws IOException {
            super (me);
            Score s = me.s;
            frameLength = 0;
            float sampleRate = ComposerSettings.DEFAULT.getSampleRate ();
            for (int i = 0; i < s.getSize (); i++)
                frameLength += (int) (Score.DURATION_SECONDS[s.getDuration (i)] * sampleRate);
        }
    }
    public static AudioInputStream makeStream (InputStream is) throws IOException {
        return new MyInputStream (new LineInFromScore (is));
    }

    /**
     * @associates LineListener 
     */
    private Set listeners;
    private int frame;
    private Score s;
    private int note;
    private int framesInNote;
    private int posInNote;
    private float frameToRadian;
    private boolean atEnd;
    private boolean isRest;
    public LineInFromScore (InputStream is) throws IOException {
        s = Score.parse (new InputStreamReader (is));
        listeners = new HashSet ();
        frame = 0;
        note = -1;
        atEnd = false;
        sampleRate = ComposerSettings.DEFAULT.getSampleRate ();
        format = new AudioFormat (sampleRate, 8, 1, true, /* irrelevant */ false);
        update ();
    }
    public int read (byte[] b, int off, int len) {
        for (int i = 0; i < len; i++) {
            if (atEnd) return i;
            frame++;
            b[off + i] = isRest ? (byte) 0 :
                         (byte) (127.0f * Math.sin (posInNote * frameToRadian));
            if (++posInNote == framesInNote) update ();
        }
        return len;
    }
    private void update () {
        note++;
        if (note >= s.getSize ()) {
            atEnd = true;
            return;
        }
        int tone = s.getTone (note);
        int octave = s.getOctave (note);
        int dur = s.getDuration (note);
        framesInNote = (int) (Score.DURATION_SECONDS[dur] * sampleRate);
        posInNote = 0;
        if (tone == 0) {
            // Rest.
            isRest = true;
        } else {
            isRest = false;
            float hertz = (float) (Score.MIDDLE_C_HERTZ *
                                   Math.pow (Score.HALF_STEP, tone - Score.WHERE_IS_C_TONE) *
                                   Math.pow (2.0, octave - Score.WHERE_IS_MIDDLE_OCTAVE));
            frameToRadian = 2.0f * (float) Math.PI / sampleRate * hertz;
        }
    }
    public int available () {
        if (atEnd)
            return 0;
        else
            return framesInNote - posInNote;
    }
    public synchronized void addLineListener (LineListener listener) {
        listeners.add (listener);
    }
    public synchronized void removeLineListener (LineListener listener) {
        listeners.remove (listener);
    }
    protected synchronized void fireEvent (LineEvent.Type type) {
        LineEvent ev = new LineEvent (this, type, frame);
        Iterator it = listeners.iterator ();
        while (it.hasNext ()) {
            LineListener listener = (LineListener) it.next ();
            listener.update (ev);
        }
    }
    public boolean isOpen () {
        return true;
    }
    public void open () throws LineUnavailableException {
        fireEvent (LineEvent.Type.OPEN);
    }
    public void open (AudioFormat format) throws LineUnavailableException {
        if (! format.equals (this.format)) throw new LineUnavailableException ();
        open ();
    }
    public void open (AudioFormat format, int bufferSize) throws LineUnavailableException {
        open (format);
    }
    public Control getControl (Control.Type control) {
        throw new IllegalArgumentException ();
    }
    public void flush () {
    }
    public void close () {
        fireEvent (LineEvent.Type.CLOSE);
    }
    public boolean isControlSupported (Control.Type control) {
        return false;
    }
    public Line.Info getLineInfo () {
        return new DataLine.Info (LineInFromScore.class, format, 1024);
    }
    public int getFramePosition () {
        return frame;
    }
    public boolean isActive () {
        return true;
    }
    public void drain () {
    }
    public boolean isRunning () {
        return true;
    }
    public void stop () {
    }
    public float getLevel () {
        return 1.0f;
    }
    public Control[] getControls () {
        return new Control[] { };
    }
    public int getBufferSize () {
        return 1024;
    }
    public void start () {
    }
    public AudioFormat getFormat () {
        return format;
    }
    public long getMicrosecondPosition () {
        return (long) (1000000.0f * frame / sampleRate);
    }
}
