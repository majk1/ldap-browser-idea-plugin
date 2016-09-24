package org.majki.intellij.ldapbrowser.ldap;

/**
 * @author Attila Majoros
 */

public final class LdapUtil {

    private LdapUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getTopDn(String dn) {
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

    public static boolean isValidDnFormula(String dnFormula) {
        if (dnFormula == null) {
            return false;
        } else {
            String[] dnParts = dnFormula.split(",");
            if (dnParts.length < 1) {
                return false;
            }
            for (String dnPart : dnParts) {
                String[] keyValues = dnPart.trim().split("=");
                if (keyValues.length < 2) {
                    return false;
                }
                for (String keyOrValue : keyValues) {
                    if (keyOrValue.trim().isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

}
