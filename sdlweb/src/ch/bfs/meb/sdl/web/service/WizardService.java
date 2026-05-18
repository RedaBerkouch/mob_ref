/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb
 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sdl.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sdl.web.ws.sdlwizard.*;
import ch.bfs.meb.util.SecurityConstants;

@Service("wizardService")
public class WizardService implements IWizardService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DV + "')")
    public UserNameListResult getDlUserNames(Long version) {
        return webServiceClientFactory.getWizardWebService().getDlUserNames(version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlWizardSchoolListResult getSchools(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().getWizardSchools(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public BurSchoolResult deleteSchool(String dlUser, Long version, BurSchool burSchool) {
        return webServiceClientFactory.getWizardWebService().deleteWizardSchool(dlUser, version, burSchool);
    }

    //	@Override
    //	@PreAuthorize("hasAuthority('"+SecurityConstants.ROLE_SDL_DL+"')")
    //	public SdlDeliveryResult deleteDelivery(String dlUser, Long version)
    //	{
    //		return webServiceClientFactory.getWizardWebService().deleteDelivery(dlUser, version);
    //	}

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlPlausiErrorListResult getErrors(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().getWizardErrors(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public FileResult getPlausireport(String dlUser, Long version, String locale) {
        return webServiceClientFactory.getWizardWebService().getWizardPlausireport(dlUser, version, locale);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlPlausiErrorListResult confirmErrors(List<SdlPlausiError> plausiErrors) {
        return webServiceClientFactory.getWizardWebService().confirmWizardErrors(plausiErrors);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().areDeliveriesValidated(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        return webServiceClientFactory.getWizardWebService().validateDeliveries(dlUser, version, locale);
    }
}
