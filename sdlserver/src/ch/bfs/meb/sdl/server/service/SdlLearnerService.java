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

import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearnerListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearnerResult;
import ch.bfs.meb.sdl.server.service.impl.ILearnerService;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SdlLearnerWebService", name = "SdlLearnerWebServicePortType")
public class SdlLearnerService extends AbstractMebWebService<ILearnerService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlLearnerListResult getLearners(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getLearners(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlLearnerListResult getLearnersOwnedByClasses(List<Long> classIds, SortContext sortContext) {
        return getService().getLearnersOwnedByClasses(classIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlLearnerResult getLearnerById(Long learnerId) {
        return getService().getLearnerById(learnerId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForLearner(Long learnerId) {
        return getService().getPlausiErrorsForLearner(learnerId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult updateLearner(SdlLearner learner, List<PlausiError> plausiErrors, boolean noPlausi) {
        return getService().updateLearner(learner, plausiErrors, noPlausi, false);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult insertLearner(SdlLearner learner, boolean noPlausi) {
        return getService().insertLearner(learner, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult deleteLearner(SdlLearner learner, boolean noPlausi) {
        return getService().deleteLearner(learner, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult validateLearners(List<Long> learnerList, boolean undo) {
        return getService().validateLearners(learnerList, undo);
    }
}