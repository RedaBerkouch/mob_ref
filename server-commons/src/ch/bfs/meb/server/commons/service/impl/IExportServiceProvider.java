/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Export;
import ch.bfs.meb.server.commons.integration.dto.FileResult;

public interface IExportServiceProvider {
    public List<Export> getExports();

    public List<Export> getActiveExports();

    public List<? extends Object> executeGenericQuery(String sqlSource);

    public FileResult runXmlExport(Export export, String locale);

    public FileResult runUsersExport(Export export, String locale);

    public FileResult runInitStatusExport(Export export, String locale);

    public FileResult runXmlDeliveryPlausireportExport(Export export, String locale);

    public Export getExportById(Long exportId);

    public Export updateExport(Export export);

    public Export insertExport(Export export);

    public void deleteExport(Export export);
}
