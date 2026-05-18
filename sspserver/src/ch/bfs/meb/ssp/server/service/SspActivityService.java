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

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.ssp.server.integration.dto.SspActivity;
import ch.bfs.meb.ssp.server.integration.dto.SspActivityListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspActivityResult;
import ch.bfs.meb.ssp.server.service.impl.IActivityService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspActivityWebService", name = "SspActivityWebServicePortType")
public class SspActivityService extends AbstractMebWebService<IActivityService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspActivityListResult getActivities(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getActivities(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspActivityListResult getActivitiesOwnedByPersons(List<Long> personIds, SortContext sortContext) {
        return getService().getActivitiesOwnedByPersons(personIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspActivityResult getActivityById(Long activityId) {
        return getService().getActivityById(activityId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForActivity(Long activityId) {
        return getService().getPlausiErrorsForActivity(activityId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspActivityResult updateActivity(SspActivity activity, List<PlausiError> plausiErrors, boolean noPlausi) {
        return getService().updateActivity(activity, plausiErrors, noPlausi, false);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspActivityResult insertActivity(SspActivity activity, boolean noPlausi) {
        return getService().insertActivity(activity, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspActivityResult deleteActivity(SspActivity activity, boolean noPlausi) {
        return getService().deleteActivity(activity, noPlausi);
    }
}