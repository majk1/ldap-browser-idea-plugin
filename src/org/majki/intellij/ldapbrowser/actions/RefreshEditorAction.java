package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.editor.LdapNodeEditor;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapAttributesTableModel;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;

/**
 * @author Attila Majoros
 */
public class RefreshEditorAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        FileEditor fileEditor = DataKeys.FILE_EDITOR.getData(e.getDataContext());
        if (fileEditor instanceof LdapNodeEditor) {
            LdapNode ldapNode = ((LdapNodeEditor) fileEditor).getVirtualFile().getLdapTreeNode().getLdapNode();
            try {
                ldapNode.refresh();
            } catch (LdapException e1) {
                LdapErrorHandler.handleError(e1, "Could not refresh editor");
            }
            ((LdapAttributesTableModel) ((LdapNodeEditor) fileEditor).getTable().getModel()).refresh();
            ((LdapNodeEditor) fileEditor).getTable().repaint();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.SYNCHRONIZE_ICON);
    }
}
