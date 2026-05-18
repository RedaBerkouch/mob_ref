/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: QualificationServiceAdvice.java 432 2010-01-14 13:20:52Z dzw $
 */
package ch.bfs.meb.sba.server.service.impl;

import ch.bfs.meb.sba.server.integration.dto.SbaQualificationListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaQualificationResult;
import ch.bfs.meb.server.commons.service.impl.BaseServiceAdvice;

/**
 * @author jfu
 *
 */
public class QualificationServiceAdvice extends BaseServiceAdvice {
    @Override
    protected Object newResult(String message) {
        return new SbaQualificationResult(message);
    }

    @Override
    protected Object newListResult(String message) {
        return new SbaQualificationListResult(message);
    }
}
