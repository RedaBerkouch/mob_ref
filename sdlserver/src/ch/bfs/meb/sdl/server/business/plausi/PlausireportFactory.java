package ch.bfs.meb.sdl.server.business.plausi;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import ch.bfs.meb.sdl.server.integration.dto.SdlCanton;
import ch.bfs.meb.sdl.server.integration.dto.SdlDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.repository.ILearnerRepository;
import ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.sdl.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.server.commons.service.impl.PlausiReportOptions;
import ch.bfs.meb.server.commons.service.impl.PlausireportFactoryBase;
import ch.bfs.meb.util.CodegroupUtility;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Factory for of creating plausi reports. Plausireports of all supported languages are stored 
 * separately in an action.
 * Creation of a plausi reports is based on an Excel Template (different templates for different
 * langugages).
 * Additional languages as well as changes in the templates have to be considered and updated 
 * explicitly in the code of this class.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class PlausireportFactory extends PlausireportFactoryBase {

    // Detail Sheet - Specific constants
    private final static int NOF_ERROR_COLUMNS = 8;
    private final static int ERROR_NAME_COLUMN = 1;
    private final static int ERROR_OBJECTTYPE_COLUMN = 2;
    private final static int ERROR_SCHOOLLABEL_COLUMN = 3;
    private final static int ERROR_CLASSLABEL_COLUMN = 4;
    private final static int ERROR_LEARNERTYPE_COLUMN = 5;
    private final static int ERROR_LEARNERID_COLUMN = 6;
    private final static int ERROR_MESSAGE_COLUMN = 7;
    private final static int ERROR_ORIGINTEXT_COLUMN = 8;

    // Detail Sheet DL - Specific constants
    private final static int NOF_ERROR_COLUMNS_DL = 8;
    private final static int DL_ERROR_CANTON_COLUMN = 1;
    private final static int DL_ERROR_SCHOOLLABEL_COLUMN = 2;
    private final static int DL_ERROR_CLASSLABEL_COLUMN = 3;
    private final static int DL_ERROR_LEARNERTYPE_COLUMN = 4;
    private final static int DL_ERROR_LEARNERID_COLUMN = 5;
    private final static int DL_ERROR_MESSAGE_COLUMN = 6;
    private final static int DL_ERROR_NAME_COLUMN = 7;
    private final static int DL_ERROR_CONFIRM_COLUMN = 8;

    // Spring injection
    private IPlausiErrorRepository _plausiErrorRepository;
    private IPlausiRepository _plausiRepository;
    private ILearnerRepository _learnerRepository;

    public void setPlausiErrorRepository(IPlausiErrorRepository plausiErrorRepository) {
        _plausiErrorRepository = plausiErrorRepository;
    }

    public void setPlausiRepository(IPlausiRepository plausiRepository) {
        _plausiRepository = plausiRepository;
    }

    public void setLearnerRepository(ILearnerRepository learnerRepository) {
        _learnerRepository = learnerRepository;
    }

    /**
     * Creates canton plausireport (Excel-File) as byte array for all languages
     * 
     * @param delivery 		delivery to create the report for
     * @return				HashMap containing language dependent plausireports with Key = Locale.GERMAN, Locale.FRENCH and ITALIAN
     * @throws IOException
     */
    public HashMap<Locale, byte[]> create(SdlCanton canton) throws IOException {

        // Get all plausi data from database and build list and HashMap of relevant plausis
        PlausiReportOptions cantonOptions = new PlausiReportOptions();
        cantonOptions.calculateSheetsForCantonAndDelivery(true);
        List<SdlPlausi> allPlausiList = _plausiRepository.getPlausis();
        List<SdlPlausi> plausiList = new ArrayList<SdlPlausi>();
        Long version = canton.getVersion();
        for (SdlPlausi plausi : allPlausiList) {
            if (plausi.getObjectLevel().equals(CodegroupUtility.SDL_OBJECTTYPE_CANTON) && plausi.getIsActive()
                    && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)) {
                plausiList.add(plausi);
            }
        }

        // Get all plausi errors
        List<SdlPlausiError> plausiErrors = _plausiErrorRepository.getPlausiErrorsForCanton(canton.getCantonId());
        Collections.sort(plausiErrors, new Comparator<SdlPlausiError>() {
            public int compare(SdlPlausiError error1, SdlPlausiError error2) {
                SdlPlausi plausi1 = error1.getPlausi();
                SdlPlausi plausi2 = error2.getPlausi();
                long order1 = (plausi1 != null && plausi1.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi1.getPlausiOrder();
                long order2 = (plausi2 != null && plausi2.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi2.getPlausiOrder();
                return order1 < order2 ? -1 : order1 > order2 ? 1 : 0;
            }
        });
        Map<Long, Long> plausiErrorNumbers = _plausiErrorRepository.getNumberOfPlausiErrorsForCanton(canton.getCantonId());

        Long numberOfLearners = _learnerRepository.getNumberOfLearnersForCanton(canton.getCanton(), canton.getVersion());

        HashMap<Locale, byte[]> plausireports = new HashMap<Locale, byte[]>();
        // Create plausi report for Locale.GERMAN
        InputStream inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_DE);
        byte[] plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, canton.getCanton(), canton.getVersion(), null, numberOfLearners,
                Locale.GERMAN.getLanguage(), cantonOptions);
        plausireports.put(Locale.GERMAN, plausireport);
        // Create plausi report for Locale.FRENCH
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_FR);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, canton.getCanton(), canton.getVersion(), null, numberOfLearners,
                Locale.FRENCH.getLanguage(), cantonOptions);
        plausireports.put(Locale.FRENCH, plausireport);
        // Create plausi report for Locale.ITALIAN
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_TEMPLATE_IT);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, canton.getCanton(), canton.getVersion(), null, numberOfLearners,
                Locale.ITALIAN.getLanguage(), cantonOptions);
        plausireports.put(Locale.ITALIAN, plausireport);

        return plausireports;
    }

    /**
     * Creates delivery plausireport (Excel-File) as byte array for all languages
     * 
     * @param delivery 		delivery to create the report for
     * @return				HashMap containing language dependent plausireports with Key = Locale.GERMAN, Locale.FRENCH and ITALIAN
     * @throws IOException
     */
    public HashMap<Locale, byte[]> create(SdlDelivery delivery) throws IOException {

        PlausiReportOptions deliveryOptions = new PlausiReportOptions();
        deliveryOptions.calculateSheetsForCantonAndDelivery(false);
        // Get all plausi data from database and build list and HashMap of relevant plausis
        List<SdlPlausi> allPlausiList = _plausiRepository.getPlausis();
        List<SdlPlausi> plausiList = new ArrayList<SdlPlausi>();
        Long version = delivery.getVersion();
        for (SdlPlausi plausi : allPlausiList) {
            if (plausi.getObjectLevel() >= CodegroupUtility.SDL_OBJECTTYPE_DELIVERY && plausi.getIsActive()
                    && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)
                    && !existsIdenticalLowerLevelRule(allPlausiList, (Plausi) plausi)) {
                plausiList.add(plausi);
            }
        }

        // Get all plausi errors
        List<SdlPlausiError> plausiErrors = _plausiErrorRepository.getPlausiErrorsForDelivery(delivery.getDeliveryId());
        Collections.sort(plausiErrors, new Comparator<SdlPlausiError>() {
            public int compare(SdlPlausiError error1, SdlPlausiError error2) {
                SdlPlausi plausi1 = error1.getPlausi();
                SdlPlausi plausi2 = error2.getPlausi();
                long order1 = (plausi1 != null && plausi1.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi1.getPlausiOrder();
                long order2 = (plausi2 != null && plausi2.getPlausiOrder() == null) ? Long.MAX_VALUE : plausi2.getPlausiOrder();
                return order1 < order2 ? -1 : order1 > order2 ? 1 : 0;
            }
        });
        Map<Long, Long> plausiErrorNumbers = _plausiErrorRepository.getNumberOfPlausiErrorsForDelivery(delivery.getDeliveryId());

        Long numberOfLearners = _learnerRepository.getNumberOfLearnersForDelivery(delivery.getDeliveryId());

        HashMap<Locale, byte[]> plausireports = new HashMap<Locale, byte[]>();
        // Create plausi report for Locale.GERMAN
        InputStream inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_DELIVERY_TEMPLATE_DE);
        byte[] plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, delivery.getCanton(), delivery.getVersion(), delivery.getDeliveryCode(),
                numberOfLearners, Locale.GERMAN.getLanguage(), deliveryOptions);
        plausireports.put(Locale.GERMAN, plausireport);
        // Create plausi report for Locale.FRENCH
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_DELIVERY_TEMPLATE_FR);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, delivery.getCanton(), delivery.getVersion(), delivery.getDeliveryCode(),
                numberOfLearners, Locale.FRENCH.getLanguage(), deliveryOptions);
        plausireports.put(Locale.FRENCH, plausireport);
        // Create plausi report for Locale.ITALIAN
        inp = getClass().getClassLoader().getResourceAsStream(PLAUSIREPORT_DELIVERY_TEMPLATE_IT);
        plausireport = create(inp, plausiList, plausiErrors, plausiErrorNumbers, delivery.getCanton(), delivery.getVersion(), delivery.getDeliveryCode(),
                numberOfLearners, Locale.ITALIAN.getLanguage(), deliveryOptions);
        plausireports.put(Locale.ITALIAN, plausireport);

        return plausireports;
    }

    /**
     * Creates dl user interface plausireport based on a list of deliveries
     * 
     * @param deliveries    deliveries to create the report for
     * @param locale        language of the report
     * @return              plausireport data
     * @throws IOException
     */
    public byte[] create(List<SdlDelivery> deliveries, String locale) throws IOException {
        locale = locale.toLowerCase();

        // Get all plausi data from database and build list and HashMap of relevant plausis
        List<SdlPlausi> allPlausiList = _plausiRepository.getPlausis();
        List<SdlPlausi> plausiList = new ArrayList<SdlPlausi>();
        Long version = deliveries.get(0).getVersion();
        for (SdlPlausi plausi : allPlausiList) {
            if (plausi.getObjectLevel() >= CodegroupUtility.SDL_OBJECTTYPE_DELIVERY && plausi.getIsActive()
                    && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)
                    && !existsIdenticalLowerLevelRule(allPlausiList, (Plausi) plausi)) {
                plausiList.add(plausi);
            }
        }

        // Get all plausi errors
        HashMap<Long, Long> cantonsForDelivery = new HashMap<Long, Long>();
        List<SdlPlausiError> plausiErrors = new ArrayList<SdlPlausiError>();
        for (SdlDelivery delivery : deliveries) {
            plausiErrors.addAll(_plausiErrorRepository.getPlausiErrorsForDelivery(delivery.getDeliveryId()));
            cantonsForDelivery.put(delivery.getDeliveryId(), delivery.getCanton());
        }
        Collections.sort(plausiErrors, new Comparator<SdlPlausiError>() {
            public int compare(SdlPlausiError error1, SdlPlausiError error2) {
                SdlPlausi plausi1 = error1.getPlausi();
                SdlPlausi plausi2 = error2.getPlausi();
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
     * Creates and returns the language dependent plausireport (Excel-File) as byte array
     * 
     * @param fs			Excel file system, created from language dependent template
     * @param delivery 		delivery to create the report for
     * @param plausiList	list of all Plausi rules for SDL
     * @param plausiErrors	list of all relevant plausi errors
     * @param plausiErrorNumbers	Map with key = plausiId, value = number of errors
     * @param locale		language of plausi report
     * @param options       PlausiReportOptions object with parameters for the plausi report
     * @return				plausireport (Excel-File) as byte array
     * @throws IOException
     */
    private byte[] create(InputStream inp, List<SdlPlausi> plausis, List<SdlPlausiError> plausiErrors, Map<Long, Long> plausiErrorNumbers, Long canton,
            Long version, String deliveryCode, Long nrOfLearners, String locale, PlausiReportOptions options) throws IOException {
        // Create excel workbook
        XSSFWorkbook plausireport = new XSSFWorkbook(inp);

        if (deliveryCode == null) { // overwrite sheet titles for plausireport of canton
            writeSheetTitles(plausireport, locale, true, options);
        }

        // Write the tabs in the excel file
        writeTitleSection(plausireport, canton, version, deliveryCode, nrOfLearners, locale);
        writeErrorOverview(plausireport, plausis, plausiErrors, plausiErrorNumbers, locale, options);

        if(options.isCantonReport()){
            writeHistoryDiagramData(plausireport, plausiErrors, locale, options);}

        writeErrorDetails(plausireport, plausiErrors, locale, options);

        // Convert to byte[]
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        plausireport.write(bos);
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Writes title section (first sheet of report).
     */
    private void writeTitleSection(XSSFWorkbook plausireport, Long canton, Long version, String deliveryCode, Long nrOfLearners, String locale) {
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
        curCell.setCellValue(nrOfLearners);
    }

    /**
     * Writes overview of errors per plausi-macro (first sheet of report).
     */
    private void writeErrorOverview(XSSFWorkbook plausireport, List<SdlPlausi> plausis, List<SdlPlausiError> plausiErrors, Map<Long, Long> plausiErrorNumbers,
            String locale, PlausiReportOptions options) {
        XSSFSheet overviewSheet = plausireport.getSheetAt(options.getOverviewSheetNumber());
        XSSFRow curRow;
        XSSFCell curCell;

        int macroRow = FIRST_MACRO_ROW;
        for (SdlPlausi plausi : plausis) {
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
    private void writeErrorDetails(XSSFWorkbook plausireport, List<SdlPlausiError> somePlausiErrors, String locale, PlausiReportOptions options) {
        XSSFSheet detailSheet = plausireport.getSheetAt(options.getDetailSheetNumber());
        XSSFRow curRow;
        XSSFCell curCell;

        // Get object types
        List<CodeGroup> objectTypes = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.SDL_OBJECTTYPE, locale);

        int errorRow = FIRST_ERROR_ROW;
        for (SdlPlausiError error : somePlausiErrors) {
            if (errorRow > FIRST_ERROR_ROW + NOF_ERROR_ROWS) {
                appendRow(detailSheet, errorRow, NOF_ERROR_COLUMNS);
            }

            curRow = detailSheet.getRow(errorRow);
            curRow.setHeightInPoints(ERROR_DETAILS_ROW_HEIGHT);

            if (errorRow == FIRST_ERROR_ROW + MAX_ERRORS) {
                // Insert last pseudo row into plausierror details list
                String[] parameterList = { String.valueOf(MAX_ERRORS) };
                String description = _localizationManager.getMessageByLanguage(TOO_MANY_ERRORS, locale);
                description = MessageFormat.format(description, (Object[]) parameterList);
                curCell = curRow.getCell(ERROR_MESSAGE_COLUMN);
                curCell.setCellValue(description);
                break;
            }

            // Write name and type of Macro
            SdlPlausi plausi = error.getPlausi();
            curCell = curRow.getCell(ERROR_NAME_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                curCell.setCellValue(plausi.getName_it());
            }

            curCell = curRow.getCell(ERROR_OBJECTTYPE_COLUMN);
            long objectLevel = Long.valueOf(CodegroupUtility.SDL_OBJECTTYPE_CANTON);
            if (error.getLearnerId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_LEARNER;
            } else if (error.getClassId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_CLASS;
            } else if (error.getSchoolId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_SCHOOL;
            } else if (error.getDeliveryId() != null) {
                objectLevel = CodegroupUtility.SDL_OBJECTTYPE_DELIVERY;
            }
            curCell.setCellValue(getCodeText(objectTypes, objectLevel));

            // Write error message
            curCell = curRow.getCell(ERROR_MESSAGE_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_de());
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_fr());
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                curCell.setCellValue(error.getErrorMsg_it());
            }

            // fill additional data (identification)
            curCell = curRow.getCell(ERROR_SCHOOLLABEL_COLUMN);
            curCell.setCellValue(error.getSchoolLabel());
            curCell = curRow.getCell(ERROR_CLASSLABEL_COLUMN);
            curCell.setCellValue(error.getClassLabel());
            curCell = curRow.getCell(ERROR_LEARNERTYPE_COLUMN);
            curCell.setCellValue(error.getDeliveredLearnerIdType() != null ? error.getDeliveredLearnerIdType() : "");
            curCell = curRow.getCell(ERROR_LEARNERID_COLUMN);
            curCell.setCellValue(error.getDeliveredLearnerId());

            curCell = curRow.getCell(ERROR_ORIGINTEXT_COLUMN);
            curCell.setCellValue(error.getLearnerOrigDeliveryData());

            errorRow++;
        }
    }

    /**
     * Writes all errors in detail for dl plausireport
     */
    private void writeErrorDetailsDl(XSSFWorkbook plausireport, List<SdlPlausiError> somePlausiErrors, HashMap<Long, Long> cantonsForDelivery, String locale) {
        XSSFSheet detailSheet = plausireport.getSheetAt(DETAIL_SHEET_DL);
        XSSFRow curRow;
        XSSFCell curCell;

        // Get cantons
        List<CodeGroup> cantons = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.CANTON, locale);

        int errorRow = FIRST_ERROR_ROW;
        for (SdlPlausiError error : somePlausiErrors) {
            if (errorRow > FIRST_ERROR_ROW + NOF_ERROR_ROWS) {
                appendRow(detailSheet, errorRow, NOF_ERROR_COLUMNS_DL);
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
            SdlPlausi plausi = error.getPlausi();
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
            curCell = curRow.getCell(DL_ERROR_CLASSLABEL_COLUMN);
            curCell.setCellValue(error.getClassLabel());
            curCell = curRow.getCell(DL_ERROR_LEARNERTYPE_COLUMN);
            curCell.setCellValue(error.getDeliveredLearnerIdType() != null ? error.getDeliveredLearnerIdType() : "");
            curCell = curRow.getCell(DL_ERROR_LEARNERID_COLUMN);
            curCell.setCellValue(error.getDeliveredLearnerId());

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
