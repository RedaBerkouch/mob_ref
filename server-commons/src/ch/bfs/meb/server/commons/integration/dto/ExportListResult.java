/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: ExportListResult 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Export specific implementation for the return type of the soap web services
 * 
 * @author $Author: dzw $
 * @version $Revision: 228 $
 */
public class ExportListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 8272216981938239106L;

    List<Export> _exports;

    public ExportListResult() {}

    public ExportListResult(List<Export> exports) {
        _exports = exports;
        setState(OK);
    }

    public ExportListResult(String message) {

        setExports(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the exports.
     */
    public List<Export> getExports() {
        return _exports;
    }

    /**
     * @param exports
     *            The exports to set.
     */
    public void setExports(List<Export> exports) {
        _exports = exports;
    }
}
