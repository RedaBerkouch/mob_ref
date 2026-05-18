/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb
 */
package ch.bfs.meb.sba.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbawizard.*;
import ch.bfs.meb.util.SecurityConstants;

@Service("wizardService")
public class WizardService implements IWizardService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public UserNameListResult getDlUserNames(Long version) {
        return webServiceClientFactory.getWizardWebService().getDlUserNames(version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaWizardSchoolListResult getSchools(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().getWizardSchools(dlUser, version);
    }

    //	@Override
    //	@PreAuthorize("hasAuthority('"+SecurityConstants.ROLE_SBA_DL+"')")
    //	public SbaSchoolResult deleteSchool(SbaSchool sbaSchool)
    //	{
    //		return webServiceClientFactory.getWizardWebService().deleteWizardSchool(sdlSchool);
    //	}

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryListResult deleteDeliveries(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().deleteDeliveries(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPlausiErrorListResult getErrors(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().getWizardErrors(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public FileResult getPlausireport(String dlUser, Long version, String locale) {
        return webServiceClientFactory.getWizardWebService().getWizardPlausireport(dlUser, version, locale);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPlausiErrorListResult confirmErrors(List<SbaPlausiError> plausiErrors) {
        return webServiceClientFactory.getWizardWebService().confirmWizardErrors(plausiErrors);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        return webServiceClientFactory.getWizardWebService().areDeliveriesValidated(dlUser, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        return webServiceClientFactory.getWizardWebService().validateDeliveries(dlUser, version, locale);
    }
}
