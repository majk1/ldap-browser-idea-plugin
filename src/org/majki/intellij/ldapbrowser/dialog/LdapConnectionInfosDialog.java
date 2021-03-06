package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.DimensionService;
import com.intellij.ui.*;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionInfo;
import org.majki.intellij.ldapbrowser.ldap.LdapUtil;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapServerTreeNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LdapConnectionInfosDialog extends DialogWrapper {

    private CollectionListModel<LdapConnectionInfo> connectionListModel;
    private boolean initialized;
    private LdapConnectionInfo selectedConnectionInfo;
    private JBSplitter splitter;
    private JBList<LdapConnectionInfo> connectionList;
    private JBPanel detailPanel;
    private JPanel detailContent;
    private JTextField nameField;
    private JTextField hostnameField;
    private JTextField portField;
    private JTextField baseDnField;
    private JButton testConnectionButton;
    private JTextField bindDnField;
    private JCheckBox authenticateCheckBox;
    private JPasswordField passwordField;
    private JLabel connectionTestResultLabel;
    private SeparatorWithText authenticationSeparator;
    private JLabel bindDnLabel;
    private JLabel passwordLabel;
    private JCheckBox sslCheckBox;

    public LdapConnectionInfosDialog(@NotNull Component parent, Project project, List<LdapConnectionInfo> connectionInfos) {
        super(parent, true);
        String selectedServerName = getSelectedServerNodeConnectionName(parent);

        this.connectionListModel = new CollectionListModel<>(connectionInfos.stream().
            map(LdapConnectionInfo::getCopy).
            collect(Collectors.toList()));
        this.initialized = false;
        this.selectedConnectionInfo = null;

        setTitle("LDAP Connections");
        init();
        validate();
        initialSize(project);
        selectServerByConnectionName(selectedServerName);
    }

    private String getSelectedServerNodeConnectionName(Component parent) {
        if (parent instanceof Tree) {
            LdapServerTreeNode[] selectedNodes = ((Tree) parent).getSelectedNodes(LdapServerTreeNode.class, null);
            if (selectedNodes.length > 0) {
                return selectedNodes[0].getConnectionInfo().getName();
            }
        }
        return null;
    }

    private void selectServerByConnectionName(String connectionName) {
        if (connectionName != null) {
            connectionListModel.toList().stream()
                .filter(info -> connectionName.equals(info.getName()))
                .map(connectionInfo -> connectionListModel.getElementIndex(connectionInfo))
                .findFirst()
                .ifPresent(this::selectIndex);
        }
    }

    private void selectIndex(Integer index) {
        connectionList.setSelectedIndex(index);
    }

    private void initialSize(Project project) {
        String dimensionServiceKey = getDimensionServiceKey();
        if (dimensionServiceKey != null && project != null) {
            Dimension size = DimensionService.getInstance().getSize(dimensionServiceKey);
            if (size == null) {
                size = new Dimension(750, 500);
                DimensionService.getInstance().setSize(dimensionServiceKey, size, project);
            }
        }
    }

    private void addTextChangedDocumentAdapter(JTextField textField, TextChangedFunction textChangedFunction) {
        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {
                if (selectedConnectionInfo != null) {
                    connectionTestResultLabel.setText("");
                    if (textField instanceof JPasswordField) {
                        textChangedFunction.changed(new String(((JPasswordField) textField).getPassword()));
                    } else {
                        textChangedFunction.changed(textField.getText());
                    }
                }
            }
        });
    }

    private void addTextChangedToIntDocumentAdapter(JTextField textField, TextChangedToIntFunction textChangedToIntFunction) {
        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {
                if (selectedConnectionInfo != null) {
                    connectionTestResultLabel.setText("");
                    try {
                        Integer number = Integer.parseInt(textField.getText());
                        textChangedToIntFunction.changed(number);
                    } catch (NumberFormatException e) {
                        textChangedToIntFunction.changed(-1);
                    }
                }
            }
        });
    }

    private void initialize() {
        if (!initialized) {

            final Color normalFieldForegroundColor = nameField.getForeground();

            addTextChangedDocumentAdapter(hostnameField, text -> selectedConnectionInfo.setHost(text));
            addTextChangedDocumentAdapter(baseDnField, text -> selectedConnectionInfo.setBaseDn(text));
            addTextChangedDocumentAdapter(bindDnField, text -> selectedConnectionInfo.setUsername(text));
            addTextChangedDocumentAdapter(passwordField, text -> selectedConnectionInfo.setPassword(text));
            addTextChangedToIntDocumentAdapter(portField, number -> {
                selectedConnectionInfo.setPort(number);
                if (number <= 0) {
                    portField.setForeground(JBColor.RED);
                } else {
                    portField.setForeground(normalFieldForegroundColor);
                }
            });
            addTextChangedDocumentAdapter(nameField, text -> {
                selectedConnectionInfo.setName(text);
                connectionListModel.contentsChanged(selectedConnectionInfo);
                if (isConnectionNameAlreadyExists(text)) {
                    nameField.setForeground(JBColor.RED);
                } else {
                    nameField.setForeground(normalFieldForegroundColor);
                }
            });

            authenticationSeparator.setCaption("Authentication");
            authenticationSeparator.setCaptionCentered(false);

            connectionTestResultLabel.setText("");
            authenticateCheckBox.addActionListener(e -> updateAuthenticationPanelByCheckboxValue());
            sslCheckBox.addActionListener(e -> updatePortAccordingToSSL());

            connectionList = new JBList<>(connectionListModel);
            connectionList.setCellRenderer(new ColoredListCellRenderer<LdapConnectionInfo>() {
                @Override
                protected void customizeCellRenderer(@NotNull JList<? extends LdapConnectionInfo> list, LdapConnectionInfo value, int index, boolean selected, boolean hasFocus) {
                    append(value.isSsl() ? "\uD83D\uDD12" : "\uD83D\uDD13");
                    append(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                    append("  (" + value.asUrl() + ")", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
                }
            });
            connectionList.addListSelectionListener(e -> handleListSelection());
            ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(connectionList);
            toolbarDecorator.setAddAction(anActionButton -> {
                LdapConnectionInfo connectionInfo = new LdapConnectionInfo();
                connectionListModel.add(connectionInfo);
                selectIndex(connectionListModel.getElementIndex(connectionInfo));
            });
            JPanel connectionListPanel = toolbarDecorator.createPanel();

            detailPanel = new JBPanel(new BorderLayout());
            setupDetailPanel();

            splitter = new JBSplitter("ldapbrowser.LdapConnectionInfosDialogSplitterProportion", 0.4f);
            splitter.setFirstComponent(connectionListPanel);
            splitter.setSecondComponent(detailPanel);

            testConnectionButton.addActionListener(e -> testConnection());

            initialized = true;
        }
    }

    private boolean isConnectionNameAlreadyExists(String connectionName) {
        Map<String, Long> nameCountMap = connectionListModel.getItems().stream().collect(Collectors.groupingBy(LdapConnectionInfo::getName, Collectors.counting()));
        Long nameCount = nameCountMap.get(connectionName);
        return nameCount == null || nameCount > 1;
    }

    private void testConnection() {
        testConnectionButton.setIcon(AllIcons.Process.Step_passive);
        testConnectionButton.setText("Testing...");
        testConnectionButton.setEnabled(false);
        SwingUtilities.invokeLater(() -> {
            if (selectedConnectionInfo.testConnection()) {
                connectionTestResultLabel.setForeground(JBColor.GREEN);
                connectionTestResultLabel.setText("Connection successful");
            } else {
                connectionTestResultLabel.setForeground(JBColor.RED);
                connectionTestResultLabel.setText("Connection failed");
            }
            testConnectionButton.setEnabled(true);
            testConnectionButton.setText("Test connection");
            testConnectionButton.setIcon(null);
        });
    }

    private void updateAuthenticationPanelByCheckboxValue() {
        boolean selected = authenticateCheckBox.isSelected();
        selectedConnectionInfo.setAuth(selected);
        bindDnField.setEnabled(selected);
        bindDnLabel.setEnabled(selected);
        passwordField.setEnabled(selected);
        passwordLabel.setEnabled(selected);
    }

    private void updatePortAccordingToSSL() {
        int port = Integer.parseInt(portField.getText());
        boolean sslSelected = sslCheckBox.isSelected();
        if (sslSelected && port == LdapConnectionConfig.DEFAULT_LDAP_PORT) {
            portField.setText(String.valueOf(LdapConnectionConfig.DEFAULT_LDAPS_PORT));
        } else if (!sslSelected && port == LdapConnectionConfig.DEFAULT_LDAPS_PORT) {
            portField.setText(String.valueOf(LdapConnectionConfig.DEFAULT_LDAP_PORT));
        }
        selectedConnectionInfo.setSsl(sslSelected);
    }

    private void setupDetailPanel() {
        if (selectedConnectionInfo != null) {

            detailPanel.setBorder(new IdeaTitledBorder("Connection Details", 2, JBUI.emptyInsets()));

            nameField.setText(selectedConnectionInfo.getName());
            hostnameField.setText(selectedConnectionInfo.getHost());
            portField.setText(String.valueOf(selectedConnectionInfo.getPort()));
            baseDnField.setText(selectedConnectionInfo.getBaseDn());
            sslCheckBox.setSelected(selectedConnectionInfo.isSsl());
            authenticateCheckBox.setSelected(selectedConnectionInfo.isAuth());
            bindDnField.setText(selectedConnectionInfo.getUsername());
            passwordField.setText(selectedConnectionInfo.getPassword());
            updateAuthenticationPanelByCheckboxValue();

            detailPanel.add(detailContent, BorderLayout.CENTER);

        } else {
            detailPanel.setBorder(new EmptyBorder(JBUI.emptyInsets()));

            JBLabel noConnectionSelectedLabel = new JBLabel("<html><b>No connection selected</b></html>");
            noConnectionSelectedLabel.setForeground(JBColor.DARK_GRAY);
            noConnectionSelectedLabel.setHorizontalAlignment(JLabel.CENTER);
            detailPanel.add(noConnectionSelectedLabel, BorderLayout.CENTER);

        }
    }

    private void handleListSelection() {
        LdapConnectionInfo newSelection = connectionList.getSelectedValue();
        if (newSelection != selectedConnectionInfo) {
            selectedConnectionInfo = newSelection;
            detailPanel.removeAll();
            setupDetailPanel();
            detailPanel.revalidate();
            detailPanel.repaint();
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        initialize();
        return splitter;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return connectionList;
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        JBPanel panel = new JBPanel(new BorderLayout());
        panel.add(new JSeparator(), BorderLayout.PAGE_START);
        JComponent southPanel = super.createSouthPanel();
        if (southPanel != null) {
            panel.add(southPanel, BorderLayout.CENTER);
        }
        return panel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "ldapbrowser.LdapConnectionInfosDialog";
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo validationInfo;
        for (LdapConnectionInfo connectionInfo : connectionListModel.getItems()) {
            validationInfo = validateName(connectionInfo);
            if (validationInfo != null) {
                return validationInfo;
            }

            validationInfo = validateHost(connectionInfo);
            if (validationInfo != null) {
                return validationInfo;
            }

            validationInfo = validatePort(connectionInfo);
            if (validationInfo != null) {
                return validationInfo;
            }

            validationInfo = validateAuthentication(connectionInfo);
            if (validationInfo != null) {
                return validationInfo;
            }
        }
        return null;
    }

    private ValidationInfo validateName(LdapConnectionInfo connectionInfo) {
        ValidationInfo validationInfo = null;
        String name = connectionInfo.getName();
        if (name == null || name.trim().isEmpty()) {
            validationInfo = new ValidationInfo("Connection name must be defined", nameField);
        } else if (isConnectionNameAlreadyExists(name)) {
            validationInfo = new ValidationInfo("Connection name must be unique", nameField);
        }

        if (validationInfo != null) {
            selectConnectionAndFocusField(connectionInfo, nameField);
        }

        return validationInfo;
    }

    private ValidationInfo validateHost(LdapConnectionInfo connectionInfo) {
        ValidationInfo validationInfo = null;
        String host = connectionInfo.getHost();
        if (host == null || host.trim().isEmpty()) {
            validationInfo = new ValidationInfo("Hostname must be defined", hostnameField);
            selectConnectionAndFocusField(connectionInfo, hostnameField);
        }
        return validationInfo;
    }

    private ValidationInfo validatePort(LdapConnectionInfo connectionInfo) {
        ValidationInfo validationInfo = null;
        if (connectionInfo.getPort() <= 0) {
            validationInfo = new ValidationInfo("Invalid connection port", portField);
            selectConnectionAndFocusField(connectionInfo, portField);
        }
        return validationInfo;
    }

    private ValidationInfo validateAuthentication(LdapConnectionInfo connectionInfo) {
        ValidationInfo validationInfo = null;
        if (connectionInfo.isAuth()) {
            String bindDn = connectionInfo.getUsername();
            String password = connectionInfo.getPassword();

            if (bindDn == null || bindDn.trim().isEmpty()) {
                validationInfo = new ValidationInfo("BindDN must be defined", bindDnField);
                selectConnectionAndFocusField(connectionInfo, bindDnField);
            } else if (!LdapUtil.isValidDnFormula(bindDn)) {
                validationInfo = new ValidationInfo("BindDN is not valid", bindDnField);
                selectConnectionAndFocusField(connectionInfo, bindDnField);
            } else if (password == null || password.isEmpty()) {
                validationInfo = new ValidationInfo("Password must be defined", passwordField);
                selectConnectionAndFocusField(connectionInfo, passwordField);
            }
        }
        return validationInfo;
    }

    private void selectConnectionAndFocusField(LdapConnectionInfo connectionInfo, JComponent field) {
        selectIndex(connectionListModel.getElementIndex(connectionInfo));
        field.requestFocusInWindow();
    }

    public List<LdapConnectionInfo> getConnectionInfos() {
        return new ArrayList<>(connectionListModel.getItems());
    }

    @FunctionalInterface
    private interface TextChangedFunction {
        void changed(String text);
    }

    @FunctionalInterface
    private interface TextChangedToIntFunction {
        void changed(Integer number);
    }
}
