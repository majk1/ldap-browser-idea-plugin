package org.majki.intellij.ldapbrowser.toolwindow;

import com.intellij.util.enumeration.ArrayListEnumeration;
import org.majki.intellij.ldapbrowser.config.LdapConnectionInfo;
import org.majki.intellij.ldapbrowser.ldap.OrganizationUnit;
import org.majki.intellij.ldapbrowser.ldap.OrganizationalPerson;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Attila Majoros
 */

public class LdapOrganizationUnitTreeNode implements TreeNode {

    private TreeNode parent;
    private LdapConnectionInfo info;
    private OrganizationUnit unit;
    private List<OrganizationalPerson> persons;

    public LdapOrganizationUnitTreeNode(TreeNode parent, LdapConnectionInfo ldapConnectionInfo, OrganizationUnit unit) {
        this.parent = parent;
        this.unit = unit;
        this.info = ldapConnectionInfo;
        persons = info.getHandler().getOrganizationalPersons(unit);
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return new LdapOrganizationPersonTreeNode(this, info, persons.get(childIndex));
    }

    @Override
    public int getChildCount() {
        return persons.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        if (node instanceof LdapOrganizationPersonTreeNode) {
            LdapOrganizationPersonTreeNode personTreeNode = (LdapOrganizationPersonTreeNode) node;
            OrganizationalPerson person = personTreeNode.getPerson();
            return persons.indexOf(person);
        } else {
            return 0;
        }
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
        return new ArrayListEnumeration((ArrayList) persons);
    }

    @Override
    public String toString() {
        return unit.getName();
    }

    public LdapConnectionInfo getInfo() {
        return info;
    }

    public OrganizationUnit getUnit() {
        return unit;
    }

    public List<OrganizationalPerson> getPersons() {
        return persons;
    }
}
