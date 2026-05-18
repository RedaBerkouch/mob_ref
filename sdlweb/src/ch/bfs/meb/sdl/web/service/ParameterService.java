/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sdl.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sdl.web.ws.sdlparameter.Parameter;
import ch.bfs.meb.sdl.web.ws.sdlparameter.ParameterListResult;
import ch.bfs.meb.sdl.web.ws.sdlparameter.ParameterResult;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Generic parameter services.
 * 
 * @author $Author$
 * @version $Revision$
 */
@Service("parameterService")
public class ParameterService implements IParameterService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterListResult getParametersForFilter(Long filterId) {
        return _webServiceClientFactory.getParameterWebService().getParametersForFilter(filterId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterListResult getParametersForExport(Long exportId) {
        return _webServiceClientFactory.getParameterWebService().getParametersForExport(exportId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterListResult getParametersForPlausi(Long plausiId) {
        return _webServiceClientFactory.getParameterWebService().getParametersForPlausi(plausiId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterResult getParameterById(Long parameterId) {
        return _webServiceClientFactory.getParameterWebService().getParameterById(parameterId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public ParameterResult updateParameter(Parameter parameter) {
        return _webServiceClientFactory.getParameterWebService().updateParameter(parameter);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public ParameterResult insertParameter(Parameter parameter) {
        return _webServiceClientFactory.getParameterWebService().insertParameter(parameter);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public ParameterResult deleteParameter(Parameter parameter) {
        return _webServiceClientFactory.getParameterWebService().deleteParameter(parameter);
    }
}