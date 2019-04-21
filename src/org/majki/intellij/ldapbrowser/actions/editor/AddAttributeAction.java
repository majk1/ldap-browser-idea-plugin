package org.majki.intellij.ldapbrowser.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.majki.intellij.ldapbrowser.dialog.LdapAddAttributeDialog;
import org.majki.intellij.ldapbrowser.dialog.LdapAttributeValuePanel;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

import java.util.ArrayList;
import java.util.List;


public class AddAttributeAction extends LdapNodeEditorAction {

    public static final String ID = "ldapbrowser.addAttribute";

    @Override
    public void actionPerformed(AnActionEvent e) {
        getNodeEditor(e).ifPresent(nodeEditor -> {
            LdapTreeNode ldapTreeNode = nodeEditor.getVirtualFile().getLdapTreeNode();
            LdapAddAttributeDialog addAttributeDialog = new LdapAddAttributeDialog(nodeEditor.getComponent(), ldapTreeNode.getLdapNode());
            if (addAttributeDialog.showAndGet()) {
                LdapNode ldapNode = ldapTreeNode.getLdapNode();

                List<Modification> modifications = new ArrayList<>();
                for (LdapAttributeValuePanel attributeValuePanel : addAttributeDialog.getAttributeValuePanels()) {
                    byte[] byteValue = attributeValuePanel.getByteValue();
                    if (byteValue != null) {
                        modifications.add(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, attributeValuePanel.getSelectedAttribute().getName(), byteValue));
                    } else {
                        modifications.add(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, attributeValuePanel.getSelectedAttribute().getName(), attributeValuePanel.getValue()));
                    }
                }

                try {
                    ldapNode.getConnection().modify(ldapNode.getDn(), modifications.toArray(new Modification[modifications.size()]));
                    ldapNode.refresh();
                    nodeEditor.getTableWrapper().getModel().refresh();
                    nodeEditor.getTableWrapper().getTable().repaint();
                } catch (LdapException e1) {
                    LdapErrorHandler.handleError(e1, "Could not add attribute value");
                }
            }
        });
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.ADD_ICON);
    }
}
