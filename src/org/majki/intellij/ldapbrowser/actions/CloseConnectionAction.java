package org.majki.intellij.ldapbrowser.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import java.util.Optional;


public class CloseConnectionAction extends LdapTreeAction {

    public static final String ID = "ldapbrowser.closeConnection";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Tree tree = getTreePanel().getTree();
        getSelectedNodes(tree)
            .map(this::findLdapServerNode)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .distinct()
            .forEach(ldapServerTreeNode -> {
                ldapServerTreeNode.getConnectionInfo().disconnect();
                ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(ldapServerTreeNode);
            });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Tree tree = getTreePanel().getTree();
        Boolean closeable = getSelectedNodes(tree)
            .findFirst()
            .map(this::isCloseable)
            .orElse(false);

        Presentation presentation = e.getPresentation();
        presentation.setIcon(AllIcons.Actions.Suspend);
        presentation.setEnabled(closeable);
    }
}
