package org.majki.intellij.ldapbrowser.ldap.ui;

import org.majki.intellij.ldapbrowser.ldap.LdapAttribute;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapAttributeTableModel implements TableModel {

    public static final String[] COLUMN_NAMES = new String[] {"Attribute", "Value"};
    public static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {String.class, String.class};
    public static final boolean[] COLUMN_EDITABLE = new boolean[] {false, true};

    public static class Item {
        private LdapAttribute attribute;
        private LdapAttribute.Value value;

        public Item(LdapAttribute attribute, LdapAttribute.Value value) {
            this.attribute = attribute;
            this.value = value;
        }

        public LdapAttribute getAttribute() {
            return attribute;
        }

        public LdapAttribute.Value getValue() {
            return value;
        }
    }

    private LdapNode ldapNode;
    private List<Item> items;
    private List<TableModelListener> listeners;

    public LdapAttributeTableModel(LdapNode ldapNode) {
        this.ldapNode = ldapNode;
        this.items = createItems();
        this.listeners = new ArrayList<>();
    }

    private List<Item> createItems() {
        List<Item> items = new ArrayList<>();
        for (LdapAttribute ldapAttribute : ldapNode.getAttributes()) {
            items.addAll(ldapAttribute.values().stream().map(value -> new Item(ldapAttribute, value)).collect(Collectors.toList()));
        }
        return items;
    }

    public void reload() {
        items = createItems();
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_CLASSES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return COLUMN_EDITABLE[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Item item = items.get(rowIndex);
        if (columnIndex == 0) {
            return item.getAttribute().upName();
        } else if (columnIndex == 1) {
            return item.getValue().asString();
        } else {
            return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    public void refresh() {
        items = createItems();
        for (TableModelListener listener : listeners) {
            listener.tableChanged(new TableModelEvent(this));
        }
    }

    public void refresh(int row) {
        items = createItems();
        for (TableModelListener listener : listeners) {
            listener.tableChanged(new TableModelEvent(this, row));
        }
    }

}
