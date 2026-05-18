/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.service;

import ch.bfs.meb.ssp.web.ws.sspexport.Export;
import ch.bfs.meb.ssp.web.ws.sspexport.ExportListResult;
import ch.bfs.meb.ssp.web.ws.sspexport.ExportResult;
import ch.bfs.meb.ssp.web.ws.sspexport.FileResult;

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
