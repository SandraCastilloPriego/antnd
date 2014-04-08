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
package ND.modules.file.openfile;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.parser.Parser;
import ND.data.parser.impl.BasicFilesParserSBML;
import ND.main.NDCore;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.io.File;

/**
 *
 * @author scsandra
 */
public class OpenBasicFileTask extends AbstractTask {

        private String fileDir;
        private Parser parser;

        public OpenBasicFileTask(String fileDir) {
                if (fileDir != null) {
                        this.fileDir = fileDir;
                }
        }

        @Override
        public String getTaskDescription() {
                return "Opening File... ";
        }

        @Override
        public double getFinishedPercentage() {
                if (parser != null) {
                        return parser.getProgress();
                } else {
                        return 0.0f;
                }
        }

        @Override
        public void cancel() {
                setStatus(TaskStatus.CANCELED);
        }

        @Override
        public void run() {
                try {
                        this.openFile();
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public void openFile() {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {
                                parser = new BasicFilesParserSBML(fileDir);
                                File f = new File(fileDir);
                                parser.createDataset(f.getName());
                                Dataset dataset = (SimpleBasicDataset) parser.getDataset();
                                if (dataset.getDocument() != null) {
                                        NDCore.getDesktop().AddNewFile(dataset);
                                }
                        }
                } catch (Exception ex) {
                }

                setStatus(TaskStatus.FINISHED);
        }
}
