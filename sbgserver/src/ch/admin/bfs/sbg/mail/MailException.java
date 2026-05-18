/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MailException.java 277 2007-08-22 14:24:52Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.mail;

/**
 * Mail service Exception wrapper
 * 
 * @author $Author: lsc $
 * @version $Revision: 277 $
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
