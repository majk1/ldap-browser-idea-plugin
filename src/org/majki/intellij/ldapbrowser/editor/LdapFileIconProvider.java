package org.majki.intellij.ldapbrowser.editor;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LdapFileIconProvider implements FileIconProvider, EditorTabTitleProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull VirtualFile virtualFile, @Iconable.IconFlags int i, @Nullable Project project) {
        if (virtualFile instanceof LdapNodeVirtualFile) {
            return ((LdapNodeVirtualFile) virtualFile).getLdapTreeNode().getIcon();
        }
        return null;
    }

    @Nullable
    @Override
    public String getEditorTabTitle(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof LdapNodeVirtualFile) {
            return "RDN: " + ((LdapNodeVirtualFile) virtualFile).getLdapTreeNode().toString();
        }
        return null;
    }

}
