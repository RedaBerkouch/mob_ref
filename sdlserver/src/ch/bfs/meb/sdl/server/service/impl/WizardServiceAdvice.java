/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: WizardServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.bfs.meb.sdl.server.service.impl;

import ch.bfs.meb.sdl.server.integration.dto.SdlWizardSchoolListResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 *
 */
public class WizardServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        // no single obj so far
        return new SdlWizardSchoolListResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SdlWizardSchoolListResult(message);
    }
}
