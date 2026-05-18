/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.security;

import ch.bfs.meb.exception.MebCheckedException;

/**
 * TODO Document this class
 * 
 */
@SuppressWarnings("serial")
public class EncryptionException extends MebCheckedException {

    /**
     * 
     */
    public EncryptionException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public EncryptionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public EncryptionException(Throwable cause) {
        super(cause);
    }
}