package org.majki.intellij.ldapbrowser.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.PlatformIcons;

/**
 * @author Attila Majoros
 */
public class AddAttribute extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here

    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(PlatformIcons.ADD_ICON);
    }
}
