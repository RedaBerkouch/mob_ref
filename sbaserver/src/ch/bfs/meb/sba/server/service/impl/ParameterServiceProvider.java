/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaParameter;
import ch.bfs.meb.sba.server.integration.repository.IParameterRepository;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.service.impl.IParameterServiceProvider;

/**
 * Sba specific parameter services.
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
        return _parameterRepository.insertParameter(new SbaParameter(parameter));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * updateParameter(ch.bfs.meb.server.commons.integration.dto.Parameter)
     */
    @Override
    public Parameter updateParameter(Parameter parameter) {
        return _parameterRepository.updateParameter(new SbaParameter(parameter));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * deleteParameter(ch.bfs.meb.server.commons.integration.dto.Parameter)
     */
    @Override
    public void deleteParameter(Parameter parameter) {
        _parameterRepository.deleteParameter(new SbaParameter(parameter));
    }
}
