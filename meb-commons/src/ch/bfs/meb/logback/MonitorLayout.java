/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: MonitorLayout.java  01.03.2010 15:21:26 jfu $

 */
package ch.bfs.meb.logback;

import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;

public class MonitorLayout extends PatternLayout {
    public final static Marker SERVICE_MARKER = new BasicMarkerFactory().getDetachedMarker("SERVICE");

    public final static Marker ALIVE_MARKER = new BasicMarkerFactory().getDetachedMarker("ALIVE");

    public final static Marker METASTAT_SYNCH_MARKER = new BasicMarkerFactory().getDetachedMarker("METASTAT_SYNCH");

    public final static Marker NO_MONITOR_MARKER = new BasicMarkerFactory().getDetachedMarker("NO_MONITOR");

    public final static Marker ERROR_MARKER = new BasicMarkerFactory().getDetachedMarker("ERROR");

    // interfaces
    public final static Marker INTERFACE_BUR_MARKER = new BasicMarkerFactory().getDetachedMarker("INTERFACE_BUR");

    public final static Marker INTERFACE_IDM_MARKER = new BasicMarkerFactory().getDetachedMarker("INTERFACE_IDM");

    public final static Marker INTERFACE_METASTAT_MARKER = new BasicMarkerFactory().getDetachedMarker("INTERFACE_METASTAT");

    public final static Marker INTERFACE_SAS_MARKER = new BasicMarkerFactory().getDetachedMarker("INTERFACE_SAS");

    @Override
    public String doLayout(ILoggingEvent event) {
        Level level = event.getLevel();
        Marker marker = event.getMarker();
        if (Level.ERROR.equals(level)) {
            if (event.getThrowableProxy() instanceof ThrowableProxy) {
                Throwable t = ((ThrowableProxy) event.getThrowableProxy()).getThrowable();
                if (t instanceof MebUncheckedException) {
                    marker = ((MebUncheckedException) t).getMarker();
                }
            }

            if (INTERFACE_BUR_MARKER.equals(marker)) {
                return "ERROR <INTERFACE BUR>      " + super.doLayout(event);
            } else if (INTERFACE_IDM_MARKER.equals(marker)) {
                return "ERROR <INTERFACE IDM>      " + super.doLayout(event);
            } else if (INTERFACE_METASTAT_MARKER.equals(marker)) {
                return "ERROR <INTERFACE METASTAT> " + super.doLayout(event);
            } else if (INTERFACE_SAS_MARKER.equals(marker)) {
                return "ERROR <INTERFACE SAS> " + super.doLayout(event);
            } else if (!NO_MONITOR_MARKER.equals(marker)) {
                return "ERROR   " + super.doLayout(event);
            }
        } else if (Level.INFO.equals(level)) {
            if (SERVICE_MARKER.equals(marker)) {
                return "SERVICE " + super.doLayout(event);
            } else if (ALIVE_MARKER.equals(marker)) {
                return "ALIVE   " + super.doLayout(event);
            } else if (METASTAT_SYNCH_MARKER.equals(marker)) {
                return "METASTAT SYNCH " + super.doLayout(event);
            }
        }
        return null;
    }
}
