package org.majki.intellij.ldapbrowser.ldap;

import com.intellij.util.xmlb.annotations.Transient;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.template.exception.LdapRuntimeException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


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

    public static final String NAMING_CONTEXT_ATTRIBUTE_NAME = "namingcontexts";
    public static final String NAMING_CONTEXT_ATTRIBUTE_NAME_UP = "namingContexts";

    private static final String DEFAULT_FILTER = "(objectclass=*)";
    private static final String DEFAULT_SEARCH_ATTRIBUTE = "*";

    @Transient
    private final LdapConnectionInfo ldapConnectionInfo;

    private String dn;
    private String rdn;
    private LdapNode parent;
    private List<LdapNode> children;
    private List<LdapAttribute> attributes;
    private List<String> objectClassValues;
    private LdapObjectClass topObjectClass;

    private LdapNode(LdapConnectionInfo ldapConnectionInfo, LdapNode parent, LdapObjectClass topObjectClass, String dn, String rdn, List<LdapAttribute> attributes) {
        this.ldapConnectionInfo = ldapConnectionInfo;
        this.dn = dn;
        this.rdn = rdn;
        this.parent = parent;
        this.topObjectClass = topObjectClass;
        this.attributes = attributes;
        this.children = null;
        this.objectClassValues = null;
    }

    public static LdapNode createRoot(LdapConnectionInfo ldapConnectionInfo) throws LdapException {
        return createRoot(ldapConnectionInfo, "");
    }

    public static LdapNode createRoot(LdapConnectionInfo ldapConnectionInfo, String baseDn) throws LdapException {
        LdapObjectClass topObjectClass = LdapObjectClass.getTop(ldapConnectionInfo);
        return new LdapNode(ldapConnectionInfo, null, topObjectClass, baseDn, LdapUtil.getTopDn(baseDn), Collections.emptyList());
    }

    public static LdapNode createNew(LdapConnectionInfo ldapConnectionInfo, LdapNode parent) throws LdapException {
        LdapObjectClass topObjectClass = LdapObjectClass.getTop(ldapConnectionInfo);
        return new LdapNode(null, parent, topObjectClass, "", "", new ArrayList<>());
    }

    public static void valueModifier(LdapAttribute.Value value, String newValue) {
        value.setValue(newValue);
    }

    public static void valueModifier(LdapAttribute.Value value, byte[] newValue) {
        value.setValue(newValue);
    }

    public LdapConnectionInfo getLdapConnectionInfo() {
        return ldapConnectionInfo;
    }

    public Optional<LdapAttribute> getAttributeByName(String name) {
        return attributes.stream()
            .filter(attribute -> attribute.name().equalsIgnoreCase(name))
            .findFirst();
    }

    public void refresh() throws LdapException {
        Set<LdapAttribute> notSeenAttributes = new HashSet<>(attributes);
        objectClassValues = null;
        try (EntryCursor cursor = getConnection().search(dn, DEFAULT_FILTER, SearchScope.OBJECT, DEFAULT_SEARCH_ATTRIBUTE)) {
            if (cursor.next()) {
                Entry entry = cursor.get();
                for (Attribute attribute : entry.getAttributes()) {
                    Optional<LdapAttribute> foundLdapAttribute = getAttributeByName(attribute.getId());
                    if (foundLdapAttribute.isPresent()) {
                        LdapAttribute ldapAttribute = foundLdapAttribute.get();
                        notSeenAttributes.remove(ldapAttribute);
                        List<LdapAttribute.Value> values = ldapAttribute.values();
                        values.clear();
                        addValuesFromAttribute(attribute, values);
                    } else {
                        mapLdapAttributes(attributes, attribute);
                    }
                }
            }
            attributes.removeAll(notSeenAttributes);
        } catch (CursorException | IOException e) {
            throw new LdapException("Cursor error", e);
        }
    }

    private void addValuesFromAttribute(Attribute attribute, List<LdapAttribute.Value> values) {
        attribute.forEach(value -> values.add(new LdapAttribute.Value(value.isNull(), value.isHumanReadable(), value.getString(), value.getBytes())));
    }

    private void readRootDSN() throws LdapException {
        Entry rootDse = getConnection().getRootDse(NAMING_CONTEXT_ATTRIBUTE_NAME);
        if (rootDse == null) {
            throw new LdapException("No root dse has been found");
        }
        for (Attribute attribute : rootDse.getAttributes()) {
            if (attribute.getId().equalsIgnoreCase(NAMING_CONTEXT_ATTRIBUTE_NAME)) {
                for (Value value : attribute) {
                    LdapNode node = new LdapNode(ldapConnectionInfo, this, topObjectClass, value.getString(), value.getString(), new ArrayList<>());
                    node.refresh();
                    children.add(node);
                }
            }
        }
    }

    private void searchChildren() throws LdapException {
        children = new ArrayList<>();

        if (dn == null || dn.trim().isEmpty()) {
            readRootDSN();
        } else {
            try (EntryCursor cursor = getConnection().search(dn, DEFAULT_FILTER, SearchScope.ONELEVEL, DEFAULT_SEARCH_ATTRIBUTE)) {
                while (cursor.next()) {
                    Entry entry = cursor.get();
                    List<LdapAttribute> attributes = new ArrayList<>();
                    for (Attribute attribute : entry.getAttributes()) {
                        mapLdapAttributes(attributes, attribute);
                    }
                    children.add(new LdapNode(ldapConnectionInfo, this, topObjectClass, entry.getDn().getName(), entry.getDn().getRdn().getName(), attributes));
                }
            } catch (CursorException | IOException e) {
                throw new LdapException("Cursor error", e);
            }
        }

        children.sort(Comparator.comparing(LdapNode::getRdn));
    }

    private void mapLdapAttributes(List<LdapAttribute> attributes, Attribute attribute) {
        List<LdapAttribute.Value> values = new ArrayList<>();
        addValuesFromAttribute(attribute, values);
        int index = OBJECTCLASS_ATTRIBUTE_NAME.equalsIgnoreCase(attribute.getId()) ? 0 : attributes.size();
        attributes.add(index, new LdapAttribute(attribute.getId(), attribute.getUpId(), attribute.isHumanReadable(), values));
    }

    private void extractObjectClassValues() {
        objectClassValues = new ArrayList<>();
        for (LdapAttribute attribute : attributes) {
            if (OBJECTCLASS_ATTRIBUTE_NAME.equalsIgnoreCase(attribute.name())) {
                objectClassValues.addAll(attribute.values().stream().map(LdapAttribute.Value::asString).collect(Collectors.toList()));
                break;
            }
        }
    }

    public LdapConnection getConnection() {
        return ldapConnectionInfo.getLdapConnection();
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
        return attributes.stream()
            .filter(attribute -> attribute.name().equalsIgnoreCase(attributeName))
            .flatMap(attribute -> attribute.values().stream())
            .anyMatch(attributeValue -> attributeValue.asString().equals(value));
    }

    public boolean containsAttribute(String attributeName) {
        return getAttributeByName(attributeName).isPresent();
    }

    // TODO: data structure modification only
    public Set<LdapObjectClass> addObjectClass(LdapObjectClass objectClass) {
        Set<LdapObjectClass> allObjectClasses = objectClass.getAllSuperObjectClasses();
        allObjectClasses.add(objectClass);

        Set<LdapObjectClassAttribute> requiredAttributes = objectClass.getObjectClassAttributesWithInherited(true);

        for (LdapObjectClass oc : allObjectClasses) {
            if (!containsAttributeWithValue(OBJECTCLASS_ATTRIBUTE_NAME, oc.getName())) {
                getAttributeByName(OBJECTCLASS_ATTRIBUTE_NAME).orElseGet(() -> {
                    LdapAttribute ldapAttribute = new LdapAttribute(OBJECTCLASS_ATTRIBUTE_NAME, OBJECTCLASS_ATTRIBUTE_NAME_UP, true, new ArrayList<>());
                    attributes.add(ldapAttribute);
                    return ldapAttribute;
                }).values().add(new LdapAttribute.Value(false, true, oc.getName(), oc.getName().getBytes()));
            }
        }

        requiredAttributes.stream()
            .filter(requiredAttribute -> !containsAttribute(requiredAttribute.getName()))
            .map(requiredAttribute -> new LdapAttribute(
                requiredAttribute.getName().toLowerCase(),
                requiredAttribute.getName(),
                true, new ArrayList<>(Collections.singletonList(
                new LdapAttribute.Value(false, true, "", "".getBytes())))))
            .forEach(attributes::add);

        extractObjectClassValues();

        return allObjectClasses;
    }

    // TODO: data structure modification only
    public Set<LdapObjectClass> removeObjectClass(LdapObjectClass objectClass) {
        Set<LdapObjectClass> removedObjectClasses = new HashSet<>();
        LdapAttribute objectClassAttribute = getAttributeByName(OBJECTCLASS_ATTRIBUTE_NAME)
            .orElseThrow(() -> new LdapRuntimeException(new LdapException("Could not find attribute " + OBJECTCLASS_ATTRIBUTE_NAME)));

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

}
