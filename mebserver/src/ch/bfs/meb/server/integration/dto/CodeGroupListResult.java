/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2013

  Projekt: server-commons

  $Id: CodeGroupListResult 228 2013-09-16 15:56:15Z jfu $
 */
package ch.bfs.meb.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;

/**
 * Filter specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class CodeGroupListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 5343089536179055050L;

    List<CodeGroup> _codeGroups;

    public CodeGroupListResult() {}

    public CodeGroupListResult(List<CodeGroup> codeGroups) {
        _codeGroups = codeGroups;
        setState(OK);
    }

    public CodeGroupListResult(String message) {
        setCodeGroups(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the codeGroups.
     */
    public List<CodeGroup> getCodeGroups() {
        return _codeGroups;
    }

    /**
     * @param codeGroups
     *            The codeGroups to set.
     */
    public void setCodeGroups(List<CodeGroup> codeGroups) {
        _codeGroups = codeGroups;
    }
}
