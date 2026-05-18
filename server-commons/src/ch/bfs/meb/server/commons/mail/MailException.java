/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.mail;

/**
 * Mail service Exception wrapper
 * 
 * @author $Author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class MailException extends RuntimeException {

    /**
     * 
     */
    public MailException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MailException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MailException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MailException(Throwable cause) {
        super(cause);
    }

}
