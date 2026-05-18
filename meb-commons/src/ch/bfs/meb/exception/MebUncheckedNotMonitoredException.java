/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: MebUncheckedNotLoggedException.java  15.03.2010 10:42:54 msc $

 */
package ch.bfs.meb.exception;

import ch.bfs.meb.logback.MonitorLayout;

/**
 * Meb implementation of an unchecked exception, based on the MebUncheckedException.
 * Use this class to avoid logging to the monitoring log.
 *
 */
public class MebUncheckedNotMonitoredException extends MebUncheckedException {
    private static final long serialVersionUID = 5829000358491857397L;

    public MebUncheckedNotMonitoredException() {
        super();
        setMarker(MonitorLayout.NO_MONITOR_MARKER);
    }

    public MebUncheckedNotMonitoredException(String message, Throwable cause) {
        super(message, cause);
        setMarker(MonitorLayout.NO_MONITOR_MARKER);
    }

    public MebUncheckedNotMonitoredException(String message) {
        super(message);
        setMarker(MonitorLayout.NO_MONITOR_MARKER);
    }

    public MebUncheckedNotMonitoredException(Throwable cause) {
        super(cause);
        setMarker(MonitorLayout.NO_MONITOR_MARKER);
    }
}
