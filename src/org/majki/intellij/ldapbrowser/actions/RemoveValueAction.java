package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.TextBundle;
import org.majki.intellij.ldapbrowser.editor.LdapNodeEditor;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapAttributeTableModel;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;

public class RemoveValueAction extends AnAction {

    public static final String ID = "ldapbrowser.removeValue";

    @Override
    public void actionPerformed(AnActionEvent e) {
        FileEditor fileEditor = DataKeys.FILE_EDITOR.getData(e.getDataContext());
        if (fileEditor instanceof LdapNodeEditor) {
            LdapNodeEditor nodeEditor = (LdapNodeEditor) fileEditor;
            int selectedRow = nodeEditor.getTableWrapper().getTable().getSelectedRow();
            if (selectedRow != -1) {
                LdapAttributeTableModel.Item selectedItem = nodeEditor.getTableWrapper().getModel().getItems().get(selectedRow);

                int result = Messages.showOkCancelDialog(
                    fileEditor.getComponent(),
                    TextBundle.message("ldapbrowser.remove-message", selectedItem.getValue().asString(), selectedItem.getAttribute().name()),
                    TextBundle.message("ldapbrowser.remove-value"),
                    TextBundle.message("ldapbrowser.remove"),
                    TextBundle.message("ldapbrowser.cancel"),
                    PlatformIcons.DELETE_ICON
                );

                if (result == Messages.OK) {
                    LdapNode ldapNode = nodeEditor.getTableWrapper().getLdapNode();

                    Modification modification = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, selectedItem.getAttribute().name(), selectedItem.getValue().asString());
                    try {
                        ldapNode.getConnection().modify(ldapNode.getDn(), modification);

                        ldapNode.refresh();
                        nodeEditor.getTableWrapper().getModel().refresh();
                        nodeEditor.getTableWrapper().getTable().repaint();
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
