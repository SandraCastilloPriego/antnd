/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.DB.Visualize;

import ND.main.NDCore;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author scsandra
 */
public class PushableTableGraph implements MouseListener {

    private GraphDataModel model;
    private JTable table;

    public PushableTableGraph(GraphDataModel model) {
        this.model = model;
        table = this.tableRowsColor(model);
        this.setTableProperties();
        this.table.addMouseListener(this);
    }

    public JTable getTable() {
        return this.table;
    }

    protected JTable tableRowsColor(final GraphDataModel tableModel) {
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
                    } else if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col)) {
                        comp.setBackground(new Color(234, 235, 243));
                    } else if (isCellSelected(Index_row, Index_col)) {
                        comp.setBackground(new Color(173, 205, 203));
                        if (comp.getBackground().getRGB() != new Color(173, 205, 203).getRGB()) {
                            this.repaint();
                        }

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

        };

        return colorTable;
    }

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

    }

    /**
     * Creates the tooltips of the table.
     *
     */
    public void createTooltips() {
        try {
            PushableTableGraph.ToolTipHeader toolheader;
            String[] toolTipStr = new String[model.getColumnCount()];
            for (int i = 0; i < model.getColumnCount(); i++) {
                toolTipStr[i] = model.getColumnName(i);
            }

            toolheader = new PushableTableGraph.ToolTipHeader(table.getColumnModel());
            toolheader.setToolTipStrings(toolTipStr);
            table.setTableHeader(toolheader);
        } catch (Exception e) {
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

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

    @Override
    public void mousePressed(MouseEvent me) {
        Point p = me.getPoint();
        int row = table.rowAtPoint(p);
        if (me.getClickCount() == 2) {
           MiniGraph mg = this.model.getGraph(row);
            PrintGraph pg = new PrintGraph();
            JInternalFrame frame = new JInternalFrame("Graph DB", true, true, true, true);
            JPanel pn = new JPanel();
            JScrollPane panel = new JScrollPane(pn);

            frame.setSize(new Dimension(500, 500));
            frame.add(panel);
            NDCore.getDesktop().addInternalFrame(frame);
            try {

                System.out.println("Visualize");
                pn.add(pg.printPathwayInFrame(mg));

            } catch (NullPointerException ex) {
                System.out.println(ex.toString());
            }
        }
    }

}
