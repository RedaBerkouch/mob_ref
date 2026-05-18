/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$

 */
package ch.bfs.meb.configuration;

import javax.management.Notification;

/**
 * TODO Document this class
 * 
 */
public interface IConfigurationChangedListener {

    void configurationChanged(Notification notification);
}
