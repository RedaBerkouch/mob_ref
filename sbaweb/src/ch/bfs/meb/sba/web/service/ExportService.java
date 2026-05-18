/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb
 */
package ch.bfs.meb.sba.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbaexport.Export;
import ch.bfs.meb.sba.web.ws.sbaexport.ExportListResult;
import ch.bfs.meb.sba.web.ws.sbaexport.ExportResult;
import ch.bfs.meb.sba.web.ws.sbaexport.FileResult;
import ch.bfs.meb.util.SecurityConstants;

@Service("exportService")
public class ExportService implements IExportService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public FileResult runExport(Export export, String locale) {
        return webServiceClientFactory.getExportWebService().runExport(export, locale);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public ExportListResult getExports() {
        return webServiceClientFactory.getExportWebService().getExports();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public ExportListResult getActiveExports() {
        return webServiceClientFactory.getExportWebService().getActiveExports();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public ExportResult getExportById(Long exportId) {
        return webServiceClientFactory.getExportWebService().getExportById(exportId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public ExportResult updateExport(Export export) {
        return webServiceClientFactory.getExportWebService().updateExport(export);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public ExportResult insertExport(Export export) {
        return webServiceClientFactory.getExportWebService().insertExport(export);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public ExportResult deleteExport(Export export) {
        return webServiceClientFactory.getExportWebService().deleteExport(export);
    }
}
