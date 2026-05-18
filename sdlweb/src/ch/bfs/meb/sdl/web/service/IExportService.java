/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.ws.sdlexport.Export;
import ch.bfs.meb.sdl.web.ws.sdlexport.ExportListResult;
import ch.bfs.meb.sdl.web.ws.sdlexport.ExportResult;
import ch.bfs.meb.sdl.web.ws.sdlexport.FileResult;

/**
 * Interface fuer Beispielservice fuer Testzwecke
 * 
 */
public interface IExportService {
    public FileResult runExport(Export export, String locale);

    public ExportListResult getExports();

    public ExportListResult getActiveExports();

    public ExportResult getExportById(Long exportId);

    public ExportResult updateExport(Export export);

    public ExportResult insertExport(Export export);

    public ExportResult deleteExport(Export export);
}
