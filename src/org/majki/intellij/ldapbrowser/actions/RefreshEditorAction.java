package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.editor.LdapNodeEditor;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;


public class RefreshEditorAction extends AnAction {

    public static final String ID = "ldapbrowser.refreshEditor";

    @Override
    public void actionPerformed(AnActionEvent e) {
        FileEditor fileEditor = DataKeys.FILE_EDITOR.getData(e.getDataContext());
        if (fileEditor instanceof LdapNodeEditor) {
            LdapNodeEditor nodeEditor = (LdapNodeEditor) fileEditor;
            LdapNode ldapNode = nodeEditor.getVirtualFile().getLdapTreeNode().getLdapNode();
            try {
                ldapNode.refresh();
            } catch (LdapException e1) {
                LdapErrorHandler.handleError(e1, "Could not refresh editor");
            }
            nodeEditor.getTableWrapper().getModel().refresh();
            nodeEditor.getTableWrapper().getTable().repaint();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.SYNCHRONIZE_ICON);
    }
}
