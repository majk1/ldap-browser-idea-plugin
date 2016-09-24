package org.majki.intellij.ldapbrowser.editor;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Attila Majoros
 */

public class LdapFileIconProvider implements FileIconProvider, EditorTabTitleProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull VirtualFile virtualFile, @Iconable.IconFlags int i, @Nullable Project project) {
        if (virtualFile instanceof LdapNodeVirtualFile) {
            return IconLoader.getIcon("/images/person.png");
        }
        return null;
    }

    @Nullable
    @Override
    public String getEditorTabTitle(Project project, VirtualFile virtualFile) {
        if (virtualFile instanceof LdapNodeVirtualFile) {
            return "RDN: " + ((LdapNodeVirtualFile) virtualFile).getLdapTreeNode().toString();
        }
        return null;
    }

}