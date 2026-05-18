/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Export specific implementation for the return type of the soap web services
 *
 */
public class ExportResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 3442360267526209726L;

    Export _export;

    public ExportResult() {}

    public ExportResult(Export export) {
        _export = export;
        export.getParameters(); // prevents null value in transfer object
        setState(OK);
    }

    public ExportResult(String message) {
        setExport(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the export.
     */
    public Export getExport() {
        return _export;
    }

    /**
     * @param export
     *            The export to set.
     */
    public void setExport(Export export) {
        _export = export;
    }
}
