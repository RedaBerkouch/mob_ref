/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: EncryptionException.java 305 2009-12-03 10:25:28Z msc $

 */
package ch.bfs.meb.security;

import ch.bfs.meb.exception.MebCheckedException;

/**
 * 
 * 
 */
@SuppressWarnings("serial")
public class MebSoapHeaderException extends MebCheckedException {

    /**
     * 
     */
    public MebSoapHeaderException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MebSoapHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MebSoapHeaderException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MebSoapHeaderException(Throwable cause) {
        super(cause);
    }
}