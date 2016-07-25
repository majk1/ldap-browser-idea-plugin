package org.majki.intellij.ldapbrowser.ldap.ui;

import com.intellij.util.enumeration.ArrayListEnumeration;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapRootTreeNode extends DefaultMutableTreeNode implements TreeNode {

    private List<LdapServerTreeNode> children;

    public LdapRootTreeNode(List<LdapConnectionInfo> ldapConnectionInfos) {
        children = new ArrayList<>();
        children.addAll(ldapConnectionInfos.stream().map(ldapConnectionInfo -> new LdapServerTreeNode(ldapConnectionInfo, this)).collect(Collectors.toList()));
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public TreeNode getParent() {
        return null;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Enumeration children() {
        return new ArrayListEnumeration((ArrayList) children);
    }

    @Override
    public String toString() {
        return "LDAP Servers";
    }
}
