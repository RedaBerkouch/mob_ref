/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: TextFileHttpResult.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Implementation of the XmlHTTPResponse for a donwload request
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class FileHttpResult extends BinaryHttpResultBase {
    private final byte[] _deliveryDocument;
    private String _filename;
    private String _message = "";
    private int _state = ResultBase.OK;

    public FileHttpResult(byte[] deliveryDocument) {
        _deliveryDocument = deliveryDocument;
        setFilename(null);
    }

    public FileHttpResult(byte[] deliveryDocument, String fileName) {
        _deliveryDocument = deliveryDocument;
        setFilename(fileName);
    }

    public FileHttpResult(byte[] deliveryDocument, String filename, String message, int state) {
        _deliveryDocument = deliveryDocument;
        setFilename(filename);
        _message = message;
        _state = state;
    }

    public void setFilename(String filename) {
        if (filename == null || filename.trim().length() == 0)
            _filename = "Download.txt";
        else
            _filename = filename;
    }

    public void writeTo(OutputStream os) {
        try {
            if (_state > 1) {
                os.write(_message.getBytes());
            } else {
                os.write(_deliveryDocument);
            }
        } catch (IOException e) {}
    }

    public String getContentType() {
        if (_state > 1) {
            return "text/plain; charset=UTF-8";
        } else {
            String suffix = FilenameUtils.getExtension(_filename);
            if ("txt".equalsIgnoreCase(suffix)) {
                return "text/plain; charset=UTF-8";
            }
            return "application/octet-stream";
        }
    }

    public String getContentDisposition() {
        String filename;
        if (_state > 1) {
            filename = "Error.txt";
        } else {
            filename = _filename;
        }

        return "attachment;filename=" + filename;
    }
}
