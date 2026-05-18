package ch.bfs.meb.server.commons.delivery.csv;

import org.junit.Test;

import static org.junit.Assert.*;

public class BaseCsvDelimitedLineTokenizerTest {

    // MEB-112 "Lieferung von ZH läuft nicht" is due to a bug in BaseCsvDelimitedLineTokenizer
    // This string can reproduce the bug. For this, you have to set back the line
    //   pos = line.indexOf(DEFAULT_QUOTE_CHARACTER, start);
    // to the erroneous version
    //   pos = line.indexOf(start, DEFAULT_QUOTE_CHARACTER);
    public static final String MEB_112_REASON_OFF = "C;7566405104390;201814272;39080027;1;1;71797;100;2018-08-20;82213676;29511;Press&Books \"Stadttor\";Bahnhofplatz; ;8401;Winterthur;0; ;";

    @Test
    public void whenAStringWithQuotesButNoSemicolonInsideQuotes_thenReturnUnchangedString() {
        BaseCsvDelimitedLineTokenizer tokenizer = new BaseCsvDelimitedLineTokenizer();
        assertEquals(MEB_112_REASON_OFF, tokenizer.hideQuotedDelimiter(MEB_112_REASON_OFF));

    }

}
