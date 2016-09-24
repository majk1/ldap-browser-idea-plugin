package org.majki.intellij.ldapbrowser.editor;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.dummy.DummyFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

import javax.swing.tree.TreeNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Attila Majoros
 */

public class LdapNodeVirtualFile extends VirtualFile {

    private LdapTreeNode ldapTreeNode;

    public LdapNodeVirtualFile(LdapTreeNode ldapTreeNode) {
        this.ldapTreeNode = ldapTreeNode;
    }

    @NotNull
    @Override
    public String getName() {
        return ldapTreeNode.toString()+ "." + LdapNodeFileType.EXTENSION;
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return new DummyFileSystem();
    }

    @NotNull
    @Override
    public String getPath() {
        LdapNode ldapNode = ldapTreeNode.getLdapNode();
        return ldapNode.getParent() != null ? ldapNode.getParent().getDn() : "";
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return ldapTreeNode.getAllowsChildren();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public VirtualFile getParent() {
        TreeNode parentTreeNode = ldapTreeNode.getParent();
        if (parentTreeNode instanceof LdapTreeNode) {
            return ((LdapTreeNode) parentTreeNode).getFile();
        } else {
            return null;
        }
    }

    @Override
    public VirtualFile[] getChildren() {
        List<VirtualFile> childVirtualFiles = new ArrayList<>();
        for (TreeNode treeNode : ldapTreeNode.childrenList()) {
            if (treeNode instanceof LdapTreeNode) {
                childVirtualFiles.add(((LdapTreeNode) treeNode).getFile());
            }
        }
        return childVirtualFiles.toArray(new VirtualFile[childVirtualFiles.size()]);
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return new ByteArrayOutputStream();
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray() throws IOException {
        return new byte[0];
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public void refresh(boolean b, boolean b1, @Nullable Runnable runnable) {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public long getModificationStamp() {
        return 0;
    }

    public LdapTreeNode getLdapTreeNode() {
        return ldapTreeNode;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
