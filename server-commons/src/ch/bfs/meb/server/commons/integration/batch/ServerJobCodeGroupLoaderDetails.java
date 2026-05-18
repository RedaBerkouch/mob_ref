/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebserver

  $Id: ServerJobCodeGroupLoaderDetails  16.09.2013 09:23:30 Administrator $

 */
package ch.bfs.meb.server.commons.integration.batch;

import ch.bfs.meb.server.commons.codes.ICodegroupManager;

public class ServerJobCodeGroupLoaderDetails {
    private ICodegroupManager _codegroupManager;

    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    public void refreshCodegroups() {
        _codegroupManager.refreshCache();
    }
}
