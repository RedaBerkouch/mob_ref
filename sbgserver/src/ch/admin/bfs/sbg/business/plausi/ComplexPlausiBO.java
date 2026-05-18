/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ComplexPlausiBO.java 645 2010-12-06 15:44:02Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.sas.*;
import ch.bfs.meb.server.commons.integration.sas.SASResult.Status;

/**
 * @author $Author: lsc $
 * @version $Revision: 645 $
 */
public class ComplexPlausiBO extends PlausiBO {
    private final static Logger LOGGER = LoggerFactory.getLogger(ComplexPlausiBO.class);

    public final static String DUMMY_SAS_MACRO = "dummy.sas";

    private final static Long PLAUSI_OBJECT_TYPE_DELIVERY = 0L;
    private final static Long PLAUSI_OBJECT_TYPE_PERSON = 1L;
    private final static Long PLAUSI_OBJECT_TYPE_EVENT = 2L;

    protected final PlausierrorDAO _plausierrorDAO;
    private final ISasService _sasService;

    public ComplexPlausiBO(Macro plausi, PlausierrorDAO plausierrorDAO, ISasService sasService) {
        super(plausi);

        _plausierrorDAO = plausierrorDAO;
        _sasService = sasService;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        if (get_thisPlausi().getObjecttype().equals(PLAUSI_OBJECT_TYPE_DELIVERY)) {
            // call SAS macro with parameters
            return callSasMacro(delivery.get_thisDelivery().getDeliveryid(), null, null);
        }
        return true;
    }

    protected boolean doVerify(PersonBO person) {
        if (get_thisPlausi().getObjecttype().equals(PLAUSI_OBJECT_TYPE_PERSON)) {
            // Get old errors from learner so that plausierrors are merged correctly 
            // even if the first SAS rule writes errors for all SAS rules on level person
            List<Plausierror> oldErrors = new ArrayList<Plausierror>();
            for (Plausierror error : person.get_thisPerson().getPlausiErrors()) {
                if (error.getPlausiId().equals(this.get_thisPlausi().getMacroid())) {
                    oldErrors.add(error);
                }
            }

            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(person.get_deliveryId(), person.get_thisPerson().getPid(), null);

            // reload plausierrors for persons
            List<Plausierror> errors = _plausierrorDAO.findForPerson(person.get_thisPerson().getPid(), this.get_thisPlausi().getMacroid());
            // Merge with existing errors
            updateErrors(person.get_plausierrors(), errors);
            person.get_thisPerson().getPlausiErrors().addAll(errors);
            person.get_thisPerson().getPlausiErrors().removeAll(oldErrors);

            return isCorrect; // return returnvalue of macro call
        } else {
            return true;
        }
    }

    protected boolean doVerify(EventBO event) {
        if (get_thisPlausi().getObjecttype().equals(PLAUSI_OBJECT_TYPE_EVENT)) {
            // Get old errors from event so that plausierrors are merged correctly 
            // even if the first SAS rule writes errors for all SAS rules on level event
            List<Plausierror> oldErrors = new ArrayList<Plausierror>();
            for (Plausierror error : event.getThisEvent().getPlausierrors()) {
                if (error.getPlausiId().equals(this.get_thisPlausi().getMacroid())) {
                    oldErrors.add(error);
                }
            }

            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(event.getPerson().get_deliveryId(), event.getPerson().get_thisPerson().getPid(),
                    event.getThisEvent().getEventid());

            // reload plausierrors
            List<Plausierror> errors = _plausierrorDAO.findForEvent(event.getThisEvent().getEventid(), this.get_thisPlausi().getMacroid());
            // Merge with existing errors
            updateErrors(event.getPlausiErrors(), errors);
            event.getThisEvent().getPlausierrors().addAll(errors);
            event.getThisEvent().getPlausierrors().removeAll(oldErrors);

            return isCorrect; // return returnvalue of macro call
        } else {
            return true;
        }
    }

    /**
     * Overridden - empty implementation. Updating of confirmed plausi errors is
     * done in the different doVerify-Methods.
     */
    protected void updateConfirmedPlausierrors(List<PlausierrorBO> errorList) {
        // do nothing
    }

