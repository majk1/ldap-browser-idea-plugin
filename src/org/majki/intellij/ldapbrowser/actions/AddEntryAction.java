package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.jetbrains.annotations.NotNull;
import org.majki.intellij.ldapbrowser.dialog.LdapAddEntryDialog;
import org.majki.intellij.ldapbrowser.ldap.*;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapConnectionInfoTreeNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;


public class AddEntryAction extends LdapTreeAction {

    public static final String ID = "ldapbrowser.addEntry";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Tree tree = getTreePanel().getTree();
        getSelectedNodes()
            .filter(LdapTreeNode.class::isInstance)
            .map(LdapTreeNode.class::cast)
            .findFirst()
            .ifPresent(treeNode -> {
                LdapAddEntryDialog addEntryDialog = new LdapAddEntryDialog(tree, treeNode);
                if (addEntryDialog.showAndGet()) {
                    if (addEntry(addEntryDialog, treeNode.getLdapNode().getConnection())) {
                        ActionManager.getInstance().getAction(RefreshAction.ID).actionPerformed(null);
                    }
                }
            });
    }

    private boolean addEntry(LdapAddEntryDialog dialog, LdapConnection ldapConnection) {
        String dn = dialog.getDn();
        LdapObjectClassAttribute rdn = dialog.getRdn();
        String rdnValue = dialog.getRdnValue();
        LdapNode newLdapNode = dialog.getNewLdapNode();

        String entryDn = rdn.getName() + "=" + rdnValue + "," + dn;

        try {
            DefaultEntry newEntry = new DefaultEntry();
            newEntry.setDn(entryDn);

            for (LdapObjectClass objectClass : newLdapNode.getObjectClasses()) {
                newEntry.add(LdapNode.OBJECTCLASS_ATTRIBUTE_NAME_UP, objectClass.getName());
            }

            newEntry.add(rdn.getName(), rdnValue);

            for (LdapAttribute ldapAttribute : newLdapNode.getAttributes()) {
                newEntry.add(ldapAttribute.upName(), ldapAttribute.valuesAsString());
            }

            ldapConnection.add(newEntry);
            return true;
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not add new entry");
            return false;
        }
    }

    @Override
    public void update(AnActionEvent e) {
        boolean canAddEntry = getSelectedNodes()
            .findFirst()
            .map(LdapConnectionInfoTreeNode::getConnectionInfo)
            .filter(LdapConnectionInfo::isOpened)
            .isPresent();

        Presentation presentation = e.getPresentation();
        presentation.setIcon(PlatformIcons.ADD_ICON);
        presentation.setEnabled(canAddEntry);
    }
}
