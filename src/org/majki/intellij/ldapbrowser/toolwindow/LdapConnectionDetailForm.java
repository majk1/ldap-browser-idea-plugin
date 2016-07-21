package org.majki.intellij.ldapbrowser.toolwindow;

import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.JBColor;
import org.majki.intellij.ldapbrowser.config.LdapConnectionInfo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Attila Majoros
 */

public class LdapConnectionDetailForm {

    public interface UniqueNameListener {
        boolean isUnique(String name);
    }

    private JTextField nameField;
    private JTextField hostField;
    private JTextField portField;
    private JTextField baseDnField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox authenticateCheckBox;
    private JButton testConnectionButton;
    private JLabel testConnectionResulLabel;
    private JPanel content;

    private UniqueNameListener uniqueNameListener;
    private LdapConnectionInfo info;

    public LdapConnectionDetailForm(UniqueNameListener uniqueNameListener) {
        this.info = null;
        this.uniqueNameListener = uniqueNameListener;
    }

    public LdapConnectionDetailForm() {
        this.info = null;
        this.uniqueNameListener = null;
    }

    public void setUniqueNameListener(UniqueNameListener uniqueNameListener) {
        this.uniqueNameListener = uniqueNameListener;
    }

    public void setLdapConnectionInfo(LdapConnectionInfo info) {
        this.info = info;
        load();
    }

    public void init() {
        ActionListener resetTestConnectionLabelTextActionListener = e -> testConnectionResulLabel.setText("");

        nameField.addActionListener(e -> info.setName(nameField.getText()));
        nameField.addActionListener(resetTestConnectionLabelTextActionListener);
        hostField.addActionListener(e -> info.setHost(hostField.getText()));
        hostField.addActionListener(resetTestConnectionLabelTextActionListener);
        baseDnField.addActionListener(e -> info.setBaseDn(baseDnField.getText()));
        baseDnField.addActionListener(resetTestConnectionLabelTextActionListener);
        usernameField.addActionListener(e -> info.setUsername(usernameField.getText()));
        usernameField.addActionListener(resetTestConnectionLabelTextActionListener);
        passwordField.addActionListener(e -> info.setPassword(String.valueOf(passwordField.getPassword())));
        passwordField.addActionListener(resetTestConnectionLabelTextActionListener);
        portField.addActionListener(e -> {
            String text = portField.getText();
            if (text != null) {
                try {
                    Integer port = Integer.parseInt(text);
                    info.setPort(port);
                } catch (NumberFormatException exception) {
                    // do nothing
                }
            }
        });
        portField.addActionListener(resetTestConnectionLabelTextActionListener);
        authenticateCheckBox.addActionListener(e -> refreshStateByAuthenticateCheckbox());
        authenticateCheckBox.addActionListener(resetTestConnectionLabelTextActionListener);
        refreshStateByAuthenticateCheckbox();

        testConnectionResulLabel.setText("");
        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (info.getHandler().testConnection()) {
                    testConnectionResulLabel.setText("Connection successfull");
                    testConnectionResulLabel.setForeground(JBColor.GREEN);
                } else {
                    testConnectionResulLabel.setText("Connection failed");
                    testConnectionResulLabel.setForeground(JBColor.RED);
                }
            }
        });
    }

    private void refreshStateByAuthenticateCheckbox() {
        info.setAuth(authenticateCheckBox.isSelected());
        usernameField.setEnabled(authenticateCheckBox.isSelected());
        passwordField.setEnabled(authenticateCheckBox.isSelected());
    }

    public JComponent getContent() {
        return content;
    }

    public void save() {
        info.setName(nameField.getText());
        info.setHost(hostField.getText());
        info.setPort(Integer.parseInt(portField.getText()));
        info.setBaseDn(baseDnField.getText());
        info.setAuth(authenticateCheckBox.isSelected());
        info.setUsername(usernameField.getText());
        info.setPassword(String.valueOf(passwordField.getPassword()));
    }

    public void load() {
        nameField.setText(info.getName());
        hostField.setText(info.getHost());
        portField.setText(String.valueOf(info.getPort()));
        baseDnField.setText(info.getBaseDn());
        authenticateCheckBox.getModel().setSelected(info.isAuth());
        usernameField.setText(info.getUsername());
        passwordField.setText(info.getPassword());
        refreshStateByAuthenticateCheckbox();
    }

    public ValidationInfo validate() {
        testConnectionButton.setEnabled(false);

        if (uniqueNameListener != null) {
            if (!uniqueNameListener.isUnique(nameField.getText())) {
                return new ValidationInfo("The name is not unique", nameField);
            }
        }

        String portText = portField.getText();
        if (portText == null) {
            return new ValidationInfo("Port number is mandatory", portField);
        } else {
            try {
                int portNumber = Integer.parseInt(portText);
            } catch (NumberFormatException e) {
                return new ValidationInfo("Port number is invalid: " + portText, portField);
            }
        }

        String host = hostField.getText();
        if (host == null || host.trim().isEmpty()) {
            return new ValidationInfo("Host name is mandatory", hostField);
        }

        String baseDn = baseDnField.getText();
        if (baseDn == null || baseDn.trim().isEmpty()) {
            return new ValidationInfo("Base DN is mandatory", baseDnField);
        }

        if (authenticateCheckBox.isSelected()) {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            if (username == null || username.trim().isEmpty()) {
                return new ValidationInfo("Username is mandatory if authentication is enabled", usernameField);
            }
            if (password == null || password.length == 0) {
                return new ValidationInfo("Password is mandatory if authentication is enabled", passwordField);
            }
        }

        testConnectionButton.setEnabled(true);
        return null;
    }

}
