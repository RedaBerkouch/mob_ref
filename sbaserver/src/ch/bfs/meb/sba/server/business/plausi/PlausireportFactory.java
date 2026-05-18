package ch.bfs.meb.sba.server.business.plausi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.bfs.meb.sba.server.integration.dto.SbaCanton;
import ch.bfs.meb.sba.server.integration.dto.SbaDelivery;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.repository.IPersonRepository;
import ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.sba.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.server.commons.service.impl.PlausiReportOptions;
import ch.bfs.meb.server.commons.service.impl.PlausireportFactoryBase;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Factory for of creating plausi reports. Plausireports of all supported languages are stored separately in an action.
 * Creation of a plausi reports is based on an Excel Template (different templates for different languages).
 * Additional languages as well as changes in the templates have to be considered and updated explicitly in the code of this class.
 */
public class PlausireportFactory extends PlausireportFactoryBase {

    // Detail Sheet - Specific constants
    private final static int DELIVERY_NOF_ERROR_COLUMNS = 7;
    private final static int DELIVERY_ERROR_NAME_COLUMN = 1;
    private final static int DELIVERY_ERROR_OBJECTTYPE_COLUMN = 2;
    private final static int DELIVERY_ERROR_SCHOOLLABEL_COLUMN = 3;
    private final static int DELIVERY_ERROR_PERSONLABEL_COLUMN = 4;
    private final static int DELIVERY_ERROR_QUALIFICATIONLABEL_COLUMN = 5;
    private final static int DELIVERY_ERROR_MESSAGE_COLUMN = 6;
    private final static int DELIVERY_ERROR_ORIGINTEXT_COLUMN = 7;

    private final static int CANTON_NOF_ERROR_COLUMNS = 6;
    private final static int CANTON_ERROR_NAME_COLUMN = 1;
    private final static int CANTON_ERROR_OBJECTTYPE_COLUMN = 2;
    private final static int CANTON_ERROR_PERSONLABEL_COLUMN = 3;
    private final static int CANTON_ERROR_QUALIFICATIONLABEL_COLUMN = 4;
    private final static int CANTON_ERROR_MESSAGE_COLUMN = 5;
    private final static int CANTON_ERROR_ORIGINTEXT_COLUMN = 6;

    // Detail Sheet DL - Specific constants
    private final static int DL_NOF_ERROR_COLUMNS = 7;
    private final static int DL_ERROR_CANTON_COLUMN = 1;
    private final static int DL_ERROR_SCHOOLLABEL_COLUMN = 2;
    private final static int DL_ERROR_PERSONLABEL_COLUMN = 3;
    private final static int DL_ERROR_QUALIFICATIONLABEL_COLUMN = 4;
    private final static int DL_ERROR_MESSAGE_COLUMN = 5;
    private final static int DL_ERROR_NAME_COLUMN = 6;
    private final static int DL_ERROR_CONFIRM_COLUMN = 7;

    // Spring injection
    private IPlausiErrorRepository _plausiErrorRepository;
    private IPlausiRepository _plausiRepository;
    private IPersonRepository _personRepository;

    public void setPlausiErrorRepository(IPlausiErrorRepository plausiErrorRepository) {
        _plausiErrorRepository = plausiErrorRepository;
    }

    public void setPlausiRepository(IPlausiRepository plausiRepository) {
        _plausiRepository = plausiRepository;
    }

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    /**
     * Creates canton plausireport (Excel-File) as byte array for all languages
     * @return				HashMap containing language dependent plausireports with Key = Locale.GERMAN, Locale.FRENCH and ITALIAN
     */
    public HashMap<Locale, byte[]> create(SbaCanton canton) throws IOException {

        // calculates the sheet number for the canton report
        PlausiReportOptions cantonOptions = new PlausiReportOptions();
        cantonOptions.calculateSheetsForCantonAndDelivery(true);

        // Get all plausi data from database and build list and HashMap of relevant plausis
        List<SbaPlausi> allPlausiList = _plausiRepository.getPlausis();
        List<SbaPlausi> plausiList = new ArrayList<>();
        Long version = canton.getVersion();
        for (SbaPlausi plausi : allPlausiList) {
            if (plausi.getObjectLevel().equals(CodegroupUtility.SBA_OBJECTTYPE_CANTON) && plausi.getIsActive()
                    && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)) {
                plausiList.add(plausi);
            }
        }

