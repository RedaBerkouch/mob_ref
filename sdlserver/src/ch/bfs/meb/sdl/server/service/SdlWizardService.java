/*
 * MEB Portal
 * Bundesamt für Statistik
 * 
 * adesso Schweiz AG
 * Copyright (c) 2009, 2010
 *
 * Projekt: sdlserver
 * 
 */
package ch.bfs.meb.sdl.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.sdl.server.integration.dto.SdlDeliveryListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiErrorListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlWizardSchoolListResult;
import ch.bfs.meb.sdl.server.service.impl.IWizardService;
import ch.bfs.meb.server.commons.integration.dto.BurSchool;
import ch.bfs.meb.server.commons.integration.dto.BurSchoolResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SdlWizardWebService", name = "SdlWizardWebServicePortType")
public class SdlWizardService extends AbstractMebWebService<IWizardService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DV + "')")
    public UserNameListResult getDlUserNames(Long version) {
        return getService().getDlUserNames(version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlWizardSchoolListResult getWizardSchools(String dlUser, Long version) {
        return getService().getSchools(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public BurSchoolResult deleteWizardSchool(String dlUser, Long version, BurSchool burSchool) {
        BurSchoolResult res = getService().deleteSchool(dlUser, version, burSchool);
        if (res.getMessage() != null && !res.getMessage().equals("")) {
            return new BurSchoolResult(res.getMessage());
        }
        res = getService().createPlausierrors(dlUser, version, burSchool);
        if (res.getMessage() != null && !res.getMessage().equals("")) {
            return new BurSchoolResult(res.getMessage());
        } else {
            return new BurSchoolResult(burSchool);
        }
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlPlausiErrorListResult getWizardErrors(String dlUser, Long version) {
        return getService().getErrors(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public FileResult getWizardPlausireport(String dlUser, Long version, String locale) {
        return getService().getPlausireport(dlUser, version, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlPlausiErrorListResult confirmWizardErrors(List<SdlPlausiError> plausiErrors) {
        return getService().confirmErrors(plausiErrors);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        return getService().areDeliveriesValidated(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        return getService().validateDeliveries(dlUser, version, locale);
    }
}