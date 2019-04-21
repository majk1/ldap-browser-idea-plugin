package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.MutableCollectionComboBoxModel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.PlatformIcons;
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.jetbrains.annotations.NotNull;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.LdapObjectClassAttribute;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LdapAttributeValuePanel {

    private JPanel content;
    private JComboBox<LdapObjectClassAttribute> attributeComboBox;
    private JTextField valueTextField;
    private JTextField passwordTextField;
    private JSeparator separator;
    private JButton removeButton;
    private JBPanelWithEmptyText valuePanel;
    private JLabel attributeLabel;
    private JLabel valueLabel;

    private LdapNode ldapNode;
    private boolean initialized;
    private ActionListener removeButtonActionListener;
    private byte[] passwordBytes;

    public LdapAttributeValuePanel(@NotNull LdapNode ldapNode) {
        this.ldapNode = ldapNode;
        this.initialized = false;
        this.passwordBytes = null;
    }

    public void setRemoveButtonActionListener(ActionListener removeButtonActionListener) {
        this.removeButtonActionListener = removeButtonActionListener;
    }

    private void openPasswordDialog() {
        LdapUserPasswordDialog userPasswordDialog = new LdapUserPasswordDialog(content, passwordBytes);
        if (userPasswordDialog.showAndGet()) {
            LdapSecurityConstants algorithm = userPasswordDialog.getAlgorithm();
            String newPassword = userPasswordDialog.getNewPassword();
            passwordBytes = PasswordUtil.createStoragePassword(newPassword, algorithm);
            passwordTextField.setText("Password set (" + algorithm.getName() + ")");
        }
    }

    private void initialize() {
        if (!initialized) {
            valueTextField = new JBTextField();
            passwordTextField = new JBTextField();
            passwordTextField.setEditable(false);
            passwordTextField.setText("No password has been set");
            passwordTextField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    openPasswordDialog();
                }
            });
            passwordTextField.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openPasswordDialog();
                }
            });

            valuePanel.getEmptyText().setText("Select attribute");
            valuePanel.add(valueTextField, BorderLayout.CENTER);

            List<LdapObjectClassAttribute> attributes = new ArrayList<>(ldapNode.getObjectClassAttributes());
            attributes.sort(Comparator.comparing(LdapObjectClassAttribute::getName));
            attributeComboBox.setModel(new MutableCollectionComboBoxModel<>(attributes));
            attributeComboBox.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        valueTextField.requestFocusInWindow();
                    }
                }
            });
            attributeComboBox.addActionListener(e -> {
                LdapObjectClassAttribute objectClassAttribute = (LdapObjectClassAttribute) attributeComboBox.getSelectedItem();
                valuePanel.removeAll();
                if (objectClassAttribute != null && objectClassAttribute.getName().equalsIgnoreCase(LdapNode.USERPASSWORD_ATTRIBUTE_NAME)) {
                    valuePanel.add(passwordTextField, BorderLayout.CENTER);
                } else {
                    valuePanel.add(valueTextField, BorderLayout.CENTER);
                }
                valuePanel.revalidate();
                valuePanel.repaint();
            });
            new ComboboxSpeedSearch(attributeComboBox);
            separator.setVisible(false);
            removeButton.setIcon(PlatformIcons.DELETE_ICON);
            removeButton.addActionListener(removeButtonActionListener);

            initialized = true;
        }
    }

    public void setRemoveButtonEnabled(boolean enabled) {
        removeButton.setEnabled(enabled);
        removeButton.revalidate();
        removeButton.repaint();
    }

    public void setSeparatorVisible(boolean visible) {
        separator.setVisible(visible);
        content.revalidate();
        content.repaint();
    }

    public void setLabelsVisible(boolean visible) {
        attributeLabel.setVisible(visible);
        valueLabel.setVisible(visible);
        content.revalidate();
        content.repaint();
    }

    public JComponent getComponent() {
        initialize();
        return content;
    }

    public LdapObjectClassAttribute getSelectedAttribute() {
        return (LdapObjectClassAttribute) attributeComboBox.getSelectedItem();
    }

    public String getValue() {
        return valueTextField.getText();
    }

    public byte[] getByteValue() {
        return passwordBytes;
    }

    public ValidationInfo doValidate() {
        if (getSelectedAttribute() == null) {
            return new ValidationInfo("Attribute has to be selected", attributeComboBox);
        }

        if ((getValue() == null || getValue().trim().isEmpty()) && (getByteValue() == null)) {
            return new ValidationInfo("Value cannot be empty", valueTextField);
        }

        return null;
    }

}
