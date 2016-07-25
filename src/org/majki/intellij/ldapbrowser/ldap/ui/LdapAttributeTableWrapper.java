package org.majki.intellij.ldapbrowser.ldap.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.ldap.LdapAttribute;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapAttributeTableWrapper {

    private LdapAttributeTableModel model;
    private JTable table;
    private LdapNode ldapNode;
    private boolean directEdit;

    public LdapAttributeTableWrapper(JTable table, LdapNode node) {
        this(table, node, true);
    }

    public LdapAttributeTableWrapper(JTable table, LdapNode node, boolean directEdit) {
        this.table = table;
        this.ldapNode = node;
        this.directEdit = directEdit;
        configure();
    }

    private void configure() {

        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setAutoCreateColumnsFromModel(true);

        if (table instanceof JBTable) {
            ((JBTable) table).getEmptyText().setText("No attributes");
            ((JBTable) table).setShowColumns(true);
            ((JBTable) table).setEnableAntialiasing(true);
        }

        model = new LdapAttributeTableModel(ldapNode);
        table.setModel(model);

        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(24);

        TableColumn attributeColumn = table.getColumn(LdapAttributeTableModel.COLUMN_NAMES[0]);
        attributeColumn.setResizable(true);
        attributeColumn.setWidth(300);
        attributeColumn.setMinWidth(120);
        attributeColumn.setMaxWidth(400);
        TableCellRenderer objectClassBoldTableCellRenderer = (table1, value, isSelected, hasFocus, row, column) -> {
            JBLabel label = new JBLabel();

            if (isSelected) {
                label.setBackground(table1.getSelectionBackground());
                label.setForeground(table1.getSelectionForeground());
            } else {
                label.setBackground(table1.getBackground());
                label.setForeground(table1.getForeground());
            }

            UIUtil.addBorder(label, new EmptyBorder(1, 8, 1, 8));

            LdapAttributeTableModel model1 = (LdapAttributeTableModel) table1.getModel();
            String attributeName = (String) model1.getValueAt(row, 0);

            if (value instanceof String) {
                // if (column != 0 && LdapNode.USERPASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                //     label.setText("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");
                // } else {
                //     label.setText((String) value);
                // }
                label.setText((String) value);
                if (LdapNode.OBJECTCLASS_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                    Font font = new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize());
                    label.setFont(font);
                }
            } else {
                label.setForeground(UIUtil.getInactiveTextColor());
                label.setText("Empty value");
            }
            return label;
        };
        attributeColumn.setCellRenderer(objectClassBoldTableCellRenderer);

        TableColumn valueColumn = table.getColumn(LdapAttributeTableModel.COLUMN_NAMES[1]);
        valueColumn.setResizable(true);
        valueColumn.setCellRenderer(objectClassBoldTableCellRenderer);
        LdapTableCellEditor ldapTableCellEditor = new LdapTableCellEditor(ldapNode, table);
        ldapTableCellEditor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                LdapTableCellEditor editor = (LdapTableCellEditor) e.getSource();
                LdapAttributeTableModel model = (LdapAttributeTableModel) table.getModel();
                LdapAttributeTableModel.Item selectedItem = model.getItems().get(table.getSelectedRow());

                if (directEdit) {
                    String newValue = (String) editor.getCellEditorValue();
                    String oldValue = selectedItem.getValue().asString();

                    if (!oldValue.equals(newValue)) {

                        String attributeName = selectedItem.getAttribute().name();
                        LdapAttribute attribute = ldapNode.getAttributeByName(attributeName);
                        Set<String> values = attribute.values().stream().map(LdapAttribute.Value::asString).collect(Collectors.toSet());
                        values.remove(oldValue);
                        values.add(newValue);

                        Modification modification = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, attributeName, values.toArray(new String[values.size()]));
                        try {
                            ldapNode.getConnection().modify(ldapNode.getDn(), modification);

                            ldapNode.refresh();
                            ((LdapAttributeTableModel) table.getModel()).refresh();
                        } catch (LdapException e1) {
                            LdapErrorHandler.handleError(e1, "Could not modify attribute");
                        }
                    }
                } else {

                    ((LdapAttributeTableModel) table.getModel()).refresh();
                    LdapNode.valueModifier(selectedItem.getValue(), (String) editor.getCellEditorValue());
                }
            }

            @Override
            public void editingCanceled(ChangeEvent e) {

            }
        });
        valueColumn.setCellEditor(ldapTableCellEditor);

    }

    public JTable getTable() {
        return table;
    }

    public LdapAttributeTableModel getModel() {
        return model;
    }

    public LdapNode getLdapNode() {
        return ldapNode;
    }
}
