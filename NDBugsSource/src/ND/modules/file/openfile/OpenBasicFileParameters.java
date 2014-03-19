/*
 * Copyright 2007-2013 VTT Biotechnology
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
package ND.modules.file.openfile;


import ND.main.NDCore;
import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.FileNamesParameter;
import ND.util.dialogs.ExitCode;
import java.io.File;
import javax.swing.JFileChooser;

public class OpenBasicFileParameters extends SimpleParameterSet {

        
        public static final FileNamesParameter fileNames = new FileNamesParameter("File name", "Set the path of the file", null);

        public OpenBasicFileParameters() {
                super(new Parameter[]{fileNames});
        }

        @Override
        public ExitCode showSetupDialog() {

                JFileChooser chooser = new JFileChooser();                

                File lastFiles[] = getParameter(fileNames).getValue();
                if ((lastFiles != null) && (lastFiles.length > 0)) {
                        File currentDir = lastFiles[0].getParentFile();
                        if (currentDir.exists()) {
                                chooser.setCurrentDirectory(currentDir);
                        }
                }

                chooser.setMultiSelectionEnabled(true);

                int returnVal = chooser.showOpenDialog(NDCore.getDesktop().getMainFrame());

                if (returnVal != JFileChooser.APPROVE_OPTION) {
                        return ExitCode.CANCEL;
                }

                File selectedFiles[] = chooser.getSelectedFiles();

                getParameter(fileNames).setValue(selectedFiles);

                return ExitCode.OK;

        }
}
