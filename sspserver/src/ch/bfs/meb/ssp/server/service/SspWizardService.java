/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiErrorListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspWizardSchoolListResult;
import ch.bfs.meb.ssp.server.service.impl.IWizardService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspWizardWebService", name = "SspWizardWebServicePortType")
public class SspWizardService extends AbstractMebWebService<IWizardService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public UserNameListResult getDlUserNames(Long version) {
        return getService().getDlUserNames(version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspWizardSchoolListResult getWizardSchools(String dlUser, Long version) {
        return getService().getSchools(dlUser, version);
    }

    //	@WebMethod
    //	@PreAuthorize("hasAuthority('"+SecurityConstants.ROLE_SSP_DL+"')")
    //	public BurSchoolResult deleteWizardSchool(String dlUser, Long version, BurSchool burSchool)
    //	{
    //		return getService().deleteSchool(dlUser, version, burSchool);
    //	}

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryListResult deleteDeliveries(String dlUser, Long version) {
        return getService().deleteDeliveries(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPlausiErrorListResult getWizardErrors(String dlUser, Long version) {
        return getService().getErrors(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public FileResult getWizardPlausireport(String dlUser, Long version, String locale) {
        return getService().getPlausireport(dlUser, version, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPlausiErrorListResult confirmWizardErrors(List<SspPlausiError> plausiErrors) {
        return getService().confirmErrors(plausiErrors);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        return getService().areDeliveriesValidated(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        return getService().validateDeliveries(dlUser, version, locale);
    }
}