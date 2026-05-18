/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: MebRollingFileAppender.java  31.03.2010 16:53:00 jfu $

 */
package ch.bfs.meb.logback;

import java.io.File;

import ch.bfs.meb.util.StringUtils;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.RollingFileAppender;

public class MebRollingFileAppender<E> extends RollingFileAppender<E> {
    @Override
    public void setContext(Context context) {
        super.setContext(context);
        // set default path
        context.putProperty("BASE_DIR", "");

        String loggingBaseDir = System.getProperty("MEB_LOGGING_BASE_DIR");
        if (!StringUtils.isEmpty(loggingBaseDir)) {
            loggingBaseDir = loggingBaseDir.trim();
            File loggingBase = new File(loggingBaseDir);
            context.putProperty("BASE_DIR", loggingBase.getAbsolutePath() + System.getProperty("file.separator"));
        }
    }
}
