package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.MutableCollectionComboBoxModel;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapObjectClassAttribute;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Attila Majoros
 */

public class LdapAddAttributeDialog extends DialogWrapper {

    private LdapTreeNode treeNode;
    private JPanel content;
    private JComboBox<LdapObjectClassAttribute> attributeComboBox;
    private JTextField valueTextField;
    private JPanel leftPanel;

    private boolean initialized;

    public LdapAddAttributeDialog(LdapTreeNode treeNode) {
        super(null, true, true);
        this.treeNode = treeNode;
        this.initialized = false;

        setTitle("Add Attribute to Entry");
        getContentPane().setPreferredSize(new Dimension(600, 300));
        init();
        validate();
    }

    private void initialize() {
        if (!initialized) {
            ArrayList<LdapObjectClassAttribute> attributes = new ArrayList<>(treeNode.getLdapNode().getObjectClassAttributes());
            Collections.sort(attributes, (o1, o2) -> o1.getName().compareTo(o2.getName()));

            MutableCollectionComboBoxModel<LdapObjectClassAttribute> attributeComboBoxModel = new MutableCollectionComboBoxModel<>(attributes);
            attributeComboBox.setModel(attributeComboBoxModel);
            //AutoCompleteDecorator.decorate(attributeComboBox);
            new ComboboxSpeedSearch(attributeComboBox);

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
    protected ValidationInfo doValidate() {

        Object selectedItem = attributeComboBox.getSelectedItem();
        if (!(selectedItem instanceof LdapObjectClassAttribute)) {
            return new ValidationInfo("Attribute has to be set", attributeComboBox);
        }

        String text = valueTextField.getText();
        if (text == null || text.trim().isEmpty()) {
            return new ValidationInfo("Value has to be set", valueTextField);
        }

        return null;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "ldapbrowser.LdapAddAttributeDialog";
    }

    public LdapObjectClassAttribute getSelectedLdapObjectClassAttribute() {
        return (LdapObjectClassAttribute) attributeComboBox.getSelectedItem();
    }

    public String getValue() {
        return valueTextField.getText();
    }
}
