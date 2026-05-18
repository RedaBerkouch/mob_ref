/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.exception;

/**
 * TODO Document this class
 * 
 */
@SuppressWarnings("serial")
public class MebCheckedException extends Exception {

    /**
     * 
     */
    public MebCheckedException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MebCheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MebCheckedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MebCheckedException(Throwable cause) {
        super(cause);
    }
}
