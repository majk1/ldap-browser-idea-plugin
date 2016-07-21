package org.majki.intellij.ldapbrowser.ldap;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Attila Majoros
 */

public class OrganizationUnit {

    public static final String OBJECT_CLASS = "organizationalUnit";

    private String fullId;
    private String name;

    private OrganizationUnit(String fullId, String name) {
        this.fullId = fullId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFullId() {
        return fullId;
    }

    public static List<OrganizationUnit> getOrganizationUnits(LdapConnection connection, OrganizationUnit unit) throws LdapException, CursorException {
        return getOrganizationUnits(connection, unit.getFullId());
    }

    public static List<OrganizationUnit> getOrganizationUnits(LdapConnection connection, String baseDn) throws LdapException, CursorException {
        List<OrganizationUnit> organizationUnits = new ArrayList<>();

        EntryCursor cursor = connection.search(baseDn, "(objectClass=*)", SearchScope.ONELEVEL, "*");
        while (cursor.next()) {
            Entry entry = cursor.get();

            String fullId = entry.getDn().getName();
            String name = null;
            String objectClass = null;

            for (Attribute attribute : entry.getAttributes()) {
                switch (attribute.getUpId()) {
                    case "ou":
                        name = attribute.get().toString();
                        break;
                    case "objectClass":
                        objectClass = attribute.get().toString();
                        break;
                    default:
                }
            }

            if (OBJECT_CLASS.equals(objectClass)) {
                organizationUnits.add(new OrganizationUnit(fullId, name));
            }
        }

        return organizationUnits;
    }

}
