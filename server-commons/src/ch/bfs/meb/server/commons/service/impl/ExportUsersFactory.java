package ch.bfs.meb.server.commons.service.impl;

import java.io.*;
import java.util.*;


import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Factory base class for of creating excel reports.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ExportUsersFactory extends ExcelFactoryBase {

    protected static final String APPLICATION_TITLE = "application.title";

    private static final String EXPORT_USERS_TEMPLATE_DE = "/exports/Userlist_Template_de.xlsx";
    private static final String EXPORT_USERS_TEMPLATE_FR = "/exports/Userlist_Template_fr.xlsx";
    private static final String EXPORT_USERS_TEMPLATE_IT = "/exports/Userlist_Template_it.xlsx";

    private static final int USERLIST_SHEET = 0;
    private static final int APPLICATION_ROW = 5;
    private static final int APPLICATION_COLUMN = 1;
    private static final int DATE_ROW = 8;
    private static final int DATE_COLUMN = 2;

    private static final int FIRST_USER_ROW = 11;
    private static final int NOF_USER_ROWS = 1;
    private static final int NOF_USER_COLUMNS = 8;
    private static final int USER_CANTON_COLUMN = 1;
    private static final int USER_ROLE_COLUMN = 2;
    private static final int USER_NAME_COLUMN = 3;
    private static final int USER_FIRSTNAME_COLUMN = 4;
    private static final int USER_EMAIL_COLUMN = 5;
    private static final int USER_PHONE_COLUMN = 6;
    private static final int USER_DELIVERIES_COLUMN = 7;
    private static final int USER_MINDELIVERYSTATUS_COLUMN = 8;

    private ICodegroupManager _codegroupManager;

    private List<CodeGroup> _roles = null;
    private List<CodeGroup> _deliveryStatus = null;

    // Spring injection
    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    /**
     * Creates users export (Excel-File) as byte array for the given language
     * 
     * @param dvUsers		Sorted list of dv users
     * @param dlUsers		Sorted list of dl users
     * @param roUsers		Sorted list of read-only users
     * @param locale 		user language
     * @return				Excel export
     * @throws IOException
     */
    public byte[] createUsersExport(String applicationTitle, List<ExportUser> dvUsers, List<ExportUser> dlUsers, List<ExportUser> roUsers, String locale)
            throws IOException {

        // Get all cantons from database
        List<CodeGroup> cantons = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.CANTON, locale, true);
        _roles = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.MEB_ROLE, locale, true);
        _deliveryStatus = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.MEB_DELIVERYSTATUS, locale, true);

        // Create current canons list from canton codes of the user (Erweiterung E05, Mantis 1128)
        List<CodeGroup> userCantons;
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (CodegroupUtility.getCodeForRoleName(user.getRoleName()) < SecurityConstants.ROLE_EV) {
            userCantons = new ArrayList<CodeGroup>();
            List<Long> userCantonCodes = user.getCantons();
            for (CodeGroup canton : cantons) {
                if (userCantonCodes.contains(canton.getCode())) {
                    userCantons.add(canton);
                }
            }
        } else {
            userCantons = cantons; // all cantons for EV and EA
        }

        // Create users export for given locale (DE is default)
        File file;
        XSSFWorkbook usersExport;
        if (locale.equals(Locale.FRENCH.getLanguage())) {
            usersExport = getUsersExportWorkbook(EXPORT_USERS_TEMPLATE_FR);
        } else if (locale.equals(Locale.ITALIAN.getLanguage())) {
            usersExport = getUsersExportWorkbook(EXPORT_USERS_TEMPLATE_IT);
        } else {
            usersExport = getUsersExportWorkbook(EXPORT_USERS_TEMPLATE_DE);
        }
        // Create excel workbook
        //XSSFWorkbook usersExport = new XSSFWorkbook(inp);
        writeTitleSection(usersExport, applicationTitle, locale);

        int userRow = FIRST_USER_ROW;

        // first export users with no canton defined
        userRow = writeUsers(usersExport, userRow, SecurityConstants.ROLE_SDL_DV, null, dvUsers, locale);
        userRow = writeUsers(usersExport, userRow, SecurityConstants.ROLE_SDL_DL, null, dlUsers, locale);
        userRow = writeUsers(usersExport, userRow, SecurityConstants.ROLE_SDL_RO, null, roUsers, locale);

        for (CodeGroup canton : userCantons) {
            userRow = writeUsers(usersExport, userRow, SecurityConstants.ROLE_SDL_DV, canton, dvUsers, locale);
            userRow = writeUsers(usersExport, userRow, SecurityConstants.ROLE_SDL_DL, canton, dlUsers, locale);
            userRow = writeUsers(usersExport, userRow, SecurityConstants.ROLE_SDL_RO, canton, roUsers, locale);
        }

        // Convert to byte[]
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        usersExport.write(bos);
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Returns the usersExport XSSFWorkbook for the given template
     * @Return XSSFWorkbook usersExport;
     */
    private XSSFWorkbook getUsersExportWorkbook(String exportUsersTemplate) throws IOException {
        XSSFWorkbook usersExport;
        try (InputStream inp = getClass().getClassLoader().getResourceAsStream(exportUsersTemplate);) {
            usersExport = new XSSFWorkbook(inp);
        }
        return usersExport;
    }

    /**
     * Writes title section (first sheet of report).
     */
    private void writeTitleSection(XSSFWorkbook usersExport, String applicationTitle, String locale) {
        XSSFSheet usersSheet = usersExport.getSheetAt(USERLIST_SHEET);

        // write application title
        XSSFRow curRow = usersSheet.getRow(APPLICATION_ROW);
        XSSFCell curCell = curRow.getCell(APPLICATION_COLUMN);
        curCell.setCellValue(applicationTitle);

        // write current Date
        GregorianCalendar cal = new GregorianCalendar();
        curRow = usersSheet.getRow(DATE_ROW);
        curCell = curRow.getCell(DATE_COLUMN);
        curCell.setCellValue(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "." + String.valueOf(cal.get(Calendar.MONTH) + 1) + "."
                + String.valueOf(cal.get(Calendar.YEAR)));
    }

    /**
     * Writes list of users per canton (first sheet of report).
     */
    private int writeUsers(XSSFWorkbook usersExport, int userRow, String appRoleName, CodeGroup canton, List<ExportUser> users, String locale)
            throws IOException {
        XSSFSheet usersSheet = usersExport.getSheetAt(USERLIST_SHEET);
        XSSFRow curRow;
        XSSFCell curCell;

        for (ExportUser exportUser : users) {
            if ((canton == null && exportUser.getCanton() == null) || (canton != null && canton.getCode().equals(exportUser.getCanton()))) {
                if (userRow > FIRST_USER_ROW + NOF_USER_ROWS) {
                    appendRow(usersSheet, userRow, NOF_USER_COLUMNS);
                }

                curRow = usersSheet.getRow(userRow);

                curCell = curRow.getCell(USER_CANTON_COLUMN);
                curCell.setCellValue(canton == null ? "" : canton.getCodeTextAbbr());

                curCell = curRow.getCell(USER_ROLE_COLUMN);
                // search role name for RoleName
                String roleName = getCodeText(_roles, CodegroupUtility.getCodeForRoleName(appRoleName));
                curCell.setCellValue(roleName);

                curCell = curRow.getCell(USER_NAME_COLUMN);
                curCell.setCellValue(exportUser.getUser().getSurname());
                curCell = curRow.getCell(USER_FIRSTNAME_COLUMN);
                curCell.setCellValue(exportUser.getUser().getGivenName());
                curCell = curRow.getCell(USER_EMAIL_COLUMN);
                curCell.setCellValue(exportUser.getUser().getUsername());
                curCell = curRow.getCell(USER_PHONE_COLUMN);
                curCell.setCellValue(exportUser.getUser().getBusinessPhone());

                curCell = curRow.getCell(USER_DELIVERIES_COLUMN);
                curCell.setCellValue(exportUser.getDeliveries());

                curCell = curRow.getCell(USER_MINDELIVERYSTATUS_COLUMN);
                curCell.setCellValue(getCodeText(_deliveryStatus, exportUser.getMinDeliveryStatus()));

                userRow++;
            }
        }

        return userRow;
    }

}
