package org.majki.intellij.ldapbrowser.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

/**
 * @author Attila Majoros
 */

public class LdapToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        LdapTreePanel ldapTreePanel = ApplicationManager.getApplication().getComponent(LdapTreePanel.class);
        ldapTreePanel.setProject(project);
        Content content = toolWindow.getContentManager().getFactory().createContent(ldapTreePanel, "Connections", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setStripeTitle("LDAP");
    }

}
