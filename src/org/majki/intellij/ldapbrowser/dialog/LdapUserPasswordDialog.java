package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.MutableCollectionComboBoxModel;
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.password.PasswordDetails;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class LdapUserPasswordDialog extends DialogWrapper {

    private byte[] currentPassword;
    private boolean initialized;

    private JPanel content;
    private JTextField currentAlgorithmTextField;
    private JButton verifyPasswordButton;
    private JIconPasswordField verifyPasswordField;
    private JComboBox<LdapSecurityConstants> newAlgorithmComboBox;
    private JPasswordField newPasswordField;
    private JPasswordField newConfirmPasswordField;
    private JCheckBox showTestPasswordCheckbox;

    public LdapUserPasswordDialog(@NotNull Component parent, @Nullable byte[] currentPassword) {
        super(parent, true);
        this.currentPassword = currentPassword;
        this.initialized = false;

        setTitle("Manage Password");
        getContentPane().setPreferredSize(new Dimension(600, 400));
        setSize(600, 400);
        init();
        validate();
    }

    private void createAlgorithmComboBoxModel(JComboBox<LdapSecurityConstants> comboBox) {
        comboBox.setModel(new MutableCollectionComboBoxModel<>(Arrays.asList(LdapSecurityConstants.values())));
    }

    private boolean verifyPassword(char[] password) {
        byte[] passwordBytes = toSecureBytes(password);
        Arrays.fill(password, (char) 0);
        boolean result = PasswordUtil.compareCredentials(passwordBytes, currentPassword);
        Arrays.fill(passwordBytes, (byte) 0);
        return result;
    }

    private byte[] toSecureBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.defaultCharset().encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        Arrays.fill(charBuffer.array(), (char) 0);
        return bytes;
    }

    private void initialize() {
        if (!initialized) {
            createAlgorithmComboBoxModel(newAlgorithmComboBox);
            new ComboboxSpeedSearch(newAlgorithmComboBox);
            char echoChar = verifyPasswordField.getEchoChar();
            showTestPasswordCheckbox.addActionListener(e -> verifyPasswordField.setEchoChar(showTestPasswordCheckbox.isSelected() ? 0 : echoChar));
            verifyPasswordField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    verifyPasswordField.setIcon(null);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    verifyPasswordField.setIcon(null);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    verifyPasswordField.setIcon(null);
                }
            });

            if (currentPassword == null || currentPassword.length == 0) {
                currentAlgorithmTextField.setText("No password set");
                verifyPasswordField.setEnabled(false);
                verifyPasswordButton.setEnabled(false);
            } else {
                PasswordDetails passwordDetails = PasswordUtil.splitCredentials(currentPassword);
                // handle plaintext passwords
                if (passwordDetails.getAlgorithm() != null) {
                    newAlgorithmComboBox.setSelectedItem(passwordDetails.getAlgorithm());
                    currentAlgorithmTextField.setText(passwordDetails.getAlgorithm().getName());
                }
                ActionListener verifyPasswordAction = e -> {
                    verifyPasswordField.setEnabled(false);
                    verifyPasswordButton.setEnabled(false);
                    SwingUtilities.invokeLater(() -> {
                        if (verifyPassword(verifyPasswordField.getPassword())) {
                            verifyPasswordField.setIcon(AllIcons.Actions.CheckedBlack);
                        } else {
                            verifyPasswordField.setIcon(AllIcons.Actions.Cross);
                            Messages.showErrorDialog(LdapUserPasswordDialog.this.getContentPane(), "Password Mismatch", "Password Mismatch");
                        }
                        verifyPasswordField.setEnabled(true);
                        verifyPasswordButton.setEnabled(true);
                    });
                };
                verifyPasswordField.addActionListener(verifyPasswordAction);
                verifyPasswordButton.addActionListener(verifyPasswordAction);
            }

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
        return verifyPasswordField;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "ldapbrowser.LdapUserPasswordDialog";
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String newPassword = new String(newPasswordField.getPassword());
        String newConfirmPassword = new String(newConfirmPasswordField.getPassword());

        if (newPassword.trim().isEmpty()) {
            return new ValidationInfo("Password is empty", newPasswordField);
        }

        if (!newPassword.equals(newConfirmPassword)) {
            return new ValidationInfo("Passwords don't match", newPasswordField);
        }

        return null;
    }

    public LdapSecurityConstants getAlgorithm() {
        return (LdapSecurityConstants) newAlgorithmComboBox.getSelectedItem();
    }

    public byte[] getNewPassword() {
        return toSecureBytes(newPasswordField.getPassword());
    }

}
