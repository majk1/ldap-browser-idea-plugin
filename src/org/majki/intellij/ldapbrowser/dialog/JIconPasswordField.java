package org.majki.intellij.ldapbrowser.dialog;

import com.intellij.ui.components.JBPasswordField;

import javax.swing.*;
import java.awt.*;

public class JIconPasswordField extends JBPasswordField {

    private Icon icon;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (icon != null) {
            int x = getWidth() - (icon.getIconWidth() + 8);
            int y = (getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g, x, y);
        }
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
        invalidate();
        repaint();
    }
}
