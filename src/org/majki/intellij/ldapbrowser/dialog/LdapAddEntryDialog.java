package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapAttribute;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;
import org.majki.intellij.ldapbrowser.ldap.LdapObjectClass;
import org.majki.intellij.ldapbrowser.ldap.LdapObjectClassAttribute;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapAttributeTableModel;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapAttributeTableWrapper;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapErrorHandler;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Attila Majoros
 */

public class LdapAddEntryDialog extends DialogWrapper {

    private LdapNode newLdapNode;
    private LdapTreeNode treeNode;
    private boolean initialized;

    private JPanel content;
    private JTextField parentDnTextField;
    private JTextField rdnTextField;
    private JComboBox<LdapObjectClassAttribute> rdnComboBox;
    private JPanel ocaContent;
    private JBList sourceClassList;
    private JBTable attrValueTable;
    private LdapAttributeTableWrapper attrValueTableWrapper;

    public LdapAddEntryDialog(LdapTreeNode treeNode) {
        super(null);
        this.treeNode = treeNode;
        this.initialized = false;

        setTitle("Add New Entry");
        createNewLdapNode();
        init();
        validate();
    }

    private void createNewLdapNode() {
        LdapNode ldapNode = treeNode.getLdapNode();
        try {
            newLdapNode = LdapNode.createNew(ldapNode.getConnection(), ldapNode);
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not create new Ldap Node for new entry creation");
        }
    }

    private void regenerateRdnComboBoxModel() {
        List<LdapObjectClassAttribute> attributes = new ArrayList<>(newLdapNode.getObjectClassAttributes());
        Collections.sort(attributes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        rdnComboBox.setModel(new MutableCollectionComboBoxModel<>(attributes));
    }

    private void initialize() {
        if (!initialized) {
            parentDnTextField.setText(treeNode.getLdapNode().getDn());
            regenerateRdnComboBoxModel();
            new ComboboxSpeedSearch(rdnComboBox);

            ArrayList<LdapObjectClass> objectClasses = new ArrayList<>(treeNode.getLdapNode().getTopObjectClass().getAllObjectClasses());
            Collections.sort(objectClasses, (o1, o2) -> o1.getName().compareTo(o2.getName()));

            sourceClassList = new JBList(new CollectionListModel<>(objectClasses));
            sourceClassList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            attrValueTable = new JBTable();
            attrValueTableWrapper = new LdapAttributeTableWrapper(attrValueTable, newLdapNode, false);

            new ListSpeedSearch(sourceClassList);

            ToolbarDecorator sourceToolbar = ToolbarDecorator.createDecorator(sourceClassList);
            sourceToolbar.disableRemoveAction();
            sourceToolbar.disableUpDownActions();
            sourceToolbar.setAddAction(new AnActionButtonRunnable() {
                @Override
                public void run(AnActionButton anActionButton) {
                    java.util.List<LdapObjectClass> selectedObjectClasses = sourceClassList.getSelectedValuesList();
                    for (LdapObjectClass selectedObjectClass : selectedObjectClasses) {
                        newLdapNode.addObjectClass(selectedObjectClass);
                        ((CollectionListModel<LdapObjectClass>) sourceClassList.getModel()).remove(selectedObjectClass);
                        attrValueTableWrapper.getModel().refresh();
                        regenerateRdnComboBoxModel();
                    }
                }
            });

            ToolbarDecorator attrValueToolbar = ToolbarDecorator.createDecorator(attrValueTable);
            attrValueToolbar.disableUpDownActions();
            attrValueToolbar.setAddAction(new AnActionButtonRunnable() {
                @Override
                public void run(AnActionButton anActionButton) {
                    LdapAddAttributeDialog addAttributeDialog = new LdapAddAttributeDialog(newLdapNode);
                    if (addAttributeDialog.showAndGet()) {
                        LdapObjectClassAttribute selectedLdapObjectClassAttribute = addAttributeDialog.getSelectedLdapObjectClassAttribute();
                        String value = addAttributeDialog.getValue();

                        // TODO: add attribute
                    }
                }
            });
            attrValueToolbar.setRemoveAction(new AnActionButtonRunnable() {
                @Override
                public void run(AnActionButton anActionButton) {

                    // TODO: implement

                    int selectedRow = attrValueTable.getSelectedRow();
                    if (selectedRow != -1) {
                        LdapAttributeTableModel.Item selectedItem = attrValueTableWrapper.getModel().getItems().get(selectedRow);

                    }
                }
            });

            JBSplitter splitter = new JBSplitter(false, 0.4f);
            splitter.setFirstComponent(sourceToolbar.createPanel());
            splitter.setSecondComponent(attrValueToolbar.createPanel());

            ocaContent.add(splitter, BorderLayout.CENTER);
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
        return rdnComboBox;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String parentDn = parentDnTextField.getText();
        if (parentDn == null || parentDn.trim().isEmpty()) {
            return new ValidationInfo("Parent DN is invalid", rdnTextField);
        }

        String rdn = rdnTextField.getText();
        if (rdn == null || rdn.trim().isEmpty()) {
            return new ValidationInfo("RDN is mandatory", rdnTextField);
        }

        Object rdnAttribute = rdnComboBox.getSelectedItem();
        if (rdnAttribute == null) {
            return new ValidationInfo("RDN attribute must be selected", rdnComboBox);
        }

        for (LdapAttribute ldapAttribute : newLdapNode.getAttributes()) {
            LdapAttribute.Value value = ldapAttribute.firstValue();
            if (value == null || value.asString() == null || value.asString().trim().isEmpty()) {
                return new ValidationInfo("Entry attributes must be filled", attrValueTable);
            }
        }

        return super.doValidate();
    }

    public String getDn() {
        return parentDnTextField.getText();
    }

    public LdapObjectClassAttribute getRdn() {
        return (LdapObjectClassAttribute) rdnComboBox.getSelectedItem();
    }

    public String getRdnValue() {
        return rdnTextField.getText();
    }

    public LdapNode getNewLdapNode() {
        return newLdapNode;
    }

}
