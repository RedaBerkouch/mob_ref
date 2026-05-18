/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.resultmapper;

import ch.bfs.meb.sbg.web.ws.sbgaction.ActionResult;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 300 $
 */
public class ActionListTableSingleResultMapper extends SingleResultMapperBase {
    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public ActionListTableSingleResultMapper(String command, String originalId, ActionResult result, IWebLocalizationManager languageManager) throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((EventResult) getResult()).getEvent();
    }
}
