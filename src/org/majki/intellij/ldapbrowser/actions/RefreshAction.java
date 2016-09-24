package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapServerTreeNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;
import org.majki.intellij.ldapbrowser.toolwindow.LdapTreePanel;

import javax.swing.tree.DefaultTreeModel;

/**
 * @author Attila Majoros
 */
public class RefreshAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        LdapTreePanel treePanel = ApplicationManager.getApplication().getComponent(LdapTreePanel.class);

        LdapTreeNode[] selectedTreeNodes = treePanel.getTree().getSelectedNodes(LdapTreeNode.class, null);
        if (selectedTreeNodes.length > 0) {
            for (LdapTreeNode selectedNode : selectedTreeNodes) {
                try {
                    selectedNode.getLdapNode().refreshWithChildren();
                    ((DefaultTreeModel) treePanel.getTree().getModel()).nodeStructureChanged(selectedNode);
                } catch (LdapException e1) {
                    LdapErrorHandler.handleError(e1, "Could not refresh node");
                }
            }
        }

        LdapServerTreeNode[] selectedServerTreeNodes = treePanel.getTree().getSelectedNodes(LdapServerTreeNode.class, null);
        if (selectedServerTreeNodes.length > 0) {
            treePanel.reloadTree();
            // TODO: reload only selected
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.SYNCHRONIZE_ICON);
    }
}
