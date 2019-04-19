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
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Attila Majoros
 */

public class LdapUserPasswordDialog extends DialogWrapper {

    private byte[] currentPassword;
    private boolean initialized;

    private JPanel content;
    private JTextField currentAlgorithmTextField;
    private JButton verifyPasswordButton;
    private JPasswordField verifyPasswordField;
    private JComboBox newAlgorithmComboBox;
    private JPasswordField newPasswordField;
    private JPasswordField newConfirmPasswordField;

    public LdapUserPasswordDialog(@NotNull Component parent,  @Nullable byte[] currentPassword) {
        super(parent, true);
        this.currentPassword = currentPassword;
        this.initialized = false;

        setTitle("Manage Password");
        getContentPane().setPreferredSize(new Dimension(600, 400));
        setSize(600, 400);
        init();
        validate();
    }

    private void createAlogirithmComboBoxModel(JComboBox comboBox) {
        //noinspection unchecked
        comboBox.setModel(new MutableCollectionComboBoxModel<>(new ArrayList<>(Arrays.asList(LdapSecurityConstants.values()))));
    }

    private boolean verifyPassword(String password) {
        return PasswordUtil.compareCredentials(password.getBytes(), currentPassword);
    }

    private void initialize() {
        if (!initialized) {
            createAlogirithmComboBoxModel(newAlgorithmComboBox);
            new ComboboxSpeedSearch(newAlgorithmComboBox);
            verifyPasswordButton.setIcon(AllIcons.Actions.Checked);

            if (currentPassword == null || currentPassword.length == 0) {
                currentAlgorithmTextField.setText("No password set");
                verifyPasswordField.setEnabled(false);
                verifyPasswordButton.setEnabled(false);
            } else {
                PasswordDetails passwordDetails = PasswordUtil.splitCredentials(currentPassword);
                // handle plaintext passwords
                if(passwordDetails.getAlgorithm()!=null) {
                    newAlgorithmComboBox.setSelectedItem(passwordDetails.getAlgorithm());
                    currentAlgorithmTextField.setText(passwordDetails.getAlgorithm().getName());
                }
                ActionListener verifyPasswordAction = e -> {
                    if (verifyPassword(new String(verifyPasswordField.getPassword()))) {
                        Messages.showInfoMessage(LdapUserPasswordDialog.this.getContentPane(), "Password Match", "Password Match");
                    } else {
                        Messages.showErrorDialog(LdapUserPasswordDialog.this.getContentPane(), "Password Mismatch", "Password Mismatch");
                    }
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

    public String getNewPassword() {
        return new String(newPasswordField.getPassword());
    }

}
