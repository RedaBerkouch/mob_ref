/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Common return type of the soap web services for delivery uploads
 * 
 * @author $Author$
 * @version $Revision$
 */
public class UploadResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -2163731492063075079L;

    Object[] _args = new Object[0];
    protected List<String> _unconfiguredSchoolIds;
    protected List<String> _unconfiguredSchoolTypes;

    public UploadResult() {
        setState(OK);
    }

    public UploadResult(String message) {
        setMessage(message);
        setState(FAILURE);
    }

    public UploadResult(String message, Object... args) {
        setMessage(message);
        setArgs(args);
        setState(FAILURE);
    }

    /**
     * @return
     */
    public Object[] getArgs() {
        return _args;
    }

    /**
     * @param args
     */
    public void setArgs(Object[] args) {
        _args = args;
    }

    public List<String> getUnconfiguredSchoolIds() {
        return _unconfiguredSchoolIds;
    }

    public void setUnconfiguredSchoolIds(List<String> unconfiguredSchoolIds) {
        _unconfiguredSchoolIds = unconfiguredSchoolIds;
    }

    public List<String> getUnconfiguredSchoolTypes() {
        return _unconfiguredSchoolTypes;
    }

    public void setUnconfiguredSchoolTypes(List<String> unconfiguredSchoolTypes) {
        _unconfiguredSchoolTypes = unconfiguredSchoolTypes;
    }
}