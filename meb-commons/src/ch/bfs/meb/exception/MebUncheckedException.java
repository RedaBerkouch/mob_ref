/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.exception;

import org.slf4j.Marker;

import ch.bfs.meb.logback.MonitorLayout;

/**
 * Meb specific implementation of an unchecked exception
 * 
 */
@SuppressWarnings("serial")
public class MebUncheckedException extends RuntimeException {
    private Marker marker = MonitorLayout.ERROR_MARKER;

    /**
     * 
     */
    public MebUncheckedException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MebUncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MebUncheckedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MebUncheckedException(Throwable cause) {
        super(cause);
    }

    public void setMarker(Marker marker) {
        if (marker != null) {
            this.marker = marker;
        }
    }

    public Marker getMarker() {
        return marker;
    }
}
