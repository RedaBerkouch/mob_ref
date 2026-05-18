package ch.bfs.meb.server.commons.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;

/**
 * Factory for of creating the plausi reports.
 * Creation of the export is based on an Excel Template (different templates for different
 * languages).
 * Additional languages as well as changes in the templates have to be considered and updated 
 * explicitly in the code of this class.
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class PlausireportFactoryBase extends ExcelFactoryBase {

    public static final int MAX_ERRORS = 10000;

    protected static final String PLAUSIREPORT_TEMPLATE_DE = "/plausireports/Plausibericht_Vorlage_de.xlsx";
    protected static final String PLAUSIREPORT_TEMPLATE_FR = "/plausireports/Plausibericht_Vorlage_fr.xlsx";
    protected static final String PLAUSIREPORT_TEMPLATE_IT = "/plausireports/Plausibericht_Vorlage_it.xlsx";

    protected static final String PLAUSIREPORT_DELIVERY_TEMPLATE_DE = "/plausireports/Plausibericht_delivery_Vorlage_de.xlsx";
    protected static final String PLAUSIREPORT_DELIVERY_TEMPLATE_FR = "/plausireports/Plausibericht_delivery_Vorlage_fr.xlsx";
    protected static final String PLAUSIREPORT_DELIVERY_TEMPLATE_IT = "/plausireports/Plausibericht_delivery_Vorlage_it.xlsx";

    protected static final String PLAUSIREPORT_TEMPLATE_DL_DE = "/plausireports/Plausibericht_Vorlage_dl_de.xlsx";
    protected static final String PLAUSIREPORT_TEMPLATE_DL_FR = "/plausireports/Plausibericht_Vorlage_dl_fr.xlsx";
    protected static final String PLAUSIREPORT_TEMPLATE_DL_IT = "/plausireports/Plausibericht_Vorlage_dl_it.xlsx";

    protected static final String TOO_MANY_ERRORS = "plausi.report.tooManyErrors";
    protected static final String IS_CONFIRMABLE = "plausi.report.confirmable";
    protected static final int ERROR_DETAILS_ROW_HEIGHT = 36;

    protected static final String OVERVIEW_SHEET_TITLE = "plausi.report.canton.overview.title";
    protected static final String DIAGRAM_SHEET_TITLE = "plausi.report.canton.diagram.title";
    protected static final String DETAIL_SHEET_TITLE = "plausi.report.canton.detail.title";

    protected static final int TITLE_SHEET = 0;
    protected static final int TITLE_ROW = 6;
    protected static final int TITLE_COLUMN = 1;
    protected static final int DATE_ROW = 8;
    protected static final int DATE_COLUMN = 2;
    protected static final int CANTON_ROW = 9;
    protected static final int CANTON_COLUMN = 2;
    protected static final int VERSION_ROW = 10;
    protected static final int VERSION_COLUMN = 2;
    protected static final int DELIVERY_ROW = 11;
    protected static final int DELIVERY_TITLE_COLUMN = 1;
    protected static final int DELIVERY_COLUMN = 2;
    protected static final int NOF_PERSONS_ROW = 12;
    protected static final int NOF_PERSONS_COLUMN = 2;

    protected static final int FIRST_MACRO_ROW = 15;
    protected static final int NOF_MACRO_ROWS = 5;
    protected static final int NOF_MACRO_COLUMNS = 3;
    protected static final int MACRO_ERRORS_COLUMN = 1;
    protected static final int MACRO_NAME_COLUMN = 2;
    protected static final int MACRO_DESCRIPTION_COLUMN = 3;

    protected static final int MAX_DIAGRAMS = 7;
    protected static final String DIAGRAM_ZEITREIHE = "ZEITREIHENGRAFIK:";
    protected static final String DIAGRAM_TITLE_SEPARATOR = ";";
    protected static final String DIAGRAM_VALUES_START = "[";
    protected static final String DIAGRAM_VALUES_END = "]";
    protected static final String DIAGRAM_VALUE_SEPARATOR = ",";

    protected static final int DIAGRAM_FIRST_TITLE_ROW = 10;
    protected static final int DIAGRAM_FIRST_YEAR_ROW = 13;
    protected static final int DIAGRAM_ROWS_BETWEEN = 12;
    protected static final int DIAGRAM_MAX_YEARS = 7;
    protected static final int DIAGRAM_TITLE_COLUMN = 1;
    protected static final int DIAGRAM_YEAR_COLUMN = 1;
    protected static final int DIAGRAM_MIN_COLUMN = 2;
    protected static final int DIAGRAM_VALUE_COLUMN = 3;
    protected static final int DIAGRAM_MAX_COLUMN = 4;

    protected static final int FIRST_ERROR_ROW = 9;
    protected static final int NOF_ERROR_ROWS = 10;

    protected static final int DETAIL_SHEET_DL = 0;

    private static final SimpleDateFormat MEB_DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    static {
        MEB_DATE_TIME_FORMAT.setLenient(false);
    }

    // Spring injection
    protected IServerLocalizationManager _localizationManager;
    protected ICodegroupManager _codegroupManager;

    public void setLocalizationManager(IServerLocalizationManager localizationManager) {
        _localizationManager = localizationManager;
    }

    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    /** Writes sheet titles for plausiReport of canton. */
    protected void writeSheetTitles(XSSFWorkbook plausireport, String locale, boolean isPlausiReportForCanton, PlausiReportOptions options) {
        if (isPlausiReportForCanton) {
            XSSFSheet sheet = plausireport.getSheetAt(options.getOverviewSheetNumber());
            XSSFRow curRow = sheet.getRow(TITLE_ROW);
            XSSFCell curCell = curRow.getCell(TITLE_COLUMN);
            curCell.setCellValue(_localizationManager.getMessageByLanguage(OVERVIEW_SHEET_TITLE, locale));

            sheet = plausireport.getSheetAt(options.getDiagramSheetNumber());
            curRow = sheet.getRow(TITLE_ROW);
            curCell = curRow.getCell(TITLE_COLUMN);
            curCell.setCellValue(_localizationManager.getMessageByLanguage(DIAGRAM_SHEET_TITLE, locale));

            sheet = plausireport.getSheetAt(options.getDetailSheetNumber());
            curRow = sheet.getRow(TITLE_ROW);
            curCell = curRow.getCell(TITLE_COLUMN);
            curCell.setCellValue(_localizationManager.getMessageByLanguage(DETAIL_SHEET_TITLE, locale));
        }
    }

    /** Avoiding duplicates of rules defined on level delivery and a lower level. */
    protected boolean existsIdenticalLowerLevelRule(List<? extends Plausi> allPlausiList, Plausi plausi) {
        for (Plausi p : allPlausiList) {
            // TODO lsc: instead of name_de take business id from plausi
            if (p.getName_de() != null && plausi.getName_de() != null && p.getName_de().equals(plausi.getName_de())
                    && p.getObjectLevel() > plausi.getObjectLevel() && p.getIsActive()) {
                return true;
            }
        }
        return false;
    }

    protected static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return ((SimpleDateFormat) MEB_DATE_TIME_FORMAT.clone()).format(date);
    }

    /**
     * Writes diagram data (second sheet of report).
     * Format of report data (example):
     * ZEITREIHENGRAFIK: Titel_d;Titel_f;{[2001,300,350,400], [2002,310,360,410], [2003,320,370,420], [2004,330,380,430], [2005,330,380,430], [2006,340,390,440], [2007,350,290,450]}
     */
    protected void writeHistoryDiagramData(XSSFWorkbook plausireport, List<? extends PlausiError> plausiErrors, String locale, PlausiReportOptions options) {
        XSSFSheet diagramSheet = plausireport.getSheetAt(options.getDiagramSheetNumber());
        XSSFRow curRow;
        XSSFCell curCell;

        // find relevant Plausi errors
        List<PlausiError> diagramErrors = new ArrayList<PlausiError>();
        for (PlausiError error : plausiErrors) {
            if (error.getReportData() != null && error.getReportData().startsWith(DIAGRAM_ZEITREIHE)) {
                diagramErrors.add(error);
            }
        }

        // Only consider the first n diagram errors
        for (int i = 0; i < diagramErrors.size() && i < MAX_DIAGRAMS; i++) {
            PlausiError error = diagramErrors.get(i);
            String reportdata = error.getReportData().substring(DIAGRAM_ZEITREIHE.length());

            // Parse titles
            int fromIndex = 0;
            int toIndex = reportdata.indexOf(DIAGRAM_TITLE_SEPARATOR, fromIndex);
            String title_de = reportdata.substring(fromIndex, toIndex).trim();
            fromIndex = toIndex + DIAGRAM_TITLE_SEPARATOR.length();
            toIndex = reportdata.indexOf(DIAGRAM_TITLE_SEPARATOR, fromIndex);
            String title_fr = reportdata.substring(fromIndex, toIndex).trim();
            fromIndex = toIndex + DIAGRAM_TITLE_SEPARATOR.length();
            toIndex = reportdata.indexOf(DIAGRAM_TITLE_SEPARATOR, fromIndex);
            String title_it = reportdata.substring(fromIndex, toIndex).trim();

            // fill title into Excel
            curRow = diagramSheet.getRow(DIAGRAM_FIRST_TITLE_ROW + i * DIAGRAM_ROWS_BETWEEN);
            curCell = curRow.getCell(DIAGRAM_TITLE_COLUMN);
            if (Locale.GERMAN.getLanguage().equals(locale)) {
                curCell.setCellValue(title_de);
            } else if (Locale.FRENCH.getLanguage().equals(locale)) {
                curCell.setCellValue(title_fr);
            } else if (Locale.ITALIAN.getLanguage().equals(locale)) {
                curCell.setCellValue(title_it);
            }

            // parse values and fill values into sheet (Format: [year,min,value,max],[..])
            fromIndex = reportdata.indexOf(DIAGRAM_VALUES_START, toIndex);
            int yearRow = DIAGRAM_FIRST_YEAR_ROW + i * DIAGRAM_ROWS_BETWEEN;
            while (fromIndex >= 0 && yearRow < DIAGRAM_FIRST_YEAR_ROW + i * DIAGRAM_ROWS_BETWEEN + DIAGRAM_MAX_YEARS) {
                curRow = diagramSheet.getRow(yearRow);

                fromIndex = fromIndex + DIAGRAM_VALUE_SEPARATOR.length();
                toIndex = reportdata.indexOf(DIAGRAM_VALUE_SEPARATOR, fromIndex);
                fillNumberCell(curRow, DIAGRAM_YEAR_COLUMN, reportdata.substring(fromIndex, toIndex));
                fromIndex = toIndex + DIAGRAM_VALUE_SEPARATOR.length();
                toIndex = reportdata.indexOf(DIAGRAM_VALUE_SEPARATOR, fromIndex);
                fillNumberCell(curRow, DIAGRAM_MIN_COLUMN, reportdata.substring(fromIndex, toIndex));
                fromIndex = toIndex + DIAGRAM_VALUE_SEPARATOR.length();
                toIndex = reportdata.indexOf(DIAGRAM_VALUE_SEPARATOR, fromIndex);
                fillNumberCell(curRow, DIAGRAM_VALUE_COLUMN, reportdata.substring(fromIndex, toIndex));
                fromIndex = toIndex + DIAGRAM_VALUE_SEPARATOR.length();
                toIndex = reportdata.indexOf(DIAGRAM_VALUES_END, fromIndex);
                fillNumberCell(curRow, DIAGRAM_MAX_COLUMN, reportdata.substring(fromIndex, toIndex));

                toIndex = reportdata.indexOf(DIAGRAM_VALUES_END, fromIndex);
                fromIndex = reportdata.indexOf(DIAGRAM_VALUES_START, toIndex);
                yearRow++;
            }
        }
    }
}