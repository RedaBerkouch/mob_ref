/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlExport;
import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;

public interface IExportRepository {
    public List<SdlExport> getExports();

    public List<SdlExport> getActiveExports();

    public List<? extends Object> executeGenericQuery(String sqlSource);

    public List<SdlParameter> getParameters(Long filterId);

    public SdlExport getExportById(Long exportId);

    public SdlExport updateExport(SdlExport export);

    public SdlExport insertExport(SdlExport export);

    public void deleteExport(SdlExport export);
}
