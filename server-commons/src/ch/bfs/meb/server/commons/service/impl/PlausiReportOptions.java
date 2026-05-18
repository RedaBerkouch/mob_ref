package ch.bfs.meb.server.commons.service.impl;

import lombok.Data;

/**
 * Options for Plausireports
 * Contains static methods that set parameters according to the report version
 * @author $Author$
 * @version $Revision$
 */
@Data
public class PlausiReportOptions {

    private int overviewSheetNumber;
    private int diagramSheetNumber;
    private int detailSheetNumber;
    private boolean isCantonReport;

    /**
     * Sets the pagination of the plausireport
     * Canton report has 3 sheets where as delivery report has only 2
     */
    public void calculateSheetsForCantonAndDelivery(boolean isCanton) {
        // the overview sheet is in both cases at the beginning of the report
        setOverviewSheetNumber(0);

        if (isCanton) {
            setDiagramSheetNumber(1);
            setDetailSheetNumber(2);
            setCantonReport(true);
        } else {
            setDetailSheetNumber(1);
            setCantonReport(false);
        }
    }
}
