/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.dto.ParameterListResult;
import ch.bfs.meb.server.commons.integration.dto.ParameterResult;

/**
 * Generic parameter services. Will route to specific provider.
 *
 * @author $Author: dwi $
 * @version $Revision: 132 $
 */
@Service
public class ParameterServiceImpl implements IParameterService {
    IParameterServiceProvider _parameterServiceProvider;

    public void setParameterServiceProvider(IParameterServiceProvider parameterServiceProvider) {
        _parameterServiceProvider = parameterServiceProvider;
    }

    @Override
    @Transactional
    public ParameterResult deleteParameter(Parameter parameter) {
        _parameterServiceProvider.deleteParameter(parameter);
        return new ParameterResult();
    }

    @Override
    public ParameterResult getParameterById(Long parameterId) {
        return new ParameterResult(_parameterServiceProvider.getParameterById(parameterId));
    }

    @Override
    public ParameterListResult getParametersForExport(Long exportId) {
        return new ParameterListResult(_parameterServiceProvider.getParametersForExport(exportId));
    }

    @Override
    public ParameterListResult getParametersForFilter(Long filterId) {
        return new ParameterListResult(_parameterServiceProvider.getParametersForFilter(filterId));
    }

    @Override
    public ParameterListResult getParametersForPlausi(Long plausiId) {
        return new ParameterListResult(_parameterServiceProvider.getParametersForPlausi(plausiId));
    }

    @Override
    @Transactional
    public ParameterResult insertParameter(Parameter parameter) {
        return new ParameterResult(_parameterServiceProvider.insertParameter(parameter));
    }

    @Override
    @Transactional
    public ParameterResult updateParameter(Parameter parameter) {
        return new ParameterResult(_parameterServiceProvider.updateParameter(parameter));
    }
}
