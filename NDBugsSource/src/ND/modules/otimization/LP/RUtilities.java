package ND.modules.otimization.LP;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

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

/**
 *
 * @author scsandra
 */
public class RUtilities {
         // Logger.
    private static final Logger LOG = Logger.getLogger(RUtilities.class.getName());

    /**
     * R semaphore - all usage of R engine must be synchronized using this semaphore.
     */
    public static final Object R_SEMAPHORE = new Object();

    // An R Engine singleton.
    private static Rengine rEngine = null;

    /**
     * Utility class - no public access.
     */
    private RUtilities() {
        // no public access.
    }

    /**
     * Gets the R Engine.
     *
     * @return the R Engine - creating it if necessary.
     */
    public static Rengine getREngine() {

        synchronized (R_SEMAPHORE) {

            if (rEngine == null) {

                LOG.finest("Checking R Engine.");
                if (!Rengine.versionCheck()) {
                    throw new IllegalStateException("JRI version mismatch");
                }

                LOG.finest("Creating R Engine.");
                rEngine = new Rengine(new String[]{"--vanilla"}, false, (RMainLoopCallbacks) new LoggerConsole());

                LOG.finest("Rengine created, waiting for R.");
                if (!rEngine.waitForR()) {
                    throw new IllegalStateException("Could not start R");
                }
            }
            return rEngine;
        }
    }

    /**
     * Logs all output.
     */
    private static class LoggerConsole implements RMainLoopCallbacks {
        @Override
        public void rWriteConsole(final Rengine re, final String text, final int oType) {
            LOG.finest(text);
            System.out.println(text);
        }

        @Override
        public void rBusy(final Rengine re, final int which) {
            LOG.log(Level.FINEST, "rBusy({0})", which);
        }

        @Override
        public String rReadConsole(final Rengine re, final String prompt, final int addToHistory) {
            return null;
        }

        @Override
        public void rShowMessage(final Rengine re, final String message) {
            LOG.log(Level.FINEST, "rShowMessage \"{0}\"", message);
            System.out.println(message);
        }

        @Override
        public String rChooseFile(final Rengine re, final int newFile) {
            return null;
        }

        @Override
        public void rFlushConsole(final Rengine re) {
        }

        @Override
        public void rLoadHistory(final Rengine re, final String filename) {
        }

        @Override
        public void rSaveHistory(final Rengine re, final String filename) {
        }
    }
}
