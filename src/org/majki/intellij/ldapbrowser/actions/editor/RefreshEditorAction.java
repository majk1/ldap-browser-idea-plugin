package org.majki.intellij.ldapbrowser.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;


public class RefreshEditorAction extends LdapNodeEditorAction {

    public static final String ID = "ldapbrowser.refreshEditor";

    @Override
    public void actionPerformed(AnActionEvent e) {
        getNodeEditor(e).ifPresent(nodeEditor -> {
            LdapNode ldapNode = nodeEditor.getVirtualFile().getLdapTreeNode().getLdapNode();
            try {
                ldapNode.refresh();
            } catch (LdapException e1) {
                LdapErrorHandler.handleError(e1, "Could not refresh editor");
            }
            nodeEditor.getTableWrapper().getModel().refresh();
            nodeEditor.getTableWrapper().getTable().repaint();
        });
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.SYNCHRONIZE_ICON);
    }
}
