/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.Export;
import ch.bfs.meb.server.commons.integration.dto.ExportListResult;
import ch.bfs.meb.server.commons.integration.dto.ExportResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;

public interface IExportService {
    static String RETURN_VALUE_FILE_LOCATION = "MEB_ExportFile";
    static String RETURN_VALUE_ERROR_MESSAGE = "MEB_ErrorMessage";

    public ExportListResult getExports();

    public ExportListResult getActiveExports();

    public FileResult runExport(Export export, String locale);

    public ExportResult getExportById(Long exportId);

    public ExportResult updateExport(Export export);

    public ExportResult insertExport(Export export);

    public ExportResult deleteExport(Export export);
}
