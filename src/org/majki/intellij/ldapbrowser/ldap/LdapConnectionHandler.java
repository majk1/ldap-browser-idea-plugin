package org.majki.intellij.ldapbrowser.ldap;

import com.intellij.openapi.ui.Messages;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.majki.intellij.ldapbrowser.config.LdapConnectionInfo;

import java.io.IOException;
import java.util.List;

/**
 * @author Attila Majoros
 */

public class LdapConnectionHandler {

    private LdapConnectionInfo connectionInfo;
    private LdapConnection connection;

    public LdapConnectionHandler(LdapConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        if (this.connectionInfo.isOpened()) {
            connect();
        }
    }

    public LdapConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(LdapConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public boolean testConnection() {
        LdapConnection connection = new LdapNetworkConnection(connectionInfo.getHost(), connectionInfo.getPort());

        try {
            if (connectionInfo.isAuth()) {
                connection.bind(connectionInfo.getUsername(), connectionInfo.getPassword());
            } else {
                connection.bind();
            }

            connection.unBind();
            connection.close();

            return true;
        } catch (LdapException e) {
            Messages.showErrorDialog(e.getMessage(), "LDAP Connection Failed");
            return false;
        } catch (IOException e) {
            Messages.showErrorDialog(e.getMessage(), "LDAP Connection Failed");
            return false;
        }
    }

    public final void connect() {
        connection = new LdapNetworkConnection(connectionInfo.getHost(), connectionInfo.getPort());
        try {
            if (connectionInfo.isAuth()) {
                connection.bind(connectionInfo.getUsername(), connectionInfo.getPassword());
            } else {
                connection.bind();
            }
            connectionInfo.setOpened(true);
        } catch (LdapException e) {
            // TODO: handle error
        }
    }

    public void close() {
        if (isOpened()) {
            try {
                connection.unBind();
                connection.close();
            } catch (LdapException e) {
                // TODO: handle error
            } catch (IOException e) {
                // TODO: handle error
            }
            connectionInfo.setOpened(false);
        }
    }

    public boolean isOpened() {
        return connectionInfo.isOpened();
    }

    public List<OrganizationUnit> getOrganizationUnits() {
        return getOrganizationUnits(null);
    }

    public List<OrganizationUnit> getOrganizationUnits(OrganizationUnit unit) {
        if (!isOpened()) {
            connect();
        }

        try {
            if (unit == null) {
                return OrganizationUnit.getOrganizationUnits(connection, connectionInfo.getBaseDn());
            } else {
                return OrganizationUnit.getOrganizationUnits(connection, unit);
            }
        } catch (LdapException e) {
            // TODO: handle error
            throw new IllegalStateException(e);
        } catch (CursorException e) {
            // TODO: handle error
            throw new IllegalStateException(e);
        }
    }

    public List<OrganizationalPerson> getOrganizationalPersons(OrganizationUnit unit) {
        if (!isOpened()) {
            connect();
        }

        try {
            return OrganizationalPerson.getOrganizationalPersons(connection, unit);
        } catch (LdapException e) {
            // TODO: handle error
            throw new IllegalStateException(e);
        } catch (CursorException e) {
            // TODO: handle error
            throw new IllegalStateException(e);
        }
    }

}
