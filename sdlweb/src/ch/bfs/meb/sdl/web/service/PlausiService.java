/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb
 */
package ch.bfs.meb.sdl.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sdl.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sdl.web.ws.sdlplausi.Plausi;
import ch.bfs.meb.sdl.web.ws.sdlplausi.PlausiListResult;
import ch.bfs.meb.sdl.web.ws.sdlplausi.PlausiResult;
import ch.bfs.meb.util.SecurityConstants;

@Service("plausiService")
public class PlausiService implements IPlausiService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiListResult getPlausis() {
        return webServiceClientFactory.getPlausiWebService().getPlausis();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiResult getPlausiById(Long plausiId) {
        return webServiceClientFactory.getPlausiWebService().getPlausiById(plausiId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public PlausiResult updatePlausi(Plausi plausi) {
        return webServiceClientFactory.getPlausiWebService().updatePlausi(plausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public PlausiResult insertPlausi(Plausi plausi) {
        return webServiceClientFactory.getPlausiWebService().insertPlausi(plausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EA + "')")
    public PlausiResult deletePlausi(Plausi plausi) {
        return webServiceClientFactory.getPlausiWebService().deletePlausi(plausi);
    }
}
