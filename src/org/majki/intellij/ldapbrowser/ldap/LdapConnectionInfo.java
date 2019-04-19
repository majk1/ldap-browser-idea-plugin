package org.majki.intellij.ldapbrowser.ldap;

import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.annotations.Transient;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.majki.intellij.ldapbrowser.TextBundle;

import java.io.IOException;
import java.io.Serializable;


public class LdapConnectionInfo implements Serializable {

    private String name = "<unnamed ldap connection>";
    private String host = "localhost";
    private int port = 389;
    private String baseDn;
    private boolean auth = false;
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
        return ldapConnection != null && ldapConnection.isConnected();
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
        info.setUsername(getUsername());
        info.setPassword(getPassword());
        return info;
    }

    @Transient
    LdapConnection getLdapConnection() {

        connect();

        return ldapConnection;
    }

    public void connect() {
        ldapConnection = new LdapNetworkConnection(host, port);
        try {
            if (auth) {
                ldapConnection.bind(username, password);
            } else {
                ldapConnection.bind();
            }
        } catch (LdapException e) {
            Messages.showErrorDialog(e.getMessage(), TextBundle.message("ldapbrowser.connection-failure"));
        }
    }

    public void disconnect() {
        if (isOpened()) {
            try {
                ldapConnection.unBind();
                ldapConnection.close();
            } catch (LdapException e) {
                Messages.showErrorDialog(e.getMessage(), TextBundle.message("ldapbrowser.connection-unbind-failure"));
            } catch (IOException e) {
                Messages.showErrorDialog(e.getMessage(), TextBundle.message("ldapbrowser.connection-close-failure"));
            }
        }
        ldapConnection = null;
    }

    public boolean testConnection() {
        try (LdapConnection connection = new LdapNetworkConnection(host, port)) {
            if (auth) {
                connection.bind(username, password);
            } else {
                connection.bind();
            }

            connection.unBind();

            return true;
        } catch (LdapException | IOException e) {
            Messages.showErrorDialog(
                TextBundle.message("ldapbrowser.connection-failure-msg", host, port, e.getMessage()),
                TextBundle.message("ldapbrowser.connection-failure")
            );
            return false;
        }
    }

}
