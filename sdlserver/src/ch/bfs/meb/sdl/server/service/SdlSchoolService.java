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

import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchoolListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchoolResult;
import ch.bfs.meb.sdl.server.service.impl.ISchoolService;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SdlSchoolWebService", name = "SdlSchoolWebServicePortType")
public class SdlSchoolService extends AbstractMebWebService<ISchoolService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlSchoolListResult getSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getSchools(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlSchoolListResult getSchoolsOwnedByClasses(List<Long> classIds, SortContext sortContext) {
        return getService().getSchoolsOwnedByClasses(classIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlSchoolResult getSchoolById(Long schoolId) {
        return getService().getSchoolById(schoolId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForSchool(Long schoolId) {
        return getService().getPlausiErrorsForSchool(schoolId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult updateSchool(SdlSchool school, List<PlausiError> plausiErrors, boolean noPlausi) {
        return getService().updateSchool(school, plausiErrors, noPlausi, false);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult insertSchool(SdlSchool school, boolean noPlausi) {
        return getService().insertSchool(school, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult deleteSchool(SdlSchool school, boolean noPlausi) {
        return getService().deleteSchool(school, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult validateSchools(List<Long> schoolList, boolean undo) {
        return getService().validateSchools(schoolList, undo);
    }
}