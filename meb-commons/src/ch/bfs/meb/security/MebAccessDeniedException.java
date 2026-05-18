/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: MebAccessDeniedException.java 833 2010-02-25 13:00:30Z jfu $

 */
package ch.bfs.meb.security;

import ch.bfs.meb.exception.MebUncheckedException;

/**
 * Meb implementation of an unchecked exception to address access denied errors, based on the MebUncheckedException.
 *
 */
public class MebAccessDeniedException extends MebUncheckedException {
    private static final long serialVersionUID = -4557353638252675981L;

    /**
     * 
     */
    public MebAccessDeniedException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MebAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MebAccessDeniedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MebAccessDeniedException(Throwable cause) {
        super(cause);
    }

}
