/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroParamTableManager.java 322 2007-09-06 14:29:48Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.frontend.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.service.IMacroParameterService;
import ch.bfs.meb.sbg.web.service.IMacroService;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterList;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 322 $
 */
@Scope("session")
@Component("macroParamTableManager")
public class MacroParamTableManager extends ParamTableManagerBase {
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(MacroParamTableManager.class);

    public static final String MANAGER_NAME = "macroParam";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IMacroParameterService _macroParameterService;

    @Autowired
    private IMacroService _macroService;

    /**
     * Return the name of the manager
     *
     * @return the managers name
     */
    public String getName() {
        return MANAGER_NAME;
    }

    /**
     * Return the control name of the manager
     *
     * @return the managers control name
     */
    public String getControlName() {
        return CONTROL_NAME;
    }

    /**
     * @see ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager#getLocalizationManager()
     */
    @Override
    public IWebLocalizationManager getLocalizationManager() {
        return _localizationManager;
    }

    public IMacroParameterService getMacroParameterService() {
        return _macroParameterService;
    }

    public String getMasterTableManagerName() {
        return AdminMacroTableManager.MANAGER_NAME;
    }

    @Override
    public IDhtmlxControl getFilterTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return AdminMacroTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return AdminMacroTableManager.MANAGER_NAME;
            }
        };
    }

    public ParameterListResult getRows(ParameterList params) {
        ParameterListResult parameter;
        if (params.getSelectedRows().size() > 0) {
            Long selected = params.getSelectedRows().get(0);
            parameter = _macroParameterService.getParametersForMacro(selected);
        } else {
            parameter = new ParameterListResult();
            parameter.setState(ResultBase.OK);
        }
        return parameter;
    }

    protected void setMasterKey(Parameter parameter, Long foreignKey) {
        parameter.setExportId(foreignKey);
    }
}
