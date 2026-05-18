/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiErrorListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaWizardSchoolListResult;
import ch.bfs.meb.sba.server.service.impl.IWizardService;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SbaWizardWebService", name = "SbaWizardWebServicePortType")
public class SbaWizardService extends AbstractMebWebService<IWizardService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DV + "')")
    public UserNameListResult getDlUserNames(Long version) {
        return getService().getDlUserNames(version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaWizardSchoolListResult getWizardSchools(String dlUser, Long version) {
        return getService().getSchools(dlUser, version);
    }

    //	@WebMethod
    //	@PreAuthorize("hasAuthority('"+SecurityConstants.ROLE_SBA_DL+"')")
    //	public BurSchoolResult deleteWizardSchool(String dlUser, Long version, BurSchool burSchool)
    //	{
    //		return getService().deleteSchool(dlUser, version, burSchool);
    //	}

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryListResult deleteDeliveries(String dlUser, Long version) {
        return getService().deleteDeliveries(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPlausiErrorListResult getWizardErrors(String dlUser, Long version) {
        return getService().getErrors(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public FileResult getWizardPlausireport(String dlUser, Long version, String locale) {
        return getService().getPlausireport(dlUser, version, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPlausiErrorListResult confirmWizardErrors(List<SbaPlausiError> plausiErrors) {
        return getService().confirmErrors(plausiErrors);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public Boolean areDeliveriesValidated(String dlUser, Long version) {
        return getService().areDeliveriesValidated(dlUser, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryListResult validateDeliveries(String dlUser, Long version, String locale) {
        return getService().validateDeliveries(dlUser, version, locale);
    }
}