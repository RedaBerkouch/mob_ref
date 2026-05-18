/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.configuration;

import java.util.List;

import javax.management.Notification;

/**
 * TODO Document this class
 * 
 */
public class ConfigurationChangedNotifier {

    private List<IConfigurationChangedListener> listeners;

    public void configurationChanged(Notification notification) {

        for (IConfigurationChangedListener listener : listeners) {
            listener.configurationChanged(notification);
        }
    }

    public void setListeners(List<IConfigurationChangedListener> listeners) {

        this.listeners = listeners;
    }
}
