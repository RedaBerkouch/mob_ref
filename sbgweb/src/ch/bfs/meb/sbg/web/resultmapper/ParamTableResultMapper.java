/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ParamTableResultMapper.java 300 2007-08-30 14:20:29Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.resultmapper;

import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.MacroParameterResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 300 $
 */
public class ParamTableResultMapper extends SingleResultMapperBase {
    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public ParamTableResultMapper(String command, String originalId, MacroParameterResult result, IWebLocalizationManager languageManager)
            throws DhtmlxException {
        super(command, originalId, result, languageManager);
    }

    public Object getData() {
        return ((MacroParameterResult) getResult()).getMacroParameter();
    }
}
