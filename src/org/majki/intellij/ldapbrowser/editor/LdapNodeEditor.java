package org.majki.intellij.ldapbrowser.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapAttributesTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * @author Attila Majoros
 */

public class LdapNodeEditor implements FileEditor {

    private Project project;
    private LdapNodeVirtualFile virtualFile;
    private boolean initialized;

    private JPanel content;
    private JBTable table;
    private JBPanel toolbarPanel;

    public LdapNodeEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        this.project = project;
        if (virtualFile instanceof LdapNodeVirtualFile) {
            this.virtualFile = (LdapNodeVirtualFile) virtualFile;
        } else {
            throw new IllegalArgumentException("Virtual file is not LdapNodeVirtualFile");
        }
        this.initialized = false;
    }

    private void createToolbar() {

        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("ldapbrowser.editorActionGroup");
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("editorActionToolbar", actionGroup, false);
        //actionToolbar.setTargetComponent(content);
        actionToolbar.setOrientation(JToolBar.HORIZONTAL);
        Box toolbarBox = Box.createHorizontalBox();
        toolbarBox.add(actionToolbar.getComponent());

        toolbarPanel.setLayout(new BorderLayout());
        toolbarPanel.add(toolbarBox, BorderLayout.PAGE_START);
        actionToolbar.getComponent().setVisible(true);
        //super.setToolbar(toolbarBox);
    }

    private void initialize() {
        if (!initialized) {

            createToolbar();

            table.getEmptyText().setText("No attributes");

            table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            table.setAutoCreateColumnsFromModel(true);
            table.setShowColumns(true);
            table.setEnableAntialiasing(true);

            TableModel tableModel = new LdapAttributesTableModel(virtualFile.getLdapTreeNode().getLdapNode());
            table.setModel(tableModel);

            TableColumn attributeColumn = table.getColumn(LdapAttributesTableModel.COLUMN_NAMES[0]);
            attributeColumn.setResizable(true);
            attributeColumn.setWidth(300);
            attributeColumn.setMinWidth(120);
            attributeColumn.setMaxWidth(400);
            TableCellRenderer objectClassBoldTableCellRenderer = new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JBLabel label = new JBLabel();

                    if (isSelected) {
                        label.setBackground(table.getSelectionBackground());
                        label.setForeground(table.getSelectionForeground());
                    } else {
                        label.setBackground(table.getBackground());
                        label.setForeground(table.getForeground());
                    }

                    UIUtil.addBorder(label, new EmptyBorder(1, 8, 1, 8));

                    LdapAttributesTableModel model = (LdapAttributesTableModel) table.getModel();
                    String attributeName = (String) model.getValueAt(row, 0);

                    if (value instanceof String) {
                        label.setText((String) value);
                        if (LdapNode.OBJECTCLASS_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                            Font font = new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize());
                            label.setFont(font);
                        }
                    }
                    return label;
                }
            };
            attributeColumn.setCellRenderer(objectClassBoldTableCellRenderer);

            TableColumn valueColumn = table.getColumn(LdapAttributesTableModel.COLUMN_NAMES[1]);
            valueColumn.setResizable(true);
            valueColumn.setCellRenderer(objectClassBoldTableCellRenderer);

            table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionAllowed(true);


            initialized = true;
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        initialize();
        return content;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return content;
    }

    @NotNull
    @Override
    public String getName() {
        return virtualFile.getName();
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T t) {

    }
}
