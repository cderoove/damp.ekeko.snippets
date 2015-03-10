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
import org.openide.util.NbBundle;
public class Score {
    public static final String[] TONES_SHORT = new String[] {
                "-", "C", "D", "d", "E", "e", "F", "G", "g", "A", "a", "B", "b"
            };
    private static final String[] KEYS4_TONES_LONG = new String[] {
                "rest", "c", "d", "d_flat", "e", "e_flat", "f", "g", "g_flat", "a", "a_flat", "b", "b_flat"
            };
    public static final String[] TONES_LONG = new String[KEYS4_TONES_LONG.length];
    public static final int DEFAULT_TONE = 1;
    public static final String[] OCTAVES_SHORT = new String[] {
                "--", "-", ".", "+", "++"
            };
    private static final String[] KEYS4_OCTAVES_LONG = new String[] {
                "low", "middle_low", "middle", "middle_high", "high"
            };
    public static final String[] OCTAVES_LONG = new String[KEYS4_OCTAVES_LONG.length];
    public static final int DEFAULT_OCTAVE = 2;
    public static final float MIDDLE_C_HERTZ = 440.0f;
    public static final int WHERE_IS_C_TONE = 1;
    public static final int WHERE_IS_MIDDLE_OCTAVE = 2;
    public static final float HALF_STEP = (float) Math.pow (2.0, 1.0 / 12.0);
    public static final String[] DURATIONS_SHORT = new String[] {
                "1", "2", "4"
            };
    private static final String[] KEYS4_DURATIONS_LONG = new String[] {
                "quarter", "half", "full"
            };
    public static final String[] DURATIONS_LONG = new String[KEYS4_DURATIONS_LONG.length];
    public static final float[] DURATION_SECONDS = new float[] {
                0.25f, 0.5f, 1.0f
            };
    public static final int DEFAULT_DURATION = 0;
    static {
        ResourceBundle bundle = NbBundle.getBundle (Score.class);
        for (int i = 0; i < TONES_LONG.length; i++)
            TONES_LONG[i] = bundle.getString ("TONE_" + KEYS4_TONES_LONG[i]);
        for (int i = 0; i < OCTAVES_LONG.length; i++)
            OCTAVES_LONG[i] = bundle.getString ("OCTAVE_" + KEYS4_OCTAVES_LONG[i]);
        for (int i = 0; i < DURATIONS_LONG.length; i++)
            DURATIONS_LONG[i] = bundle.getString ("DURATION_" + KEYS4_DURATIONS_LONG[i]);
    }
    private List tones;
    private List octaves;
    private List durations;
    public Score (List tones, List octaves, List durations) {
        this.tones = Collections.unmodifiableList (new ArrayList (tones));
        this.octaves = Collections.unmodifiableList (new ArrayList (octaves));
        this.durations = Collections.unmodifiableList (new ArrayList (durations));
        int len = this.tones.size ();
        if (this.octaves.size () != len || this.durations.size () != len)
            throw new IllegalArgumentException ();
    }
    public int getSize () {
        return tones.size ();
    }
    public int getTone (int pos) {
        return ((Integer) tones.get (pos)).intValue ();
    }
    public int getOctave (int pos) {
        return ((Integer) octaves.get (pos)).intValue ();
    }
    public int getDuration (int pos) {
        return ((Integer) durations.get (pos)).intValue ();
    }
    public boolean equals (Object o) {
        if (o == null || ! (o instanceof Score)) return false;
        Score s = (Score) o;
        return tones.equals (s.tones) && octaves.equals (s.octaves) &&
               durations.equals (s.durations);
    }
    public int hashCode () {
        return Score.class.hashCode () ^ tones.hashCode () ^
               octaves.hashCode () ^ durations.hashCode ();
    }
    public String toString () {
        return "Score[size=" + getSize () + "]";
    }
    public static Score parse (Reader r) throws IOException {
        BufferedReader reader = new BufferedReader (r);
        List tones = new LinkedList ();
        List octaves = new LinkedList ();
        List durations = new LinkedList ();
        String line;
        while ((line = reader.readLine ()) != null) {
            StringTokenizer tok = new StringTokenizer (line, "/");
            if (tok.hasMoreTokens ()) {
                String toneToken = tok.nextToken ();
                if (tok.hasMoreTokens ()) {
                    String octaveToken = tok.nextToken ();
                    if (tok.hasMoreTokens ()) {
                        String durationToken = tok.nextToken ();
                        if (tok.hasMoreTokens ()) {
                            throw new IOException (NbBundle.getBundle (Score.class).getString ("EXC_more_than_3_items_on_line"));
                        } else {
                            int tone = find (toneToken, Score.TONES_SHORT);
                            if (tone == -1) throw new IOException (NbBundle.getBundle (Score.class).getString ("EXC_unknown_tone"));
                            int octave = find (octaveToken, Score.OCTAVES_SHORT);
                            if (octave == -1) throw new IOException (NbBundle.getBundle (Score.class).getString ("EXC_unknown_octave"));
                            int duration = find (durationToken, Score.DURATIONS_SHORT);
                            if (duration == -1) throw new IOException (NbBundle.getBundle (Score.class).getString ("EXC_unknown_duration"));
                            tones.add (new Integer (tone));
                            octaves.add (new Integer (octave));
                            durations.add (new Integer (duration));
                        }
                    } else {
                        throw new IOException (NbBundle.getBundle (Score.class).getString ("EXC_only_2_items_on_line"));
                    }
                } else {
                    throw new IOException (NbBundle.getBundle (Score.class).getString ("EXC_only_one_item_on_line"));
                }
            } else {
                throw new IOException (NbBundle.getBundle (Score.class).getString ("EXC_no_items_on_line"));
            }
        }
        return new Score (tones, octaves, durations);
    }
    public static void generate (Score s, Writer w) throws IOException {
        int len = s.getSize ();
        for (int i = 0; i < len; i++) {
            w.write (TONES_SHORT[s.getTone (i)]);
            w.write ((int) '/');
            w.write (OCTAVES_SHORT[s.getOctave (i)]);
            w.write ((int) '/');
            w.write (DURATIONS_SHORT[s.getDuration (i)]);
            w.write ((int) '\n');
        }
    }
    private static int find (String key, String[] list) {
        for (int i = 0; i < list.length; i++)
            if (key.equals (list[i]))
                return i;
        return -1;
    }
}
