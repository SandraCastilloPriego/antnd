/*
 * Copyright 2007-2012 
 * This file is part of AntND.
 *
 * AntND is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * AntND is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.desktop.impl.helpsystem;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import javax.help.Map;
import javax.help.Map.ID;
import javax.help.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 */
public class NDTOCView extends TOCView {

        private NDHelpMap hm;
        private HelpSet hs;
        private File file;

        public NDTOCView(HelpSet hs, String name, String label, NDHelpMap hm, File file) {
                super(hs, name, label, null);
                this.hm = hm;
                this.hs = hs;
                this.file = file;
        }

        /**
         * Public method that gets a DefaultMutableTreeNode representing the
         * information in this view instance.
         */
        @Override
        public DefaultMutableTreeNode getDataAsTree() {

                try {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode();

                        TreeSet<TOCItem> sortedItems = new TreeSet<>(new TOCItemSorterByName());

                        List<String> list = Collections.list(hm.getAllIDs());
                        Collections.sort(list);
                        Iterator<String> e = list.iterator();

                        while (e.hasNext()) {
                                String target = (String) e.next();
                                if (target.contains(".png")) {
                                        continue;
                                }
                                sortedItems.add((TOCItem) createMyItem(target));
                                System.out.print(target + "\n");
                        }

                        Iterator<TOCItem> i = sortedItems.iterator();

                        while (i.hasNext()) {
                                TOCItem item = i.next();
                                DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(item);
                                node.add(newChild);
                        }

                        return node;

                } catch (Exception ex) {
                        throw new Error("Trouble creating TOC data progamatically; " + ex);
                }

        }

        /**
         * Create an TOCItem with the given data.
         *
         * @param tagName
         *            The TOC type to create. Valid types are "tocitem". Null or
         *            invalid types will throw an IllegalArgumentException
         * @param atts
         *            Attributes of the Item. Valid attributes are "target",
         *            "image", and "text". A null atts is valid and means no
         *            attributes
         * @param hs
         *            HelpSet this item was created under.
         * @param locale
         *            Locale of this item. A null locale is valid.
         * @returns A fully constructed TreeItem.
         * @throws IllegalArgumentExcetpion
         *             if tagname is null or invalid.
         */
        public TreeItem createMyItem(String target) {

                String line, title = "Test";
                try {
                        JarFile jarFile = new JarFile(file);
                        InputStream test = jarFile.getInputStream(jarFile.getEntry(target));
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(test))) {
                                if (!in.ready()) {
                                        throw new IOException();
                                }

                                while ((line = in.readLine()) != null) {
                                        if (line.toLowerCase().contains("title")) {
                                                int beginIndex = line.toLowerCase().indexOf("title") + 6;
                                                int endIndex = line.toLowerCase().indexOf("</title>");
                                                title = line.substring(beginIndex, endIndex);
                                                break;
                                        }
                                }
                        }
                } catch (IOException e) {
                }

                Map.ID mapID = null;
                try {
                        mapID = ID.create(target, hs);
                } catch (BadIDException bex1) {
                }

                Map.ID imageMapID = null;
                String imageID = "topic.png";
                try {
                        imageMapID = ID.create(imageID, hs);
                } catch (BadIDException bex2) {
                }

                TOCItem item = new TOCItem(mapID, imageMapID, hs, Locale.getDefault());
                item.setName(title);
                item.setMergeType("javax.help.AppendMerge");
                item.setExpansionType(TreeItem.COLLAPSE);

                return item;
        }

        /**
         * Creates a default TOCItem.
         */
        public TreeItem createItem() {
                return new TOCItem();
        }
}
