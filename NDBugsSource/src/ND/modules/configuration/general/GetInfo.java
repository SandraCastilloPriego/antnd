/*
 * Copyright 2007-2013 VTT Biotechnology
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
package ND.modules.configuration.general;

import ND.data.Dataset;
import ND.modules.configuration.sources.SourcesConfParameters;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;

/**
 *
 * @author scsandra
 */
public class GetInfo {

        File sources, boundsFile;

        public GetInfo() {
                SourcesConfParameters sourcesParameters = new SourcesConfParameters();
                this.sources = sourcesParameters.getParameter(SourcesConfParameters.exchange).getValue();
                this.boundsFile = sourcesParameters.getParameter(SourcesConfParameters.bounds).getValue();
        }

        public Map<String, Double[]> GetSourcesInfo() {

                Map<String, Double[]> exchangeMap = new HashMap<>();
                try {
                        CsvReader exchange = new CsvReader(new FileReader(this.sources), '\t');

                        try {
                                while (exchange.readRecord()) {
                                        try {
                                                String[] exchangeRow = exchange.getValues();
                                                Double[] bounds = new Double[2];
                                                bounds[0] = Double.parseDouble(exchangeRow[1]);
                                                bounds[1] = Double.parseDouble(exchangeRow[2]);
                                                exchangeMap.put(exchangeRow[0], bounds);
                                        } catch (IOException | NumberFormatException e) {
                                                System.out.println(e.toString());
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(ND.modules.configuration.general.GetInfo.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return exchangeMap;
                } catch (FileNotFoundException ex) {
                        Logger.getLogger(ND.modules.configuration.general.GetInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
                return exchangeMap;
        }
        
        public HashMap<String, String[]> readBounds(Dataset networkDS) {
                HashMap<String, String[]> b = new HashMap<>();
                try {
                        SBMLDocument doc = networkDS.getDocument();
                        Model m = doc.getModel();

                        CsvReader reader = new CsvReader(new FileReader(this.boundsFile));

                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                String reactionName = data[0].replace("-", "");
                                b.put(reactionName, data);

                                Reaction r = m.getReaction(reactionName);
                                if (r != null && r.getKineticLaw() == null) {
                                        KineticLaw law = new KineticLaw();
                                        LocalParameter lbound = new LocalParameter("LOWER_BOUND");
                                        lbound.setValue(Double.valueOf(data[3]));
                                        law.addLocalParameter(lbound);
                                        LocalParameter ubound = new LocalParameter("UPPER_BOUND");
                                        ubound.setValue(Double.valueOf(data[4]));
                                        law.addLocalParameter(ubound);
                                        r.setKineticLaw(law);
                                }
                        }
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(ND.modules.configuration.general.GetInfo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(ND.modules.configuration.general.GetInfo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return b;
        }
}
