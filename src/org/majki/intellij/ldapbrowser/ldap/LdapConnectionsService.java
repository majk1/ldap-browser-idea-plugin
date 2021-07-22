package org.majki.intellij.ldapbrowser.ldap;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


@State(name = "ldapConnections", storages = {
    @Storage(value = "ldapConnections.xml", exportable = true)
})
public class LdapConnectionsService implements PersistentStateComponent<LdapConnectionsService.State>, ApplicationComponent {

    private static final Logger log = Logger.getInstance(LdapConnectionsService.class);
    private static final String COMPONENT_NAME = "ldapbrowser.ldapConnectionsService";
    private State state;

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        log.debug("loading ldap connections from xml");
        this.state = state;
    }

    @Override
    public void initComponent() {
        if (state == null) {
            state = new State();
        }
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public List<LdapConnectionInfo> getLdapConnectionInfos() {
        return state.getLdapConnectionInfos();
    }

    public void setLdapConnectionInfos(List<LdapConnectionInfo> connectionInfos) {
        state.setLdapConnectionInfos(connectionInfos);
    }

    @SuppressWarnings("WeakerAccess")
    public static class State {
        private List<LdapConnectionInfo> ldapConnectionInfos = new ArrayList<>();

        public List<LdapConnectionInfo> getLdapConnectionInfos() {
            return ldapConnectionInfos;
        }

        public void setLdapConnectionInfos(List<LdapConnectionInfo> ldapConnectionInfos) {
            this.ldapConnectionInfos = ldapConnectionInfos;
        }
    }
}
