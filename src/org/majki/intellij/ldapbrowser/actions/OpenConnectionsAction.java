package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionsService;
import org.majki.intellij.ldapbrowser.toolwindow.LdapConnectionsDialog;
import org.majki.intellij.ldapbrowser.toolwindow.LdapTreePanel;

/**
 * @author Attila Majoros
 */
public class OpenConnectionsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        LdapConnectionsService connectionsService = ApplicationManager.getApplication().getComponent(LdapConnectionsService.class);
        LdapConnectionsDialog ldapConnectionsDialog = new LdapConnectionsDialog(connectionsService.getLdapConnectionInfos());
        if (ldapConnectionsDialog.showAndGet()) {
            connectionsService.getState().setLdapConnectionInfos(ldapConnectionsDialog.getConnectionInfos());
            LdapTreePanel treePanel = ApplicationManager.getApplication().getComponent(LdapTreePanel.class);
            treePanel.reloadTree();
        }
    }
}
