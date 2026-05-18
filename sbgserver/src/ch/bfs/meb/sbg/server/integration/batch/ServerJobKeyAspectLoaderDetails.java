/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebserver

  $Id: ServerJobCodeGroupLoaderDetails  16.09.2013 09:23:30 Administrator $

 */
package ch.bfs.meb.sbg.server.integration.batch;

import ch.bfs.meb.sbg.server.keyaspect.KeyAspectManager;
import lombok.Setter;

public class ServerJobKeyAspectLoaderDetails {
    @Setter
    private KeyAspectManager keyAspectManager;

    public void refreshCache() {
        keyAspectManager.refreshCache();
    }
}
