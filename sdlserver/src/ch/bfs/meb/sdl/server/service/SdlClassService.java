/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlClassListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlClassResult;
import ch.bfs.meb.sdl.server.service.impl.IClassService;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SdlClassWebService", name = "SdlClassWebServicePortType")
public class SdlClassService extends AbstractMebWebService<IClassService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlClassListResult getClasses(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getClasses(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlClassListResult getClassesOwnedBySchools(List<Long> schoolIds, SortContext sortContext) {
        return getService().getClassesOwnedBySchools(schoolIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlClassListResult getClassesOwnedByLearners(List<Long> learnerIds, SortContext sortContext) {
        return getService().getClassesOwnedByLearners(learnerIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlClassResult getClassById(Long sdlClassId) {
        return getService().getClassById(sdlClassId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForClass(Long classId) {
        return getService().getPlausiErrorsForClass(classId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlClassResult updateClass(SdlClass sdlClass, List<PlausiError> plausiErrors, boolean noPlausi) {
        return getService().updateClass(sdlClass, plausiErrors, noPlausi, false, true);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlClassResult insertClass(SdlClass sdlClass, boolean noPlausi) {
        return getService().insertClass(sdlClass, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlClassResult deleteClass(SdlClass sdlClass, boolean noPlausi) {
        return getService().deleteClass(sdlClass, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlClassResult validateClasses(List<Long> classList, boolean undo) {
        return getService().validateClasses(classList, undo);
    }
}