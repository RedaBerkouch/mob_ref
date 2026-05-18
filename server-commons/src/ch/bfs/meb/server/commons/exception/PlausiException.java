/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$

 */
package ch.bfs.meb.server.commons.exception;

import ch.bfs.meb.exception.MebUncheckedException;

/**
 * Unchecked exception thrown during the verification process, based on the MebUncheckedException.
 * Use this class to singal an exception during verification. The message will be displayed in
 * the intervention text.
 *
 */
@SuppressWarnings("serial")
public class PlausiException extends MebUncheckedException {
    /**
     * @param message
     */
    public PlausiException(String message) {
        super(message);
    }
}