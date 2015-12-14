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
package ND.modules.file.saveProject;

import ND.data.Dataset;
import ND.desktop.impl.ItemSelector;
import ND.main.NDCore;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import ND.util.StreamCopy;
import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;

/**
 *
 * @author scsandra
 */
public class SaveProjectTask extends AbstractTask {

    private File file;
    private double finishedPercentage = 0.0f;
    private int i;

    public SaveProjectTask(File file) {
        if (file != null) {
            this.file = file;
        }
    }

    @Override
    public String getTaskDescription() {
        return "Saving Project... ";
    }

    @Override
    public double getFinishedPercentage() {
        return finishedPercentage;
    }

    @Override
    public void cancel() {
        setStatus(TaskStatus.CANCELED);
    }

    @Override
    public void run() {
        try {
            setStatus(TaskStatus.PROCESSING);
            File tempFile = File.createTempFile(file.getName(), ".tmp",
                file.getParentFile());
            tempFile.deleteOnExit();
            // Create a ZIP stream writing to the temporary file
            FileOutputStream tempStream = new FileOutputStream(tempFile);
            try (ZipOutputStream zipStream = new ZipOutputStream(tempStream)) {
                saveSBMLFiles(zipStream);
                saveHistory(zipStream);
                zipStream.close();
            }
            boolean renameOK = tempFile.renameTo(file);
            if (!renameOK) {
                throw new IOException("Could not move the temporary file "
                    + tempFile + " to the final location " + file);
            }

            setStatus(TaskStatus.FINISHED);
        } catch (IOException e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }

    }

    private void saveSBMLFiles(ZipOutputStream zipStream) throws IOException {
        Dataset[] selectedFiles = NDCore.getDesktop().getAllDataFiles();

        for (final Dataset datafile : selectedFiles) {
            if (datafile != null) {
                zipStream.putNextEntry(new ZipEntry(datafile.getDatasetName()));
                File tempFile = File.createTempFile(datafile.getDatasetName(), ".tmp");
                SBMLWriter writer = new SBMLWriter("AntND", "1.0");
                try {
                    writer.write(datafile.getDocument(), tempFile.getAbsolutePath());
                } catch (XMLStreamException | FileNotFoundException | SBMLException ex) {
                    Logger.getLogger(ItemSelector.class.getName()).log(Level.SEVERE, null, ex);
                }
                try (FileInputStream fileStream = new FileInputStream(tempFile)) {
                    StreamCopy copyMachine = new StreamCopy();
                    copyMachine.copy(fileStream, zipStream);
                }
                tempFile.delete();
                finishedPercentage = ((double) i++ / selectedFiles.length) / 2;
            }

        }

    }

    private void saveHistory(ZipOutputStream zipStream) throws IOException {
        Dataset[] selectedFiles = NDCore.getDesktop().getAllDataFiles();
        for (final Dataset datafile : selectedFiles) {
            String info = datafile.getInfo().getText();
            String biomass = datafile.getBiomassId();
            List<String> sources = datafile.getSources();
            Graph graph = datafile.getGraph();
            zipStream.putNextEntry(new ZipEntry(datafile.getDatasetName() + ".info"));
            File tempFile = File.createTempFile(datafile.getDatasetName() + "-info", ".tmp");

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(tempFile.getAbsoluteFile()));
                writer.write(info);
                if (biomass != null) {
                    writer.write("Biomass= " + biomass);
                }
                if (sources != null) {
                    for (String source : sources) {
                        writer.write("\nSources= " + source);
                    }
                }
                if (graph != null) {
                    for (Node node : graph.getNodes()) {
                        Point2D position = node.getPosition();
                        if (position != null) {
                            writer.write("\nNodes= " + node.getId() + " // " + position.getX() + " , " + position.getY());
                        } else {
                            writer.write("\nNodes= " + node.getId());
                        }
                    }
                    for (Edge edge : graph.getEdges()) {
                        writer.write("\nEdges= " + edge.getId() + " // " + edge.getSource().getId() + " || " + edge.getDestination().getId());
                    }
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                }
            }

            try (FileInputStream fileStream = new FileInputStream(tempFile)) {
                StreamCopy copyMachine = new StreamCopy();
                copyMachine.copy(fileStream, zipStream);
            }
            tempFile.delete();
            finishedPercentage = ((double) i++ / selectedFiles.length) / 2;
        }
    }

}
