/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: BurServiceException.java 1222 2010-04-01 13:39:39Z jfu $

 */
package ch.bfs.meb.exception;

import ch.bfs.meb.logback.MonitorLayout;

/**
 * Bur specific implementation of an unchecked exception, based on the MebUncheckedException.
 * Use this class to handle the monitoring log for Bur errors properly.
 *
 */
public class BurException extends MebUncheckedException {
    private static final long serialVersionUID = -4557353638252675981L;

    /**
     * 
     */
    public BurException() {
        super();
        setMarker(MonitorLayout.INTERFACE_BUR_MARKER);
    }

    /**
     * @param message
     * @param cause
     */
    public BurException(String message, Throwable cause) {
        super(message, cause);
        setMarker(MonitorLayout.INTERFACE_BUR_MARKER);
    }

    /**
     * @param message
     */
    public BurException(String message) {
        super(message);
        setMarker(MonitorLayout.INTERFACE_BUR_MARKER);
    }

    /**
     * @param cause
     */
    public BurException(Throwable cause) {
        super(cause);
        setMarker(MonitorLayout.INTERFACE_BUR_MARKER);
    }
}