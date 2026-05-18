/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: MacroParameterServiceImpl.java 564 2008-11-28 12:57:40Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.webservice;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.admin.bfs.sbg.db.dao.MacroParameterDAO;
import ch.admin.bfs.sbg.transfer.MacroParameterResult;
import ch.bfs.meb.sbg.server.integration.dto.ParameterListResult;
import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * This class implements the MacroParameterService interface. It handles all the
 * calls that the macro parameter service provides to the client.
 *
 * @author $Author: lsc $
 * @version $Revision: 564 $
 */
@Service
public class MacroParameterServiceImpl implements IMacroParameterService, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DUPLICATE_PARAMETER_ERROR_MESSAGE = "duplicate.parameter.error.message";

    private final static Logger LOGGER = LoggerFactory.getLogger(MacroParameterServiceImpl.class);

    protected MacroParameterDAO _macroParameterDAO;

    public void setMacroParameterDAO(MacroParameterDAO macroParameterDAO) {
        _macroParameterDAO = macroParameterDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public MacroParameterResult getParameterById(Long parameterId) {
        SbgParameter parameter = _macroParameterDAO.findById(parameterId);
        return new MacroParameterResult(parameter);
    }

    @Override
    @Transactional(readOnly = true)
    public ParameterListResult getParametersForFilter(Long filterId) {
        List<SbgParameter> parameters = Collections.unmodifiableList(_macroParameterDAO.findByFilterid(filterId));
        return new ParameterListResult(parameters);
    }

    @Override
    @Transactional(readOnly = true)
    public ParameterListResult getParametersForMacro(Long macroId) {
        List<SbgParameter> parameters = Collections.unmodifiableList(_macroParameterDAO.findByMacroid(macroId));
        return new ParameterListResult(parameters);
    }

    @Override
    @Transactional
    public MacroParameterResult updateParameter(Parameter parameter, String locale) {
        List<SbgParameter> paramsFor;
        if (parameter.getFilterId() != null) {
            paramsFor = _macroParameterDAO.findByFilterid(parameter.getFilterId());
        } else {
            //XXX Simon: Unsafe downcast workaround! Will be replaced with general macro/filter combination from MEB.
            paramsFor = _macroParameterDAO.findByMacroid(((SbgParameter) parameter).getMacroId());
        }
        HashSet<String> uniqueIds = new HashSet<String>();
        for (SbgParameter p : paramsFor) {
            if (!p.getParameterId().equals(parameter.getParameterId())) {
                uniqueIds.add(p.getUniqueName());
            }
        }
        if (uniqueIds.contains(parameter.getUniqueName())) {
            return new MacroParameterResult(DUPLICATE_PARAMETER_ERROR_MESSAGE);
        }

        SbgParameter sbgParameter = _macroParameterDAO.merge(parameter);
        return new MacroParameterResult(sbgParameter);
    }

    @Override
    @Transactional
    public MacroParameterResult insertParameter(Parameter parameter, String locale) {
        //XXX Workaround/hack to get the macroid to link new parameter with macro in sql.
        SbgParameter sbgParameter = (SbgParameter) parameter;
        if (parameter.getExportId() != null && sbgParameter.getMacroId() == null) {
            sbgParameter.setMacroId(parameter.getExportId());
        }

        List<SbgParameter> paramsFor;
        if (sbgParameter.getFilterId() != null) {
            paramsFor = _macroParameterDAO.findByFilterid(sbgParameter.getFilterId());
        } else {
            //XXX Simon: Unsafe downcast workaround! Will be replaced with general macro/filter combination from MEB.
            paramsFor = _macroParameterDAO.findByMacroid(sbgParameter.getMacroId());
        }
        HashSet<String> uniqueIds = new HashSet<String>();
        for (SbgParameter p : paramsFor) {
            uniqueIds.add(p.getUniqueName());
        }
        if (uniqueIds.contains(sbgParameter.getUniqueName())) {
            return new MacroParameterResult(DUPLICATE_PARAMETER_ERROR_MESSAGE);
        }

        sbgParameter = _macroParameterDAO.merge(sbgParameter);
        return new MacroParameterResult(sbgParameter);
    }

    @Override
    @Transactional
    public MacroParameterResult deleteParameter(Parameter parameter) {
        _macroParameterDAO.delete(_macroParameterDAO.findById(parameter.getParameterId()));
        return new MacroParameterResult();
    }
}