/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: LearnerServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.sdl.server.integration.dto.SdlLearnerListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearnerResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class LearnerServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SdlLearnerResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SdlLearnerListResult(message);
    }
}
