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
package ND.data.parser.impl;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.parser.Parser;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

/**
 *
 * @author scsandra
 */
public class BasicFilesParserSBML implements Parser {

        private String datasetPath;
        private SimpleBasicDataset dataset;
        private int rowsNumber;
        private int rowsReaded;
        private SBMLDocument document;

        public BasicFilesParserSBML(String datasetPath) {
                this.rowsNumber = 0;
                this.rowsReaded = 0;
                this.datasetPath = datasetPath;
                dataset = new SimpleBasicDataset();
        }

        @Override
        public void createDataset() {
                try {
                        this.document = SBMLReader.read(new File(this.datasetPath));
                        dataset.setDocument(document);
                        dataset.setDatasetName(document.getModel().getId());
                } catch (XMLStreamException | IOException ex) {
                        Logger.getLogger(BasicFilesParserSBML.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public float getProgress() {
                return (float) rowsReaded / rowsNumber;
        }

        @Override
        public Dataset getDataset() {
                return this.dataset;
        }

      
}
