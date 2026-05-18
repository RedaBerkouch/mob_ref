/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: MetastatServiceException.java 340 2009-12-10 13:11:55Z jfu $

 */
package ch.bfs.meb.server.rest.metastat;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.logback.MonitorLayout;

/**
 * Metastat specific implementation of an unchecked exception, based on the MebUncheckedException.
 * Use this class to handle the monitoring log for Metastat errors properly.
 *
 */
public class MetastatServiceException extends MebUncheckedException {
    private static final long serialVersionUID = -655793129964834575L;

    /**
     * 
     */
    public MetastatServiceException() {
        super();
        setMarker(MonitorLayout.INTERFACE_METASTAT_MARKER);
    }

    /**
     * @param message
     * @param cause
     */
    public MetastatServiceException(String message, Throwable cause) {
        super(message, cause);
        setMarker(MonitorLayout.INTERFACE_METASTAT_MARKER);
    }

    /**
     * @param message
     */
    public MetastatServiceException(String message) {
        super(message);
        setMarker(MonitorLayout.INTERFACE_METASTAT_MARKER);
    }

    /**
     * @param cause
     */
    public MetastatServiceException(Throwable cause) {
        super(cause);
        setMarker(MonitorLayout.INTERFACE_METASTAT_MARKER);
    }
}