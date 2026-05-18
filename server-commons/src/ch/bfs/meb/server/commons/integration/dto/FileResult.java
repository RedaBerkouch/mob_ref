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
 * Common return type of the soap web services for file transmissions
 *
 */
public class FileResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 251728802153540631L;

    private byte[] _binaryFile;
    private String _filename = null;

    public FileResult() {}

    public FileResult(byte[] anFile) {
        _binaryFile = anFile;
        setState(OK);
    }

    public FileResult(byte[] anFile, String filename) {
        this(anFile);
        _filename = filename;
    }

    public FileResult(String message) {
        setBinaryFile(null);
        setMessage(message);
        setState(FAILURE);
    }

    public byte[] getBinaryFile() {
        return _binaryFile;
    }

    public void setBinaryFile(byte[] anFile) {
        _binaryFile = anFile;
    }

    public String getFilename() {
        return _filename;
    }

    public void setFilename(String filename) {
        _filename = filename;
    }

    public void setMessage(String message) {
        super.setMessage(message);
        setState(FAILURE);
    }
}