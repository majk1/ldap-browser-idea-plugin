package org.majki.intellij.ldapbrowser.ldap;

import java.io.Serializable;
import java.util.List;

/**
 * @author Attila Majoros
 */

public class LdapAttribute implements Serializable {

    public static class Value implements Serializable {

        private boolean _null;
        private boolean humanReadable;
        private String stringValue;
        private byte[] byteArrayValue;

        protected Value(boolean isNull, boolean isHumanReadable, String stringValue, byte[] byteArrayValue) {
            this._null = isNull;
            this.humanReadable = isHumanReadable;
            this.stringValue = stringValue;
            this.byteArrayValue = byteArrayValue;
        }

        public boolean isNull() {
            return _null;
        }

        public boolean isHumanReadable() {
            return humanReadable;
        }

        public String asString() {
            return stringValue;
        }

        public byte[] asByteArray() {
            return byteArrayValue;
        }
    }

    private String name;
    private String upName;
    private boolean humanReadable;
    private List<Value> values;

    protected LdapAttribute(String name, String upName, boolean humanReadable, List<Value> values) {
        this.name = name;
        this.upName = upName;
        this.humanReadable = humanReadable;
        this.values = values;
    }

    public String name() {
        return name;
    }

    public String upName() {
        return upName;
    }

    public boolean isHumanReadable() {
        return humanReadable;
    }

    public List<Value> values() {
        return values;
    }

    public Value firstValue() {
        return values.get(0);
    }

}
