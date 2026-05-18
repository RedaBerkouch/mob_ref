/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.sba.server.business.DeliveryBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.repository.IRepositoryProvider;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

/**
 * Business class for optimized external (SAS) plausi process
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ExternalPlausiProcess {
    private final IRepositoryProvider _repositories;
    private final List<PlausiBO> _externalPlausis;
    private HashMap<Long, String> _personConfirmRules;
    private HashMap<Long, String> _qualificationConfirmRules;

    public ExternalPlausiProcess(IRepositoryProvider repositories, List<PlausiBO> externalPlausis) {
        _repositories = repositories;
        _externalPlausis = externalPlausis;
    }

    /**
     * // Optimized handling of external plausi process
     * 
     * @param delivery
     */
    public void verify(DeliveryBO delivery) {
        // 1. Get all old external plausi errors for delivery
        List<SbaPlausiError> oldErrors = _repositories.getPlausierrorRepository().findExternalErrorsForDelivery(delivery.getThisDelivery().getDeliveryId(),
                true);
        // build Map for confirmed errors
        HashMap<String, SbaPlausiError> oldConfirmedErrorsMap = new HashMap<String, SbaPlausiError>();
        for (SbaPlausiError error : oldErrors) {
            if (error.getIsConfirmed()) {
                oldConfirmedErrorsMap.put(getErrorKey(error), error);
            }
        }

        // 2. Run the external plausis
        for (PlausiBO plausi : _externalPlausis) {
            plausi.verify(delivery);
        }

        // 3. Read back all the new errors for delivery
        List<SbaPlausiError> newErrors = _repositories.getPlausierrorRepository().findExternalErrorsForDelivery(delivery.getThisDelivery().getDeliveryId(),
                false);

        // 4. Set confirmation flag according to confirmRules and old errors for persons and activities
        List<SbaPlausiError> newDeliveryErrors = new ArrayList<SbaPlausiError>();
        for (SbaPlausiError newError : newErrors) {
            if (newError.getPersonId() == null) {
                // Extract errors on level delivery
                newDeliveryErrors.add(newError);
            } else if (newError.getPlausi().getIsConfirmable()) {
                // Error has been associated with another business object
                // Get corresponding old confirmed error and update confirmation data from old to new error
                SbaPlausiError oldConfirmedError = oldConfirmedErrorsMap.get(getErrorKey(newError));
                // take over confirmation information
                if (oldConfirmedError != null) {
                    newError.setIsConfirmed(oldConfirmedError.getIsConfirmed());
                    newError.setModification_user(oldConfirmedError.getModification_user());
                    newError.setModification_date(oldConfirmedError.getModification_date());
                    _repositories.getPlausierrorRepository().updatePlausiError(newError);
                }
                checkForConfirmRules(newError);
            }
        }

        // Merge with existing errors on business object
        updateBusinessObjectErrors(delivery, delivery.getPlausierrors(), newDeliveryErrors, oldConfirmedErrorsMap);
        delivery.getThisDelivery().getPlausierrors().removeAll(oldErrors);
    }

    protected String getErrorKey(SbaPlausiError error) {
        String errorKey = "";

        errorKey = errorKey + error.getDeliveryId().toString();
        errorKey = errorKey + (error.getPersonId() == null ? "" : "_" + error.getPersonId().toString());
        errorKey = errorKey + (error.getQualificationId() == null ? "" : "_" + error.getQualificationId().toString());
        errorKey = errorKey + (error.getPlausi().getId() == null ? "" : "_" + error.getPlausi().getId().toString());
        errorKey = errorKey + (error.getConfirmId() == null ? "" : "#" + error.getConfirmId());

        return errorKey;
    }

    protected void checkForConfirmRules(SbaPlausiError newError) {
        if (newError.getPlausi().getIsConfirmable() && !newError.getIsConfirmed()) {
            // initialise confirmRules data?
            if (_personConfirmRules == null) {
                _personConfirmRules = _repositories.getDeliveryRepository().getPersonConfirmRules(newError.getDeliveryId());
                _qualificationConfirmRules = _repositories.getDeliveryRepository().getQualificationConfirmRules(newError.getDeliveryId());
            }

            // Lookup confirmRules information
            String confirmRules;
            if (newError.getQualificationId() == null) {
                confirmRules = _personConfirmRules.get(newError.getPersonId());
            } else {
                confirmRules = _qualificationConfirmRules.get(newError.getQualificationId());
            }

            // Confirm error if id is in confirmRules
            List<String> plausiIdsToConfirm = null;
            if (!StringUtils.isEmpty(confirmRules)) {
                plausiIdsToConfirm = new ArrayList<String>();
                for (String plausi : confirmRules.split("\\|")) {
                    plausiIdsToConfirm.add(plausi.trim());
                }
            }
            if (plausiIdsToConfirm != null && newError.getPlausi().getId() != null && plausiIdsToConfirm.contains(newError.getPlausi().getId())) {
                newError.setIsConfirmed(true);
                _repositories.getPlausierrorRepository().updatePlausiError(newError);
            }
        }
    }

    // update errors for verified business object
    private void updateBusinessObjectErrors(BOBase businessObject, List<PlausierrorBO> allOldErrors, List<SbaPlausiError> allNewErrors,
            HashMap<String, SbaPlausiError> oldConfirmedErrorsMap) {
        // 1. extract all old external plausierrors
        List<PlausierrorBO> oldExternalErrorBos = new ArrayList<PlausierrorBO>();
        for (PlausierrorBO peBo : allOldErrors) {
            if (peBo.getThisPlausierror().getPlausi().getType().equals(CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL)) {
                oldExternalErrorBos.add(peBo);
            }
        }

        // 2. update confirmation data from old to new errors
        for (SbaPlausiError newError : allNewErrors) {
            // get corresponding old confirmed error and update confirmation data from old to new error
            SbaPlausiError oldConfirmedError = oldConfirmedErrorsMap.get(getErrorKey(newError));
            // take over confirmation information
            if (oldConfirmedError != null) {
                newError.setIsConfirmed(oldConfirmedError.getIsConfirmed());
                newError.setModification_user(oldConfirmedError.getModification_user());
                newError.setModification_date(oldConfirmedError.getModification_date());
            }
            // check confirmRules attribute and confirm error if required
            if (businessObject.canBeConfirmed(newError.getPlausi().getId())) {
                newError.setIsConfirmed(true); // date and user set by PlausierrorBO.save
            }
        }

        // 3. Remove all old external errors 
        _repositories.getPlausierrorRepository().deletePlausiErrors(oldExternalErrorBos);
        for (PlausierrorBO peBo : oldExternalErrorBos) {
            _repositories.getPlausierrorRepository().deletePlausiError(peBo.getThisPlausierror());
            allOldErrors.remove(peBo);
        }

        // 4. Add new external errors to the BO
        for (SbaPlausiError newError : allNewErrors) {
            allOldErrors.add(new PlausierrorBO(newError));
        }
    }
}
