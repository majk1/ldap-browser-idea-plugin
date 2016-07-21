package org.majki.intellij.ldapbrowser.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.OrganizationalPerson;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author Attila Majoros
 */

public class PersonEditor implements FileEditor {

    private VirtualFile virtualFile;
    private JPanel content;
    private JTextField idField;
    private JTextField mailField;
    private JTextField surnameField;
    private JTextField commonNameField;
    private JTextField givenNameField;
    private JTextField passwordField;
    private JLabel headerLabel;

    public PersonEditor(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        PersonVirtualFile file = (PersonVirtualFile) virtualFile;
        OrganizationalPerson person = file.getPerson();

        headerLabel.setText(person.getFullId());
        idField.setText(person.getId());
        mailField.setText(person.getMail());
        surnameField.setText(person.getSurName());
        commonNameField.setText(person.getCommonName());
        givenNameField.setText(person.getGivenName());
        passwordField.setText(person.getPasswordString());

        return content;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return "LDAP person editor";
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T t) {

    }


}
