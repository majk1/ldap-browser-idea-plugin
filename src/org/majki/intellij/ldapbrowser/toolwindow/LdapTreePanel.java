package org.majki.intellij.ldapbrowser.toolwindow;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.majki.intellij.ldapbrowser.ldap.LdapConnectionsService;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapIconProviderTreeNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapRootTreeNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapServerTreeNode;
import org.majki.intellij.ldapbrowser.ldap.ui.LdapTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Attila Majoros
 */

public class LdapTreePanel extends SimpleToolWindowPanel implements ApplicationComponent {

    public static final String COMPONENT_NAME = "ldapbrowser.treePanel";

    private Tree tree;
    private TreeNode root;
    private Project project;

    public LdapTreePanel() {
        super(true, true);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private void openTreePopupMenu(LdapServerTreeNode ldapServerTreeNode, int x, int y) {
        JBPopupMenu menu = new JBPopupMenu(ldapServerTreeNode.toString());
        if (ldapServerTreeNode.getConnectionInfo().isOpened()) {
            JMenuItem disconnect = menu.add("Disconnect");
            disconnect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ldapServerTreeNode.getConnectionInfo().disconnect();
                    tree.repaint();
                }
            });
        } else {
            JMenuItem connect = menu.add("Connect");
            connect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ldapServerTreeNode.getConnectionInfo().connect();
                    tree.repaint();
                }
            });
        }

        menu.show(tree, x, y);
    }

    private Object getTreeComponent(int x, int y) {
        int closestRowForLocation = tree.getClosestRowForLocation(x, y);
        if (closestRowForLocation != -1) {
            TreePath pathForRow = tree.getPathForRow(closestRowForLocation);
            return pathForRow.getLastPathComponent();
        }
        return null;
    }

    @Override
    public void initComponent() {
        addToolbar();

        root = generateTree();
        tree = new Tree(root);
        tree.getEmptyText().setText("No connections");
        tree.setRootVisible(false);
        //tree.setShowsRootHandles(false);

        tree.setCellRenderer(new TreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JBLabel label = new JBLabel(value.toString());
                if (value instanceof LdapIconProviderTreeNode) {
                    label.setIcon(((LdapIconProviderTreeNode) value).getIcon());
                }
                return label;
            }
        });

        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    LdapTreeNode[] selectedNodes = tree.getSelectedNodes(LdapTreeNode.class, null);
                    if (selectedNodes.length > 0) {
                        FileEditorManager.getInstance(project).openFile(selectedNodes[0].getFile(), true, true);
                    }
                }
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int closestRowForLocation = tree.getClosestRowForLocation(e.getX(), e.getY());
                    if (closestRowForLocation != -1) {
                        TreePath pathForRow = tree.getPathForRow(closestRowForLocation);
                        Object lastPathComponent = pathForRow.getLastPathComponent();
                        if (lastPathComponent instanceof LdapServerTreeNode) {
                            LdapServerTreeNode ldapServerTreeNode = (LdapServerTreeNode) lastPathComponent;
                            openTreePopupMenu(ldapServerTreeNode, e.getX(), e.getY());
                        }
                    }
                } else if (e.getClickCount() > 1) {
                    Object comp = getTreeComponent(e.getX(), e.getY());
                    if (comp instanceof LdapTreeNode) {
                        FileEditorManager.getInstance(project).openFile(((LdapTreeNode) comp).getFile(), true, true);
                    }
                }
            }
        });


        new TreeSpeedSearch(tree);

        JBScrollPane treeScroll = new JBScrollPane(tree);
        super.setContent(treeScroll);
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    private TreeNode generateTree() {
        LdapConnectionsService ldapConnectionsService = ApplicationManager.getApplication().getComponent(LdapConnectionsService.class);
        return new LdapRootTreeNode(ldapConnectionsService.getLdapConnectionInfos());
    }

    private void addToolbar() {

        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("ldapbrowser.actionGroup");
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("qwe", actionGroup, false);
        actionToolbar.setTargetComponent(this);
        actionToolbar.setOrientation(JToolBar.HORIZONTAL);
        Box toolbarBox = Box.createHorizontalBox();
        toolbarBox.add(actionToolbar.getComponent());

        super.setToolbar(toolbarBox);

        actionToolbar.getComponent().setVisible(true);

    }

    public void reloadTree() {
        tree.setModel(new DefaultTreeModel(generateTree()));
    }

}

