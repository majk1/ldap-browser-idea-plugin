package org.majki.intellij.ldapbrowser.ldap;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObjectWrapper;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.ldap.client.api.LdapConnection;

import java.util.*;
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
    private String schemaName;

    private Set<LdapObjectClassAttribute> attributes;

    private LdapObjectClass(String superClassName, String name, String schemaName, String description, Set<LdapObjectClassAttribute> attributes) {
        this.superClassName = superClassName;
        this.name = name;
        this.schemaName = schemaName;
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

    public String getSchemaName() {
        return schemaName;
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

    public static LdapObjectClass getTop(LdapConnectionInfo ldapConnectionInfo) throws LdapException {
        Map<String, LdapObjectClass> objectClassMap = new HashMap<>();

        LdapConnection connection = ldapConnectionInfo.getLdapConnection();
        connection.loadSchemaRelaxed();
        for (Schema schema : connection.getSchemaManager().getAllSchemas()) {
            for (SchemaObjectWrapper schemaObjectWrapper : schema.getContent()) {
                if (schemaObjectWrapper.get() instanceof ObjectClass) {
                    ObjectClass objectClass = (ObjectClass) schemaObjectWrapper.get();

                    List<String> names = objectClass.getNames();
                    if (names.isEmpty()) {
                        names = Collections.singletonList(objectClass.getName());
                    }

                    String superClassName = null;
                    if (!objectClass.getSuperiorOids().isEmpty()) {
                        superClassName = objectClass.getSuperiorOids().get(0); // TODO: handle multiple inheritance
                    }

                    Set<LdapObjectClassAttribute> objectClassAttributes = new HashSet<>();
                    objectClassAttributes.addAll(objectClass.getMustAttributeTypeOids().stream().map(attributeName -> new LdapObjectClassAttribute(true, attributeName)).collect(Collectors.toList()));
                    objectClassAttributes.addAll(objectClass.getMayAttributeTypeOids().stream().map(attributeName -> new LdapObjectClassAttribute(false, attributeName)).collect(Collectors.toList()));

                    for (String name : names) {
                        objectClassMap.put(name.toLowerCase(), new LdapObjectClass(superClassName, name, objectClass.getSchemaName(), objectClass.getDescription(), objectClassAttributes));
                    }
                }
            }
        }

        for (LdapObjectClass ldapObjectClass : objectClassMap.values()) {
            if (ldapObjectClass.getSuperClassName() != null) {
                LdapObjectClass superClass = objectClassMap.get(ldapObjectClass.getSuperClassName().toLowerCase());
                superClass.getSubObjectClasses().add(ldapObjectClass);
                ldapObjectClass.setSuperObjectClass(superClass);
            }
        }

        return objectClassMap.get(TOP);
    }
}
