package org.majki.intellij.ldapbrowser.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

import javax.swing.tree.TreePath;


public class DeleteEntryAction extends LdapTreeAction {

    public static final String ID = "ldapbrowser.deleteEntry";

    @Override
    public void actionPerformed(AnActionEvent e) {

        Tree tree = getTreePanel().getTree();
        getSelectedNodes()
            .filter(LdapTreeNode.class::isInstance)
            .map(LdapTreeNode.class::cast)
            .findFirst()
            .ifPresent(treeNode -> {
                TreePath treeNodePath = new TreePath(treeNode.getPath());
                treeNodePath.getParentPath();

                String dn = treeNode.getLdapNode().getDn();
                int result = Messages.showOkCancelDialog("Do you really want to delete Entry: " + dn, "Delete Entry", "Delete", "Cancel", AllIcons.Actions.Delete);
                if (result == Messages.OK) {
                    try {
                        treeNode.getLdapNode().getConnection().delete(dn);
                        ActionManager.getInstance().getAction(RefreshAction.ID).actionPerformed(null);
                        tree.setSelectionPath(treeNodePath.getParentPath());
                    } catch (LdapException e1) {
                        LdapErrorHandler.handleError(e1, "Could not delete entry: " + dn);
                    }
                }
            });
    }

    @Override
    public void update(AnActionEvent e) {
        boolean canDeleteEntry = getSelectedNodes()
            .filter(LdapTreeNode.class::isInstance)
            .map(LdapTreeNode.class::cast)
            .findFirst()
            .isPresent();

        Presentation presentation = e.getPresentation();
        presentation.setIcon(PlatformIcons.DELETE_ICON);
        presentation.setEnabled(canDeleteEntry);
    }
}
