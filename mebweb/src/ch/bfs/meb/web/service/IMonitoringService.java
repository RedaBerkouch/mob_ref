/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

 */
package ch.bfs.meb.web.service;

public interface IMonitoringService {
    public Boolean checkIdmService();

    public Boolean checkSasService();

    public Boolean checkMetastatService();

    public Boolean checkBurService();

    public Boolean checkDatabase();
}
