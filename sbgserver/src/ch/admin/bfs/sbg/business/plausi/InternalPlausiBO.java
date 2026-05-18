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
package ch.admin.bfs.sbg.business.plausi;

import java.util.HashMap;
import java.util.List;

import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;

/** 
 * Base class for internal plausis
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 892 $ 
 */
public class InternalPlausiBO extends PlausiBO {
    protected HashMap<String, Plausierror> _confirmedInternalErrorMap = null;

    public InternalPlausiBO(Macro plausi) {
        super(plausi);
    }

    public InternalPlausiBO(Macro plausi, HashMap<String, Plausierror> confirmedInternalErrorMap) {
        super(plausi);
        _confirmedInternalErrorMap = confirmedInternalErrorMap;
    }

    public void setConfirmablePlausiErrorMap(HashMap<String, Plausierror> confirmedInternalErrorMap) {
        _confirmedInternalErrorMap = confirmedInternalErrorMap;
    }

    protected void updateConfirmedPlausierrors(List<PlausierrorBO> errorList) {
        if (_confirmedInternalErrorMap == null) {
            super.updateConfirmedPlausierrors(errorList);
            return;
        }
        // else: replace/amend use case (see Mantis 1783)
        for (PlausierrorBO plausierrorBO : errorList) {
            Plausierror newError = plausierrorBO.get_thisPlausierror();
            if (newError.getPlausiId().equals(_thisPlausi.getMacroid()) && (newError.getErrorId() == null)) {
                Plausierror confirmedError = _confirmedInternalErrorMap.get(newError.getLogicalKey());
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
