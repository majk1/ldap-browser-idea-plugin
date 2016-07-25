package org.majki.intellij.ldapbrowser.ldap.ui;

import org.majki.intellij.ldapbrowser.ldap.LdapConnectionInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author Attila Majoros
 */

public abstract class LdapConnectionInfoTreeNode extends DefaultMutableTreeNode implements LdapIconProviderTreeNode {

    private LdapConnectionInfo connectionInfo;
    private TreeNode parent;

    public LdapConnectionInfoTreeNode(LdapConnectionInfo connectionInfo, TreeNode parent) {
        this.connectionInfo = connectionInfo;
        this.parent = parent;
    }

    public LdapConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }
}
