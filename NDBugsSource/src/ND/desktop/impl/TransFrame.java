/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.desktop.impl;

/**
 *
 * @author scsandra
 */
import ND.main.NDCore;
import java.awt.*;
import javax.swing.*;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

public class TransFrame extends JInternalFrame {

        StringBuffer info;

        public TransFrame(Model m, String name) {
                Reaction reaction = m.getReaction(name);
                this.info = new StringBuffer();
                if (reaction != null) {
                        KineticLaw law = reaction.getKineticLaw();
                        if (law != null) {
                                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                                info.append(reaction.getId()).append(" lb: ").append(lbound.getValue()).append(" up: ").append(ubound.getValue()).append(":\n");
                        } else {
                                info.append(reaction.getId()).append(":\n");
                        }
                        info.append("Reactants: \n");
                        for (SpeciesReference sr : reaction.getListOfReactants()) {
                                Species sp = sr.getSpeciesInstance();
                                info.append(sr.getStoichiometry()).append(" ").append(sp.getId()).append(" - ").append(sp.getName()).append("\n");
                        }
                        info.append("Products: \n");
                        for (SpeciesReference sr : reaction.getListOfProducts()) {
                                Species sp = sr.getSpeciesInstance();
                                info.append(sr.getStoichiometry()).append(" ").append(sp.getId()).append(" - ").append(sp.getName()).append(" \n");
                        }
                } else {
                        Species sp = m.getSpecies(name);
                        if (sp != null) {
                                info.append(sp.getId()).append(" - ").append(sp.getName());
                                info.append("\n\nPresent in: \n");
                                int count = 0;
                                for (Reaction r : m.getListOfReactions()) {
                                        if (r.hasReactant(sp) || r.hasProduct(sp)) {
                                                info.append(r.getId()).append(", ");
                                                count++;
                                                if (count == 2) {
                                                        count = 0;
                                                        info.append("\n");
                                                }
                                        }
                                }
                        }
                }
                this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                this.setClosable(true);
                this.setResizable(true);
                // create and add a scroll pane and text area
                JTextArea textArea = new JTextArea(5, 30);
                textArea.setBackground(new Color(251, 161, 82));
                Font font = textArea.getFont();
                textArea.setFont(font.deriveFont(Font.BOLD));
                JScrollPane scrollPane = new JScrollPane(textArea);
                textArea.setText(info.toString());
                scrollPane.setPreferredSize(new Dimension(300, 185));
                this.getContentPane().add(scrollPane, BorderLayout.CENTER);

                this.pack();
                NDCore.getDesktop().addInternalFrame(this);
                
        }
}