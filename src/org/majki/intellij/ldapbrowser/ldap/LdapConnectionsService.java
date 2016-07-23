package org.majki.intellij.ldapbrowser.ldap;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Attila Majoros
 */

@State(name = "ldapConnections", storages = {
    @Storage("ldapConnections.xml")
})
public class LdapConnectionsService implements PersistentStateComponent<LdapConnectionsService.State>, ApplicationComponent {

    public static final String COMPONENT_NAME = "ldapbrowser.ldapConnectionsService";

    public static class State {
        private List<LdapConnectionInfo> ldapConnectionInfos = new ArrayList<>();

        public List<LdapConnectionInfo> getLdapConnectionInfos() {
            return ldapConnectionInfos;
        }

        public void setLdapConnectionInfos(List<LdapConnectionInfo> ldapConnectionInfos) {
            this.ldapConnectionInfos = ldapConnectionInfos;
        }
    }

    private State state;

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(State state) {
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
}
