package org.majki.intellij.ldapbrowser.toolwindow;

import com.intellij.util.enumeration.ArrayListEnumeration;
import org.majki.intellij.ldapbrowser.config.LdapConnectionInfo;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Attila Majoros
 */

public class LdapServerTreeNode implements TreeNode {

    private LdapConnectionInfo info;
    private TreeNode parent;

    public LdapServerTreeNode(TreeNode parent, LdapConnectionInfo ldapConnectionInfo) {
        this.parent = parent;
        this.info = ldapConnectionInfo;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return new LdapOrganizationUnitTreeNode(this, info, info.getHandler().getOrganizationUnits().get(childIndex));
    }

    @Override
    public int getChildCount() {
        return info.getHandler().getOrganizationUnits().size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return 0;
    }

    @Override
    public boolean getAllowsChildren() {
        return info.getHandler().isOpened();
    }

    @Override
    public boolean isLeaf() {
        return !info.getHandler().isOpened();
    }

    @Override
    public Enumeration children() {
        return new ArrayListEnumeration((ArrayList) info.getHandler().getOrganizationUnits());
    }

    @Override
    public String toString() {
        return info.getName();
    }

    public LdapConnectionInfo getInfo() {
        return info;
    }
}
