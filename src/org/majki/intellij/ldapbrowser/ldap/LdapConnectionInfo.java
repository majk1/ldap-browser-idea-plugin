package org.majki.intellij.ldapbrowser.ldap;

import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.annotations.Transient;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Attila Majoros
 */

public class LdapConnectionInfo implements Serializable {

    private String name = "<unnamed ldap connection>";
    private String host = "localhost";
    private int port = 389;
    private String baseDn;
    private boolean auth = false;
    private boolean opened = false;
    private String username;
    private String password;

    @Transient
    private LdapConnection ldapConnection;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBaseDn() {
        return baseDn == null ? "" : baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return name;
    }

    public LdapConnectionInfo getCopy() {
        LdapConnectionInfo info = new LdapConnectionInfo();
        info.setName(getName());
        info.setHost(getHost());
        info.setPort(getPort());
        info.setBaseDn(getBaseDn());
        info.setAuth(isAuth());
        info.setOpened(isOpened());
        info.setUsername(getUsername());
        info.setPassword(getPassword());
        return info;
    }

    @Transient
    public LdapConnection getLdapConnection() {
        if (!opened) {
            return null;
        } else if (ldapConnection == null || !ldapConnection.isConnected()) {
            connect();
        }
        return ldapConnection;
    }

    public void connect() {
        if (ldapConnection == null || !ldapConnection.isConnected()) {
            ldapConnection = new LdapNetworkConnection(host, port);
            try {
                if (auth) {
                    ldapConnection.bind(username, password);
                } else {
                    ldapConnection.bind();
                }
                opened = true;

                if (!isSchemaPresent(ldapConnection)) {
                    Messages.showWarningDialog("Schema DN (" + LdapNode.SCHEMA_DN + ") was not found!", "LDAP Connection Failed");
                }
            } catch (LdapException e) {
                Messages.showErrorDialog(e.getMessage(), "LDAP Connection Failed");
            }
        }
    }

    public void disconnect() {
        if (ldapConnection != null) {
            if (ldapConnection.isConnected()) {
                try {
                    ldapConnection.unBind();
                    ldapConnection.close();
                } catch (LdapException e) {
                    Messages.showErrorDialog(e.getMessage(), "LDAP Connection Unbind Failed");
                } catch (IOException e) {
                    Messages.showErrorDialog(e.getMessage(), "LDAP Connection Close Failed");
                }
            }
            ldapConnection = null;
        }
        opened = false;
    }

    public boolean testConnection() {
        LdapConnection connection = new LdapNetworkConnection(host, port);
        try {
            if (auth) {
                connection.bind(username, password);
            } else {
                connection.bind();
            }

            boolean schemaPresent = isSchemaPresent(connection);
            connection.unBind();
            connection.close();

            if (!schemaPresent) {
                Messages.showWarningDialog("Connection successful to " + host + ":" + port + ",\n<br>but schema DN (" + LdapNode.SCHEMA_DN + ") was not found!", "LDAP Connection Success");
            }

            return true;
        } catch (LdapException e) {
            Messages.showErrorDialog("Connection failed to " + host + ":" + port + "\n<br>" + e.getMessage(), "LDAP Connection Failed");
            return false;
        } catch (IOException e) {
            Messages.showErrorDialog("Connection failed to " + host + ":" + port + "\n<br>" + e.getMessage(), "LDAP Connection Failed");
            return false;
        }
    }

    private boolean isSchemaPresent(LdapConnection connection) throws LdapException {
        Entry rootDseEntryWithNamingContextAttributes = connection.getRootDse(LdapNode.NAMING_CONTEXT_ATTRIBUTE_NAME_UP);
        for (Attribute attribute : rootDseEntryWithNamingContextAttributes.getAttributes()) {
            if (attribute.getId().equalsIgnoreCase(LdapNode.NAMING_CONTEXT_ATTRIBUTE_NAME)) {
                for (Value<?> value : attribute) {
                    if (value.getString().equals(LdapNode.SCHEMA_DN)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
