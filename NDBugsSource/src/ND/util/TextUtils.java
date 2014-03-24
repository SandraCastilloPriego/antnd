/*
 * Copyright 2007-2012 
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 * Text processing utilities
 */
public class TextUtils {

        /**
         * Wraps the words of the given (long) text to several lines of maximum
         * given length
         */
        public static String wrapText(String text, int len) {

                // return text if less than length
                if (text.length() <= len) {
                        return text;
                }

                StringBuilder result = new StringBuilder();
                StringBuilder line = new StringBuilder();
                StringBuffer word = new StringBuffer();

                char[] chars = text.toCharArray();

                for (int i = 0; i < chars.length; i++) {
                        word.append(chars[i]);

                        if (chars[i] == ' ') {
                                if ((line.length() + word.length()) > len) {
                                        if (result.length() != 0) {
                                                result.append("\n");
                                        }
                                        result.append(line.toString());
                                        line.delete(0, line.length());
                                }

                                line.append(word);
                                word.delete(0, word.length());
                        }
                }

                // handle any extra chars in current word
                if (word.length() > 0) {
                        if ((line.length() + word.length()) > len) {
                                if (result.length() != 0) {
                                        result.append("\n");
                                }
                                result.append(line.toString());
                                line.delete(0, line.length());
                        }
                        line.append(word);
                }

                // handle extra line
                if (line.length() > 0) {
                        result.append("\n");
                        result.append(line.toString());
                }

                return result.toString();
        }

        /**
         * Reads a line of text from a given input stream or null if the end of the
         * stream is reached.
         */
        public static String readLineFromStream(InputStream in) throws IOException {
                byte buf[] = new byte[1024];
                int pos = 0;
                while (true) {
                        int ch = in.read();
                        if ((ch == '\n') || (ch < 0)) {
                                break;
                        }
                        buf[pos++] = (byte) ch;
                        if (pos == buf.length) {
                                buf = Arrays.copyOf(buf, pos * 2);
                        }
                }
                if (pos == 0) {
                        return null;
                }

                return new String(Arrays.copyOf(buf, pos), "UTF-8");
        }
}
