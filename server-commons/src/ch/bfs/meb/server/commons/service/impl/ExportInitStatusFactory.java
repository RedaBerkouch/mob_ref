package ch.bfs.meb.server.commons.service.impl;

import java.io.*;
import java.util.*;

import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.BurSchool;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.commons.integration.dto.ConfigDelivery;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Factory  class for  creating init status excel reports.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ExportInitStatusFactory extends ExcelFactoryBase {

    private final String USER_TEXT_DELIMITER = ";";

    private static final String EXPORT_INIT_TEMPLATE_DE = "/exports/Initialisierung_Template_de.xlsx";
    private static final String EXPORT_INIT_TEMPLATE_FR = "/exports/Initialisierung_Template_fr.xlsx";
    private static final String EXPORT_INIT_TEMPLATE_IT = "/exports/Initialisierung_Template_it.xlsx";

    private static final int SCHOOL_SHEET = 0;
    private static final int APPLICATION_ROW = 5;
    private static final int APPLICATION_COLUMN = 1;
    private static final int VERSION_ROW = 8;
    private static final int VERSION_COLUMN = 2;
    private static final int DATE_ROW = 9;
    private static final int DATE_COLUMN = 2;

    private static final int FIRST_SCHOOL_ROW = 12;
    private static final int NOF_SCHOOL_ROWS = 1;
    private static final int NOF_SCHOOL_COLUMNS = 3;
    private static final int SCHOOL_CANTON_COLUMN = 1;
    private static final int SCHOOL_ID_COLUMN = 2;
    private static final int SCHOOL_NAME_COLUMN = 3;

    private static final int USER_SHEET = 1;

    private static final int FIRST_USER_ROW = 12;
    private static final int NOF_USER_ROWS = 1;
    private static final int NOF_USER_COLUMNS = 8;
    private static final int USER_CANTON_COLUMN = 1;
    private static final int USER_DELIVERIES_COLUMN = 2;
    private static final int USER_ROLE_COLUMN = 3;
    private static final int USER_STATUS_COLUMN = 4;
    private static final int USER_NAME_COLUMN = 5;
    private static final int USER_FIRSTNAME_COLUMN = 6;
    private static final int USER_EMAIL_COLUMN = 7;
    private static final int USER_PHONE_COLUMN = 8;

    private static final int CANTON_SHEET = 2;

    private static final int FIRST_CANTON_ROW = 12;
    private static final int NOF_CANTON_ROWS = 1;
    private static final int NOF_CANTON_COLUMNS = 3;
    private static final int CANTON_CANTON_COLUMN = 1;
    private static final int CANTON_DEFAULTDELIVERY_COLUMN = 2;
    private static final int CANTON_DV_COLUMN = 3;

    private static final String STATUS_USER_NOT_FOUND = "user.status.not.found";
    private static final String STATUS_USER_NOT_ACTIVE = "user.status.not.active";
    private static final String STATUS_USER_NOT_AUTHORIZED = "user.status.not.authorized";

    private static final String ROLE_RO_NAME = "RO";
    private static final String ROLE_DL_NAME = "DL";
    private static final String ROLE_DV_NAME = "DV";
    private static final String ROLE_EV_NAME = "EV";
    private static final String ROLE_EA_NAME = "EA";

    private ICodegroupManager _codegroupManager;

    // Spring injection
    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    /**
     * 
     * Creates init status export (Excel-File) as byte array for the given language
     * 
     * @param applicationTitle
     * @param version
     * @param notConfiguredSchools
     * @param configDeliveries
     * @param users
     * @param localizationManager
     * @param locale
     * @return
     * @throws IOException
     */
    public byte[] createInitStatusExport(String applicationTitle, Long version, List<BurSchool> notConfiguredSchools, List<ConfigDelivery> configDeliveries,
            HashMap<String, ExportUser> users, IServerLocalizationManager localizationManager, String locale) throws IOException {

        // Get all cantons from database
        List<CodeGroup> cantons = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.CANTON, locale, true);

        // Create users export for given locale (DE is default)
        XSSFWorkbook initStatusExport;
        if (locale.equals(Locale.FRENCH.getLanguage())) {
            initStatusExport = getInitStatusExportWorkbook(EXPORT_INIT_TEMPLATE_FR);
        } else if (locale.equals(Locale.ITALIAN.getLanguage())) {
            initStatusExport = getInitStatusExportWorkbook(EXPORT_INIT_TEMPLATE_IT);
        } else {
            initStatusExport = getInitStatusExportWorkbook(EXPORT_INIT_TEMPLATE_DE);
        }
        // Create excel workbook
        writeTitleSection(initStatusExport.getSheetAt(SCHOOL_SHEET), applicationTitle, version, locale);
        writeNotConfiguredSchools(initStatusExport, notConfiguredSchools, cantons, locale);

        writeTitleSection(initStatusExport.getSheetAt(USER_SHEET), applicationTitle, version, locale);
        writeNotValidUsers(initStatusExport, configDeliveries, users, cantons, localizationManager, locale);

        writeTitleSection(initStatusExport.getSheetAt(CANTON_SHEET), applicationTitle, version, locale);
        writeCantons(initStatusExport.getSheetAt(CANTON_SHEET), cantons, configDeliveries, users, locale);

        // Convert to byte[]
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        initStatusExport.write(bos);
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Returns the initStatusExport XSSFWorkbook for the given template
     * @Return XSSFWorkbook initStatusExport;
     */
    private XSSFWorkbook getInitStatusExportWorkbook(String initStatusExpTemplate) throws IOException {
        XSSFWorkbook initStatusExportTemplate;
        try (InputStream inp = getClass().getClassLoader().getResourceAsStream(initStatusExpTemplate);) {
            initStatusExportTemplate = new XSSFWorkbook(inp);
        }
        return initStatusExportTemplate;
    }

    /**
     * Writes title section (first part of each sheet of report).
     */
    private void writeTitleSection(XSSFSheet sheet, String applicationTitle, Long version, String locale) {

        // write application title
        XSSFRow curRow = sheet.getRow(APPLICATION_ROW);
        XSSFCell curCell = curRow.getCell(APPLICATION_COLUMN);
        curCell.setCellValue(applicationTitle);

        // write version
        curRow = sheet.getRow(VERSION_ROW);
        curCell = curRow.getCell(VERSION_COLUMN);
        curCell.setCellValue(version);

        // write current Date
        GregorianCalendar cal = new GregorianCalendar();
        curRow = sheet.getRow(DATE_ROW);
        curCell = curRow.getCell(DATE_COLUMN);
        curCell.setCellValue(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "." + String.valueOf(cal.get(Calendar.MONTH) + 1) + "."
                + String.valueOf(cal.get(Calendar.YEAR)));
    }

    /**
     * Writes list of already sorted schools (first sheet of report).
     */
    private void writeNotConfiguredSchools(XSSFWorkbook initStatusExport, List<BurSchool> notConfiguredSchools, List<CodeGroup> cantons, String locale)
            throws IOException {
        XSSFSheet schoolsSheet = initStatusExport.getSheetAt(SCHOOL_SHEET);
        XSSFRow curRow;
        XSSFCell curCell;

        int schoolRow = FIRST_SCHOOL_ROW;
        for (BurSchool school : notConfiguredSchools) {
            if (schoolRow > (FIRST_SCHOOL_ROW + NOF_SCHOOL_ROWS)) {
                appendRow(schoolsSheet, schoolRow, NOF_SCHOOL_COLUMNS);
            }

            curRow = schoolsSheet.getRow(schoolRow);

            curCell = curRow.getCell(SCHOOL_CANTON_COLUMN);
            curCell.setCellValue(getCodeText(cantons, school.getCanton()));

            curCell = curRow.getCell(SCHOOL_ID_COLUMN);
            curCell.setCellValue(school.getBurNr());

            curCell = curRow.getCell(SCHOOL_NAME_COLUMN);
            curCell.setCellValue(school.getLabel());

            schoolRow++;
        }
    }

    /**
     * Writes list of already sorted schools (first sheet of report).
     */
    private void writeNotValidUsers(XSSFWorkbook initStatusExport, List<ConfigDelivery> configDeliveries, HashMap<String, ExportUser> allUsers,
            List<CodeGroup> cantons, IServerLocalizationManager localizationManager, String locale) throws IOException {
        XSSFSheet usersSheet = initStatusExport.getSheetAt(USER_SHEET);

        // Sort configDeliveries by Canton
        Collections.sort(configDeliveries, new Comparator<ConfigDelivery>() {
            public int compare(ConfigDelivery delivery1, ConfigDelivery delivery2) {
                return delivery1.getCanton().compareTo(delivery2.getCanton());
            }
        });

        // Build not valid users for config deliveries
        String[] roArray;
        String[] dlArray;
        int userRow = FIRST_USER_ROW;
        for (ConfigDelivery configDelivery : configDeliveries) {
            roArray = configDelivery.getRo_users().split(USER_TEXT_DELIMITER);
            for (String username : roArray) {
                userRow = writeNotValidUser(usersSheet, allUsers, configDelivery, cantons, username, SecurityConstants.ROLE_RO, localizationManager, locale,
                        userRow);
            }
            dlArray = configDelivery.getDl_users().split(USER_TEXT_DELIMITER);
            for (String username : dlArray) {
                userRow = writeNotValidUser(usersSheet, allUsers, configDelivery, cantons, username, SecurityConstants.ROLE_DL, localizationManager, locale,
                        userRow);
            }
        }
    }

    // write user if not valid
    private int writeNotValidUser(XSSFSheet usersSheet, HashMap<String, ExportUser> allUsers, ConfigDelivery configDelivery, List<CodeGroup> cantons,
            String username, long neededRole, IServerLocalizationManager localizationManager, String locale, int userRow) throws IOException {
        XSSFRow curRow;
        XSSFCell curCell;
        String status = null;

        // ignore empty users
        if (username.trim().isEmpty()) {
            return userRow;
        }

        ExportUser exportUser = allUsers.get(username.trim().toLowerCase());

        // User not in IDM
        User user = null;
        if (exportUser == null) {
            status = STATUS_USER_NOT_FOUND;
        } else {
            user = exportUser.getUser();
            if (!user.isActive()) {
                // User not active in IDM
                status = STATUS_USER_NOT_ACTIVE;
            } else if (exportUser.getRole() < neededRole) {
                // User not authorized
                status = STATUS_USER_NOT_AUTHORIZED;
            }
        }

        if (status != null) {
            if (userRow > (FIRST_USER_ROW + NOF_USER_ROWS)) {
                appendRow(usersSheet, userRow, NOF_USER_COLUMNS);
            }

            curRow = usersSheet.getRow(userRow);

            curCell = curRow.getCell(USER_CANTON_COLUMN);
            curCell.setCellValue(getCodeText(cantons, configDelivery.getCanton()));

            curCell = curRow.getCell(USER_ROLE_COLUMN);
            curCell.setCellValue(exportUser != null ? getRoleName(exportUser.getRole()) : null);

            curCell = curRow.getCell(USER_STATUS_COLUMN);
            curCell.setCellValue(localizationManager.getMessageByLanguage(status, locale));

            curCell = curRow.getCell(USER_NAME_COLUMN);
            curCell.setCellValue(user != null ? user.getSurname() : null);
            curCell = curRow.getCell(USER_FIRSTNAME_COLUMN);
            curCell.setCellValue(user != null ? user.getGivenName() : null);
            curCell = curRow.getCell(USER_EMAIL_COLUMN);
            curCell.setCellValue(username);
            curCell = curRow.getCell(USER_PHONE_COLUMN);
            curCell.setCellValue(user != null ? user.getBusinessPhone() : null);

            curCell = curRow.getCell(USER_DELIVERIES_COLUMN);
            curCell.setCellValue(configDelivery.getDeliveryCode());

            userRow++;
        }
        return userRow;
    }

    private void writeCantons(XSSFSheet cantonsSheet, List<CodeGroup> cantons, List<ConfigDelivery> configDeliveries, HashMap<String, ExportUser> users,
            String locale) throws IOException {
        XSSFRow curRow;
        XSSFCell curCell;

        int cantonRow = FIRST_CANTON_ROW;
        for (CodeGroup canton : cantons) {
            if (cantonRow > (FIRST_CANTON_ROW + NOF_CANTON_ROWS)) {
                appendRow(cantonsSheet, cantonRow, NOF_CANTON_COLUMNS);
            }

            curRow = cantonsSheet.getRow(cantonRow);

            curCell = curRow.getCell(CANTON_CANTON_COLUMN);
            curCell.setCellValue(canton.getCodeTextAbbr());

            curCell = curRow.getCell(CANTON_DEFAULTDELIVERY_COLUMN);
            ConfigDelivery defaultDelivery = findDefaultDelivery(configDeliveries, canton.getCode());
            curCell.setCellValue((defaultDelivery != null) ? defaultDelivery.getDeliveryCode() : null);

            curCell = curRow.getCell(CANTON_DV_COLUMN);
            curCell.setCellValue(getDVUsers(canton, users));

            cantonRow++;
        }
    }

    private ConfigDelivery findDefaultDelivery(List<ConfigDelivery> configDeliveries, Long canton) {
        for (ConfigDelivery delivery : configDeliveries) {
            if (delivery.getIsDefault() && delivery.getCanton().equals(canton)) {
                return delivery;
            }
        }
        return null;
    }

    private String getRoleName(long role) {
        int roleInt = Long.valueOf(role).intValue();
        String roleName;
        switch (roleInt) {
        case 0:
            roleName = ROLE_RO_NAME;
            break;
        case 1:
            roleName = ROLE_DL_NAME;
            break;
        case 2:
            roleName = ROLE_DV_NAME;
            break;
        case 3:
            roleName = ROLE_EV_NAME;
            break;
        case 4:
            roleName = ROLE_EA_NAME;
            break;
        default:
            roleName = null;
        }
        return roleName;
    }

    private String getDVUsers(CodeGroup canton, HashMap<String, ExportUser> users) {
        String dvUsers = "";
        for (String username : users.keySet()) {
            ExportUser user = users.get(username); // must be not null!
            String locality = user.getUser().getCantons().size() > 0 ? user.getUser().getCantonsAsString() : "";
            List<Long> cantons = StringUtils.splitLongs(locality);
            if (user.getRole() == SecurityConstants.ROLE_DV && cantons.contains(canton.getCode())) {
                if (dvUsers.length() > 0) {
                    dvUsers += USER_TEXT_DELIMITER;
                }
                dvUsers += username;
            }
        }
        return dvUsers;
    }
}
