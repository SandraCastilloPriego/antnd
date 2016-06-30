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
package ND.data.parser.impl;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.parser.Parser;
import ND.main.NDCore;
import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

/**
 *
 * @author scsandra
 */
public class BasicFilesParserSBML implements Parser {

        private final String datasetPath;
        private final SimpleBasicDataset dataset;
        private final int rowsNumber;
        private final int rowsReaded;
        private SBMLDocument document;

        public BasicFilesParserSBML(String datasetPath) {
                this.rowsNumber = 0;
                this.rowsReaded = 0;
                this.datasetPath = datasetPath;
                dataset = new SimpleBasicDataset();
        }

        @Override
        public void createDataset(String name) {
                try {
                        this.document = SBMLReader.read(new File(this.datasetPath));
                        
                        dataset.setDocument(document);
                        dataset.setDatasetName(name);
                        dataset.setPath(this.datasetPath);
                        dataset.setIsParent(true);
                } catch (XMLStreamException | IOException ex) {
                        NDCore.getDesktop().displayErrorMessage("The file should be SBML.");
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
