/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

 */
package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.ws.sdlplausi.Plausi;
import ch.bfs.meb.sdl.web.ws.sdlplausi.PlausiListResult;
import ch.bfs.meb.sdl.web.ws.sdlplausi.PlausiResult;

public interface IPlausiService {
    public PlausiListResult getPlausis();

    public PlausiResult getPlausiById(Long plausiId);

    public PlausiResult updatePlausi(Plausi plausi);

    public PlausiResult insertPlausi(Plausi plausi);

    public PlausiResult deletePlausi(Plausi plausi);
}
