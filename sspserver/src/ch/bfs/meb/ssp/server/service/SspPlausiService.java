/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.server.commons.integration.dto.PlausiListResult;
import ch.bfs.meb.server.commons.integration.dto.PlausiResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IPlausiService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspPlausiWebService", name = "SspPlausiWebServicePortType")
public class SspPlausiService extends AbstractMebWebService<IPlausiService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public PlausiListResult getPlausis() {
        return getService().getPlausis();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public PlausiResult getPlausiById(Long plausiId) {
        return getService().getPlausiById(plausiId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EA + "')")
    public PlausiResult updatePlausi(Plausi plausi) {
        return getService().updatePlausi(plausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EA + "')")
    public PlausiResult insertPlausi(Plausi plausi) {
        return getService().insertPlausi(plausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EA + "')")
    public PlausiResult deletePlausi(Plausi plausi) {
        return getService().deletePlausi(plausi);
    }
}