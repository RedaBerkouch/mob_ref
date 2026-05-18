/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.ssp.server.business.DeliveryBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.ssp.server.integration.repository.IRepositoryProvider;
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
    private HashMap<Long, String> _activityConfirmRules;

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
        List<SspPlausiError> oldErrors = _repositories.getPlausierrorRepository().findExternalErrorsForDelivery(delivery.getThisDelivery().getDeliveryId(),
                true);
        // build Map for confirmed errors
        HashMap<String, SspPlausiError> oldConfirmedErrorsMap = new HashMap<String, SspPlausiError>();
        for (SspPlausiError error : oldErrors) {
            if (error.getIsConfirmed()) {
                oldConfirmedErrorsMap.put(getErrorKey(error), error);
            }
        }

        // 2. Run the external plausis
        for (PlausiBO plausi : _externalPlausis) {
            plausi.verify(delivery);
        }

        // 3. Read back all the new errors for delivery
        List<SspPlausiError> newErrors = _repositories.getPlausierrorRepository().findExternalErrorsForDelivery(delivery.getThisDelivery().getDeliveryId(),
                false);

        // 4. Set confirmation flag according to confirmRules and old errors for persons and activities
        List<SspPlausiError> newDeliveryErrors = new ArrayList<SspPlausiError>();
        for (SspPlausiError newError : newErrors) {
            if (newError.getPersonId() == null) {
                // Extract errors on level delivery
                newDeliveryErrors.add(newError);
            } else if (newError.getPlausi().getIsConfirmable()) {
                // Error has been associated with another business object
                // Get corresponding old confirmed error and update confirmation data from old to new error
                SspPlausiError oldConfirmedError = oldConfirmedErrorsMap.get(getErrorKey(newError));
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

    protected String getErrorKey(SspPlausiError error) {
        String errorKey = "";

        errorKey = errorKey + error.getDeliveryId().toString();
        errorKey = errorKey + (error.getPersonId() == null ? "" : "_" + error.getPersonId().toString());
        errorKey = errorKey + (error.getActivityId() == null ? "" : "_" + error.getActivityId().toString());
        errorKey = errorKey + (error.getPlausi().getId() == null ? "" : "_" + error.getPlausi().getId().toString());
        errorKey = errorKey + (error.getConfirmId() == null ? "" : "#" + error.getConfirmId());

        return errorKey;
    }

    protected void checkForConfirmRules(SspPlausiError newError) {
        if (newError.getPlausi().getIsConfirmable() && !newError.getIsConfirmed()) {
            // initialise confirmRules data?
            if (_personConfirmRules == null) {
                _personConfirmRules = _repositories.getDeliveryRepository().getPersonConfirmRules(newError.getDeliveryId());
                _activityConfirmRules = _repositories.getDeliveryRepository().getActivityConfirmRules(newError.getDeliveryId());
            }

            // Lookup confirmRules information
            String confirmRules;
            if (newError.getActivityId() == null) {
                confirmRules = _personConfirmRules.get(newError.getPersonId());
            } else {
                confirmRules = _activityConfirmRules.get(newError.getActivityId());
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
    private void updateBusinessObjectErrors(BOBase businessObject, List<PlausierrorBO> allOldErrors, List<SspPlausiError> allNewErrors,
            HashMap<String, SspPlausiError> oldConfirmedErrorsMap) {
        // 1. extract all old external plausierrors
        List<PlausierrorBO> oldExternalErrorBos = new ArrayList<PlausierrorBO>();
        for (PlausierrorBO peBo : allOldErrors) {
            if (peBo.getThisPlausierror().getPlausi().getType().equals(CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL)) {
                oldExternalErrorBos.add(peBo);
            }
        }

        // 2. update confirmation data from old to new errors
        for (SspPlausiError newError : allNewErrors) {
            // get corresponding old confirmed error and update confirmation data from old to new error
            SspPlausiError oldConfirmedError = oldConfirmedErrorsMap.get(getErrorKey(newError));
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
        for (SspPlausiError newError : allNewErrors) {
            allOldErrors.add(new PlausierrorBO(newError));
        }
    }
}
