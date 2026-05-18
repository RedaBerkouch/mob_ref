/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: ExternalPlausiProcess.java 2519 2012-05-08 13:38:39Z jfu $

 */
package ch.admin.bfs.sbg.business.plausi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Business class for optimized external (SAS) plausi process
 *
 * @author $Author: jfu $
 * @version $Revision: 2519 $
 */
public class ExternalPlausiProcess {
    private final PlausierrorDAO _plausierrorDAO;
    private final HashMap<Long, PlausiBO> _allExternalPlausis;
    private final HashMap<Long, PlausiBO> _externalPlausisOnDelivery;

    public ExternalPlausiProcess(PlausierrorDAO plausierrorDAO, List<PlausiBO> externalPlausis) {
        _plausierrorDAO = plausierrorDAO;
        _externalPlausisOnDelivery = new HashMap<Long, PlausiBO>();
        _allExternalPlausis = new HashMap<Long, PlausiBO>();
        for (PlausiBO plausi : externalPlausis) {
            if (plausi.get_thisPlausi().getObjecttype().equals(CodegroupUtility.SBG_OBJECTTYPE_DELIVERY)) {
                _externalPlausisOnDelivery.put(plausi.get_thisPlausi().getMacroid(), plausi);
            }
            _allExternalPlausis.put(plausi.get_thisPlausi().getMacroid(), plausi);
        }
    }

    /**
     * // Optimized handling of external plausi process
     *
     * @param delivery
     */
    public void verify(DeliveryBO delivery) {
        // 1. Get all old external plausi errors for delivery
        List<Plausierror> oldErrors = _plausierrorDAO.findExternalErrorsForDelivery(delivery.get_thisDelivery().getDeliveryid(), true);

        // build Map for confirmed errors
        HashMap<String, Plausierror> oldConfirmedErrorsMap = new HashMap<String, Plausierror>();
        for (Plausierror error : oldErrors) {
            if (error.getIsConfirmed()) {
                oldConfirmedErrorsMap.put(error.getLogicalKey(), error);
            }
        }

        // 2. Run the external plausis
        for (PlausiBO plausi : _externalPlausisOnDelivery.values()) {
            plausi.verify(delivery);
        }

        // 3. Read back all the new errors for delivery
        List<Plausierror> newErrors = _plausierrorDAO.findExternalErrorsForDelivery(delivery.get_thisDelivery().getDeliveryid(), false);

        // 4. Set confirmation flag according to confirmRules and old errors for persons and events
        List<Plausierror> newDeliveryErrors = new ArrayList<Plausierror>();
        for (Plausierror newError : newErrors) {
            if (newError.getPid() == null) {
                // Extract errors on level delivery
                newDeliveryErrors.add(newError);
            }
            // Check on all external plausis (these are errors on person or event)
            else if (Macro.MACRO_IS_CONFIRMABLE.equals(_allExternalPlausis.get(newError.getPlausiId()).get_thisPlausi().getIsconfirmable())) {
                // Error has been associated with another business object
                // Get corresponding old confirmed error and update confirmation data from old to new error
                Plausierror oldConfirmedError = oldConfirmedErrorsMap.get(newError.getLogicalKey());
                // take over confirmation information
                if (oldConfirmedError != null) {
                    newError.setIsConfirmed(oldConfirmedError.getIsConfirmed());
                    newError.setModification_user(oldConfirmedError.getModification_user());
                    newError.setModification_date(oldConfirmedError.getModification_date());
                    _plausierrorDAO.merge(newError);
                }
                //TODO: confirmRules?
                //				checkForConfirmRules(newError);
            }
        }

        // Merge with existing errors on business object
        updateBusinessObjectErrors(delivery, delivery.get_plausierrors(), newDeliveryErrors, oldConfirmedErrorsMap);
        delivery.get_thisDelivery().getPlausiErrors().removeAll(oldErrors);
    }

    /*protected String getErrorKey (Plausierror error)
    {
    	String errorKey = "";
    
    	errorKey = errorKey + error.getDeliveryId().toString();
    	errorKey = errorKey + (error.getPid() == null ? "" : "_" + error.getPid().toString());
    	errorKey = errorKey + (error.getEventId() == null ? "" : "_" + error.getEventId().toString());
    	errorKey = errorKey + (error.getPlausiId() == null ? "" : "_" + error.getPlausiId().toString());
    	errorKey = errorKey + (error.getConfirmId() == null ? "" : "#" + error.getConfirmId());
    
    	return errorKey;
    } */

    // update errors for verified business object
    private void updateBusinessObjectErrors(DeliveryBO businessObject, List<PlausierrorBO> allOldErrors, List<Plausierror> allNewErrors,
            HashMap<String, Plausierror> oldConfirmedErrorsMap) {
        // 1. extract all old external plausierrors
        List<PlausierrorBO> oldExternalErrorBos = new ArrayList<PlausierrorBO>();
        for (PlausierrorBO peBo : allOldErrors) {
            if (_externalPlausisOnDelivery.containsKey(peBo.get_thisPlausierror().getPlausiId())) {
                oldExternalErrorBos.add(peBo);
            }
        }

        // 2. update confirmation data from old to new errors
        for (Plausierror newError : allNewErrors) {
            // get corresponding old confirmed error and update confirmation data from old to new error
            Plausierror oldConfirmedError = oldConfirmedErrorsMap.get(newError.getLogicalKey());
            // take over confirmation information
            if (oldConfirmedError != null) {
                newError.setIsConfirmed(oldConfirmedError.getIsConfirmed());
                newError.setModification_user(oldConfirmedError.getModification_user());
                newError.setModification_date(oldConfirmedError.getModification_date());
            }
            // check confirmRules attribute and confirm error if required
            //TODO: confirmRules?
            //			if(businessObject.canBeConfirmed(newError.getPlausiId()))
            //			{
            //				newError.setIsConfirmed(true); // date and user set by PlausierrorBO.save
            //			}
        }

        // 3. Remove all old external errors
        _plausierrorDAO.deletePlausiErrors(oldExternalErrorBos);
        for (PlausierrorBO peBo : oldExternalErrorBos) {
            allOldErrors.remove(peBo);
        }

        // 4. Add new external errors to the BO
        for (Plausierror newError : allNewErrors) {
            allOldErrors.add(new PlausierrorBO(newError));
        }
    }
}
