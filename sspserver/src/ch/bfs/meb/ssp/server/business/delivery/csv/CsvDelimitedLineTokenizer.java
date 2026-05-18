/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: CsvDelimitedLineTokenizer.java  11.02.2010 10:09:28 jfu $

 */
package ch.bfs.meb.ssp.server.business.delivery.csv;

import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;

import ch.bfs.meb.server.commons.delivery.csv.BaseCsvDelimitedLineTokenizer;

public class CsvDelimitedLineTokenizer extends BaseCsvDelimitedLineTokenizer {
    public static final int COLUMN_COUNT = 18;

    @Override
    public FieldSet tokenize(String line) {
        line = hideQuotedDelimiter(line);
        String[] columns = line.split(";", -1);
        if (columns.length > COLUMN_COUNT) {
            line = "";
            for (int i = 0; i < COLUMN_COUNT; i++) {
                if (i != 0) {
                    line += ";";
                }
                line += columns[i];
            }
        } else if (columns.length < COLUMN_COUNT) {
            for (int i = columns.length; i < COLUMN_COUNT; i++) {
                line += ";";
            }
        }
        line = showQuotedDelimiter(line);

        FieldSet oldFieldSet = super.tokenize(line);

        String[] oldNames = oldFieldSet.getNames();
        String[] newNames = new String[COLUMN_COUNT + 1];
        newNames[0] = "ORIGDELIVERYDATA";
        System.arraycopy(oldNames, 0, newNames, 1, oldNames.length);

        String[] oldValues = oldFieldSet.getValues();
        String[] newValues = new String[COLUMN_COUNT + 1];
        newValues[0] = line;
        System.arraycopy(oldValues, 0, newValues, 1, oldValues.length);

        return new DefaultFieldSet(newValues, newNames);
    }
}
