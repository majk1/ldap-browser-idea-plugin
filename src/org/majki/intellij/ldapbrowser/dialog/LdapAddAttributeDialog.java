package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.majki.intellij.ldapbrowser.ldap.LdapNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Attila Majoros
 */

public class LdapAddAttributeDialog extends DialogWrapper {

    private LdapNode ldapNode;
    private boolean initialized;

    private JBPanel content;
    private JBPanel scrollPanel;
    private List<LdapAttributeValuePanel> attributeValuePanels;

    public LdapAddAttributeDialog(@NotNull Component parent, LdapNode ldapNode) {
        super(parent, true);
        this.ldapNode = ldapNode;
        this.initialized = false;
        this.attributeValuePanels = new ArrayList<>();

        setTitle("Add Attributes to Entry");
        getContentPane().setPreferredSize(new Dimension(600, 300));
        init();
        validate();
    }

    private void initialize() {
        if (!initialized) {
            scrollPanel = new JBPanel(new VerticalLayout(0));

            JBScrollPane scrollPane = new JBScrollPane(scrollPanel);

            content = new JBPanel(new BorderLayout());
            content.add(scrollPane, BorderLayout.CENTER);

            addAttributeValueComponent();

            initialized = true;
        }
    }

    private void revalidateAttribtueValueComponents() {
        boolean first = true;
        for (LdapAttributeValuePanel attributeValuePanel : attributeValuePanels) {
            attributeValuePanel.setLabelsVisible(first);
            attributeValuePanel.setRemoveButtonEnabled(attributeValuePanels.size() > 1);
            attributeValuePanel.setSeparatorVisible(false);
            first = false;
        }
        scrollPanel.revalidate();
        scrollPanel.repaint();
    }

    private void addAttributeValueComponent() {
        LdapAttributeValuePanel attributeValuePanel = new LdapAttributeValuePanel(ldapNode);
        attributeValuePanel.setRemoveButtonActionListener(e -> {
            scrollPanel.remove(attributeValuePanel.getComponent());
            attributeValuePanels.remove(attributeValuePanel);
            LdapAddAttributeDialog.this.revalidateAttribtueValueComponents();
        });
        attributeValuePanels.add(attributeValuePanel);
        scrollPanel.add(attributeValuePanel.getComponent());

        revalidateAttribtueValueComponents();
    }

    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();
    }

    @NotNull
    @Override
    protected Action[] createLeftSideActions() {
        Action addAttributeValuePanelAction = new DialogWrapperAction("Add attribute") {
            @Override
            protected void doAction(ActionEvent actionEvent) {
                addAttributeValueComponent();
            }
        };
        return new Action[] {addAttributeValuePanelAction};
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
        return attributeValuePanels.get(0).getComponent();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        for (LdapAttributeValuePanel attributeValuePanel : attributeValuePanels) {
            ValidationInfo validationInfo = attributeValuePanel.doValidate();
            if (validationInfo != null) {
                return validationInfo;
            }
        }
        return null;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "ldapbrowser.LdapAddAttributeDialog";
    }

    public List<LdapAttributeValuePanel> getAttributeValuePanels() {
        return attributeValuePanels;
    }
}
