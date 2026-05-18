package ch.admin.bfs.sbg.business.plausi;

import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;


import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PersonDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.util.CodegroupUtility;
import org.apache.poi.xssf.usermodel.*;

/**
 * Factory for of creating plausi reports. Plausireports of all supported
 * languages are stored separately in an action. Creation of a plausi reports is
 * based on an Excel Template (different templates for different langugages).
 * Additional languages as well as changes in the templates have to be
 * considered and updated explicitly in the code of this class.
 *
 * @author $Author: msc $
 * @version $Revision: 595 $
 */
@SuppressWarnings("deprecation")
public class PlausireportFactory {
    private final static String TOO_MANY_ERRORS = "plausi.report.tooManyErrors";

    private final static String PLAUSIREPORT_TEMPLATE_DE = "/plausireports/Plausibericht_Vorlage_d.xlsx";
    private final static String PLAUSIREPORT_TEMPLATE_FR = "/plausireports/Plausibericht_Vorlage_f.xlsx";

    private final static int TITLE_SHEET = 0;
    private final static int DATE_ROW = 8;
    private final static short DATE_COLUMN = 2;
    private final static int CANTON_ROW = 9;
    private final static short CANTON_COLUMN = 2;
    private final static int VERSION_ROW = 10;
    private final static short VERSION_COLUMN = 2;
    private final static int NOF_PERSONS_ROW = 11;
    private final static short NOF_PERSONS_COLUMN = 2;

    private final static int OVERVIEW_SHEET = 0;
    private final static int FIRST_MACRO_ROW = 14;
    private final static int NOF_MACRO_ROWS = 5;
    private final static int NOF_MACRO_COLUMNS = 3;
    private final static short MACRO_ERRORS_COLUMN = 1;
    private final static short MACRO_NAME_COLUMN = 2;
    private final static short MACRO_DESCRIPTION_COLUMN = 3;

    private final static int MAX_DIAGRAMS = 7;
    private final static String DIAGRAM_ZEITREIHE = "ZEITREIHENGRAFIK:";
    private final static String DIAGRAM_TITLE_SEPARATOR = ";";
    private final static String DIAGRAM_VALUES_START = "[";
    private final static String DIAGRAM_VALUES_END = "]";
    private final static String DIAGRAM_VALUE_SEPARATOR = ",";

    private final static int DIAGRAM_FIRST_TITLE_ROW = 10;
    private final static int DIAGRAM_FIRST_YEAR_ROW = 13;
    private final static int DIAGRAM_ROWS_BETWEEN = 12;
    private final static int DIAGRAM_MAX_YEARS = 7;
    private final static short DIAGRAM_TITLE_COLUMN = 1;
    private final static short DIAGRAM_YEAR_COLUMN = 1;
    private final static short DIAGRAM_MIN_COLUMN = 2;
    private final static short DIAGRAM_VALUE_COLUMN = 3;
    private final static short DIAGRAM_MAX_COLUMN = 4;

    private final static int MAX_ERRORS = 10000;
    private final static int DETAIL_SHEET = 1;
    private final static int FIRST_ERROR_ROW = 9;
    private final static int NOF_ERROR_ROWS = 10;
    private final static int NOF_ERROR_COLUMNS = 6;
    private final static short ERROR_NAME_COLUMN = 1;
    private final static short ERROR_OBJECTTYPE_COLUMN = 2;
    private final static short ERROR_PERSONID_COLUMN = 3;
    private final static short ERROR_CONTRACTID_COLUMN = 4;
    private final static short ERROR_MESSAGE_COLUMN = 5;
    private final static short ERROR_ORIGINTEXT_COLUMN = 6;

    private static final SimpleDateFormat MEB_DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    static {
        MEB_DATE_TIME_FORMAT.setLenient(false);
    }

    // Singleton
    private final static PlausireportFactory instance = new PlausireportFactory();

    private PlausireportFactory() {}

