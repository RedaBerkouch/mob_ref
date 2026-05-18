/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: TableManagerBaseAdvice.java  08.04.2010 13:23:42 jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import org.aspectj.lang.ProceedingJoinPoint;

import ch.bfs.meb.web.commons.exception.MebDhtmlxTableDataXMLException;

public class TableManagerBaseAdvice {
    public Object handleSingleRowException(ProceedingJoinPoint call) throws Throwable {
        try {

            return call.proceed();
        } catch (Exception e) {
            throw new MebDhtmlxTableDataXMLException(e);
        }
    }
}
