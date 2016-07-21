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

public class OrganizationalPerson {

    public static final String OBJECT_CLASS = "organizationalPerson";

    private String fullId;

    private String id;
    private String mail;
    private String surName;
    private String commonName;
    private String givenName;
    private String displayName;
    private byte[] password;
    private String passwordString;

    protected OrganizationalPerson(String fullId, String id, String mail, String surName, String commonName, String givenName, String displayName, byte[] password, String passwordString) {
        this.fullId = fullId;
        this.id = id;
        this.mail = mail;
        this.surName = surName;
        this.commonName = commonName;
        this.givenName = givenName;
        this.displayName = displayName;
        this.password = password;
        this.passwordString = passwordString;
    }

    public String getFullId() {
        return fullId;
    }

    public String getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public String getSurName() {
        return surName;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public byte[] getPassword() {
        return password;
    }

    public String getPasswordString() {
        return passwordString;
    }

    public static List<OrganizationalPerson> getOrganizationalPersons(LdapConnection connection, OrganizationUnit organizationUnit) throws LdapException, CursorException {
        List<OrganizationalPerson> organizationalPersons = new ArrayList<>();
        EntryCursor cursor = connection.search(organizationUnit.getFullId(), "(objectclass=*)", SearchScope.ONELEVEL, "*");

        while (cursor.next()) {
            Entry entry = cursor.get();

            String fullId = entry.getDn().getName();
            String uid = null;
            String mail = null;
            String userPasswordString = null;
            byte[] userPassword = null;
            String sn = null;
            String cn = null;
            String givenName = null;
            String displayName = null;
            String objectClass = null;

            for (Attribute attribute : entry.getAttributes()) {
                switch (attribute.getUpId()) {
                    case "uid":
                        uid = attribute.get().toString();
                        break;
                    case "mail":
                        mail = attribute.get().toString();
                        break;
                    case "sn":
                        sn = attribute.get().toString();
                        break;
                    case "cn":
                        cn = attribute.get().toString();
                        break;
                    case "givenName":
                        givenName = attribute.get().toString();
                        break;
                    case "displayName":
                        displayName = attribute.get().toString();
                        break;
                    case "userPassword":
                        userPasswordString = attribute.get().toString();
                        userPassword = attribute.getBytes();
                        break;
                    case "objectClass":
                        objectClass = attribute.get().toString();
                        break;
                    default:
                }
            }

            if (OBJECT_CLASS.equals(objectClass)) {
                organizationalPersons.add(new OrganizationalPerson(fullId, uid, mail, sn, cn, givenName, displayName, userPassword, userPasswordString));
            }
        }
        return organizationalPersons;
    }
}
