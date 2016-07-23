package org.majki.intellij.ldapbrowser.ldap.ui;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.Messages;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * @author Attila Majoros
 */

public final class LdapErrorHandler {

    private static final String NOTIFICATION_GROUP = "LDAP notifications";

    private LdapErrorHandler() {
        throw new UnsupportedOperationException();
    }

    public static void handleError(Exception e, String message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos);
        e.printStackTrace(writer);
        final String stackTrace = new String(bos.toByteArray());

        Notifications.Bus.notify(new Notification(NOTIFICATION_GROUP, "LDAP Exception", message + " (<a href=\"#showException\">show exception</a>)", NotificationType.ERROR, (notification, hyperlinkEvent) -> {
            if ("#showException".equals(hyperlinkEvent.getDescription())) {
                Messages.showErrorDialog(stackTrace, "LDAP Exception");
            }
        }));
    }

}
