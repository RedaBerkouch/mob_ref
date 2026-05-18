/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.Export;
import ch.bfs.meb.server.commons.integration.dto.ExportListResult;
import ch.bfs.meb.server.commons.integration.dto.ExportResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IExportService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SbaExportWebService", name = "SbaExportWebServicePortType")
public class SbaExportService extends AbstractMebWebService<IExportService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public ExportListResult getExports() {
        return getService().getExports();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public ExportListResult getActiveExports() {
        return getService().getActiveExports();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public FileResult runExport(Export export, String locale) {
        return getService().runExport(export, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public ExportResult getExportById(Long exportId) {
        return getService().getExportById(exportId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public ExportResult updateExport(Export export) {
        return getService().updateExport(export);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public ExportResult insertExport(Export export) {
        return getService().insertExport(export);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public ExportResult deleteExport(Export export) {
        return getService().deleteExport(export);
    }
}