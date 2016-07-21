package org.majki.intellij.ldapbrowser.editor;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.dummy.DummyFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.OrganizationalPerson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Attila Majoros
 */

public class PersonVirtualFile extends VirtualFile {

    private OrganizationalPerson person;

    public PersonVirtualFile(OrganizationalPerson person) {
        this.person = person;
    }

    @NotNull
    @Override
    public String getName() {
        return person.getId();
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return new DummyFileSystem();
    }

    @NotNull
    @Override
    public String getPath() {
        return "";
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public VirtualFile getParent() {
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        return new VirtualFile[0];
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
        return System.currentTimeMillis();
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

    public OrganizationalPerson getPerson() {
        return person;
    }

    @Override
    public long getModificationStamp() {
        return System.currentTimeMillis();
    }
}
