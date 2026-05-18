package ch.bfs.meb.server.commons.delivery.csv;
/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: BaseCsvDelimitedLineTokenizer.java  11.02.2010 10:09:28 msc $

 */

import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class BaseCsvDelimitedLineTokenizer extends DelimitedLineTokenizer {
    private static final String DELIMITER = "__s_e_m_i_c_o_l_o_n__";

    protected String hideQuotedDelimiter(String line) {
        String res = "";
        int start = 0;
        int pos = line.indexOf(DEFAULT_QUOTE_CHARACTER);
        while (pos > 0) {
            res += line.substring(start, pos);
            start = line.indexOf(DEFAULT_QUOTE_CHARACTER, pos + 1);
            if (start >= 0) {
                res += line.substring(pos, ++start).replaceAll(";", DELIMITER);
                pos = line.indexOf(DEFAULT_QUOTE_CHARACTER, start);
            } else // missing closing DEFAULT_QUOTE_CHARACTER (probably ")
            {
                res += line.substring(pos).replaceAll(";", DELIMITER) + DEFAULT_QUOTE_CHARACTER;
                pos = -1;
            }
        }

        if (start >= 0) {
            res += line.substring(start);
        }

        return res;
    }

    protected String showQuotedDelimiter(String line) {
        return line.replaceAll(DELIMITER, ";");
    }
}
