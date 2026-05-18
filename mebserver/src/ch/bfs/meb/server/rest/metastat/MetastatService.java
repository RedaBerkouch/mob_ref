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

public class MetastatService implements IMetastatService {
    private IMetastatServiceProvider _metastatServiceProvider;

    public void setMetastatServiceProvider(IMetastatServiceProvider metastatServiceProvider) {
        this._metastatServiceProvider = metastatServiceProvider;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.rest.service.IMetastatService#getCodesFor(java.lang.String)
     */
    public List<CodeGroup> getCodesFor(String codegroup, Long canton) {
        return this._metastatServiceProvider.getCodesFor(codegroup, canton);
    }
}
