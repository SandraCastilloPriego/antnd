package ND.util.Tables.implement;

import ND.data.DatasetType;
import ND.util.Tables.DataTable;
import ND.util.Tables.DataTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Creates a table for showing the data sets. It implements DataTable.
 *
 * @author scsandra
 */
public class PushableTable implements DataTable, ActionListener {

    protected DataTableModel model;
    JTable table;
    private String rowstring, value;
    private Clipboard system;
    private StringSelection stsel;
    private ArrayList<register> registers;
    int indexRegister = 0;

    public PushableTable() {
        registers = new ArrayList<register>();
    }

    public PushableTable(DataTableModel model) {
        this.model = model;
        table = this.tableRowsColor(model);
        setTableProperties();
        registers = new ArrayList<register>();
    }

    /**
     * Changes the model of the table.
     *
     * @param model
     */
    public void createTable(DataTableModel model) {
        this.model = model;
        // Color of the cells
        table = this.tableRowsColor(model);
    }

    /**
     * Returns the table.
     *
     * @return Table
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Changes the color of the cells depending of determinates conditions.
     *
     * @param tableModel
     * @return table
     */
    protected JTable tableRowsColor(final DataTableModel tableModel) {
        JTable colorTable = new JTable(tableModel) {

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
                Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
                try {
                    // Coloring conditions
                    if (isDataSelected(Index_row)) {
                        comp.setBackground(new Color(173, 205, 203));
                        if (comp.getBackground().getRGB() != new Color(173, 205, 203).getRGB()) {
                            this.repaint();
                        }
                    } else if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col) && getRowColor(Index_row) == null) {
                        comp.setBackground(new Color(234, 235, 243));
                    } else if (isCellSelected(Index_row, Index_col)) {
                        comp.setBackground(new Color(173, 205, 203));
                        if (comp.getBackground().getRGB() != new Color(173, 205, 203).getRGB()) {
                            this.repaint();
                        }

                    } else if (getRowColor(Index_row) != null) {
                        comp.setBackground(getRowColor(Index_row));
                    } else {
                        comp.setBackground(Color.white);
                    }

                    if (getCellColor(Index_row, Index_col) != null) {
                        comp.setBackground(getCellColor(Index_row, Index_col));
                    }
                    
                    if(isExchange(Index_row)){
                        comp.setBackground(Color.YELLOW);
                    }
                    if(isTransport(Index_row)){
                        comp.setBackground(Color.ORANGE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return comp;
            }

            private boolean isDataSelected(int row) {
                try {
                    return ((Boolean) table.getValueAt(row, 0)).booleanValue();
                } catch (Exception e) {
                    return false;
                }
            }

            private Color getRowColor(int row) {
                return tableModel.getRowColor(row);
            }

            private Color getCellColor(int row, int column) {
                return tableModel.getCellColor(row, column);
            }
            
            private boolean isExchange(int row){
                return tableModel.isExchange(row);
            }
            
             private boolean isTransport(int row){
                return tableModel.isTransport(row);
            }
        };

        return colorTable;
    }

    /**
     * Sets the properties of the table: selection mode, tooltips, actions with
     * keys..
     *
     */
    public void setTableProperties() {

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setColumnSelectionAllowed(true);

        // Tooltips
        this.createTooltips();

        // Sorting
        table.setAutoCreateRowSorter(true);
        table.setUpdateSelectionOnSort(true);

        // Size
        table.setMinimumSize(new Dimension(300, 800));

        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //key actions
        registerKey(KeyEvent.VK_C, ActionEvent.CTRL_MASK, "Copy");
        registerKey(KeyEvent.VK_V, ActionEvent.CTRL_MASK, "Paste");
        registerKey(KeyEvent.VK_DELETE, 0, "Delete");
        registerKey(KeyEvent.VK_Z, ActionEvent.CTRL_MASK, "Back");
        registerKey(KeyEvent.VK_Y, ActionEvent.CTRL_MASK, "Forward");

        system = Toolkit.getDefaultToolkit().getSystemClipboard();

    }

    /**
     * Adds a concrete action to a combination of keys.
     *
     * @param key Key responsible of the action
     * @param mask Mask of the key
     * @param name Name of the action
     */
    private void registerKey(int key, int mask, String name) {
        KeyStroke action = KeyStroke.getKeyStroke(key, mask, false);
        table.registerKeyboardAction(this, name, action, JComponent.WHEN_FOCUSED);
    }

