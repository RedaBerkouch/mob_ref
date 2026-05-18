/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.ws.sbaexport.Export;
import ch.bfs.meb.sba.web.ws.sbaexport.ExportListResult;
import ch.bfs.meb.sba.web.ws.sbaexport.ExportResult;
import ch.bfs.meb.sba.web.ws.sbaexport.FileResult;

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
