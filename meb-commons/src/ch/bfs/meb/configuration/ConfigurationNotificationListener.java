/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.configuration;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener, der bei mbean-aenderungen aufgerufen wird. Hiermit koennen listeners
 * automatisch benachrichtigt werden.
 */
@SuppressWarnings("serial")
public class ConfigurationNotificationListener implements NotificationListener, NotificationFilter {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationNotificationListener.class);

    public void handleNotification(Notification notification, Object handback) {

        if (handback != null && handback instanceof ConfigurationChangedNotifier) {

            ConfigurationChangedNotifier notifier = (ConfigurationChangedNotifier) handback;

            notifier.configurationChanged(notification);
        } else {

            LOG.error("Handback not of type ConfigurationNotificationListener - check spring configuration");
        }
    }

    public boolean isNotificationEnabled(Notification notification) {
        return AttributeChangeNotification.class.isAssignableFrom(notification.getClass());
    }
}
