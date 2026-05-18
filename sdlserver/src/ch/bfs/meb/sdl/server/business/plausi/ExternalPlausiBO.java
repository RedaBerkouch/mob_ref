/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.sdl.server.business.*;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.repository.IRepositoryProvider;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.sas.*;
import ch.bfs.meb.server.commons.integration.sas.SASResult.Status;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Business class for external (SAS) plausis
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class ExternalPlausiBO extends PlausiBO {
    public final static String DUMMY_SAS_MACRO = "dummy.sas";

    private final static Logger LOGGER = LoggerFactory.getLogger(ExternalPlausiBO.class);

    private final IRepositoryProvider _repositories;
    private final ISasService _sasService;

    public ExternalPlausiBO(SdlPlausi plausi, IRepositoryProvider repositories, ISasService sasService, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repositories = repositories;
        _sasService = sasService;
    }

    protected boolean doVerify(CantonBO canton) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SDL_OBJECTTYPE_CANTON)) {
            List<SdlPlausiError> oldErrors = _repositories.getPlausierrorRepository().findForCanton(canton.getThisCanton().getCantonId(),
                    this.getThisPlausi().getPlausiId(), true);
            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(canton.getThisCanton().getCantonId(), null, null, null, null);

            List<SdlPlausiError> newErrors = _repositories.getPlausierrorRepository().findForCanton(canton.getThisCanton().getCantonId(),
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
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY)) {
            // call SAS macro with parameters
            return callSasMacro(null, delivery.getThisDelivery().getDeliveryId(), null, null, null);
        }
        return true;
    }

    protected boolean doVerify(SchoolBO school) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL)) {
            // Get old errors from learner so that plausierrors are merged correctly 
            // even if the first SAS rule writes errors for all SAS rules on level person
            List<SdlPlausiError> oldErrors = new ArrayList<SdlPlausiError>();
            for (SdlPlausiError error : school.getThisSchool().getPlausierrors()) {
                if (error.getPlausi().getPlausiId().equals(this.getThisPlausi().getPlausiId())) {
                    oldErrors.add(error);
                }
            }
            //List<SdlPlausiError> oldErrors = _repositories.getPlausierrorRepository().findForSchool (school.getThisSchool().getSchoolId(), this.getThisPlausi().getPlausiId());

            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(null, school.getThisSchool().getDeliveryId(), school.getThisSchool().getSchoolId(), null, null);

            // reload plausierrors for persons
            List<SdlPlausiError> errors = _repositories.getPlausierrorRepository().findForSchool(school.getThisSchool().getSchoolId(),
                    this.getThisPlausi().getPlausiId());
            // Merge with existing errors
            updateErrors(school.getPlausierrors(), errors, school.canBeConfirmed(_thisPlausi.getId()));
            school.getThisSchool().getPlausierrors().addAll(errors);
            school.getThisSchool().getPlausierrors().removeAll(oldErrors);

            return isCorrect; // return returnvalue of macro call
        } else {
            return true;
        }
    }

    protected boolean doVerify(ClassBO classBO) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SDL_OBJECTTYPE_CLASS)) {
            // Get old errors from learner so that plausierrors are merged correctly 
            // even if the first SAS rule writes errors for all SAS rules on level person
            List<SdlPlausiError> oldErrors = new ArrayList<SdlPlausiError>();
            for (SdlPlausiError error : classBO.getThisClass().getPlausierrors()) {
                if (error.getPlausi().getPlausiId().equals(this.getThisPlausi().getPlausiId())) {
                    oldErrors.add(error);
                }
            }
            //List<SdlPlausiError> oldErrors = _repositories.getPlausierrorRepository().findForClass (classBO.getThisClass().getClassId(), this.getThisPlausi().getPlausiId());

            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(null, classBO.getSchool().getThisSchool().getDeliveryId(), classBO.getSchool().getThisSchool().getSchoolId(),
                    classBO.getThisClass().getClassId(), null);

            // reload plausierrors
            List<SdlPlausiError> errors = _repositories.getPlausierrorRepository().findForClass(classBO.getThisClass().getClassId(),
                    this.getThisPlausi().getPlausiId());
            // Merge with existing errors
            updateErrors(classBO.getPlausierrors(), errors, classBO.canBeConfirmed(_thisPlausi.getId()));
            classBO.getThisClass().getPlausierrors().addAll(errors);
            classBO.getThisClass().getPlausierrors().removeAll(oldErrors);

            return isCorrect; // return returnvalue of macro call
        } else {
            return true;
        }
    }

    protected boolean doVerify(LearnerBO learner) {
        if (getThisPlausi().getObjectLevel().equals(CodegroupUtility.SDL_OBJECTTYPE_LEARNER)) {
            // Get old errors from learner so that plausierrors are merged correctly 
            // even if the first SAS rule writes errors for all SAS rules on level person
            List<SdlPlausiError> oldErrors = new ArrayList<SdlPlausiError>();
            for (SdlPlausiError error : learner.getThisLearner().getPlausierrors()) {
                if (error.getPlausi().getPlausiId().equals(this.getThisPlausi().getPlausiId())) {
                    oldErrors.add(error);
                }
            }
            // _repositories.getPlausierrorRepository().findForLearner (learner.getThisLearner().getLearnerId(), this.getThisPlausi().getPlausiId());  

            // call SAS macro with parameters
            boolean isCorrect = callSasMacro(null, learner.getClassBO().getSchool().getThisSchool().getDeliveryId(),
                    learner.getClassBO().getSchool().getThisSchool().getSchoolId(), learner.getClassBO().getThisClass().getClassId(),
                    learner.getThisLearner().getLearnerId());

            // reload plausierrors
            List<SdlPlausiError> errors = _repositories.getPlausierrorRepository().findForLearner(learner.getThisLearner().getLearnerId(),
                    this.getThisPlausi().getPlausiId());
            // Merge with existing errors
            updateErrors(learner.getPlausierrors(), errors, learner.canBeConfirmed(_thisPlausi.getId()));
            learner.getThisLearner().getPlausierrors().addAll(errors);
            learner.getThisLearner().getPlausierrors().removeAll(oldErrors);

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

    private void updateErrors(List<PlausierrorBO> allOldErrors, List<SdlPlausiError> allMacroErrors, boolean confirmErrors) {
        // find old errors for this macro
        List<PlausierrorBO> oldMacroErrorBos = new ArrayList<PlausierrorBO>();
        for (PlausierrorBO peBo : allOldErrors) {
            if (peBo.getThisPlausierror().getPlausi().getPlausiId().equals(this.getThisPlausi().getPlausiId())) {
                oldMacroErrorBos.add(peBo);
            }
        }

        // update confirmation data from old to new errors
        boolean isNew;
        SdlPlausiError oldPe;
        for (SdlPlausiError pe : allMacroErrors) {
            isNew = true;
            for (PlausierrorBO peBo : oldMacroErrorBos) {
                oldPe = peBo.getThisPlausierror();
                if (this.getThisPlausi().getIsConfirmable() && !pe.getErrorId().equals(oldPe.getErrorId()) && pe.getConfirmId().equals(oldPe.getConfirmId())
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
            // TODO: same procedure (mark and delete old errors) for schools, classes and learners
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
     * @param deliveryid
     * @param pid
     * @param eventid
     * @return true if no errors found or no SAS connection attributes given else false
     * @throws Exception if SAS-Macro not executable or Returnvalues not given properly by SAS-Macro 
     */
    private boolean callSasMacro(Long cantonId, Long deliveryId, Long schoolId, Long classId, Long learnerId) {
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
            params.add(new SASParameter("schoolId", schoolId));
            params.add(new SASParameter("classId", classId));
            params.add(new SASParameter("learnerId", learnerId));
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
