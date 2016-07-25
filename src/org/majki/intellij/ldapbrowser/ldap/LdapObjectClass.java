package org.majki.intellij.ldapbrowser.ldap;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapObjectClass {

    private static final String TOP = "top";

    private LdapObjectClass superObjectClass;
    private Set<LdapObjectClass> subObjectClasses;

    private String superClassName;
    private String name;
    private String description;

    private Set<LdapObjectClassAttribute> attributes;

    private LdapObjectClass(String superClassName, String name, String description, Set<LdapObjectClassAttribute> attributes) {
        this.superClassName = superClassName;
        this.name = name;
        this.description = description;
        this.attributes = attributes;
        this.subObjectClasses = new HashSet<>();

        for (LdapObjectClassAttribute objectClassAttribute : this.attributes) {
            objectClassAttribute.setObjectClass(this);
        }
    }

    private void setSuperObjectClass(LdapObjectClass superObjectClass) {
        this.superObjectClass = superObjectClass;
    }

    private String getSuperClassName() {
        return superClassName;
    }

    public LdapObjectClass getSuperObjectClass() {
        return superObjectClass;
    }

    public Set<LdapObjectClass> getSubObjectClasses() {
        return subObjectClasses;
    }

    public Set<LdapObjectClass> getAllSubObjectClasses() {
        return getSubObjectClasses(this);
    }

    private Set<LdapObjectClass> getSubObjectClasses(LdapObjectClass objectClass) {
        Set<LdapObjectClass> subObjectClasses = new HashSet<>();
        for (LdapObjectClass subObjectClass: objectClass.getSubObjectClasses()) {
            subObjectClasses.add(subObjectClass);
            if (!subObjectClass.getSubObjectClasses().isEmpty()) {
                subObjectClasses.addAll(getSubObjectClasses(subObjectClass));
            }
        }
        return subObjectClasses;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<LdapObjectClassAttribute> getObjectClassAttributes() {
        return getObjectClassAttributes(false);
    }

    public Set<LdapObjectClassAttribute> getObjectClassAttributes(boolean mustOnly) {
        return mustOnly ? attributes.stream().filter(LdapObjectClassAttribute::isMust).collect(Collectors.toSet()) : attributes;
    }

    public Set<LdapObjectClassAttribute> getObjectClassAttributesWithInherited() {
        return getObjectClassAttributesWithInherited(false);
    }

    public Set<LdapObjectClassAttribute> getObjectClassAttributesWithInherited(boolean mustOnly) {
        Set<LdapObjectClassAttribute> allAttributes = new HashSet<>();
        LdapObjectClass loc = this;
        while (loc != null) {
            allAttributes.addAll(loc.getObjectClassAttributes(mustOnly));
            loc = loc.getSuperObjectClass();
        }
        return allAttributes;
    }

    public LdapObjectClass getByName(String name) {
        LdapObjectClass top = getTop();
        return getByName(name, top);
    }

    public Set<LdapObjectClass> getAllObjectClasses() {
        LdapObjectClass top = getTop();
        Set<LdapObjectClass> allObjectClasses = new HashSet<>();
        allObjectClasses.add(top);
        allObjectClasses.addAll(getAllObjectClasses(top));
        return allObjectClasses;
    }

    public Set<LdapObjectClass> getAllSuperObjectClasses() {
        Set<LdapObjectClass> superObjectClasses = new HashSet<>();
        LdapObjectClass soc = superObjectClass;
        while (soc != null) {
            superObjectClasses.add(soc);
            soc = soc.getSuperObjectClass();
        }
        return superObjectClasses;
    }

    @Override
    public String toString() {
        return name;
    }

    private LdapObjectClass getTop() {
        if (TOP.equals(name)) {
            return this;
        } else {
            LdapObjectClass loc = this;
            while (loc.getSuperObjectClass() != null) {
                loc = loc.getSuperObjectClass();
            }
            if (TOP.equals(loc.getName())) {
                return loc;
            } else {
                throw new IllegalStateException("Coult not find \"top\" ObjectClass");
            }
        }
    }

    private LdapObjectClass getByName(String name, LdapObjectClass from) {
        if (from.getName().equals(name)) {
            return from;
        } else {
            for (LdapObjectClass ldapObjectClass : from.getSubObjectClasses()) {
                LdapObjectClass sub = getByName(name, ldapObjectClass);
                if (sub != null) {
                    return sub;
                }
            }
        }
        return null;
    }

    private Set<LdapObjectClass> getAllObjectClasses(LdapObjectClass subObjectClass) {
        Set<LdapObjectClass> allObjectClasses = new HashSet<>();
        allObjectClasses.addAll(subObjectClass.getSubObjectClasses());
        for (LdapObjectClass objectClass : subObjectClass.getSubObjectClasses()) {
            allObjectClasses.addAll(getAllObjectClasses(objectClass));
        }
        return allObjectClasses;
    }

    public static LdapObjectClass getTop(LdapConnection connection) throws LdapException {
        Map<String, LdapObjectClass> objectClassMap = new HashMap<>();

        EntryCursor cursor = connection.search("ou=schema", "(objectClass=metaObjectClass)", SearchScope.SUBTREE, "*");
        try {
            while (cursor.next()) {
                Entry entry = cursor.get();

                Attribute superClassAttribute = entry.get("m-supobjectclass");
                Attribute nameAttribute = entry.get("m-name");
                Attribute descriptrionAttribute = entry.get("m-description");
                Attribute mustAttribute = entry.get("m-must");
                Attribute mayAttribute = entry.get("m-may");

                String superClassName = superClassAttribute == null ? null : superClassAttribute.get().toString();
                String name = nameAttribute.get().toString();
                String description = descriptrionAttribute == null ? null : descriptrionAttribute.get().toString();

                Set<LdapObjectClassAttribute> objectClassAttributes = new HashSet<>();

                if (mustAttribute != null) {
                    for (Value<?> value : mustAttribute) {
                        objectClassAttributes.add(new LdapObjectClassAttribute(true, value.toString()));
                    }
                }

                if (mayAttribute != null) {
                    for (Value<?> value : mayAttribute) {
                        objectClassAttributes.add(new LdapObjectClassAttribute(false, value.toString()));
                    }
                }

                objectClassMap.put(name, new LdapObjectClass(superClassName, name, description, objectClassAttributes));
            }
        } catch (CursorException e) {
            throw new LdapException("Cursor error", e);
        }

        for (LdapObjectClass ldapObjectClass : objectClassMap.values()) {
            if (ldapObjectClass.getSuperClassName() != null) {
                LdapObjectClass superClass = objectClassMap.get(ldapObjectClass.getSuperClassName());
                superClass.getSubObjectClasses().add(ldapObjectClass);
                ldapObjectClass.setSuperObjectClass(superClass);
            }
        }

        return objectClassMap.get(TOP);
    }
}
