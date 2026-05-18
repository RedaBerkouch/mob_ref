/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.service.impl.IParameterServiceProvider;
import ch.bfs.meb.ssp.server.integration.dto.SspParameter;
import ch.bfs.meb.ssp.server.integration.repository.IParameterRepository;

/**
 * Ssp specific parameter services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 382 $
 */
public class ParameterServiceProvider implements IParameterServiceProvider {
    private IParameterRepository _parameterRepository;

    public void setParameterRepository(IParameterRepository parameterRepository) {
        _parameterRepository = parameterRepository;
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * getParameterById(java.lang.Long)
     */
    @Override
    public Parameter getParameterById(Long parameterId) {
        return _parameterRepository.getParameterById(parameterId);
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * getParametersForExport(java.lang.Long)
     */
    @Override
    public List<Parameter> getParametersForExport(Long exportId) {
        return Collections.unmodifiableList(new ArrayList<Parameter>(_parameterRepository.getParametersForExport(exportId)));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * getParametersForFilter(java.lang.Long)
     */
    @Override
    public List<Parameter> getParametersForFilter(Long filterId) {
        return Collections.unmodifiableList(new ArrayList<Parameter>(_parameterRepository.getParametersForFilter(filterId)));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * getParametersForPlausi(java.lang.Long)
     */
    @Override
    public List<Parameter> getParametersForPlausi(Long plausiId) {
        return Collections.unmodifiableList(new ArrayList<Parameter>(_parameterRepository.getParametersForPlausi(plausiId)));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * insertParameter(ch.bfs.meb.server.commons.integration.dto.Parameter)
     */
    @Override
    public Parameter insertParameter(Parameter parameter) {
        return _parameterRepository.insertParameter(new SspParameter(parameter));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * updateParameter(ch.bfs.meb.server.commons.integration.dto.Parameter)
     */
    @Override
    public Parameter updateParameter(Parameter parameter) {
        return _parameterRepository.updateParameter(new SspParameter(parameter));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * deleteParameter(ch.bfs.meb.server.commons.integration.dto.Parameter)
     */
    @Override
    public void deleteParameter(Parameter parameter) {
        _parameterRepository.deleteParameter(new SspParameter(parameter));
    }
}
