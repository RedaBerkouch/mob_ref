package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * Factory for of creating the users export.
 * Creation of the export is based on an Excel Template (different templates for different
 * langugages).
 * Additional languages as well as changes in the templates have to be considered and updated 
 * explicitly in the code of this class.
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class ExcelFactoryBase {

    /**
     * Converts String to Number and fills the Number into the indicated cell
     * 
     * @param curRow
     * @param column
     * @param number
     */
    protected void fillNumberCell(XSSFRow curRow, int column, String number) {
        XSSFCell curCell = curRow.getCell(column);
        try {
            curCell.setCellValue(Integer.valueOf(number.trim()));
        } catch (NumberFormatException e) {
            // Leave empty
        }
    }

    /**
     * Appends an additional row. 
     * Attention: Number of columns must be correct (all cells must be created before use)
     * 
     * @param sheet
     * @param index start index of first column to create
     * @param nofColumns number of columns to create
     */
    protected void appendRow(XSSFSheet sheet, int index, int nofColumns) {
        // append a row
        XSSFRow lastRow = sheet.getRow(index - 1);
        XSSFRow curRow = sheet.createRow(index);
        XSSFCell curCell;

        for (int i = 1; i <= nofColumns; i++) {
            XSSFCellStyle cs = lastRow.getCell(i).getCellStyle();
            curCell = curRow.createCell(i);
            curCell.setCellStyle(cs);
        }
    }

    /**
     * Gets the given code text for a given code and a locale.
     * 
     * @param groupId 	codes language dependent codes
     * @param code	 	code 
     * @return 			code text if exists, else null
     */
    protected static String getCodeText(List<CodeGroup> codes, Long code) {
        for (CodeGroup codegroup : codes) {
            if (codegroup.getCode().equals(code)) {
                return codegroup.getCodeText();
            }
        }
        return null;
    }

    /**
     * Gets the given code text for a given code and a locale.
     * 
     * @param groupId 	codes language dependent codes
     * @param code	 	code 
     * @return 			code text if exists, else null
     */
    public static String getCodeText(List<CodeGroup> codes, long code) {
        for (CodeGroup codegroup : codes) {
            if (codegroup.getCode().equals(code)) {
                return codegroup.getCodeText();
            }
        }
        return null;
    }

    /**
     * Gets the given code text for a given code and a locale.
     * 
     * @param groupId 	codes language dependent codes
     * @param code	 	code 
     * @return 			code text if exists, else null
     */
    public static String getCodeTextAbbr(List<CodeGroup> codes, long code) {
        for (CodeGroup codegroup : codes) {
            if (codegroup.getCode().equals(code)) {
                return codegroup.getCodeTextAbbr();
            }
        }
        return null;
    }
}
