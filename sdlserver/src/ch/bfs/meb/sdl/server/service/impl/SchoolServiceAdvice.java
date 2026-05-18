/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: SchoolServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.sdl.server.integration.dto.SdlSchoolListResult;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchoolResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class SchoolServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SdlSchoolResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SdlSchoolListResult(message);
    }
}