        // Get all plausi errors
        List<SbaPlausiError> plausiErrors = _plausiErrorRepository.getPlausiErrorsForCanton(canton.getCantonId());
        Collections.sort(plausiErrors, new Comparator<SbaPlausiError>() {
            public int compare(SbaPlausiError error1, SbaPlausiError error2) {
                SbaPlausi plausi1 = error1.getPlausi();
                SbaPlausi plausi2 = error2.getPlausi();
                long order1 = (plausi1 != null && plausi1.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi1.getPlausiOrder();
                long order2 = (plausi2 != null && plausi2.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi2.getPlausiOrder();
                return order1 < order2 ? -1 : order1 > order2 ? 1 : 0;
            }
        });
        Map<Long, Long> plausiErrorNumbers = _plausiErrorRepository.getNumberOfPlausiErrorsForCanton(canton.getCantonId());
        Long numberOfPersons = _personRepository.getNumberOfPersonsForCanton(canton.getCanton(), canton.getVersion());

        HashMap<Locale, byte[]> plausireports = new HashMap<>();
        // Create plausi report for Locale.GERMAN
        InputStream inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_DE);
        byte[] plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, canton.getCanton(), canton.getVersion(), null, numberOfPersons,
                Locale.GERMAN.getLanguage(), cantonOptions);
        plausireports.put(Locale.GERMAN, plausireport);
        // Create plausi report for Locale.FRENCH
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_FR);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, canton.getCanton(), canton.getVersion(), null, numberOfPersons,
                Locale.FRENCH.getLanguage(), cantonOptions);
        plausireports.put(Locale.FRENCH, plausireport);
        // Create plausi report for Locale.ITALIAN
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_IT);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, canton.getCanton(), canton.getVersion(), null, numberOfPersons,
                Locale.ITALIAN.getLanguage(), cantonOptions);
        plausireports.put(Locale.ITALIAN, plausireport);

        return plausireports;
    }

    /**
     * Creates delivery plausireport (Excel-File) as byte array for all languages
     * 
     * @param delivery 		delivery to create the report for
     * @return				HashMap containing language dependent plausireports with Key = Locale.GERMAN, Locale.FRENCH and ITALIAN
     */
    public HashMap<Locale, byte[]> create(SbaDelivery delivery) throws IOException {

        // calculates the sheet number for the delivery report
        PlausiReportOptions deliveryOptions = new PlausiReportOptions();
        deliveryOptions.calculateSheetsForCantonAndDelivery(false);
        // Get all plausi data from database and build list and HashMap of relevant plausis
        List<SbaPlausi> allPlausiList = _plausiRepository.getPlausis();
        List<SbaPlausi> plausiList = new ArrayList<>();
        Long version = delivery.getVersion();
        for (SbaPlausi plausi : allPlausiList) {
            if (plausi.getObjectLevel() >= CodegroupUtility.SBA_OBJECTTYPE_DELIVERY && plausi.getIsActive()
                    && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)
                    && !existsIdenticalLowerLevelRule(allPlausiList, plausi)) {
                plausiList.add(plausi);
            }
        }

        // Get all plausi errors
        List<SbaPlausiError> plausiErrors = _plausiErrorRepository.getPlausiErrorsForDelivery(delivery.getDeliveryId());
        Collections.sort(plausiErrors, new Comparator<SbaPlausiError>() {
            public int compare(SbaPlausiError error1, SbaPlausiError error2) {
                SbaPlausi plausi1 = error1.getPlausi();
                SbaPlausi plausi2 = error2.getPlausi();
                long order1 = (plausi1 != null && plausi1.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi1.getPlausiOrder();
                long order2 = (plausi2 != null && plausi2.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi2.getPlausiOrder();
                return order1 < order2 ? -1 : order1 > order2 ? 1 : 0;
            }
        });
        Map<Long, Long> plausiErrorNumbers = _plausiErrorRepository.getNumberOfPlausiErrorsForDelivery(delivery.getDeliveryId());

        Long numberOfPersons = _personRepository.getNumberOfPersonsForDelivery(delivery.getDeliveryId());

        HashMap<Locale, byte[]> plausireports = new HashMap<>();
        // Create plausi report for Locale.GERMAN
        InputStream inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_DELIVERY_TEMPLATE_DE);
        byte[] plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, delivery.getCanton(), delivery.getVersion(), delivery.getDeliveryCode(),
                numberOfPersons, Locale.GERMAN.getLanguage(), deliveryOptions);
        plausireports.put(Locale.GERMAN, plausireport);
        // Create plausi report for Locale.FRENCH
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_DELIVERY_TEMPLATE_FR);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, delivery.getCanton(), delivery.getVersion(), delivery.getDeliveryCode(),
                numberOfPersons, Locale.FRENCH.getLanguage(), deliveryOptions);
        plausireports.put(Locale.FRENCH, plausireport);
        // Create plausi report for Locale.ITALIAN
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_DELIVERY_TEMPLATE_IT);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, delivery.getCanton(), delivery.getVersion(), delivery.getDeliveryCode(),
                numberOfPersons, Locale.ITALIAN.getLanguage(), deliveryOptions);
        plausireports.put(Locale.ITALIAN, plausireport);

        return plausireports;
    }

    /**
     * Creates dl user interface plausireport based on a list of deliveries
     * 
     * @param deliveries    deliveries to create the report for
     * @param locale        language of the report
     * @return              plausireport data
     */
    public byte[] create(List<SbaDelivery> deliveries, String locale) throws IOException {
        locale = locale.toLowerCase();

        // Get all plausi data from database and build list and HashMap of relevant plausis
        List<SbaPlausi> allPlausiList = _plausiRepository.getPlausis();
        List<SbaPlausi> plausiList = new ArrayList<>();
        Long version = deliveries.get(0).getVersion();
        for (SbaPlausi plausi : allPlausiList) {
            if (plausi.getObjectLevel() >= CodegroupUtility.SDL_OBJECTTYPE_DELIVERY && plausi.getIsActive()
                    && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)
                    && !existsIdenticalLowerLevelRule(allPlausiList, plausi)) {
                plausiList.add(plausi);
            }
        }

        // Get all plausi errors
        HashMap<Long, Long> cantonsForDelivery = new HashMap<>();
        List<SbaPlausiError> plausiErrors = new ArrayList<>();
        for (SbaDelivery delivery : deliveries) {
            plausiErrors.addAll(_plausiErrorRepository.getPlausiErrorsForDelivery(delivery.getDeliveryId()));
            cantonsForDelivery.put(delivery.getDeliveryId(), delivery.getCanton());
        }
        Collections.sort(plausiErrors, new Comparator<SbaPlausiError>() {
            public int compare(SbaPlausiError error1, SbaPlausiError error2) {
                SbaPlausi plausi1 = error1.getPlausi();
                SbaPlausi plausi2 = error2.getPlausi();
                long order1 = (plausi1 != null && plausi1.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi1.getPlausiOrder();
                long order2 = (plausi2 != null && plausi2.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi2.getPlausiOrder();
                return order1 < order2 ? -1 : order1 > order2 ? 1 : 0;
            }
        });

        InputStream inp;
        if (Locale.ITALIAN.getLanguage().equals(locale)) {
            inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_DL_IT);
        } else if (Locale.FRENCH.getLanguage().equals(locale)) {
            inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_DL_FR);
        } else {
            inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_DL_DE);
        }

        // Create excel workbook
        XSSFWorkbook plausireport = new XSSFWorkbook(inp);
        writeErrorDetailsDl(plausireport, plausiErrors, cantonsForDelivery, locale);

        // Convert to byte[]
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        plausireport.write(bos);
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Creates the language dependent Plausireport (Excel-File) as byte array.
     * 
     * @param inputStream Excel file template, created from language dependent template.
     * @param plausis list of all Plausi rules for SBA
     * @param plausiErrors list of all relevant plausi errors
     * @param plausiErrorNumbers Map with key = plausiId, value = number of errors
     * @param locale language of plausi report
     * @param options PlausiReportOptions object with parameters for the plausi report
     * @return plausireport (Excel-File) as byte array
     */
    private byte[] create(InputStream inputStream, List<SbaPlausi> plausis, List<SbaPlausiError> plausiErrors, Map<Long, Long> plausiErrorNumbers, Long canton,
            Long version, String deliveryCode, Long nrOfPersons, String locale, PlausiReportOptions options) throws IOException {
        // Create excel workbook
        XSSFWorkbook plausireport = new XSSFWorkbook(inputStream);

        if (deliveryCode == null) { // overwrite sheet titles for plausireport of canton
            writeSheetTitles(plausireport, locale, true, options);
        }

        // Write the tabs in the excel file	
        writeTitleSection(plausireport, canton, version, deliveryCode, nrOfPersons, locale);
        writeErrorOverview(plausireport, plausis, plausiErrors, plausiErrorNumbers, locale, options);

        if (options.isCantonReport()) {
            writeHistoryDiagramData(plausireport, plausiErrors, locale, options);
        }

        writeErrorDetails(plausireport, plausiErrors, locale, options);

        // Convert to byte[]
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        plausireport.write(bos);
        bos.close();
        return bos.toByteArray();
    }

    /** Writes title section (first sheet of report). */
    private void writeTitleSection(XSSFWorkbook plausireport, Long canton, Long version, String deliveryCode, Long nrOfPersons, String locale) {
        XSSFSheet titleSheet = plausireport.getSheetAt(TITLE_SHEET);

        // write current Date
        GregorianCalendar cal = new GregorianCalendar();
        XSSFRow curRow = titleSheet.getRow(DATE_ROW);
        XSSFCell curCell = curRow.getCell(DATE_COLUMN);
        curCell.setCellValue(formatDateTime(cal.getTime()));

        // write canton
        curRow = titleSheet.getRow(CANTON_ROW);
        curCell = curRow.getCell(CANTON_COLUMN);
        List<CodeGroup> cantonCodes = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.CANTON, locale);
        curCell.setCellValue(getCodeText(cantonCodes, canton));

        // write version
        curRow = titleSheet.getRow(VERSION_ROW);
        curCell = curRow.getCell(VERSION_COLUMN);
        curCell.setCellValue(version);

        // write deliveryCode
        curRow = titleSheet.getRow(DELIVERY_ROW);
        if (deliveryCode == null) // plausireport for canton
        {
            curCell = curRow.getCell(DELIVERY_TITLE_COLUMN);
            curCell.setCellValue((String) null);
        } else {
            curCell = curRow.getCell(DELIVERY_COLUMN);
            curCell.setCellValue(deliveryCode);
        }

        // write number of learners	
        curRow = titleSheet.getRow(NOF_PERSONS_ROW);
        curCell = curRow.getCell(NOF_PERSONS_COLUMN);
        curCell.setCellValue(nrOfPersons);
    }

    /**
     * Writes overview of errors per plausi-macro (first sheet of report).
     */
    private void writeErrorOverview(XSSFWorkbook plausireport, List<SbaPlausi> plausis, List<SbaPlausiError> plausiErrors, Map<Long, Long> plausiErrorNumbers,
            String locale, PlausiReportOptions option) {
        XSSFSheet overviewSheet = plausireport.getSheetAt(option.getOverviewSheetNumber());
        XSSFRow curRow;
        XSSFCell curCell;

        int macroRow = FIRST_MACRO_ROW;
        for (SbaPlausi plausi : plausis) {
            if (macroRow > FIRST_MACRO_ROW + NOF_MACRO_ROWS) {
                appendRow(overviewSheet, macroRow, NOF_MACRO_COLUMNS);
            }

            curRow = overviewSheet.getRow(macroRow);

            // Get number of errors for the current plausi
            Long nofErrorsForPlausi = plausiErrorNumbers.get(plausi.getPlausiId()) != null ? (Long) plausiErrorNumbers.get(plausi.getPlausiId()) : 0;

            curCell = curRow.getCell(MACRO_ERRORS_COLUMN);
            curCell.setCellValue(nofErrorsForPlausi);
            curCell = curRow.getCell(MACRO_NAME_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_it());
            }

            curCell = curRow.getCell(MACRO_DESCRIPTION_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getDescription_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getDescription_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getDescription_fr());
            }

            macroRow++;
        }
    }

    /**
     * Writes all errors in detail (third sheet of report).
     */
    private void writeErrorDetails(XSSFWorkbook plausireport, List<SbaPlausiError> somePlausiErrors, String locale, PlausiReportOptions options) {
        XSSFSheet detailSheet = plausireport.getSheetAt(options.getDetailSheetNumber());
        XSSFRow currentRow;
        XSSFCell currentCell;

        List<CodeGroup> objectTypes = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.SBA_OBJECTTYPE, locale);

        int nofErrorColumns = DELIVERY_NOF_ERROR_COLUMNS;
        int nameColumn = DELIVERY_ERROR_NAME_COLUMN;
        int objecttypeColumn = DELIVERY_ERROR_OBJECTTYPE_COLUMN;
        int personlabelColumn = DELIVERY_ERROR_PERSONLABEL_COLUMN;
        int qualificationlabelColumn = DELIVERY_ERROR_QUALIFICATIONLABEL_COLUMN;
        int messageColumn = DELIVERY_ERROR_MESSAGE_COLUMN;
        int origintextColumn = DELIVERY_ERROR_ORIGINTEXT_COLUMN;
        if (options.isCantonReport()) {
            nofErrorColumns = CANTON_NOF_ERROR_COLUMNS;
            nameColumn = CANTON_ERROR_NAME_COLUMN;
            objecttypeColumn = CANTON_ERROR_OBJECTTYPE_COLUMN;
            personlabelColumn = CANTON_ERROR_PERSONLABEL_COLUMN;
            qualificationlabelColumn = CANTON_ERROR_QUALIFICATIONLABEL_COLUMN;
            messageColumn = CANTON_ERROR_MESSAGE_COLUMN;
            origintextColumn = CANTON_ERROR_ORIGINTEXT_COLUMN;
        }

        int errorRow = FIRST_ERROR_ROW;
        for (SbaPlausiError plausiError : somePlausiErrors) {
            if (errorRow > FIRST_ERROR_ROW + NOF_ERROR_ROWS) {
                appendRow(detailSheet, errorRow, nofErrorColumns);
            }

            currentRow = detailSheet.getRow(errorRow);
            currentRow.setHeightInPoints(ERROR_DETAILS_ROW_HEIGHT);

            if (errorRow == FIRST_ERROR_ROW + MAX_ERRORS) {
                // Insert last pseudo row into plausierror details list
                String[] parameterList = { String.valueOf(MAX_ERRORS) };
                String description = _localizationManager.getMessageByLanguage(TOO_MANY_ERRORS, locale);
                description = MessageFormat.format(description, (Object[]) parameterList);
                currentCell = currentRow.getCell(messageColumn);
                currentCell.setCellValue(description);
                break;
            }

            // Write name and type of Macro
            SbaPlausi plausi = plausiError.getPlausi();
            currentCell = currentRow.getCell(nameColumn);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                currentCell.setCellValue(plausi.getName_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                currentCell.setCellValue(plausi.getName_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                currentCell.setCellValue(plausi.getName_it());
            }

            currentCell = currentRow.getCell(objecttypeColumn);
            long objectLevel = CodegroupUtility.SBA_OBJECTTYPE_CANTON;
            if (plausiError.getQualificationId() != null) {
                objectLevel = CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION;
            } else if (plausiError.getPersonId() != null) {
                objectLevel = CodegroupUtility.SBA_OBJECTTYPE_PERSON;
            } else if (plausiError.getDeliveryId() != null) {
                objectLevel = CodegroupUtility.SBA_OBJECTTYPE_DELIVERY;
            }
            currentCell.setCellValue(getCodeText(objectTypes, objectLevel));

            // Write error message
            currentCell = currentRow.getCell(messageColumn);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                currentCell.setCellValue(plausiError.getErrorMsg_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                currentCell.setCellValue(plausiError.getErrorMsg_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                currentCell.setCellValue(plausiError.getErrorMsg_it());
            }

            // fill additional data (identification)
            if (!options.isCantonReport()) {
                currentCell = currentRow.getCell(DELIVERY_ERROR_SCHOOLLABEL_COLUMN);
                currentCell.setCellValue(plausiError.getSchoolLabel());
            }
            currentCell = currentRow.getCell(personlabelColumn);
            currentCell.setCellValue(plausiError.getPersonLabel());
            currentCell = currentRow.getCell(qualificationlabelColumn);
            currentCell.setCellValue(plausiError.getQualificationLabel());

            currentCell = currentRow.getCell(origintextColumn);
            currentCell.setCellValue(plausiError.getPersonOrigDeliveryData());

            errorRow++;
        }
    }

    /**
     * Writes all errors in detail for dl plausireport
     */
    private void writeErrorDetailsDl(XSSFWorkbook plausireport, List<SbaPlausiError> somePlausiErrors, HashMap<Long, Long> cantonsForDelivery, String locale) {
        XSSFSheet detailSheet = plausireport.getSheetAt(DETAIL_SHEET_DL);
        XSSFRow curRow;
        XSSFCell curCell;

        // Get cantons
        List<CodeGroup> cantons = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.CANTON, locale);

        int errorRow = FIRST_ERROR_ROW;
        for (SbaPlausiError error : somePlausiErrors) {
            if (errorRow > FIRST_ERROR_ROW + NOF_ERROR_ROWS) {
                appendRow(detailSheet, errorRow, DL_NOF_ERROR_COLUMNS);
            }

            curRow = detailSheet.getRow(errorRow);
            curRow.setHeightInPoints(ERROR_DETAILS_ROW_HEIGHT);

            if (errorRow == FIRST_ERROR_ROW + MAX_ERRORS) {
                // Insert last pseudo row into plausierror details list
                String[] parameterList = { String.valueOf(MAX_ERRORS) };
                String description = _localizationManager.getMessageByLanguage(TOO_MANY_ERRORS, locale);
                description = MessageFormat.format(description, (Object[]) parameterList);
                curCell = curRow.getCell(DL_ERROR_MESSAGE_COLUMN);
                curCell.setCellValue(description);
                break;
            }

            // Write canton
            curCell = curRow.getCell(DL_ERROR_CANTON_COLUMN);
            curCell.setCellValue(getCodeTextAbbr(cantons, cantonsForDelivery.get(error.getDeliveryId())));

            // Write name and type of Macro
            SbaPlausi plausi = error.getPlausi();
            curCell = curRow.getCell(DL_ERROR_NAME_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_it());
            }

            // Write error message
            curCell = curRow.getCell(DL_ERROR_MESSAGE_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_it());
            }

            // fill additional data (identification)
            curCell = curRow.getCell(DL_ERROR_SCHOOLLABEL_COLUMN);
            curCell.setCellValue(error.getSchoolLabel());
            curCell = curRow.getCell(DL_ERROR_PERSONLABEL_COLUMN);
            curCell.setCellValue(error.getPersonLabel());
            curCell = curRow.getCell(DL_ERROR_QUALIFICATIONLABEL_COLUMN);
            curCell.setCellValue(error.getQualificationLabel());

            // Write error confirm information
            if (plausi.getIsConfirmable()) {
                curCell = curRow.getCell(DL_ERROR_CONFIRM_COLUMN);
                String confirmable = _localizationManager.getMessageByLanguage(IS_CONFIRMABLE, locale);
                curCell.setCellValue(confirmable);
            }

            errorRow++;
        }
    }
}
