/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb
 */
package ch.bfs.meb.sba.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbaplausi.Plausi;
import ch.bfs.meb.sba.web.ws.sbaplausi.PlausiListResult;
import ch.bfs.meb.sba.web.ws.sbaplausi.PlausiResult;
import ch.bfs.meb.util.SecurityConstants;

@Service("plausiService")
public class PlausiService implements IPlausiService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public PlausiListResult getPlausis() {
        return webServiceClientFactory.getPlausiWebService().getPlausis();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public PlausiResult getPlausiById(Long plausiId) {
        return webServiceClientFactory.getPlausiWebService().getPlausiById(plausiId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public PlausiResult updatePlausi(Plausi plausi) {
        return webServiceClientFactory.getPlausiWebService().updatePlausi(plausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public PlausiResult insertPlausi(Plausi plausi) {
        return webServiceClientFactory.getPlausiWebService().insertPlausi(plausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public PlausiResult deletePlausi(Plausi plausi) {
        return webServiceClientFactory.getPlausiWebService().deletePlausi(plausi);
    }
}
