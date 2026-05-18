/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: ClassServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.sdl.server.integration.dto.SdlClassListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlClassResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class ClassServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SdlClassResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SdlClassListResult(message);
    }
}
