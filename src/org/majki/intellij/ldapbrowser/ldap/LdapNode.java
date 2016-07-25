package org.majki.intellij.ldapbrowser.ldap;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapNode implements Serializable {

    public static final String OBJECTCLASS_TOP = "top";
    public static final String OBJECTCLASS_PERSON = "person";
    public static final String OBJECTCLASS_INETORGPERSON = "inetOrgPerson";
    public static final String OBJECTCLASS_ORGANIZATIONAL_PERSON = "organizationalPerson";
    public static final String OBJECTCLASS_ORGANIZATIONAL_UNIT = "organizationalUnit";
    public static final String OBJECTCLASS_DOMAIN = "domain";
    public static final String OBJECTCLASS_GROUP_OF_UNIQUE_NAMES = "groupOfUniqueNames";
    public static final String OBJECTCLASS_META_SCHEMA = "metaSchema";
    public static final String OBJECTCLASS_ACCOUNT = "account";

    public static final String OBJECTCLASS_ATTRIBUTE_NAME = "objectclass";
    public static final String OBJECTCLASS_ATTRIBUTE_NAME_UP = "objectClass";

    public static final String USERPASSWORD_ATTRIBUTE_NAME = "userpassword";
    public static final String USERPASSWORD_ATTRIBUTE_NAME_UP = "userPassword";

    private static final String DEFAULT_FILTER = "(objectclass=*)";
    private static final String DEFAULT_SEARCH_ATTRIBUTE = "*";

    private transient LdapConnection connection;
    private String dn;
    private String rdn;
    private LdapNode parent;
    private List<LdapNode> children;
    private List<LdapAttribute> attributes;

    private List<String> objectClassValues;

    private LdapObjectClass topObjectClass;

    private LdapNode(LdapConnection connection, LdapNode parent, LdapObjectClass topObjectClass, String dn, String rdn, List<LdapAttribute> attributes) {
        this.connection = connection;
        this.dn = dn;
        this.rdn = rdn;
        this.parent = parent;
        this.topObjectClass = topObjectClass;
        this.attributes = attributes;
        this.children = null;
        this.objectClassValues = null;
    }

    public LdapAttribute getAttributeByName(String name) {
        for (LdapAttribute attribute : attributes) {
            if (attribute.name().equalsIgnoreCase(name)) {
                return attribute;
            }
        }
        return null;
    }

    public void refresh() throws LdapException {
        Set<LdapAttribute> removedAttributes = new HashSet<>(attributes);
        objectClassValues = null;
        EntryCursor cursor = connection.search(dn, DEFAULT_FILTER, SearchScope.OBJECT, DEFAULT_SEARCH_ATTRIBUTE);
        try {
            if (cursor.next()) {
                Entry entry = cursor.get();
                for (Attribute attribute : entry.getAttributes()) {
                    LdapAttribute ldapAttribute = getAttributeByName(attribute.getId());
                    removedAttributes.remove(ldapAttribute);
                    if (ldapAttribute != null) {
                        List<LdapAttribute.Value> values = ldapAttribute.values();
                        values.clear();
                        addValuesFromAttribute(attribute, values);
                    } else {
                        List<LdapAttribute.Value> values = new ArrayList<>();
                        addValuesFromAttribute(attribute, values);
                        int index = OBJECTCLASS_ATTRIBUTE_NAME.equals(attribute.getId()) ? 0 : attributes.size();
                        attributes.add(index, new LdapAttribute(attribute.getId(), attribute.getUpId(), attribute.isHumanReadable(), values));
                    }
                }
            }
            attributes.removeAll(removedAttributes);
        } catch (CursorException e) {
            throw new LdapException("Cursor error", e);
        }
    }

    private void addValuesFromAttribute(Attribute attribute, List<LdapAttribute.Value> values) {
        for (Value<?> value : attribute) {
            values.add(new LdapAttribute.Value(value.isNull(), value.isHumanReadable(), value.getString(), value.getBytes()));
        }
    }

    private void searchChildren() throws LdapException {
        children = new ArrayList<>();
        EntryCursor cursor = connection.search(dn, DEFAULT_FILTER, SearchScope.ONELEVEL, DEFAULT_SEARCH_ATTRIBUTE);
        try {
            while (cursor.next()) {
                Entry entry = cursor.get();
                List<LdapAttribute> attributes = new ArrayList<>();
                for (Attribute attribute : entry.getAttributes()) {
                    List<LdapAttribute.Value> values = new ArrayList<>();
                    addValuesFromAttribute(attribute, values);
                    int index = OBJECTCLASS_ATTRIBUTE_NAME.equals(attribute.getId()) ? 0 : attributes.size();
                    attributes.add(index, new LdapAttribute(attribute.getId(), attribute.getUpId(), attribute.isHumanReadable(), values));
                }
                children.add(new LdapNode(connection, this, topObjectClass, entry.getDn().getName(), entry.getDn().getRdn().getName(), attributes));
            }
        } catch (CursorException e) {
            throw new LdapException("Cursor error", e);
        }
    }

    private void extractObjectClassValues() {
        objectClassValues = new ArrayList<>();
        for (LdapAttribute attribute : attributes) {
            if (OBJECTCLASS_ATTRIBUTE_NAME.equals(attribute.name())) {
                objectClassValues.addAll(attribute.values().stream().map(LdapAttribute.Value::asString).collect(Collectors.toList()));
                break;
            }
        }
    }

    public LdapConnection getConnection() {
        return connection;
    }

    public String getDn() {
        return dn;
    }

    public String getRdn() {
        return rdn;
    }

    public LdapNode getParent() {
        return parent;
    }

    public LdapObjectClass getTopObjectClass() {
        return topObjectClass;
    }

    public boolean hasChildrent() throws LdapException {
        return !getChildren().isEmpty();
    }

    public List<LdapNode> getChildren() throws LdapException {
        if (children == null) {
            searchChildren();
        }
        return children;
    }

    public List<LdapAttribute> getAttributes() {
        return attributes;
    }

    public int getChildCount() throws LdapException {
        return getChildren().size();
    }

    public void refreshWithChildren() throws LdapException {
        refresh();
        searchChildren();
    }

    private List<String> getObjectClassValues() {
        if (objectClassValues == null) {
            extractObjectClassValues();
        }
        return objectClassValues;
    }

    public boolean isInstanceOf(String objectClass) {
        if (objectClass == null) {
            return false;
        }
        for (String objectClassValue : getObjectClassValues()) {
            if (objectClassValue.equalsIgnoreCase(objectClass.trim())) {
                return true;
            }
        }
        return false;
    }

    public Set<LdapObjectClass> getObjectClasses() {
        Set<LdapObjectClass> objectClasses = new HashSet<>();
        LdapObjectClass topObjectClass = getTopObjectClass();
        for (String objectClassValue : getObjectClassValues()) {
            LdapObjectClass loc = topObjectClass.getByName(objectClassValue);
            if (loc != null) {
                objectClasses.add(loc);
            }
        }
        return objectClasses;
    }

    public Set<LdapObjectClassAttribute> getObjectClassAttributes() {
        return getObjectClassAttributes(false);
    }

    public Set<LdapObjectClassAttribute> getObjectClassAttributes(boolean mustOnly) {
        Set<LdapObjectClassAttribute> objectClassAttributes = new HashSet<>();
        for (LdapObjectClass ldapObjectClass : getObjectClasses()) {
            objectClassAttributes.addAll(ldapObjectClass.getObjectClassAttributesWithInherited(mustOnly));
        }
        return objectClassAttributes;
    }

    public boolean containsAttributeWithValue(String attributeName, String value) {
        for (LdapAttribute attribute : attributes) {
            if (attribute.name().equalsIgnoreCase(attributeName)) {
                for (LdapAttribute.Value attributeValue : attribute.values()) {
                    if (attributeValue.asString().equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean containsAttribute(String attributeName) {
        return getAttributeByName(attributeName) != null;
    }

    // TODO: data structure modification only
    public Set<LdapObjectClass> addObjectClass(LdapObjectClass objectClass) {
        Set<LdapObjectClass> allObjectClasses = objectClass.getAllSuperObjectClasses();
        allObjectClasses.add(objectClass);

        Set<LdapObjectClassAttribute> requiredAttributes = objectClass.getObjectClassAttributesWithInherited(true);

        for (LdapObjectClass oc : allObjectClasses) {
            if (!containsAttributeWithValue(OBJECTCLASS_ATTRIBUTE_NAME, oc.getName())) {
                LdapAttribute objectClassAttribute = getAttributeByName(OBJECTCLASS_ATTRIBUTE_NAME);
                if (objectClassAttribute == null) {
                    objectClassAttribute = new LdapAttribute(OBJECTCLASS_ATTRIBUTE_NAME, OBJECTCLASS_ATTRIBUTE_NAME_UP, true, new ArrayList<>());
                    attributes.add(objectClassAttribute);
                }
                objectClassAttribute.values().add(new LdapAttribute.Value(false, true, oc.getName(), oc.getName().getBytes()));
            }
        }

        attributes.addAll(requiredAttributes.
                stream().
                filter(requiredAttribute -> !containsAttribute(requiredAttribute.getName())).
                map(requiredAttribute -> new LdapAttribute(
                        requiredAttribute.getName().toLowerCase(),
                        requiredAttribute.getName(),
                        true, new ArrayList<>(Collections.singletonList(
                        new LdapAttribute.Value(false, true, "", "".getBytes()))))).
                collect(Collectors.toList()));

        extractObjectClassValues();

        return allObjectClasses;
    }

    // TODO: data structure modification only
    public Set<LdapObjectClass> removeObjectClass(LdapObjectClass objectClass) {
        Set<LdapObjectClass> removedObjectClasses = new HashSet<>();
        LdapAttribute objectClassAttribute = getAttributeByName(OBJECTCLASS_ATTRIBUTE_NAME);
        List<String> objectClassNames = new ArrayList<>();
        for (LdapAttribute.Value value : objectClassAttribute.values()) {
            objectClassNames.add(value.asString());
        }

        removedObjectClasses.add(objectClass);
        objectClassNames.remove(objectClass.getName());

        for (LdapObjectClass subObjectClass : objectClass.getAllSubObjectClasses()) {
            objectClassNames.remove(subObjectClass.getName());
            removedObjectClasses.add(subObjectClass);
        }

        attributes.clear();
        for (String objectClassName : objectClassNames) {
            addObjectClass(getTopObjectClass().getByName(objectClassName));
        }

        extractObjectClassValues();

        return removedObjectClasses;
    }

    public static LdapNode createRoot(LdapConnection connection) throws LdapException {
        return createRoot(connection, "");
    }

    public static LdapNode createRoot(LdapConnection connection, String baseDn) throws LdapException {
        LdapObjectClass topObjectClass = LdapObjectClass.getTop(connection);
        return new LdapNode(connection, null, topObjectClass, baseDn, getRDN(baseDn), Collections.emptyList());
    }

    public static LdapNode createNew(LdapConnection connection, LdapNode parent) throws LdapException {
        LdapObjectClass topObjectClass = LdapObjectClass.getTop(connection);
        return new LdapNode(null, parent, topObjectClass, "", "", new ArrayList<>());
    }

    public static String getRDN(String dn) {
        if (dn == null) {
            return null;
        } else {
            if (dn.indexOf(',') == -1) {
                return dn;
            } else {
                return dn.split(",")[0].trim();
            }
        }
    }

    public static void valueModifier(LdapAttribute.Value value, String newValue) {
        value.setValue(newValue);
    }

    public static void valueModifier(LdapAttribute.Value value, byte[] newValue) {
        value.setValue(newValue);
    }

}
