package org.majki.intellij.ldapbrowser.config;

import com.intellij.util.xmlb.annotations.Transient;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionHandler;

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
    private LdapConnectionHandler handler;

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
        return baseDn;
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

    @Transient
    public LdapConnectionHandler getHandler() {
        if (handler == null) {
            handler = new LdapConnectionHandler(this);
        }
        return handler;
    }

    public void setHandler(LdapConnectionHandler handler) {
        this.handler = handler;
    }

    @Override
    public String toString() {
        return name;
    }

    public LdapConnectionInfo getCopy() {
        LdapConnectionInfo info = new LdapConnectionInfo();
        info.setName(name);
        info.setHost(host);
        info.setPort(port);
        info.setBaseDn(baseDn);
        info.setAuth(auth);
        info.setOpened(opened);
        info.setUsername(username);
        info.setPassword(password);
        info.setHandler(new LdapConnectionHandler(info));
        return info;
    }
}
