/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroTableResultMapper.java 300 2007-08-30 14:20:29Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.resultmapper;

import ch.bfs.meb.sbg.web.ws.sbgmacro.MacroResult;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.SingleResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 300 $
 */
public class MacroTableResultMapper extends SingleResultMapperBase {
    /**
     * @param command
     * @param originalId
     * @param result
     * @throws DhtmlxException
     */
    public MacroTableResultMapper(String command, String originalId, MacroResult result, IWebLocalizationManager localizationManager) throws DhtmlxException {
        super(command, originalId, result, localizationManager);
    }

    public Object getData() {
        return ((MacroResult) getResult()).getMacro();
    }
}
