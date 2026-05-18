package ch.bfs.meb.sbg.web.utils;

import ch.bfs.meb.sbg.web.ws.sbgmacro.SbgParameter;
import org.springframework.util.CollectionUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import java.util.StringJoiner;

import static ch.bfs.meb.util.FormatUtility.formatDate;
import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;

public final class CsvExportRow {

    private static final String CSV_DELIMITER = ";";

    private final StringJoiner joiner = new StringJoiner(CSV_DELIMITER);

    public CsvExportRow add(Long value) {
        joiner.add(escapeCsv(String.valueOf(value)));
        return this;
    }

    public CsvExportRow add(Boolean value) {
        joiner.add(escapeCsv(String.valueOf(value)));
        return this;
    }

    public CsvExportRow add(String value) {
        joiner.add(escapeCsv(value));
        return this;
    }

    public CsvExportRow add(String language, String valueFr, String valueIt, String valueDe) {
        if ("Fr".equals(language)) {
            joiner.add(escapeCsv(valueFr));
        } else if ("It".equals(language)) {
            joiner.add(escapeCsv(valueIt));
        } else {
            joiner.add(escapeCsv(valueDe));
        }
        return this;
    }

    public CsvExportRow add(String language, String keyFr, String keyIt, String keyDe, String value) {
        String parameter = "";
        if ("Fr".equals(language) && keyFr != null) {
            parameter += escapeCsv(keyFr);
        } else if ("It".equals(language) && keyIt != null) {
            parameter += escapeCsv(keyIt);
        } else if (keyDe != null) {
            parameter += escapeCsv(keyDe);
        }
        joiner.add(parameter + "=" + escapeCsv(value));
        return this;
    }

    public CsvExportRow add(String language, List<SbgParameter> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return this;
        }
        parameters
                .forEach(parameter -> add(language, parameter.getNameFr(), parameter.getNameIt(), parameter.getNameDe(), parameter.getDefaultValue()));
        return this;
    }

    public CsvExportRow add(XMLGregorianCalendar date) {
        if (date == null) {
            joiner.add("");
        } else {
            joiner.add(escapeCsv(formatDate(date.toGregorianCalendar().getTime())));
        }
        return this;
    }

    public String getRow() {
        return joiner.toString();
    }
}
