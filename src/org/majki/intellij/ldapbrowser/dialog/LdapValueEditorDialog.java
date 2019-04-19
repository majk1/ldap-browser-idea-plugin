package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class LdapValueEditorDialog extends DialogWrapper {

    private JBPanel content;
    private EditorTextField editor;

    private boolean initialized;
    private String originalValue;
    private String attributeName;

    public LdapValueEditorDialog(@NotNull Component parent, String attributeName, String originalValue) {
        super(parent, true);
        this.initialized = false;
        this.originalValue = originalValue;
        this.attributeName = attributeName;

        setTitle("Value Editor");
        init();
        validate();
    }

    private void initialize(){
        if (!initialized) {
            content = new JBPanel(new BorderLayout());
            editor = new EditorTextField(originalValue);
            editor.setMinimumSize(new Dimension(600, 400));
            editor.setOneLineMode(false);

            content.add(editor, BorderLayout.CENTER);

            initialized = true;
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        initialize();
        return content;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return editor;
    }

    @Nullable
    @Override
    protected JComponent createTitlePane() {
        if (attributeName != null && !attributeName.trim().isEmpty()) {
            JBPanel titlePanel = new JBPanel(new BorderLayout());
            JBLabel titleLabel = new JBLabel("<html>Attribute: <b>" + attributeName + "</b></html>");

            Font font = new Font(titleLabel.getFont().getName(), titleLabel.getFont().getStyle(), 16);
            titleLabel.setFont(font);

            titlePanel.add(titleLabel, BorderLayout.CENTER);
            titlePanel.add(new JSeparator(), BorderLayout.PAGE_END);
            return titlePanel;
        } else {
            return super.createTitlePane();
        }
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (editor.getText().trim().isEmpty()) {
            return new ValidationInfo("Value cannot be empty", editor);
        }

        return null;
    }

    public String getValue() {
        return editor.getText();
    }
}
