package org.majki.intellij.ldapbrowser.ldap.ui;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.enumeration.ArrayListEnumeration;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.editor.LdapNodeVirtualFile;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionInfo;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class LdapTreeNode extends LdapConnectionInfoTreeNode {

    private LdapNode node;
    private LdapNodeVirtualFile file;

    public LdapTreeNode(LdapConnectionInfo info, TreeNode parent, LdapNode node) {
        super(info, parent);
        this.node = node;
        this.file = new LdapNodeVirtualFile(this);
    }

    private TreeNode createChildNode(LdapNode node) {
        return new LdapTreeNode(getConnectionInfo(), this, node);
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        try {
            return createChildNode(node.getChildren().get(childIndex));
            // TODO: cache?
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not get child node at index " + childIndex);
            return null;
        }
    }

    @Override
    public int getChildCount() {
        try {
            return node.getChildCount();
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not get child count");
            return 0;
        }
    }

    @Override
    public int getIndex(TreeNode node) {
        if (node instanceof LdapTreeNode) {
            LdapNode ldapNode = ((LdapTreeNode) node).getLdapNode();
            try {
                return this.node.getChildren().indexOf(ldapNode);
            } catch (LdapException e) {
                LdapErrorHandler.handleError(e, "Cannot get index of LDAP node: " + ldapNode.getDn());
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public boolean getAllowsChildren() {
        try {
            return node.hasChildrent();
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not guess the present of children");
            return false;
        }
    }

    @Override
    public boolean isLeaf() {
        try {
            return !node.hasChildrent();
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not guess the present of children");
            return true;
        }
    }

    public List<TreeNode> childrenList() {
        ArrayList<TreeNode> childLdapTreeNodes = new ArrayList<>();
        List<LdapNode> children = null;
        try {
            children = node.getChildren();
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not get children");
        }
        if (children != null) {
            childLdapTreeNodes.addAll(children.stream().map(this::createChildNode).collect(Collectors.toList()));
        }
        return childLdapTreeNodes;
    }

    @Override
    public Enumeration children() {
        return new ArrayListEnumeration((ArrayList) childrenList());
    }

    public LdapNode getLdapNode() {
        return node;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        if (node.isInstanceOf(LdapNode.OBJECTCLASS_DOMAIN)) {
            return IconLoader.getIcon("/images/domain.png");
        } else if (node.isInstanceOf(LdapNode.OBJECTCLASS_PERSON)) {
            return IconLoader.getIcon("/images/person.png");
        } else if (node.isInstanceOf(LdapNode.OBJECTCLASS_GROUP_OF_UNIQUE_NAMES)) {
            return IconLoader.getIcon("/images/group.png");
        } else if (getAllowsChildren()) {
            return IconLoader.getIcon("/images/node.png");
        } else {
            return IconLoader.getIcon("/images/entry.png");
        }
    }

    public LdapNodeVirtualFile getFile() {
        return file;
    }

    @Override
    public String toString() {
        if (node.isInstanceOf(LdapNode.OBJECTCLASS_DOMAIN)) {
            return node.getDn();
        } else {
            return node.getRdn();
        }
    }
}