    /**
     * Avoiding duplicates of rules defined on level delivery and a lower level
     */
    protected boolean existsIdenticalLowerLevelRule(List<? extends Macro> allPlausiList, Macro plausi) {
        for (Macro p : allPlausiList) {
            // TODO lsc: instead of name_de take business id from plausi
            if (p.getName_de() != null && plausi.getName_de() != null && p.getName_de().equals(plausi.getName_de())
                    && p.getObjecttype() > plausi.getObjecttype() && p.getIsactive() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates and returns the language dependent plausireport (Excel-File) as
     * byte array
     *
     * @param aDelivery delivery to create the report for
     * @param locale    language of plausi report
     * @return plausireport (Excel-File) as byte array
     * @throws IOException
     */
    public byte[] create(ICodegroupManager codegroupManager, MacroDAO macroDAO, PlausierrorDAO plausierrorDAO, PersonDAO personDAO, DeliveryBO aDeliveryBo,
            String aLocale, IServerLocalizationManager localizationManager) throws IOException {
        String mainLocale;
        XSSFWorkbook plausireport;
        if (aLocale.startsWith(Locale.GERMAN.getLanguage())) {
            plausireport = getplausiReportWorkbook(PLAUSIREPORT_TEMPLATE_DE);
            mainLocale = Locale.GERMAN.getLanguage();
        } else if (aLocale.startsWith(Locale.FRENCH.getLanguage())) {
            plausireport = getplausiReportWorkbook(PLAUSIREPORT_TEMPLATE_FR);
            mainLocale = Locale.FRENCH.getLanguage();
        } else {
            throw new RuntimeException("Unknown language for plausireport");
        }


        final List<Macro> allActivePlausiMacros = macroDAO.findAllPlausis(true);
        final List<Macro> allPlausiMacros = macroDAO.findAllPlausis(false);
        final List<Macro> plausiMacros = new ArrayList<Macro>();
        for (Macro macro : allActivePlausiMacros) {
            if (!existsIdenticalLowerLevelRule(allActivePlausiMacros, macro)) {
                plausiMacros.add(macro);
            }
        }

        List<Plausierror> plausiErrors = plausierrorDAO.getPlausiErrorsForDelivery(aDeliveryBo.get_thisDelivery().getDeliveryid());

        Collections.sort(plausiErrors, new Comparator<Plausierror>() {
            public int compare(Plausierror error1, Plausierror error2) {
                Macro macro1 = findMacro(error1.getPlausiId(), allPlausiMacros);
                Macro macro2 = findMacro(error2.getPlausiId(), allPlausiMacros);
                long o1 = macro1.getOrder() == null ? Long.MAX_VALUE : macro1.getOrder();
                long o2 = macro2.getOrder() == null ? Long.MAX_VALUE : macro2.getOrder();
                return o1 < o2 ? -1 : o1 > o2 ? 1 : 0;
            }
        });

        plausireport = create(codegroupManager, personDAO, plausireport, aDeliveryBo, allPlausiMacros, plausiMacros, plausiErrors, mainLocale,
                localizationManager);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        plausireport.write(bos);
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Returns the plausireport XSSFWorkbook for the given template
     * @Return XSSFWorkbook plausireport;
     */
    private XSSFWorkbook getplausiReportWorkbook(String plausiReportTemplate) throws IOException {
        XSSFWorkbook plausireport;
        try (InputStream inp = getClass().getClassLoader().getResourceAsStream(plausiReportTemplate);) {
            plausireport = new XSSFWorkbook(inp);
        }
        return plausireport;
    }

    /**
     * Creates the plausi report based for the given locale.
     */
    private XSSFWorkbook create(ICodegroupManager codegroupManager, PersonDAO personDAO, XSSFWorkbook aPlausireport, DeliveryBO aDeliveryBo,
            List<Macro> allPlausiMacros, List<Macro> somePlausiMacros, List<Plausierror> somePlausiErrors, String locale,
            IServerLocalizationManager localizationManager) {
        writeTitleSection(codegroupManager, personDAO, aPlausireport, aDeliveryBo, locale);
        writeErrorOverview(aPlausireport, aDeliveryBo, somePlausiMacros, somePlausiErrors, locale);

        // writeHistoryDiagramData(aPlausireport, aDeliveryBo, somePlausiMacros, somePlausiErrors, locale);

        writeErrorDetails(codegroupManager, aPlausireport, aDeliveryBo, allPlausiMacros, somePlausiErrors, locale, localizationManager);

        return aPlausireport;
    }

    /**
     * Writes title section (first sheet of report).
     */
    private void writeTitleSection(ICodegroupManager codegroupManager, PersonDAO personDAO, XSSFWorkbook plausireport, DeliveryBO aDeliveryBo, String locale) {
        XSSFSheet titleSheet = plausireport.getSheetAt(TITLE_SHEET);
        SbgDelivery delivery = aDeliveryBo.get_thisDelivery();

        // write current Date
        GregorianCalendar cal = new GregorianCalendar();
        XSSFRow curRow = titleSheet.getRow(DATE_ROW);
        XSSFCell curCell = curRow.getCell(DATE_COLUMN);
        curCell.setCellValue(((SimpleDateFormat) MEB_DATE_TIME_FORMAT.clone()).format(cal.getTime()));

        // write canton
        curRow = titleSheet.getRow(CANTON_ROW);
        curCell = curRow.getCell(CANTON_COLUMN);
        curCell.setCellValue(codegroupManager.getCode(CodegroupUtility.CANTON, delivery.getCanton(), locale, delivery.getVersion()).getCodeText());

        // write version
        curRow = titleSheet.getRow(VERSION_ROW);
        curCell = curRow.getCell(VERSION_COLUMN);
        curCell.setCellValue(delivery.getVersion());

        // write number of persons
        //		long nofPersons = aDeliveryBo.get_persons().size();
        long nofPersons = personDAO.getNofPersons(aDeliveryBo.get_thisDelivery().getDeliveryid(), false);
        curRow = titleSheet.getRow(NOF_PERSONS_ROW);
        curCell = curRow.getCell(NOF_PERSONS_COLUMN);
        curCell.setCellValue(nofPersons);
    }

    /**
     * Writes overview of errors per plausi-macro (first sheet of report).
     */
    private void writeErrorOverview(XSSFWorkbook plausireport, DeliveryBO aDeliveryBo, List<Macro> somePlausiMacros, List<Plausierror> somePlausiErrors,
            String locale) {
        XSSFSheet overviewSheet = plausireport.getSheetAt(OVERVIEW_SHEET);
        XSSFRow curRow;
        XSSFCell curCell;

        int macroRow = FIRST_MACRO_ROW;
        for (Macro macro : somePlausiMacros) {
            if (macroRow > FIRST_MACRO_ROW + NOF_MACRO_ROWS) {
                appendRow(overviewSheet, macroRow, NOF_MACRO_COLUMNS);
            }

            curRow = overviewSheet.getRow(macroRow);

            // Compute number of errors for the current macro
            int nofErrorsForMacro = 0;
            for (Plausierror pe : somePlausiErrors) {
                if (pe.getPlausiId().equals(macro.getMacroid())) {
                    nofErrorsForMacro++;
                }
            }

            curCell = curRow.getCell(MACRO_ERRORS_COLUMN);
            curCell.setCellValue(nofErrorsForMacro);
            curCell = curRow.getCell(MACRO_NAME_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(macro.getName_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(macro.getName_fr());
            } else {
                throw new RuntimeException("Creating Plausireport: Unsupported language");
            }

            curCell = curRow.getCell(MACRO_DESCRIPTION_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(macro.getDescription_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(macro.getDescription_fr());
            } else {
                throw new RuntimeException("Creating Plausireport: Unsupported language");
            }

            macroRow++;
        }
    }

    /**
     * Converts String to Number and fills the Number into the indicated cell
     */
    private void fillNumberCell(XSSFRow curRow, short column, String number) {
        XSSFCell curCell = curRow.getCell(column);
        try {
            curCell.setCellValue(Integer.valueOf(number.trim()));
        } catch (NumberFormatException e) {
            // Leave empty
        }
    }

    /**
     * Writes all errors in detail (third sheet of report).
     */
    private void writeErrorDetails(ICodegroupManager codegroupManager, XSSFWorkbook plausireport, DeliveryBO aDeliveryBo, List<Macro> somePlausiMacros,
            List<Plausierror> somePlausiErrors, String locale, IServerLocalizationManager localizationManager) {
        XSSFSheet detailSheet = plausireport.getSheetAt(DETAIL_SHEET);
        XSSFRow curRow;
        XSSFCell curCell;

        HashMap<Long, PersonBO> persons = new HashMap<Long, PersonBO>();
        for (PersonBO personBo : aDeliveryBo.get_persons()) {
            persons.put(personBo.get_thisPerson().getPid(), personBo);
        }

        int errorRow = FIRST_ERROR_ROW;
        for (Plausierror error : somePlausiErrors) {
            if (errorRow > FIRST_ERROR_ROW + NOF_ERROR_ROWS) {
                appendRow(detailSheet, errorRow, NOF_ERROR_COLUMNS);
            }

            curRow = detailSheet.getRow(errorRow);

            if (errorRow == FIRST_ERROR_ROW + MAX_ERRORS) {
                String[] parameterList = { String.valueOf(MAX_ERRORS) };
                String description = localizationManager.getMessageByLanguage(TOO_MANY_ERRORS, locale);
                description = MessageFormat.format(description, (Object[]) parameterList);
                curCell = curRow.getCell(ERROR_MESSAGE_COLUMN);
                curCell.setCellValue(description);
                break;
            }

            // Write name and type of Macro
            Macro macro = findMacro(error.getPlausiId(), somePlausiMacros);
            curCell = curRow.getCell(ERROR_NAME_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(macro.getName_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(macro.getName_fr());
            } else {
                throw new RuntimeException("Creating Plausireport: Unsupported language");
            }

            curCell = curRow.getCell(ERROR_OBJECTTYPE_COLUMN);
            SbgDelivery delivery = aDeliveryBo.get_thisDelivery();
            long version = aDeliveryBo.get_thisDelivery().getVersion();
            String value = codegroupManager.getCode(CodegroupUtility.SBG_OBJECTTYPE, macro.getObjecttype(), locale, version).getCodeText();
            curCell.setCellValue(value);

            // Write error message
            curCell = curRow.getCell(ERROR_MESSAGE_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_fr());
            } else {
                throw new RuntimeException("Creating Plausireport: Unsupported language");
            }

            if (error.getPid() != null) {
                PersonBO personBo = persons.get(error.getPid());
                curCell = curRow.getCell(ERROR_PERSONID_COLUMN);
                curCell.setCellValue(personBo.get_idNr());
                curCell = curRow.getCell(ERROR_ORIGINTEXT_COLUMN);
                curCell.setCellValue(personBo.get_thisPerson().getDeliveryText());

                EventBO eventBo = findEventBo(personBo, error.getEventId());
                if (eventBo != null) {
                    curCell = curRow.getCell(ERROR_CONTRACTID_COLUMN);
                    curCell.setCellValue(eventBo.getContractNr());
                }
            }

            errorRow++;
        }
    }

    private Macro findMacro(Long macroId, List<Macro> somePlausiMacros) {
        Macro macro = null;

        for (Macro curMacro : somePlausiMacros) {
            if (curMacro.getMacroid().equals(macroId)) {
                return curMacro;
            }
        }

        return macro;
    }

    private EventBO findEventBo(PersonBO personBo, Long eventId) {
        for (EventBO curEvent : personBo.get_events()) {
            if (curEvent.getThisEvent().getEventid().equals(eventId)) {
                return curEvent;
            }
        }

        return null;
    }

    private void appendRow(XSSFSheet sheet, int index, int nofColumns) {
        // append a row
        XSSFRow lastRow = sheet.getRow(index - 1);
        XSSFRow curRow = sheet.createRow(index);
        XSSFCell curCell;

        for (int i = 1; i <= nofColumns; i++) {
            XSSFCellStyle cs = lastRow.getCell((short) i).getCellStyle();
            curCell = curRow.createCell((short) i);
            curCell.setCellStyle(cs);
        }
    }

    public static PlausireportFactory getInstance() {
        return instance;
    }
}
