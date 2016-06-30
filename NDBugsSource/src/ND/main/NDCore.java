/*
 * Copyright 2007-2012 
 *
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
package ND.main;

import ND.desktop.Desktop;
import ND.desktop.impl.MainWindow;
import ND.desktop.impl.helpsystem.HelpImpl;
import ND.modules.NDModule;
import ND.modules.NDProcessingModule;
import ND.modules.analysis.Report.ReportFBAModule;
import ND.modules.configuration.db.DBConfParameters;
import ND.modules.configuration.general.GeneralconfigurationParameters;
import ND.parameters.ParameterSet;
import ND.taskcontrol.TaskController;
import ND.taskcontrol.impl.TaskControllerImpl;
import ND.util.dialogs.ExitCode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This interface represents ND core modules - I/O, task controller and GUI.
 */
/**
 * @author Taken from MZmine2 http://mzmine.sourceforge.net/
 */
public class NDCore implements Runnable {

    public static final File CONFIG_FILE = new File("conf/config.xml");
    public static final String STANDARD_RANGE = "standard_ranges";
    public static final String STANDARD_NAME = "standard_name";
    private static Logger logger = Logger.getLogger(NDCore.class.getName());
    private static GeneralconfigurationParameters preferences;
    private static DBConfParameters DBparameters;
    private static TaskControllerImpl taskController;
    private static NDModule[] initializedModules;
    private static HelpImpl help;
    private static MainWindow desktop;

    /**
     * Returns a reference to local task controller.
     *
     * @return TaskController reference
     */
    public static TaskController getTaskController() {
        return taskController;
    }

    /**
     * Returns a reference to Desktop.
     */
    public static Desktop getDesktop() {
        return desktop;
    }

    /**
     * Returns an array of all initialized ND modules
     *
     * @return Array of all initialized ND modules
     */
    public static NDModule[] getAllModules() {
        return initializedModules;
    }

    /**
     *
     *
     * @return
     */
    public static HelpImpl getHelpImpl() {
        return help;
    }

    /**
     * Saves configuration and exits the application.
     *
     */
    public static ExitCode exitND() {

        // If we have GUI, ask if use really wants to quit
        int selectedValue = JOptionPane.showInternalConfirmDialog(desktop.getMainFrame().getContentPane(),
            "Are you sure you want to exit?", "Exiting...",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (selectedValue != JOptionPane.YES_OPTION) {
            return ExitCode.CANCEL;
        }

        desktop.getMainFrame().dispose();

        logger.info("Exiting AntND");

        System.exit(0);

        return ExitCode.OK;

    }

    /**
     * Main method
     */
    public static void main(String args[]) {
        // create the GUI in the event-dispatching thread
        NDCore core = new NDCore();
        SwingUtilities.invokeLater(core);

    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        logger.log(Level.INFO, "Starting AntND {0}", getNDVersion());

        logger.fine("Loading core classes..");

        // create instance of preferences
        preferences = new GeneralconfigurationParameters();
        DBparameters = new DBConfParameters();

        // create instances of core modules
        // load configuration from XML
        taskController = new TaskControllerImpl();
        desktop = new MainWindow();
        help = new HelpImpl();

        logger.fine("Initializing core classes..");

        // Second, initialize desktop, because task controller needs to add
        // TaskProgressWindow to the desktop
        desktop.initModule();

        // Last, initialize task controller
        taskController.initModule();

        logger.fine("Loading modules");

        List<NDModule> moduleSet = new ArrayList<>();

        for (Class<?> moduleClass : NDModulesList.MODULES) {

            try {

                logger.log(Level.FINEST, "Loading module {0}", moduleClass.getName());

                // create instance and init module
                NDModule moduleInstance = (NDModule) moduleClass.newInstance();

                // add desktop menu icon
                if (moduleInstance instanceof NDProcessingModule) {
                    desktop.getMainMenu().addMenuItemForModule(
                        (NDProcessingModule) moduleInstance);
                }

                // add to the module set
                moduleSet.add(moduleInstance);

            } catch (Throwable e) {
                logger.log(Level.SEVERE,
                    "Could not load module " + moduleClass, e);
                e.printStackTrace();
                continue;
            }

        }

        NDCore.initializedModules = moduleSet.toArray(new NDModule[0]);

        if (CONFIG_FILE.canRead()) {
            try {
                loadConfiguration(CONFIG_FILE);
            } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {

            }
        }

        // register shutdown hook
        ShutDownHook shutDownHook = new ShutDownHook();
        Runtime.getRuntime().addShutdownHook(shutDownHook);

        // show the GUI
        logger.info("Showing main window");
        ((MainWindow) desktop).setVisible(true);

        // show the welcome message
        desktop.setStatusBarText("Welcome to AntND!");
        preferences.setProxy();

    }

    public static void saveConfiguration(File file)
        throws ParserConfigurationException, TransformerException,
        FileNotFoundException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document configuration = dBuilder.newDocument();
        Element configRoot = configuration.createElement("configuration");
        configuration.appendChild(configRoot);

        Element modulesElement = configuration.createElement("modules");
        configRoot.appendChild(modulesElement);

        // traverse modules
        for (NDModule module : getAllModules()) {

            String className = module.getClass().getName();

            Element moduleElement = configuration.createElement("module");
            moduleElement.setAttribute("class", className);
            modulesElement.appendChild(moduleElement);

            Element paramElement = configuration.createElement("parameters");
            moduleElement.appendChild(paramElement);

            ParameterSet moduleParameters = module.getParameterSet();
            if (moduleParameters != null) {
                moduleParameters.saveValuesToXML(paramElement);
            }

        }

        // Save Parameters path
        String className = "ParameterPath";
        Element moduleElement = configuration.createElement("module");
        moduleElement.setAttribute("class", className);
        modulesElement.appendChild(moduleElement);

        Element paramElement = configuration.createElement("parameters");
        moduleElement.appendChild(paramElement);
        NDCore.getDesktop().saveParameterPathToXML(paramElement);

        // Save Parameters report path
        className = "ReportFBA";
        moduleElement = configuration.createElement("module");
        moduleElement.setAttribute("class", className);
        modulesElement.appendChild(moduleElement);

        paramElement = configuration.createElement("parameters");
        moduleElement.appendChild(paramElement);      
        NDCore.getDesktop().saveParameterReportToXML(paramElement);

        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer transformer = transfac.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(
            "{http://xml.apache.org/xslt}indent-amount", "4");

        StreamResult result = new StreamResult(new FileOutputStream(file));
        DOMSource source = new DOMSource(configuration);
        transformer.transform(source, result);

        logger.log(Level.INFO, "Saved configuration to file {0}", file);

    }

