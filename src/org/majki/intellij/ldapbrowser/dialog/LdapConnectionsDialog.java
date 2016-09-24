package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.*;
import com.intellij.ui.components.*;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionInfo;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * @author Attila Majoros
 */

public class LdapConnectionsDialog extends DialogWrapper {

    private class InfoWrapper {
        private LdapConnectionInfo info;
        private LdapConnectionDetailForm form;

        public InfoWrapper(LdapConnectionInfo info, LdapConnectionDetailForm.UniqueNameListener uniqueNameListener) {
            this.info = info.getCopy();
            this.form = new LdapConnectionDetailForm(uniqueNameListener);
            this.form.setLdapConnectionInfo(this.info);
            this.form.init();
        }

        public LdapConnectionInfo getInfo() {
            return info;
        }

        public LdapConnectionDetailForm getForm() {
            return form;
        }

        @Override
        public String toString() {
            return this.info.getName();
        }
    }

    private CollectionListModel<InfoWrapper> connectionListModel;
    private LdapConnectionDetailForm.UniqueNameListener uniqueNameListener;
    private JBPanelWithEmptyText detailPanel;

    public LdapConnectionsDialog(java.util.List<LdapConnectionInfo> ldapConnectionInfos) {
        super(null, true, true);
        uniqueNameListener = name -> {
            int counter = 0;
            for (InfoWrapper infoWrapper : connectionListModel.getItems()) {
                if (infoWrapper.getInfo().getName().equals(name)) {
                    counter++;
                }
                if (counter == 2) {
                    return false;
                }
            }
            return true;
        };
        connectionListModel = createInfoListModel(ldapConnectionInfos, uniqueNameListener);
        detailPanel = new JBPanelWithEmptyText(new BorderLayout());
        detailPanel.getEmptyText().setText("No connection selected");

        setTitle("LDAP Connections");
        getContentPane().setPreferredSize(new Dimension(800, 700));
        init();
        validate();
    }

    private CollectionListModel<InfoWrapper> createInfoListModel(java.util.List<LdapConnectionInfo> ldapConnectionInfos, LdapConnectionDetailForm.UniqueNameListener uniqueNameListener) {
        final CollectionListModel<InfoWrapper> listModel = new CollectionListModel<>();
        for (LdapConnectionInfo ldapConnectionInfo : ldapConnectionInfos) {
            InfoWrapper infoWrapper = new InfoWrapper(ldapConnectionInfo, uniqueNameListener);
            listModel.add(infoWrapper);
        }
        return listModel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "ldapbrowser.LdapConnectionsDialog";
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());

        JBList connectionList = new JBList(connectionListModel);

        JBTabbedPane tabPanel = new JBTabbedPane(JBTabbedPane.TOP);
        tabPanel.add("Connection details", detailPanel);

        connectionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JBList list = (JBList) e.getSource();
                int selectedIndex = list.getSelectedIndex();
                if (selectedIndex != -1) {
                    InfoWrapper infoWrapper = (InfoWrapper) list.getModel().getElementAt(selectedIndex);
                    detailPanel.removeAll();
                    detailPanel.add(infoWrapper.getForm().getContent());
                } else {
                    detailPanel.removeAll();
                }
                detailPanel.revalidate();
                detailPanel.repaint();
            }
        });

        ListSpeedSearch listSpeedSearch = new ListSpeedSearch(connectionList);

        ToolbarDecorator connectionListDecorator = ToolbarDecorator.createDecorator(connectionList);
        connectionListDecorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                LdapConnectionInfo ldapConnectionInfo = new LdapConnectionInfo();
                InfoWrapper infoWrapper = new InfoWrapper(ldapConnectionInfo, uniqueNameListener);
                connectionListModel.add(infoWrapper);
                connectionList.setSelectedIndex(connectionListModel.getElementIndex(infoWrapper));
            }
        });

        JBPanel listLabelPanel = new JBPanel(new BorderLayout());
        listLabelPanel.add(new JBLabel("Connections"), BorderLayout.PAGE_START);
        listLabelPanel.add(new JSeparator());

        JBPanel listPanel = new JBPanel(new BorderLayout());
        listPanel.add(listLabelPanel, BorderLayout.PAGE_START);
        listPanel.add(connectionListDecorator.createPanel());

        JBSplitter splitter = new JBSplitter(false, 0.4f);
        splitter.setFirstComponent(listPanel);
        splitter.setSecondComponent(tabPanel);

        centerPanel.add(splitter);
        centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.PAGE_END);

        return centerPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        for (InfoWrapper infoWrapper : connectionListModel.getItems()) {
            ValidationInfo validateInfo = infoWrapper.getForm().validate();
            if (validateInfo != null) {
                return validateInfo;
            }
            infoWrapper.getForm().save();
        }
        return null;
    }

    public java.util.List<LdapConnectionInfo> getConnectionInfos() {
        return connectionListModel.getItems().stream().map(InfoWrapper::getInfo).collect(Collectors.toList());
    }

}
