package org.majki.intellij.ldapbrowser.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;


public class LdapToolWindowFactory implements ToolWindowFactory {

    private static final Logger log = Logger.getInstance(LdapToolWindowFactory.class);

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        log.debug("Creating LDAP Tool Window");

        final LdapTreePanel ldapTreePanel = ApplicationManager.getApplication()
            .getComponent(LdapTreePanel.class); // todo: don't use Application Component

        ldapTreePanel.setProject(project);

        final Content content = toolWindow.getContentManager()
            .getFactory()
            .createContent(ldapTreePanel, "Connections", false);

        toolWindow.getContentManager().addContent(content);
        toolWindow.setStripeTitle("LDAP");
    }

}
