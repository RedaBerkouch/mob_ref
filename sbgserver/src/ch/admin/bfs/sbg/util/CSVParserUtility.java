/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id$
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** 
 * TODO Describe this class
 * 
 * @author  $Author: hto$ 
 * @version $Revision$ 
 */
public class CSVParserUtility {
    private final BufferedReader br;

    private boolean hasNext = true;

    private final char separator;

    private final char quotechar;

    /** The default separator to use if none is supplied to the constructor. */
    public static final char DEFAULT_SEPARATOR = ';';

    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    public static final char DEFAULT_QUOTE_CHARACTER = '"';

    /**
     * Constructs CSVReader using a semi-comma for the separator.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     */
    public CSVParserUtility(StringReader reader) {
        this(reader, DEFAULT_SEPARATOR);
    }

    /**
     * Constructs CSVReader with supplied separator.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries.
     */
    public CSVParserUtility(StringReader reader, char separator) {
        this(reader, separator, DEFAULT_QUOTE_CHARACTER);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     */
    public CSVParserUtility(StringReader reader, char separator, char quotechar) {
        this.br = new BufferedReader(reader);
        this.separator = separator;
        this.quotechar = quotechar;
    }

    /**
     * Reads the entire file into a List with each element being a String[] of
     * tokens.
     * 
     * @return a List of String[], with each String[] representing a line of the
     *         file.
     * 
     * @throws IOException
     *             if bad things happen during the read
     */
    public List<String[]> readAll() throws IOException {

        List<String[]> allElements = new ArrayList<String[]>();
        while (hasNext) {
            String[] nextLineAsTokens = readNext();
            if (nextLineAsTokens != null)
                allElements.add(nextLineAsTokens);
        }
        return allElements;

    }

    /**
     * Reads the next line from the buffer and converts to a string array.
     * 
     * @return a string array with each comma-separated element as a separate
     *         entry.
     * 
     * @throws IOException
     *             if bad things happen during the read
     */
    public String[] readNext() throws IOException {

        String nextLine = getNextLine();
        return hasNext ? parseLine(nextLine) : null;
    }

    /**
     * Reads the next line from the file.
     * 
     * @return the next line from the file without trailing newline
     * @throws IOException
     *             if bad things happen during the read
     */
    private String getNextLine() throws IOException {
        String nextLine = br.readLine();
        if (nextLine == null) {
            hasNext = false;
        }
        return hasNext ? nextLine : null;
    }

    /**
     * Parses an incoming String and returns an array of elements.
     * 
     * @param nextLine
     *            the string to parse
     * @return the comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException if bad things happen during the read
     */
    private String[] parseLine(String nextLine) throws IOException {

        if (nextLine == null) {
            return null;
        }

        List<String> tokensOnThisLine = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        do {
            if (inQuotes) {
                // continuing a quoted section, reappend newline
                sb.append("\n");
                nextLine = getNextLine();
                if (nextLine == null)
                    break;
            }
            for (int i = 0; i < nextLine.length(); i++) {

                char c = nextLine.charAt(i);
                if (c == quotechar) {
                    // this gets complex... the quote may end a quoted block, or escape another quote.
                    // do a 1-char lookahead:
                    if (inQuotes // we are in quotes, therefore there can be escaped quotes in here.
                            && nextLine.length() > (i + 1) // there is indeed another character to check.
                            && nextLine.charAt(i + 1) == quotechar) { // ..and that char. is a quote also.
                        // we have two quote chars in a row == one quote char, so consume them both and
                        // put one on the token. we do *not* exit the quoted text.
                        sb.append(nextLine.charAt(i + 1));
                        i++;
                    } else {
                        inQuotes = !inQuotes;
                        // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                        if (i > 2 //not on the begining of the line
                                && nextLine.charAt(i - 1) != this.separator //not at the begining of an escape sequence 
                                && nextLine.length() > (i + 1) && nextLine.charAt(i + 1) != this.separator //not at the	end of an escape sequence
                        ) {
                            sb.append(c);
                        }
                    }
                } else if (c == separator && !inQuotes) {
                    tokensOnThisLine.add(sb.toString());
                    sb = new StringBuffer(); // start work on next token
                } else {
                    sb.append(c);
                }
            }
        } while (inQuotes);
        tokensOnThisLine.add(sb.toString());
        return (String[]) tokensOnThisLine.toArray(new String[0]);

    }

    /**
     * Closes the underlying reader.
     * 
     * @throws IOException if the close fails
     */
    public void close() throws IOException {
        br.close();
    }

    private static final int TYPE = 0;
    private static final String PERSON = "P";
    private static final String CONTRACT = "C";
    private static final String ONGOING_EDUCATION = "L";
    private static final String EXAM = "E";
    private static final String CANCELLATION = "A";
    private static final int PERS_ID = 1; // for all types of entry

    public HashMap<Long, PersonData> parseSbgCsvBody() throws IOException {
        List<String[]> persons = new ArrayList<String[]>();
        List<String[]> contracts = new ArrayList<String[]>();
        List<String[]> ongoingEducations = new ArrayList<String[]>();
        List<String[]> exams = new ArrayList<String[]>();
        List<String[]> cancellations = new ArrayList<String[]>();

        HashMap<Long, PersonData> deliveryData = new HashMap<Long, PersonData>();

        String[] nextLine;
        PersonData personData;

        while ((nextLine = readNext()) != null && nextLine.length != 0) {
            // nextLine[] is an array of values from the line

            String typeChar = nextLine[TYPE];
            // check person
            if (typeChar.trim().equals(PERSON)) {
                Long persId = getPersonId(nextLine, PERS_ID);
                personData = getOrCreatePersonData(deliveryData, persId);
                personData.addPerson(nextLine);
                persons.add(nextLine);
            }
            //check contract
            if (typeChar.trim().equals(CONTRACT)) {
                Long persId = getPersonId(nextLine, PERS_ID);
                personData = getOrCreatePersonData(deliveryData, persId);
                personData.addPersContract(nextLine);
                contracts.add(nextLine);
            }
            //check ongoing_education
            if (typeChar.trim().equals(ONGOING_EDUCATION)) {
                Long persId = getPersonId(nextLine, PERS_ID);
                personData = getOrCreatePersonData(deliveryData, persId);
                personData.addPersEducation(nextLine);
                ongoingEducations.add(nextLine);
            }
            //check exam
            if (typeChar.trim().equals(EXAM)) {
                Long persId = getPersonId(nextLine, PERS_ID);
                personData = getOrCreatePersonData(deliveryData, persId);
                personData.addPersExam(nextLine);
                exams.add(nextLine);
            }
            //check cancellation
            if (typeChar.trim().equals(CANCELLATION)) {
                Long persId = getPersonId(nextLine, PERS_ID);
                personData = getOrCreatePersonData(deliveryData, persId);
                personData.addPersCancellation(nextLine);
                cancellations.add(nextLine);
            }
        }

        return deliveryData;
    }

    private Long getPersonId(String[] nextLine, int position) {
        Long persId = PersonData.DUMMY_PERS_ID;
        if (nextLine.length > position) {
            try {
                persId = new Long(nextLine[position]);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return persId;
    }

    private PersonData getOrCreatePersonData(HashMap<Long, PersonData> deliveryData, Long persId) {
        PersonData personData = deliveryData.get(persId);
        if (personData == null) {
            personData = new PersonData();
            personData.setPersId(persId);
            deliveryData.put(persId, personData);
        }
        return personData;
    }

}