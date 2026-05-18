/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.server.rest.metastat;

import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.CodeGroup;

public class MetastatMockServiceProvider implements IMetastatServiceProvider {
    @Override
    public List<CodeGroup> getCodesFor(String codegroup, Long canton) {
        return new ArrayList<CodeGroup>();
    }
}
