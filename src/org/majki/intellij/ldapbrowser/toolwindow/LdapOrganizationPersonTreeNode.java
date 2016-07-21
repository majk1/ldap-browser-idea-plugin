package org.majki.intellij.ldapbrowser.toolwindow;

import org.majki.intellij.ldapbrowser.config.LdapConnectionInfo;
import org.majki.intellij.ldapbrowser.ldap.OrganizationalPerson;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

/**
 * @author Attila Majoros
 */

public class LdapOrganizationPersonTreeNode implements TreeNode {

    private TreeNode parent;
    private LdapConnectionInfo info;
    private OrganizationalPerson person;

    public LdapOrganizationPersonTreeNode(TreeNode parent, LdapConnectionInfo info, OrganizationalPerson person) {
        this.parent = parent;
        this.info = info;
        this.person = person;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
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
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Enumeration children() {
        return null;
    }

    @Override
    public String toString() {
        return person.getId();
    }

    public LdapConnectionInfo getInfo() {
        return info;
    }

    public OrganizationalPerson getPerson() {
        return person;
    }
}
