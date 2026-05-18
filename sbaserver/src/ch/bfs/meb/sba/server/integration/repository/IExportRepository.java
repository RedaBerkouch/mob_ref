/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaExport;
import ch.bfs.meb.sba.server.integration.dto.SbaParameter;

public interface IExportRepository {
    public List<SbaExport> getExports();

    public List<SbaExport> getActiveExports();

    public List<? extends Object> executeGenericQuery(String sqlSource);

    public List<SbaParameter> getParameters(Long filterId);

    public SbaExport getExportById(Long exportId);

    public SbaExport updateExport(SbaExport export);

    public SbaExport insertExport(SbaExport export);

    public void deleteExport(SbaExport export);
}
