package org.majki.intellij.ldapbrowser.actions.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import org.majki.intellij.ldapbrowser.editor.LdapNodeEditor;

import java.util.Optional;

public abstract class LdapNodeEditorAction extends AnAction {

    protected Optional<LdapNodeEditor> getNodeEditor(AnActionEvent event) {
        return getNodeEditor(event.getDataContext());
    }

    protected Optional<LdapNodeEditor> getNodeEditor(DataContext dataContext) {
        final FileEditor fileEditor = PlatformDataKeys.FILE_EDITOR.getData(dataContext);

        if (fileEditor instanceof LdapNodeEditor) {
            return Optional.of((LdapNodeEditor) fileEditor);
        }
        return Optional.empty();
    }

}
