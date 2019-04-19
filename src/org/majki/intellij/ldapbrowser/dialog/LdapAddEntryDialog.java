package org.majki.intellij.ldapbrowser.dialog;

import com.google.common.collect.ComparisonChain;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jetbrains.annotations.NotNull;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
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

    public LdapAddEntryDialog(@NotNull Component parent, LdapTreeNode treeNode) {
        super(parent, true);
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
            newLdapNode = LdapNode.createNew(ldapNode.getLdapConnectionInfo(), ldapNode);
        } catch (LdapException e) {
            LdapErrorHandler.handleError(e, "Could not create new Ldap Node for new entry creation");
        }
    }

    private void regenerateRdnComboBoxModel() {
        LdapObjectClassAttribute selected = (LdapObjectClassAttribute) rdnComboBox.getSelectedItem();

        List<LdapObjectClassAttribute> attributes = new ArrayList<>(newLdapNode.getObjectClassAttributes());
        attributes.sort(Comparator.comparing(LdapObjectClassAttribute::getName));
        rdnComboBox.setModel(new MutableCollectionComboBoxModel<>(attributes));

        if (selected == null) {
            setPreferredRDN(attributes);
        }
    }

    private void setPreferredRDN(List<LdapObjectClassAttribute> attributes) {
        LdapObjectClassAttribute autoRdn = null;
        for (LdapObjectClassAttribute attribute : attributes) {
            if (attribute.getName().equalsIgnoreCase("uid")) {
                autoRdn = attribute;
            } else if (autoRdn == null && attribute.getName().equalsIgnoreCase("ou")) {
                autoRdn = attribute;
            }
        }
        if (autoRdn != null) {
            rdnComboBox.setSelectedItem(autoRdn);
        }
    }

    @SuppressWarnings("unchecked")
    private void addSelectedObjectClass() {
        List<LdapObjectClass> selectedObjectClasses = sourceClassList.getSelectedValuesList();
        for (LdapObjectClass selectedObjectClass : selectedObjectClasses) {
            Set<LdapObjectClass> removedObjectClasses = newLdapNode.addObjectClass(selectedObjectClass);
            for (LdapObjectClass removedObjectClass : removedObjectClasses) {
                ((CollectionListModel<LdapObjectClass>) sourceClassList.getModel()).remove(removedObjectClass);
            }
            attrValueTableWrapper.getModel().refresh();
            regenerateRdnComboBoxModel();
        }
    }

    @SuppressWarnings("unchecked")
    private void removeSelectedObjectClass() {
        int selectedRow = attrValueTable.getSelectedRow();
        if (selectedRow != -1) {
            LdapAttributeTableModel.Item selectedItem = attrValueTableWrapper.getModel().getItems().get(selectedRow);
            if (LdapNode.OBJECTCLASS_ATTRIBUTE_NAME.equalsIgnoreCase(selectedItem.getAttribute().name())) {
                String objectClassName = selectedItem.getValue().asString();
                LdapObjectClass selectedObjectClass = treeNode.getLdapNode().getTopObjectClass().getByName(objectClassName);
                Set<LdapObjectClass> removedObjectClasses = newLdapNode.removeObjectClass(selectedObjectClass);
                attrValueTableWrapper.getModel().refresh();

                List<LdapObjectClass> objectClasses = new ArrayList<>();
                if (objectClassName.equalsIgnoreCase("top")) {
                    objectClasses.addAll(treeNode.getLdapNode().getTopObjectClass().getAllObjectClasses());
                } else {
                    objectClasses.addAll(((CollectionListModel<LdapObjectClass>) sourceClassList.getModel()).getItems());
                    objectClasses.addAll(removedObjectClasses);
                }
                objectClasses.sort(Comparator.comparing(LdapObjectClass::getName));

                sourceClassList.setModel(new CollectionListModel<>(objectClasses));
                attrValueTableWrapper.getModel().refresh();

                regenerateRdnComboBoxModel();
            } else {
                Messages.showInfoMessage(content, "Required attribute cannot be removed", "Remove Attribute");
            }
        }
    }

    private void initialize() {
        if (!initialized) {
            parentDnTextField.setText(treeNode.getLdapNode().getDn());
            regenerateRdnComboBoxModel();
            new ComboboxSpeedSearch(rdnComboBox);

            ArrayList<LdapObjectClass> objectClasses = new ArrayList<>(treeNode.getLdapNode().getTopObjectClass().getAllObjectClasses());
            objectClasses.sort((o1, o2) -> ComparisonChain.start().
                    compare(o1.getSchemaName(), o2.getSchemaName()).
                    compare(o1.getName(), o2.getName()).
                    result());

            rdnTextField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        attrValueTable.requestFocusInWindow();
                    }
                }
            });

            sourceClassList = new JBList(new CollectionListModel<>(objectClasses));
            sourceClassList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            sourceClassList.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        addSelectedObjectClass();
                        rdnTextField.requestFocusInWindow();
                    }
                }
            });
            sourceClassList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        addSelectedObjectClass();
                        rdnTextField.requestFocusInWindow();
                    }
                }
            });
            sourceClassList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                LdapObjectClass objectClass = (LdapObjectClass) value;
                JBLabel label = new JBLabel("<html><span color=\"gray\">["+objectClass.getSchemaName()+"]</span> " +objectClass.getName()+ "</html>");
                if (isSelected) {
                    label.setOpaque(true);
                    if (cellHasFocus) {
                        label.setBackground(UIUtil.getListSelectionBackground());
                    } else {
                        label.setBackground(UIUtil.getListUnfocusedSelectionBackground());
                    }
                    label.setForeground(UIUtil.getListSelectionForeground());
                }
                return label;
            });

            attrValueTable = new JBTable();
            attrValueTableWrapper = new LdapAttributeTableWrapper(attrValueTable, newLdapNode, false);

            new ListSpeedSearch(sourceClassList);

            ToolbarDecorator sourceToolbar = ToolbarDecorator.createDecorator(sourceClassList);
            sourceToolbar.disableRemoveAction();
            sourceToolbar.disableUpDownActions();
            sourceToolbar.setAddAction(anActionButton -> addSelectedObjectClass());

            ToolbarDecorator attrValueToolbar = ToolbarDecorator.createDecorator(attrValueTable);
            attrValueToolbar.disableAddAction();
            attrValueToolbar.disableUpDownActions();
            attrValueToolbar.setRemoveAction(anActionButton -> removeSelectedObjectClass());

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
        return sourceClassList;
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
