/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: ExternalPlausiBO.java 892 2010-03-03 15:05:51Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.sba.server.business.CantonBO;
import ch.bfs.meb.sba.server.business.DeliveryBO;
import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.repository.IRepositoryProvider;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.sas.*;
import ch.bfs.meb.server.commons.integration.sas.SASResult.Status;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Business class for external (SAS) plausis
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 892 $ 
 */
public class ExternalPlausiBO extends PlausiBO {
    public final static String DUMMY_SAS_MACRO = "dummy.sas";

    private final static Logger LOGGER = LoggerFactory.getLogger(ExternalPlausiBO.class);

    private final IRepositoryProvider _repositories;
    private final ISasService _sasService;

    public ExternalPlausiBO(SbaPlausi plausi, IRepositoryProvider repositories, ISasService sasService, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repositories = repositories;
        _sasService = sasService;
    }

    protected boolean doVerify(CantonBO canton) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SBA_OBJECTTYPE_CANTON)) {
            List<SbaPlausiError> oldErrors = _repositories.getPlausierrorRepository().findForCanton(canton.getThisCanton().getCantonId(),
                    this.getThisPlausi().getPlausiId(), true);
            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(canton.getThisCanton().getCantonId(), null, null, null);

            List<SbaPlausiError> newErrors = _repositories.getPlausierrorRepository().findForCanton(canton.getThisCanton().getCantonId(),
                    this.getThisPlausi().getPlausiId(), false);

            // Merge with existing errors on business object
            updateErrors(canton.getPlausierrors(), newErrors, canton.canBeConfirmed(_thisPlausi.getId()));
            canton.getThisCanton().getPlausierrors().removeAll(oldErrors);

            return isCorrect;
        } else {
            return true;
        }
    }

    protected boolean doVerify(DeliveryBO delivery) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SBA_OBJECTTYPE_DELIVERY)) {
            // call SAS macro with parameters
            return callSasMacro(null, delivery.getThisDelivery().getDeliveryId(), null, null);
        }
        return true;
    }

    @Override
    protected boolean doVerify(PersonBO person) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SSP_OBJECTTYPE_PERSON)) {
            // Get old errors from learner so that plausierrors are merged correctly 
            // even if the first SAS rule writes errors for all SAS rules on level person 
            List<SbaPlausiError> oldErrors = new ArrayList<SbaPlausiError>();
            for (SbaPlausiError error : person.getThisPerson().getPlausierrors()) {
                if (error.getPlausi().getPlausiId().equals(this.getThisPlausi().getPlausiId())) {
                    oldErrors.add(error);
                }
            }
            //List<SbaPlausiError> oldErrors = _repositories.getPlausierrorRepository().findForPerson (person.getThisPerson().getPersonId(), this.getThisPlausi().getPlausiId());

            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(null, person.getThisPerson().getDeliveryId(), person.getThisPerson().getPersonId(), null);

            // reload plausierrors for persons
            List<SbaPlausiError> errors = _repositories.getPlausierrorRepository().findForPerson(person.getThisPerson().getPersonId(),
                    this.getThisPlausi().getPlausiId());
            // Merge with existing errors
            updateErrors(person.getPlausierrors(), errors, person.canBeConfirmed(_thisPlausi.getId()));
            person.getThisPerson().getPlausierrors().addAll(errors);
            person.getThisPerson().getPlausierrors().removeAll(oldErrors);

            return isCorrect; // return returnvalue of macro call
        } else {
            return true;
        }
    }

    @Override
    protected boolean doVerify(QualificationBO qualification) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SSP_OBJECTTYPE_ACTIVITY)) {
            // Get old errors from learner so that plausierrors are merged correctly 
            // even if the first SAS rule writes errors for all SAS rules on level person 
            List<SbaPlausiError> oldErrors = new ArrayList<SbaPlausiError>();
            for (SbaPlausiError error : qualification.getThisQualification().getPlausierrors()) {
                if (error.getPlausi().getPlausiId().equals(this.getThisPlausi().getPlausiId())) {
                    oldErrors.add(error);
                }
            }
            // List<SbaPlausiError> oldErrors = _repositories.getPlausierrorRepository().findForQualification (qualification.getThisQualification().getQualificationId(), this.getThisPlausi().getPlausiId());

            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(null, qualification.getPerson().getThisPerson().getDeliveryId(),
                    qualification.getPerson().getThisPerson().getPersonId(), qualification.getThisQualification().getQualificationId());

            // reload plausierrors for persons
            List<SbaPlausiError> errors = _repositories.getPlausierrorRepository()
                    .findForQualification(qualification.getThisQualification().getQualificationId(), this.getThisPlausi().getPlausiId());
            // Merge with existing errors
            updateErrors(qualification.getPlausierrors(), errors, qualification.canBeConfirmed(_thisPlausi.getId()));
            qualification.getThisQualification().getPlausierrors().addAll(errors);
            qualification.getThisQualification().getPlausierrors().removeAll(oldErrors);

            return isCorrect; // return returnvalue of macro call
        } else {
            return true;
        }
    }

    /**
     * Overridden - empty implementation. Updating of confirmed plausi errors is done in the different doVerify-Methods.
     */
    @Override
    protected void updateConfirmedPlausierrors(List<PlausierrorBO> errorList, boolean confirmErrors) {
        // do nothing
    }

    private void updateErrors(List<PlausierrorBO> allOldErrors, List<SbaPlausiError> allMacroErrors, boolean confirmErrors) {
        // find old errors for this macro
        List<PlausierrorBO> oldMacroErrorBos = new ArrayList<PlausierrorBO>();
        for (PlausierrorBO peBo : allOldErrors) {
            if (peBo.getThisPlausierror().getPlausi().getPlausiId().equals(this.getThisPlausi().getPlausiId())) {
                oldMacroErrorBos.add(peBo);
            }
        }

        // update confirmation data from old to new errors
        boolean isNew;
        SbaPlausiError oldPe;
        for (SbaPlausiError pe : allMacroErrors) {
            isNew = true;
            for (PlausierrorBO peBo : oldMacroErrorBos) {
                oldPe = peBo.getThisPlausierror();
                if (getThisPlausi().getIsConfirmable() && !pe.getErrorId().equals(oldPe.getErrorId()) && pe.getConfirmId().equals(oldPe.getConfirmId())
                        && pe.getIsConfirmed() != oldPe.getIsConfirmed()) {
                    //	take over confirmation information
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
                // confirm error if required
                if (confirmErrors) {
                    pe.setIsConfirmed(true);
                }
            }
        }

        // Remove old errors for this plausi
        for (PlausierrorBO peBo : oldMacroErrorBos) {
            // mantis 1798: all old errors will be marked and deleted for cantons anyway
            // TODO: same procedure (mark and delete old errors) for persons and qualifications
            if (peBo.getThisPlausierror().getDeliveryId() != null) {
                _repositories.getPlausierrorRepository().deletePlausiError(peBo.getThisPlausierror());
            }
            allOldErrors.remove(peBo);
        }
    }

    /**
     * Calls the SAS Plausi Macro. If no SAS connection attributes (system properties) are given, then 
     * the method returns without exception (and without call tot SAS).
     * 
     * @param cantonId
     * @param deliveryId
     * @param personId
     * @param qualificationId
     * @return true if no errors found or no SAS connection attributes given else false
     * @throws Exception if SAS-Macro not executable or Returnvalues not given properly by SAS-Macro 
     */
    private boolean callSasMacro(Long cantonId, Long deliveryId, Long personId, Long qualificationId) {
        // Optimization for dummy SAS plausis (actual dummy SAS-Macro call lasts 1 sec.)
        if (this.getThisPlausi().getSource() != null && this.getThisPlausi().getSource().toLowerCase().contains(DUMMY_SAS_MACRO)) {
            return true;
        }

        try {
            // Call example: 
            // %include '/project/SBG/dev/plausi/plausi_agg_04.sas'; %plausi_agg_04
            // (deliveryid=1,pid=0,eventid=0,macroid=0); run;
            ArrayList<SASParameter> params = new ArrayList<SASParameter>();
            params.add(new SASParameter("cantonId", cantonId));
            params.add(new SASParameter("deliveryId", deliveryId));
            params.add(new SASParameter("personId", personId));
            params.add(new SASParameter("qualificationId", qualificationId));
            params.add(new SASParameter("plausiId", this.getThisPlausi().getPlausiId()));
            // add additionally defined params for a specific SAS Plausi macro
            for (Parameter param : this.getThisPlausi().getParameters()) {
                params.add(new SASParameter(param.getUniqueName(), param.getDefaultValue()));
            }
            SASCall call = new SASCall(this.getThisPlausi().getSource(), params);

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
            LOGGER.error("SAS fatal error while calling plausi macro: ", e);
            throw new SASException("SAS fatal error while calling plausi macro: " + e.toString());
        }

        return true;
    }
}
