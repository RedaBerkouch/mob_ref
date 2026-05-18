/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: SasResult.java  13.06.2013 15:24:37 fuerter $

 */
package ch.bfs.meb.server.commons.integration.sas;

/**
 * 
 * SAS result, return value from sas call
 * 
 * @author $Author: fuerter $
 */
public class SASResult {
    public static final String RETURN_VALUE_RETURNCODE_NAME = "MEB_ReturnCode";
    public static final int RETURN_VALUE_RETURNCODE_OK = 0;

    public enum Status {
        OK, FAILURE
    }

    private final Status status;

    private final String[] logLines;

    private final String[] listLines;

    public SASResult(String[] logLines, String[] listLines) {
        super();

        this.logLines = logLines;
        this.listLines = listLines;

        Integer returnCode = getIntegerReturnValue(RETURN_VALUE_RETURNCODE_NAME);
        if (returnCode != null && returnCode.intValue() != RETURN_VALUE_RETURNCODE_OK) {
            this.status = Status.FAILURE;
        } else {
            this.status = Status.OK;
        }
    }

    public Status getStatus() {
        return status;
    }

    public String[] getLogLines() {
        return logLines;
    }

    public String[] getListLines() {
        return listLines;
    }

    /**
     * Returns in the logLines for the value following the first occurence of
     * returnValueName
     * 
     * @param returnValueName
     *            as integer
     * @return the Integer value or null if returnValueName not found in
     *         logLines
     */
    public Integer getIntegerReturnValue(String returnValueName) {
        String returnValue = getReturnValue(returnValueName);

        if (returnValue == null)
            return null;
        return Integer.valueOf(returnValue);
    }

    /**
     * Returns in the logLines for the value following the first occurence of
     * returnValueName
     * 
     * @param returnValueName
     * @return the Integer value or null if returnValueName not found in
     *         logLines
     */
    public String getReturnValue(String returnValueName) {
        String returnValue = null;

        for (String line : logLines) // TODO Allenfalls waere es interessasnt, im Fehlerfall mehr Log information von SAS auf log.debug auszugeben (Besprochen mit Christoph Stotzer, 19.1.2016)
        {
            if (line.startsWith(returnValueName)) {
                int startIndex = line.indexOf("=");
                if (startIndex >= 0) {
                    returnValue = line.substring(startIndex + 1).trim();
                }
            }
        }

        return returnValue;
    }
}