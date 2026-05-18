/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb
 */
package ch.bfs.meb.ssp.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.ssp.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.ssp.web.ws.sspwizard.*;
import ch.bfs.meb.util.SecurityConstants;

@Service("wizardService")
public class WizardService implements IWizardService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public UserNameListResult getDlUserNames(Long version) {
        return webServiceClientFactory.getWizardWebService().getDlUserNames(version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspWizardSchoolListResult getSchools(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().getWizardSchools(dlUser, version);
    }

    //	@Override
    //	@PreAuthorize("hasAuthority('"+SecurityConstants.ROLE_SSP_DL+"')")
    //	public SspSchoolResult deleteSchool(SspSchool sspSchool)
    //	{
    //		return webServiceClientFactory.getWizardWebService().deleteWizardSchool(sdlSchool);
    //	}

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryListResult deleteDeliveries(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().deleteDeliveries(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPlausiErrorListResult getErrors(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().getWizardErrors(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public FileResult getPlausireport(String dlUser, Long version, String locale) {
        return webServiceClientFactory.getWizardWebService().getWizardPlausireport(dlUser, version, locale);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPlausiErrorListResult confirmErrors(List<SspPlausiError> plausiErrors) {
        return webServiceClientFactory.getWizardWebService().confirmWizardErrors(plausiErrors);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().areDeliveriesValidated(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        return webServiceClientFactory.getWizardWebService().validateDeliveries(dlUser, version, locale);
    }
}
