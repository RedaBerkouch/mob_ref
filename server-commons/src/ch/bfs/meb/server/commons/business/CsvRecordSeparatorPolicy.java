/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: CsvRecordSeparatorPolicy.java  26.03.2010 15:55:37 jfu $

 */
package ch.bfs.meb.server.commons.business;

import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;

public class CsvRecordSeparatorPolicy extends SimpleRecordSeparatorPolicy {
    @Override
    public boolean isEndOfRecord(String line) {
        if (line.trim().length() == 0) {
            return false;
        }
        return super.isEndOfRecord(line);
    }

    @Override
    public String postProcess(String record) {
        if (record == null || record.trim().length() == 0 || "null".equals(record)) {
            return null;
        }
        return super.postProcess(record);
    }
}
