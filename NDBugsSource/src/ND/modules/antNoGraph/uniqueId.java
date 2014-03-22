/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph;

/**
 *
 * @author scsandra
 */
public class uniqueId {

        private static final String alphabet =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static int currentId;

        public static String nextId() {
                int id = currentId++;
                StringBuilder b = new StringBuilder();
                do {
                        b.append(alphabet.charAt(id % alphabet.length()));
                } while ((id /= alphabet.length()) != 0);

                return b.toString();
        }
}
