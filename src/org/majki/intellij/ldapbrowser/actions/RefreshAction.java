package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.PlatformIcons;
import org.majki.intellij.ldapbrowser.toolwindow.LdapTreePanel;

/**
 * @author Attila Majoros
 */
public class RefreshAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        LdapTreePanel treePanel = ApplicationManager.getApplication().getComponent(LdapTreePanel.class);
        treePanel.reloadTree();
        // TODO: reload only selected
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.SYNCHRONIZE_ICON);
    }
}