    /**
     * Formating of the numbers in the table depening on the data set type.
     *
     * @param type Type of dataset @see guineu.data.DatasetType
     */
    public void formatNumbers(DatasetType type) {
        try {
            NumberFormat format = NumberFormat.getNumberInstance();
            format.setMinimumFractionDigits(7);
            int init = model.getColumnCount();

            for (int i = init; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(new NumberRenderer(format));
            }
        } catch (Exception e) {
        }

    }

    /**
     * Formating of the numbers in certaing column
     *
     * @param column Column where the numbers will be formated
     */
    public void formatNumbers(int column) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(7);
        table.getColumnModel().getColumn(column).setCellRenderer(new NumberRenderer(format));
    }

    /**
     * Creates the tooltips of the table.
     *
     */
    public void createTooltips() {
        try {
            ToolTipHeader toolheader;
            String[] toolTipStr = new String[model.getColumnCount()];
            for (int i = 0; i < model.getColumnCount(); i++) {
                toolTipStr[i] = model.getColumnName(i);
            }

            toolheader = new ToolTipHeader(table.getColumnModel());
            toolheader.setToolTipStrings(toolTipStr);
            table.setTableHeader(toolheader);
        } catch (Exception e) {
        }
    }

    public void actionPerformed(ActionEvent e) {

        // Sets the action of the key combinations
        // Copy
        if (e.getActionCommand().compareTo("Copy") == 0) {
            StringBuffer sbf = new StringBuffer();
            // Check to ensure we have selected only a contiguous block of
            // cells
            int numcols = table.getSelectedColumnCount();
            int numrows = table.getSelectedRowCount();
            int[] rowsselected = table.getSelectedRows();
            int[] colsselected = table.getSelectedColumns();
            if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0]
                && numrows == rowsselected.length)
                && (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
                && numcols == colsselected.length))) {
                JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
                    "Invalid Copy Selection",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int i = 0; i < numrows; i++) {
                for (int j = 0; j < numcols; j++) {
                    sbf.append(table.getValueAt(rowsselected[i], colsselected[j]));
                    if (j < numcols - 1) {
                        sbf.append("\t");
                    }
                }
                sbf.append("\n");
            }
            stsel = new StringSelection(sbf.toString());
            system = Toolkit.getDefaultToolkit().getSystemClipboard();
            system.setContents(stsel, stsel);
        }

        // Paste
        if (e.getActionCommand().compareTo("Paste") == 0) {

            int startRow = (table.getSelectedRows())[0];
            int startCol = (table.getSelectedColumns())[0];
            register newRegister = null;
            String rtrstring;
            try {
                rtrstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
                StringTokenizer rst1 = new StringTokenizer(rtrstring, "\n");
                rowstring = rst1.nextToken();
                StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
                newRegister = new register(startRow, rst1.countTokens() + 1, startCol, st2.countTokens());
                newRegister.getValues();
            } catch (Exception ex) {
                Logger.getLogger(PushableTable.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                String trstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
                StringTokenizer st1 = new StringTokenizer(trstring, "\n");
                for (int i = 0; st1.hasMoreTokens(); i++) {
                    rowstring = st1.nextToken();
                    StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
                    for (int j = 0; st2.hasMoreTokens(); j++) {
                        value = st2.nextToken();
                        if (startRow + i < table.getRowCount()
                            && startCol + j < table.getColumnCount()) {
                            table.setValueAt(value, startRow + i, startCol + j);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            newRegister.getNewValues();
            this.registers.add(newRegister);
            this.indexRegister = this.registers.size() - 1;
        }

        // Delete
        if (e.getActionCommand().compareTo("Delete") == 0) {
            register newRegister = new register(table.getSelectedColumns(), table.getSelectedRows());
            newRegister.getValues();

            int[] selectedRow = table.getSelectedRows();
            int[] selectedCol = table.getSelectedColumns();

            try {
                int selected = table.getSelectedRowCount();
                for(int i = 0; i < selected; i++) {                    
                    table.setValueAt("NA", table.getSelectedRow(), 1);                    
                    table.repaint();
                    table.revalidate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            newRegister.getNewValues();
            this.registers.add(newRegister);
            this.indexRegister = this.registers.size() - 1;
        }

        // Undo
        if (e.getActionCommand().compareTo("Back") == 0) {
            this.registers.get(indexRegister).back();
            if (indexRegister > 0) {
                indexRegister--;
            }
        }

        // Redo
        if (e.getActionCommand().compareTo("Forward") == 0) {
            this.registers.get(indexRegister).forward();
            if (indexRegister < this.registers.size() - 1) {
                indexRegister++;
            }
        }
        System.gc();
    }

    /**
     * Tooltips
     *
     */
    class ToolTipHeader extends JTableHeader {

        private static final long serialVersionUID = 1L;
        String[] toolTips;

        public ToolTipHeader(TableColumnModel model) {
            super(model);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int col = columnAtPoint(e.getPoint());
            int modelCol = getTable().convertColumnIndexToModel(col);

            String retStr;
            try {
                retStr = toolTips[modelCol];
            } catch (NullPointerException ex) {
                retStr = "";
                System.out.println("NullPointer Exception tooltips");
            } catch (ArrayIndexOutOfBoundsException ex) {
                retStr = "";
                System.out.println("ArrayIndexOutOfBoundsException tooltips");
            }
            if (retStr.length() < 1) {
                retStr = super.getToolTipText(e);
            }
            return retStr;
        }

        public void setToolTipStrings(String[] toolTips) {
            this.toolTips = toolTips;
        }
    }

    /**
     * Push header
     *
     */
    class HeaderListener extends MouseAdapter {

        JTableHeader header;
        ButtonHeaderRenderer renderer;

        HeaderListener(JTableHeader header, ButtonHeaderRenderer renderer) {
            this.header = header;
            this.renderer = renderer;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int col = header.columnAtPoint(e.getPoint());
            renderer.setPressedColumn(col);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            renderer.setPressedColumn(-1); // clear                        
        }
    }

    /**
     * Button header
     *
     */
    class ButtonHeaderRenderer extends JButton implements TableCellRenderer {

        int pushedColumn;

        public ButtonHeaderRenderer() {
            pushedColumn = -1;
            setMargin(new Insets(0, 0, 0, 0));
        }

        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
            setText((value == null) ? "" : value.toString());
            boolean isPressed = (column == pushedColumn);
            getModel().setPressed(isPressed);
            getModel().setArmed(isPressed);
            return this;
        }

        public void setPressedColumn(int col) {
            pushedColumn = col;
        }
    }

    /**
     * Number renderer
     *
     */
    class NumberRenderer
        extends DefaultTableCellRenderer {

        private NumberFormat formatter;

        public NumberRenderer() {
            this(NumberFormat.getNumberInstance());
        }

        public NumberRenderer(NumberFormat formatter) {
            super();
            this.formatter = formatter;
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public void setValue(Object value) {
            if ((value != null) && (value instanceof Number)) {
                value = formatter.format(value);
            }

            super.setValue(value);
        }
    }

    /**
     * Defines the action of the keys in the table
     *
     */
    class register {

        int[] columnIndex;
        int[] rowIndex;
        Object[] values;
        Object[] newValues;

        public register(int[] columnIndex, int[] rowIndex) {
            this.columnIndex = columnIndex;
            this.rowIndex = rowIndex;
            values = new Object[columnIndex.length * rowIndex.length];
            newValues = new Object[columnIndex.length * rowIndex.length];
        }

        private register(int startRow, int rowCount, int startCol, int columnCount) {
            rowIndex = new int[rowCount];
            columnIndex = new int[columnCount];
            for (int i = 0; i < rowCount; i++) {
                rowIndex[i] = startRow + i;
            }
            for (int i = 0; i < columnCount; i++) {
                columnIndex[i] = startCol + i;
            }
            values = new Object[columnIndex.length * rowIndex.length];
            newValues = new Object[columnIndex.length * rowIndex.length];
        }

        public void getValues() {
            int cont = 0;
            for (int row : rowIndex) {
                for (int column : columnIndex) {
                    try {
                        values[cont++] = table.getValueAt(row, column);
                    } catch (Exception e) {
                    }
                }
            }
        }

        public void getNewValues() {
            int cont = 0;
            for (int row : rowIndex) {
                for (int column : columnIndex) {
                    try {
                        newValues[cont++] = table.getValueAt(row, column);
                    } catch (Exception e) {
                    }
                }
            }
        }

        public void back() {
            int cont = 0;
            for (int row : rowIndex) {
                for (int column : columnIndex) {
                    table.setValueAt(values[cont++], row, column);
                }
            }
        }

        public void forward() {
            int cont = 0;
            for (int row : rowIndex) {
                for (int column : columnIndex) {
                    table.setValueAt(newValues[cont++], row, column);
                }
            }
        }
    }
}
