/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: SASException.java 1222 2010-04-01 13:39:39Z jfu $

 */
package ch.bfs.meb.server.commons.integration.sas;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.logback.MonitorLayout;

/**
 * SAS specific implementation of an unchecked exception, based on the MebUncheckedException.
 * Use this class to handle the monitoring log for SAS errors properly.
 *
 */
public class SASException extends MebUncheckedException {
    private static final long serialVersionUID = 3911633675794855372L;

    /**
     * 
     */
    public SASException() {
        super();
        setMarker(MonitorLayout.INTERFACE_SAS_MARKER);
    }

    /**
     * @param message
     * @param cause
     */
    public SASException(String message, Throwable cause) {
        super(message, cause);
        setMarker(MonitorLayout.INTERFACE_SAS_MARKER);
    }

    /**
     * @param message
     */
    public SASException(String message) {
        super(message);
        setMarker(MonitorLayout.INTERFACE_SAS_MARKER);
    }

    /**
     * @param cause
     */
    public SASException(Throwable cause) {
        super(cause);
        setMarker(MonitorLayout.INTERFACE_SAS_MARKER);
    }
}