    private void updateErrors(List<PlausierrorBO> allOldErrors, List<Plausierror> allMacroErrors) {
        // find old errors for this macro
        List<PlausierrorBO> oldMacroErrorBos = new ArrayList<PlausierrorBO>();
        for (PlausierrorBO peBo : allOldErrors) {
            if (peBo.get_thisPlausierror().getPlausiId().equals(this.get_thisPlausi().getMacroid())) {
                oldMacroErrorBos.add(peBo);
            }
        }

        // update confirmation data from old to new errors
        boolean isNew;
        Plausierror oldPe;
        for (Plausierror pe : allMacroErrors) {
            isNew = true;
            for (PlausierrorBO peBo : oldMacroErrorBos) {
                oldPe = peBo.get_thisPlausierror();
                if (!pe.getErrorId().equals(oldPe.getErrorId()) && pe.getConfirmId().equals(oldPe.getConfirmId())
                        && pe.getIsConfirmed() != oldPe.getIsConfirmed()) {
                    // take over confirmation information
                    pe.setIsConfirmed(oldPe.getIsConfirmed());
                    pe.setModification_user(oldPe.getModification_user());
                    pe.setModification_date(oldPe.getModification_date());
                }
                if (pe.getErrorId().equals(oldPe.getErrorId())) {
                    isNew = false;
                }
            }
            // Add new errors to the BO
            if (isNew == true) {
                allOldErrors.add(new PlausierrorBO(pe));
            }
        }

        // Remove old errors for this plausi
        for (PlausierrorBO peBo : oldMacroErrorBos) {
            _plausierrorDAO.deletePlausiError(peBo.get_thisPlausierror());
            allOldErrors.remove(peBo);
        }
    }

    /**
     * Calls the SAS Plausi Macro. If no SAS connection attributes (system
     * properties) are given, then the method returns without exception (and
     * without call tot SAS).
     *
     * @param deliveryid
     * @param pid
     * @param eventid
     * @return true if no errors found or no SAS connection attributes given
     * else false
     * @throws Exception if SAS-Macro not executable or Returnvalues not given
     *                   properly by SAS-Macro
     */
    private boolean callSasMacro(Long deliveryid, Long pid, Long eventid) {
        // Optimization for dummy SAS plausis (actual dummy SAS-Macro call lasts 1 sec.)
        if (this.get_thisPlausi().getSource() != null && this.get_thisPlausi().getSource().toLowerCase().contains(DUMMY_SAS_MACRO)) {
            return true;
        }

        try {
            // Call example:
            // %include '/project/SBG/dev/plausi/plausi_agg_04.sas';
            // %plausi_agg_04
            // (deliveryid=1,pid=0,eventid=0,macroid=0); run;
            ArrayList<SASParameter> params = new ArrayList<SASParameter>();
            params.add(new SASParameter("deliveryid", deliveryid));
            params.add(new SASParameter("pid", pid));
            params.add(new SASParameter("eventid", eventid));
            params.add(new SASParameter("macroid", this.get_thisPlausi().getMacroid()));
            // add additionally defined params for a specific SAS Plausi macro
            for (Parameter param : this.get_thisPlausi().getParameters()) {
                params.add(new SASParameter(param.getUniqueName(), param.getDefaultValue()));
            }
            SASCall call = new SASCall(this.get_thisPlausi().getSource(), params);

            // Call to SAS
            SASResult result = _sasService.run(call.getCode());

            if (result.getStatus() != Status.OK) {
                LOGGER.error("SAS macro call returned not ok.");
                throw new SASException("SAS macro call returned not ok.");
            }
        } catch (SASException e) {
            LOGGER.error("SAS fatal error while calling plausi macro:" + e.toString());
            throw e;
        } catch (Exception e) {
            LOGGER.error("SAS fatal error while calling plausi macro: " + e.toString());
            throw new SASException("SAS fatal error while calling plausi macro: " + e.toString());
        }

        return true;
    }
}
