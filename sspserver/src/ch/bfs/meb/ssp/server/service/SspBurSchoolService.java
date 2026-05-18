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

import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IBurSchoolService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspBurSchoolWebService", name = "SspBurSchoolWebServicePortType")
public class SspBurSchoolService extends AbstractMebWebService<IBurSchoolService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public BurSchoolListResult getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch) {
        return getService().getBurSchools(start, buffer, sortContext, filterContext, version, canton, showBurSynch);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public BurSchoolListResult getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch) {
        return getService().getBurSchoolsOwnedByConfigDeliveries(configDeliveryIds, sortContext, showBurSynch);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public BurSchoolResult getBurSchoolById(Long burSchoolId, boolean showBurSynch, Long version) {
        return getService().getBurSchoolById(burSchoolId, showBurSynch, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public BurSchoolResult getBurSchoolByIdAndType(String schoolId, String schoolType, Long version) {
        return getService().getBurSchoolByIdAndType(schoolId, schoolType, version);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public BurSchoolListResult synchronizeSchools() {
        return getService().synchronizeSchools();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public BurSchoolListResult importBurSchools(Long canton) {
        return getService().importBurSchools(canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public BurSchoolResult importBurSchool(BurSchool burSchool) {
        return getService().importBurSchool(burSchool);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public BurSchoolResult updateBurSchool(BurSchool burSchool, boolean showBurSynch) {
        return getService().updateBurSchool(burSchool, showBurSynch);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public BurSchoolResult insertBurSchool(BurSchool burSchool) {
        return getService().insertBurSchool(burSchool);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EV + "')")
    public BurSchoolResult deleteBurSchool(BurSchool burSchool) {
        return getService().deleteBurSchool(burSchool);
    }
}