/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: BaseServiceAdvice.java  15.03.2010 10:52:09 msc $

 */
package ch.bfs.meb.server.commons.service.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.logback.MonitorLayout;
import ch.bfs.meb.server.commons.integration.dto.FileResult;

public abstract class BaseServiceAdvice {
    protected static final String ERROR_SQL_MESSAGE = "error.sql.message";
    protected static final String ERROR_OBEJCTNOTFOUND_MESSAGE = "error.objectnotfound.message";

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseServiceAdvice.class);

    public Object handleException(ProceedingJoinPoint call) throws Throwable {
        try {

            return call.proceed();
        } catch (SQLGrammarException e) {
            LOGGER.error(MonitorLayout.NO_MONITOR_MARKER, "Error while executing: '" + call.getSignature().getName() + "'", e);
            return newResult(ERROR_SQL_MESSAGE);
        } catch (ObjectNotFoundException e) {
            LOGGER.error(MonitorLayout.NO_MONITOR_MARKER, "Error while executing: '" + call.getSignature().getName() + "'", e);
            return newResult(ERROR_OBEJCTNOTFOUND_MESSAGE);
        } catch (RuntimeException e) {
            LOGGER.error("Error while executing: '" + call.getSignature().getName() + "'", e);
            String message = e.getLocalizedMessage();
            if (message == null) {
                message = e.toString();
            }
            return newResult(message);
        }
    }

    public Object handleListException(ProceedingJoinPoint call) throws Throwable {
        try {

            return call.proceed();
        } catch (SQLGrammarException e) {
            LOGGER.error(MonitorLayout.NO_MONITOR_MARKER, "Error while executing: '" + call.getSignature().getName() + "'", e);
            return newListResult(ERROR_SQL_MESSAGE);
        } catch (ObjectNotFoundException e) {
            LOGGER.error(MonitorLayout.NO_MONITOR_MARKER, "Error while executing: '" + call.getSignature().getName() + "'", e);
            return newListResult(ERROR_OBEJCTNOTFOUND_MESSAGE);
        } catch (RuntimeException e) {
            LOGGER.error("Error while executing: '" + call.getSignature().getName() + "'", e);
            return newListResult(e.getLocalizedMessage());
        }
    }

    public Object handleFileException(ProceedingJoinPoint call) throws Throwable {
        try {

            return call.proceed();
        } catch (SQLGrammarException e) {
            LOGGER.error(MonitorLayout.NO_MONITOR_MARKER, "Error while executing: '" + call.getSignature().getName() + "'", e);
            return newFileResult(ERROR_SQL_MESSAGE);
        } catch (ObjectNotFoundException e) {
            LOGGER.error(MonitorLayout.NO_MONITOR_MARKER, "Error while executing: '" + call.getSignature().getName() + "'", e);
            return newFileResult(ERROR_OBEJCTNOTFOUND_MESSAGE);
        } catch (RuntimeException e) {
            LOGGER.error("Error while executing: '" + call.getSignature().getName() + "'", e);
            return newFileResult(e.getLocalizedMessage());
        }
    }

    protected abstract Object newResult(String message);

    protected abstract Object newListResult(String message);

    protected Object newFileResult(String message) {
        return new FileResult(message);
    }
}
