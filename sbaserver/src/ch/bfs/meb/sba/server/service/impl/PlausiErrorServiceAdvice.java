/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: PlausiErrorServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.bfs.meb.sba.server.service.impl;

import ch.bfs.meb.sba.server.integration.dto.SbaPlausiErrorListResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 *
 */
public class PlausiErrorServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        // no single obj so far
        return new SbaPlausiErrorListResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SbaPlausiErrorListResult(message);
    }
}
