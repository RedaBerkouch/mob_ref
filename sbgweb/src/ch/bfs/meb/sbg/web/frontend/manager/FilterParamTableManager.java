/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilterParamTableManager.java 322 2007-09-06 14:29:48Z dzw $
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
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterList;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ch.bfs.meb.web.commons.util.IFilterService;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 322 $
 */
@Scope("session")
@Component("filterParamTableManager")
public class FilterParamTableManager extends ParamTableManagerBase {
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(FilterParamTableManager.class);

    public static final String MANAGER_NAME = "filterParam";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    @Autowired
    private IMacroParameterService _macroParameterService;

    @Autowired
    private IFilterService _filterService;

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
        return AdminFilterTableManager.MANAGER_NAME;
    }

    @Override
    public IDhtmlxControl getFilterTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return AdminFilterTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return AdminFilterTableManager.MANAGER_NAME;
            }
        };
    }

    @Override
    public ParameterListResult getRows(ParameterList params) {
        ParameterListResult parameters;
        if (params.getSelectedRows().size() == 0) {
            parameters = new ParameterListResult();
            parameters.setState(ResultBase.OK);
        } else {
            Long selected = params.getSelectedRows().get(0);
            parameters = _macroParameterService.getParametersForFilter(selected);
        }

        return parameters;
    }

    protected void setMasterKey(Parameter parameter, Long foreignKey) {
        parameter.setFilterId(foreignKey);
    }
}
