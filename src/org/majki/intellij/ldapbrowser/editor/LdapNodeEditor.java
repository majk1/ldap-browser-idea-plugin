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
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapAttributeTableWrapper;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class LdapNodeEditor implements FileEditor {

    private LdapNodeVirtualFile virtualFile;
    private boolean initialized;

    private JPanel content;
    private JBTable table;
    private JBPanel toolbarPanel;

    private LdapAttributeTableWrapper tableWrapper;

    public LdapNodeEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
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
            tableWrapper = new LdapAttributeTableWrapper(table, virtualFile.getLdapTreeNode().getLdapNode());
            initialized = true;
        }
    }

    public LdapNodeVirtualFile getVirtualFile() {
        return virtualFile;
    }

    public LdapAttributeTableWrapper getTableWrapper() {
        return tableWrapper;
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
