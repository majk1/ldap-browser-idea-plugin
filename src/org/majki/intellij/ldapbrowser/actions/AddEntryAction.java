package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.majki.intellij.ldapbrowser.dialog.LdapAddEntryDialog;
import org.majki.intellij.ldapbrowser.ldap.LdapAttribute;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.LdapObjectClass;
import org.majki.intellij.ldapbrowser.ldap.LdapObjectClassAttribute;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;
import org.majki.intellij.ldapbrowser.toolwindow.LdapTreePanel;

/**
 * @author Attila Majoros
 */
public class AddEntryAction extends AnAction {

    public static final String ID = "ldapbrowser.addEntry";

    @Override
    public void actionPerformed(AnActionEvent e) {

        Tree tree = ApplicationManager.getApplication().getComponent(LdapTreePanel.class).getTree();
        LdapTreeNode treeNode = null;
        LdapTreeNode[] selectedNodes = tree.getSelectedNodes(LdapTreeNode.class, null);
        if (selectedNodes.length > 0) {
            treeNode = selectedNodes[0];
        }

        if (treeNode != null) {
            LdapAddEntryDialog addEntryDialog = new LdapAddEntryDialog(treeNode);
            if (addEntryDialog.showAndGet()) {
                if (addEntry(addEntryDialog, treeNode.getLdapNode().getConnection())) {
                    ActionManager.getInstance().getAction(RefreshAction.ID).actionPerformed(null);
                }
            }
        }
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
        e.getPresentation().setIcon(PlatformIcons.ADD_ICON);
    }
}
