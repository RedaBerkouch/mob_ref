/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: WizardServiceAdvice.java 1024 2010-03-15 10:34:39Z msc $
 */
package ch.bfs.meb.sba.server.service.impl;

import ch.bfs.meb.sba.server.integration.dto.SbaWizardSchoolListResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author msc
 *
 */
public class WizardServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        // no single obj so far
        return new SbaWizardSchoolListResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SbaWizardSchoolListResult(message);
    }
}
