package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.table.JBTable;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.editor.LdapNodeEditor;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapAttributesTableModel;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

/**
 * @author Attila Majoros
 */
public class RemoveValueAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        FileEditor fileEditor = DataKeys.FILE_EDITOR.getData(e.getDataContext());
        if (fileEditor instanceof LdapNodeEditor) {
            JBTable table = ((LdapNodeEditor) fileEditor).getTable();
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                LdapAttributesTableModel model = ((LdapAttributesTableModel) table.getModel());
                LdapAttributesTableModel.Item selectedItem = model.getItems().get(selectedRow);

                int result = Messages.showOkCancelDialog(
                        fileEditor.getComponent(),
                        "Really delete value \"" + selectedItem.getValue().asString() + "\" from attribute \"" + selectedItem.getAttribute().name() + "\"?",
                        "Remove Value", "Remove", "Cancel", PlatformIcons.DELETE_ICON);
                if (result == Messages.OK) {
                    LdapTreeNode ldapTreeNode = ((LdapNodeEditor) fileEditor).getVirtualFile().getLdapTreeNode();
                    LdapNode ldapNode = ldapTreeNode.getLdapNode();

                    Modification modification = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, selectedItem.getAttribute().name(), selectedItem.getValue().asString());
                    try {
                        ldapNode.getConnection().modify(ldapNode.getDn(), modification);

                        ldapNode.refresh();
                        ((LdapAttributesTableModel) ((LdapNodeEditor) fileEditor).getTable().getModel()).refresh();
                    } catch (LdapException e1) {
                        LdapErrorHandler.handleError(e1, "Could not delete value");
                    }
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.DELETE_ICON);
    }
}
