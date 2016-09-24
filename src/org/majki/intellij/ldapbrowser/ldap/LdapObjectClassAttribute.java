package org.majki.intellij.ldapbrowser.ldap;

/**
 * @author Attila Majoros
 */

public class LdapObjectClassAttribute {

    private LdapObjectClass objectClass;
    private boolean must;
    private String name;

    protected LdapObjectClassAttribute(boolean must, String name) {
        this.must = must;
        this.name = name;
    }

    protected void setObjectClass(LdapObjectClass objectClass) {
        this.objectClass = objectClass;
    }

    public LdapObjectClass getObjectClass() {
        return objectClass;
    }

    public boolean isMust() {
        return must;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
