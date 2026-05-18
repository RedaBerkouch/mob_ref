/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: MonitorFilter.java  12.04.2010 13:54:31 jfu $

 */
package ch.bfs.meb.logback;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class MonitorFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        Level level = event.getLevel();
        Marker marker = event.getMarker();
        if (Level.ERROR.equals(level)) {
            if (!MonitorLayout.NO_MONITOR_MARKER.equals(marker)) {
                return FilterReply.ACCEPT;
            }
        } else if (Level.INFO.equals(level)) {
            if (MonitorLayout.SERVICE_MARKER.equals(marker) || MonitorLayout.ALIVE_MARKER.equals(marker)
                    || MonitorLayout.METASTAT_SYNCH_MARKER.equals(marker)) {
                return FilterReply.ACCEPT;
            }
        }
        return FilterReply.DENY;
    }
}