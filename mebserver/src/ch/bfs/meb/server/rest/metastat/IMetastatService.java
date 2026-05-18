/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.server.rest.metastat;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.CodeGroup;

public interface IMetastatService {
    public abstract List<CodeGroup> getCodesFor(String codegroup, Long canton);
}