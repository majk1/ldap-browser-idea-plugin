package org.majki.intellij.ldapbrowser.ldap.ui;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.enumeration.ArrayListEnumeration;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionInfo;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapServerTreeNode extends LdapConnectionInfoTreeNode {

    private List<LdapTreeNode> children;

    public LdapServerTreeNode(LdapConnectionInfo info, TreeNode parent) {
        super(info, parent);
        this.children = null;
    }

    private void createChildren() {
        children = new ArrayList<>();
        try {
            LdapNode root = LdapNode.createRoot(getConnectionInfo(), getConnectionInfo().getBaseDn());
            children.addAll(root.getChildren().stream().map(ldapNode -> new LdapTreeNode(getConnectionInfo(), this, ldapNode)).collect(Collectors.toList()));
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not create server child nodes");
        }
    }

    private List<LdapTreeNode> getChildren() {
        if (children == null) {
            createChildren();
        }
        return children;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        if (getConnectionInfo().isOpened()) {
            return getChildren().get(childIndex);
        } else {
            return null;
        }
    }

    @Override
    public int getChildCount() {
        if (getConnectionInfo().isOpened()) {
            return getChildren().size();
        } else {
            return 0;
        }
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public int getIndex(TreeNode node) {
        if (getConnectionInfo().isOpened()) {
            return children.indexOf(node);
        } else {
            return 0;
        }
    }

    @Override
    public boolean getAllowsChildren() {
        return getConnectionInfo().isOpened();
    }

    @Override
    public boolean isLeaf() {
        return !getConnectionInfo().isOpened();
    }

    @Override
    public Enumeration children() {
        return new ArrayListEnumeration((ArrayList) children);
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/images/server.png");
    }

    @Override
    public String toString() {
        return getConnectionInfo().getName();
    }
}