    public static void loadConfiguration(File file)
        throws ParserConfigurationException, SAXException, IOException,
        XPathExpressionException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document configuration = dBuilder.parse(file);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        logger.finest("Loading desktop configuration");

        /* XPathExpression expr = xpath.compile("//configuration/Standards");
         NodeList nodes = (NodeList) expr.evaluate(configuration,
         XPathConstants.NODESET);*/
        logger.finest("Loading modules configuration");

        for (NDModule module : getAllModules()) {

            String className = module.getClass().getName();
            XPathExpression expr = xpath.compile("//configuration/modules/module[@class='" + className + "']/parameters");
            NodeList nodes = (NodeList) expr.evaluate(configuration,
                XPathConstants.NODESET);
            if (nodes.getLength() != 1) {
                continue;
            }

            Element moduleElement = (Element) nodes.item(0);

            ParameterSet moduleParameters = module.getParameterSet();
            if (moduleParameters != null) {
                moduleParameters.loadValuesFromXML(moduleElement);
            }
        }

        String className = "ParameterPath";
        XPathExpression expr = xpath.compile("//configuration/modules/module[@class='" + className + "']/parameters");

        NodeList nodes = (NodeList) expr.evaluate(configuration,
            XPathConstants.NODESET);

        Element moduleElement = (Element) nodes.item(0);
        NDCore.getDesktop().loadParameterPathFromXML(moduleElement);
        className = "ReportFBA";
        expr = xpath.compile("//configuration/modules/module[@class='" + className + "']/parameters");
        nodes = (NodeList) expr.evaluate(configuration,
            XPathConstants.NODESET);
        
        moduleElement = (Element) nodes.item(0);
        NDCore.getDesktop().loadParameterReportFromXML(moduleElement);

        logger.log(Level.INFO, "Loaded configuration from file {0}", file);
    }

    public static String getNDVersion() {
        return NDVersion.ND;
    }

    public static GeneralconfigurationParameters getPreferences() {
        return preferences;
    }

    public static DBConfParameters getDBParameters() {
        return DBparameters;
    }

    public static void setPreferences(GeneralconfigurationParameters preferences2) {
        preferences = preferences2;
    }
}
