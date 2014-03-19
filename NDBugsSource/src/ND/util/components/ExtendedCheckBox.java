package ND.util.components;

import java.awt.Font;

import javax.swing.JCheckBox;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 * 
 * Checkbox wrapper class
 */
public class ExtendedCheckBox<Type> extends JCheckBox {

        static final Font checkBoxFont = new Font("SansSerif", Font.PLAIN, 11);
        private Type object;

        public ExtendedCheckBox(Type object) {
                this(object, false);
        }

        public ExtendedCheckBox(Type object, boolean selected) {
                super(object.toString(), selected);
                this.object = object;
                setOpaque(false);
                setFont(checkBoxFont);
        }

        /**
         * @return Returns the dataFile.
         */
        public Type getObject() {
                return object;
        }

        public int getPreferredWidth() {
                return ((int) getPreferredSize().getWidth()) + 30;
        }
}
