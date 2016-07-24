package org.majki.intellij.ldapbrowser.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;
import org.majki.intellij.ldapbrowser.toolwindow.LdapTreePanel;

/**
 * @author Attila Majoros
 */
public class DeleteEntryAction extends AnAction {

    public static final String ID = "ldapbrowser.deleteEntry";

    @Override
    public void actionPerformed(AnActionEvent e) {

        Tree tree = ApplicationManager.getApplication().getComponent(LdapTreePanel.class).getTree();
        LdapTreeNode treeNode = null;
        LdapTreeNode[] selectedNodes = tree.getSelectedNodes(LdapTreeNode.class, null);
        if (selectedNodes.length > 0) {
            treeNode = selectedNodes[0];
        }

        if (treeNode != null) {
            tree.setSelectionPath(tree.getSelectionPath().getParentPath());
            String dn = treeNode.getLdapNode().getDn();
            int result = Messages.showOkCancelDialog("Do you really want to delete Entry: " + dn, "Delete Entry", "Delete", "Cancel", AllIcons.Actions.Delete);
            if (result == Messages.OK) {
                try {
                    treeNode.getLdapNode().getConnection().delete(dn);
                    ActionManager.getInstance().getAction(RefreshAction.ID).actionPerformed(null);
                } catch (LdapException e1) {
                    LdapErrorHandler.handleError(e1, "Could not delete entry: " + dn);
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.DELETE_ICON);
    }
}
