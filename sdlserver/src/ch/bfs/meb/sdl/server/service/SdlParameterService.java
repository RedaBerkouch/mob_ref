/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.dto.ParameterListResult;
import ch.bfs.meb.server.commons.integration.dto.ParameterResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IParameterService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SdlParameterWebService", name = "SdlParameterWebServicePortType")
public class SdlParameterService extends AbstractMebWebService<IParameterService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterListResult getParametersForFilter(Long filterId) {
        return getService().getParametersForFilter(filterId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterListResult getParametersForExport(Long exportId) {
        return getService().getParametersForExport(exportId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterListResult getParametersForPlausi(Long plausiId) {
        return getService().getParametersForPlausi(plausiId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public ParameterResult getParameterById(Long parameterId) {
        return getService().getParameterById(parameterId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public ParameterResult updateParameter(Parameter parameter) {
        return getService().updateParameter(parameter);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public ParameterResult insertParameter(Parameter parameter) {
        return getService().insertParameter(parameter);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public ParameterResult deleteParameter(Parameter parameter) {
        return getService().deleteParameter(parameter);
    }
}