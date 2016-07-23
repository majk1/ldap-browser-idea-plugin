package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

import javax.swing.*;

/**
 * @author Attila Majoros
 */

public class LdapAddAttributeDialog extends DialogWrapper {

    private LdapTreeNode treeNode;

    public LdapAddAttributeDialog(LdapTreeNode treeNode) {
        super(null, true, true);
        this.treeNode = treeNode;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }
}
