/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;
import ch.bfs.meb.sdl.server.integration.repository.IParameterRepository;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.service.impl.IParameterServiceProvider;

/**
 * SdL specific parameter services.
 * 
 * @author $Author$
 * @version $Revision$
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
        return _parameterRepository.insertParameter(new SdlParameter(parameter));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * updateParameter(ch.bfs.meb.server.commons.integration.dto.Parameter)
     */
    @Override
    public Parameter updateParameter(Parameter parameter) {
        return _parameterRepository.updateParameter(new SdlParameter(parameter));
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.server.commons.service.impl.IParameterServiceProvider#
     * deleteParameter(ch.bfs.meb.server.commons.integration.dto.Parameter)
     */
    @Override
    public void deleteParameter(Parameter parameter) {
        _parameterRepository.deleteParameter(new SdlParameter(parameter));
    }
}
