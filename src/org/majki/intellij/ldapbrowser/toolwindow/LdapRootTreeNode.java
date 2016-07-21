package org.majki.intellij.ldapbrowser.toolwindow;

import com.intellij.util.enumeration.ArrayListEnumeration;
import org.majki.intellij.ldapbrowser.config.LdapConnectionInfo;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapRootTreeNode implements TreeNode {

    private ArrayList<LdapServerTreeNode> ldapServerTreeNodes;

    public LdapRootTreeNode(List<LdapConnectionInfo> ldapConnectionInfos) {
        ldapServerTreeNodes = new ArrayList<>();
        ldapServerTreeNodes.addAll(ldapConnectionInfos.stream().map(ldapConnectionInfo -> new LdapServerTreeNode(this, ldapConnectionInfo)).collect(Collectors.toList()));
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return ldapServerTreeNodes.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return ldapServerTreeNodes.size();
    }

    @Override
    public TreeNode getParent() {
        return null;
    }

    @Override
    public int getIndex(TreeNode node) {
        return ldapServerTreeNodes.indexOf(node);
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
        return new ArrayListEnumeration(ldapServerTreeNodes);
    }
}
