/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: ConfirmableInternalPlausi.java  02.05.2012 21:42:05 Administrator $

 */
/*

 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Base class for internal plausis
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 892 $ 
 */
public class InternalPlausiBO extends PlausiBO {
    protected HashMap<String, SbaPlausiError> _confirmedInternalErrorMap = null;

    public InternalPlausiBO(SbaPlausi plausi, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
    }

    public InternalPlausiBO(SbaPlausi plausi, IServerLocalizationManager localizationManager, HashMap<String, SbaPlausiError> confirmedInternalErrorMap) {
        super(plausi, localizationManager);
        _confirmedInternalErrorMap = confirmedInternalErrorMap;
    }

    public void setConfirmablePlausiErrorMap(HashMap<String, SbaPlausiError> confirmedInternalErrorMap) {
        _confirmedInternalErrorMap = confirmedInternalErrorMap;
    }

    protected void updateConfirmedPlausierrors(List<PlausierrorBO> errorList, boolean confirmErrors) {
        if (_confirmedInternalErrorMap == null) {
            super.updateConfirmedPlausierrors(errorList, confirmErrors);
            return;
        }
        // else: replace/amend use case (see Mantis 1783)
        for (PlausierrorBO plausierrorBO : errorList) {
            SbaPlausiError newError = plausierrorBO.getThisPlausierror();
            if (newError.getPlausi().getPlausiId().equals(_thisPlausi.getPlausiId()) && (newError.getErrorId() == null)) {
                if (confirmErrors) {
                    newError.setIsConfirmed(true);
                } else {
                    SbaPlausiError confirmedError = _confirmedInternalErrorMap.get(newError.getLogicalKey());
                    if (confirmedError != null && confirmedError.getIsConfirmed() != newError.getIsConfirmed()) {
                        // take over confirmation information
                        newError.setIsConfirmed(confirmedError.getIsConfirmed());
                        newError.setModification_user(confirmedError.getModification_user());
                        newError.setModification_date(confirmedError.getModification_date());
                    }
                }
            }
        }
    }
}
