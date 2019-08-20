package org.majki.intellij.ldapbrowser.ldap.ui;

import org.majki.intellij.ldapbrowser.ldap.LdapConnectionInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public abstract class LdapConnectionInfoTreeNode extends DefaultMutableTreeNode implements LdapIconProviderTreeNode {

    LdapConnectionInfoTreeNode(LdapConnectionInfo connectionInfo, MutableTreeNode parent) {
        super(connectionInfo, true);
        setParent(parent);
    }

    public LdapConnectionInfo getConnectionInfo() {
        return (LdapConnectionInfo) getUserObject();
    }
}
