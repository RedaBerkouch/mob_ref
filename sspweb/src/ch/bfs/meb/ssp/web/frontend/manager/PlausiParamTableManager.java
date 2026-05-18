/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.frontend.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.ws.sspparameter.Parameter;
import ch.bfs.meb.ssp.web.ws.sspparameter.ParameterListResult;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterList;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * This Class represents a PlausiParamTableManager for the admin tab and acts as
 * a controller for the PlausiParam Table.
 */
@Scope("session")
@Component("plausiParamTableManager")
public class PlausiParamTableManager extends ParamTableManagerBase {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(PlausiParamTableManager.class);

    public static final String MANAGER_NAME = "plausiParam";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    /**
     * Return the name of the manager
     * 
     * @return the managers name
     */
    @Override
    public String getName() {
        return MANAGER_NAME;
    }

    /**
     * Return the control name of the manager
     * 
     * @return the managers control name
     */
    @Override
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

    @Override
    public IDhtmlxControl getFilterTable() {
        return new IDhtmlxControl() {
            @Override
            public String getControlName() {
                return AdminPlausiTableManager.CONTROL_NAME;
            }

            @Override
            public String getName() {
                return AdminPlausiTableManager.MANAGER_NAME;
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
            parameters = _parameterService.getParametersForPlausi(selected);
        }

        return parameters;
    }

    @Override
    protected void setMasterKey(Parameter parameter, Long foreignKey) {
        parameter.setPlausiId(foreignKey);
    }
}
