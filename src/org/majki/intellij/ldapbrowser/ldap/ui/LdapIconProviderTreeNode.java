package org.majki.intellij.ldapbrowser.ldap.ui;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeNode;


public interface LdapIconProviderTreeNode extends TreeNode {

    @Nullable
    Icon getIcon();

}
