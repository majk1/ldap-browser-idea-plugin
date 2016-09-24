package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PlatformIcons;
import org.majki.intellij.ldapbrowser.dialog.LdapConnectionsDialog;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionsService;
import org.majki.intellij.ldapbrowser.toolwindow.LdapTreePanel;

/**
 * @author Attila Majoros
 */
public class OpenConnectionsAction extends AnAction {

    public static final String ID = "ldapbrowser.openConnections";

    @Override
    public void actionPerformed(AnActionEvent e) {
        LdapConnectionsService connectionsService = ApplicationManager.getApplication().getComponent(LdapConnectionsService.class);
        LdapTreePanel treePanel = ApplicationManager.getApplication().getComponent(LdapTreePanel.class);
        LdapConnectionsDialog ldapConnectionsDialog = new LdapConnectionsDialog(treePanel.getTree(), connectionsService.getLdapConnectionInfos());
        if (ldapConnectionsDialog.showAndGet()) {
            connectionsService.getState().setLdapConnectionInfos(ldapConnectionsDialog.getConnectionInfos());
            treePanel.reloadTree();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.SHOW_SETTINGS_ICON);
    }
}
