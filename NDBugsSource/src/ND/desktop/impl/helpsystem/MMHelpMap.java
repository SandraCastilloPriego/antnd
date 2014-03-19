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
package ND.desktop.impl.helpsystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.Map;
import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * @author Taken from MZmine2 http://mzmine.sourceforge.net/
 */
public class MMHelpMap implements Map {

        private HelpSet helpset; // the top HelpSet
        private HashMap<String, String> lookup = null;
        private boolean test;

        public MMHelpMap(boolean test) {
                lookup = new HashMap<>();
                this.helpset = new HelpSet();
                this.test = test;
        }

        public void setTarget(String target) {
                String url;
                if (test) {
                        url = "jar:file:" + System.getProperty("user.dir") + "/dist/ND.jar!/" + target;
                } else {
                        url = "jar:file:" + System.getProperty("user.dir") + "/ND.jar!/" + target;
                }

                lookup.put(target, url);
        }

        public void setTargetImage(String target) {
                String url = "file:" + System.getProperty("user.dir") + "/icons/" + target;
                lookup.put(target, url);
        }

        /**
         * The HelpSet for this Map.
         */
        public HelpSet getHelpSet() {
                return helpset;
        }

        /**
         * Determines whether the given ID is valid. If hs is null it is
         * ignored.
         *
         * @param id The String ID.
         * @param hs The HelpSet against which to resolve the string.
         * @return True if id is valid, false if not valid.
         */
        @Override
        public boolean isValidID(String id, HelpSet hs) {
                return lookup.containsKey(id);
        }

        /**
         * Gets an enumeration of all the IDs in a Map.
         *
         * @return An enumeration of all the IDs in a Map.
         */
        @Override
        public Enumeration<String> getAllIDs() {
                //return new FlatEnumeration(lookup.keys(), helpset);
                return new IteratorEnumeration(lookup.keySet().iterator());
        }

        /**
         * Gets the URL that corresponds to a given ID in the map.
         *
         * @param iden The iden to get the URL for. If iden is null it is
         * treated as an unresolved ID and will return null.
         * @return URL The matching URL. Null if this map cannot solve the ID
         * @exception MalformedURLException if the URLspecification found is
         * malformed
         */
        @Override
        public URL getURLFromID(ID iden) throws MalformedURLException {

                String id = iden.id;
                if (id == null) {
                        return null;
                }
                String tmp = null;
                try {
                        tmp = (String) lookup.get(id);
                        URL back = new URL(tmp);
                        return back;
                } catch (MissingResourceException e) {
                        return null;
                }
        }

        /**
         * Determines if the URL corresponds to an ID in the Map.
         *
         * @param url The URL to check on.
         * @return true If this is an ID, otherwise false.
         */
        @Override
        public boolean isID(URL url) {
                URL tmp;
                for (Enumeration<String> e = getAllIDs(); e.hasMoreElements();) {
                        try {
                                String key = (String) e.nextElement();
                                tmp = new URL((String) lookup.get(key));
                                // sameFile() ignores the anchor! - epll
                                if (url.sameFile(tmp) == true) {
                                        return true;
                                }
                        } catch (Exception ex) {
                        }
                }
                return false;
        }

        /**
         * Gets the ID for this URL.
         *
         * @param url The URL to get the ID for.
         * @return The id (Map.ID) or null if URL is not an ID.
         */
        @Override
        public ID getIDFromURL(URL url) {
                String tmp;
                URL tmpURL;
                if (url == null) {
                        return null;
                }
                String urlString = url.toExternalForm();
                for (Enumeration<String> e = getAllIDs(); e.hasMoreElements();) {
                        String key = (String) e.nextElement();
                        try {
                                tmp = (String) lookup.get(key);
                                tmpURL = new URL(tmp);

                                // Sometimes tmp will be null because not all keys are ids
                                if (tmpURL == null) {
                                        continue;
                                }
                                String tmpString = tmpURL.toExternalForm();
                                if (urlString.compareTo(tmpString) == 0) {
                                        return ID.create(key, helpset);
                                }
                        } catch (MalformedURLException | BadIDException ex) {
                        }
                }
                return null;
        }

        /**
         * Determines the ID that is "closest" to this URL (with a given
         * anchor).
         *
         * The definition of this is up to the implementation of Map. In
         * particular, it may be the same as getIDFromURL().
         *
         * @param url A URL
         * @return The closest ID in this map to the given URL
         */
        @Override
        public ID getClosestID(URL url) {
                return getIDFromURL(url);
        }

        /**
         * Determines the IDs related to this URL.
         *
         * @param URL The URL to compare the Map IDs to.
         * @return Enumeration of Map.IDs
         */
        @Override
        public Enumeration<Object> getIDs(URL url) {
                String tmp = null;
                URL tmpURL = null;
                List<String> ids = new ArrayList<>();
                for (Enumeration<String> e = getAllIDs(); e.hasMoreElements();) {
                        String key = (String) e.nextElement();
                        try {
                                tmp = (String) lookup.get(key);
                                tmpURL = new URL(tmp);
                                if (url.sameFile(tmpURL) == true) {
                                        ids.add(key);
                                }
                        } catch (Exception ex) {
                        }
                }
                return new FlatEnumeration(ids.listIterator(), helpset);
        }

        private static class FlatEnumeration implements Enumeration<Object> {

                private ListIterator<String> e;
                private HelpSet hs;

                public FlatEnumeration(ListIterator<String> e, HelpSet hs) {
                        this.e = e;
                        this.hs = hs;
                }

                @Override
                public boolean hasMoreElements() {
                        return e.hasNext();
                }

                @Override
                public Object nextElement() {
                        Object back = null;
                        try {
                                back = ID.create((String) e.next(), hs);
                        } catch (Exception ex) {
                        }
                        return back;
                }
        }
}
