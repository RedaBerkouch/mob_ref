/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: SessionTimeoutException.java  09.02.2011 08:54:03 msc $

 */
package ch.bfs.meb.exception;

public class SessionTimeoutException extends MebUncheckedNotMonitoredException {
    private static final long serialVersionUID = -6052044240572985268L;

    /**
     * @param message
     * @param cause
     */
    public SessionTimeoutException(Throwable cause) {
        super("session.timout.error.message", cause);
    }

    /**
     * @param message
     */
    public SessionTimeoutException() {
        super("session.timout.error.message");
    }
}
