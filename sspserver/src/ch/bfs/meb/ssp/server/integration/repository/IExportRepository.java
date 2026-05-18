/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import ch.bfs.meb.ssp.server.integration.dto.SspExport;
import ch.bfs.meb.ssp.server.integration.dto.SspParameter;

public interface IExportRepository {
    public List<SspExport> getExports();

    public List<SspExport> getActiveExports();

    public List<? extends Object> executeGenericQuery(String sqlSource);

    public List<SspParameter> getParameters(Long filterId);

    public SspExport getExportById(Long exportId);

    public SspExport updateExport(SspExport export);

    public SspExport insertExport(SspExport export);

    public void deleteExport(SspExport export);
}
