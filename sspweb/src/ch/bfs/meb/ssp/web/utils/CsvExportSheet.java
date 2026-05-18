package ch.bfs.meb.ssp.web.utils;

import java.util.StringJoiner;

public final class CsvExportSheet {

    private static final String CSV_LINE_SEPARATOR = "\n";

    private final StringJoiner joiner = new StringJoiner(CSV_LINE_SEPARATOR);

    public CsvExportSheet add(String line) {
        joiner.add(line);
        return this;
    }

    public String getSheet() {
        return joiner.toString();
    }
}
