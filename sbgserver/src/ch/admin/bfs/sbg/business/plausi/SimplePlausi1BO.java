/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi1BO.java 622 2010-11-08 08:05:30Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.Set;

import ch.admin.bfs.sbg.business.*;
import ch.admin.bfs.sbg.transfer.KeyAspect;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.bfs.meb.sbg.server.keyaspect.KeyAspectManager;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

/**
 * Plausi 1 Obligatorische Felder
 *
 * @author $Author: dzw $
 * @version $Revision: 622 $
 */
public class SimplePlausi1BO extends InternalPlausiBO {
    private final KeyAspectManager keyAspectManager;

    public SimplePlausi1BO(Macro plausi, KeyAspectManager keyAspectManager) {
        super(plausi);
        this.keyAspectManager = keyAspectManager;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;
        if (StringUtils.isEmpty(delivery.get_lidat())) {
            // generate plausierror
            delivery.get_plausierrors().add(createPlausierror(delivery.get_thisDelivery().getDeliveryid(), null, null, XML_TAG_LIDAT_NAME));
            verified = false;
        }
        return verified;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        if (StringUtils.isEmpty(person.get_idNr())) {
            // generate plausierror
            person.get_plausierrors().add(createPlausierror(person.get_deliveryId(), person, null, XML_TAG_IDNR_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(person.get_idType())) {
            // generate plausierror
            person.get_plausierrors().add(createPlausierror(person.get_deliveryId(), person, null, XML_TAG_IDTYP_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(person.get_sex())) {
            //generate plausierror
            person.get_plausierrors().add(createPlausierror(person.get_deliveryId(), person, null, XML_TAG_SEX_NAME));
            verified = false;
        }
        if (person.get_year() < CodegroupUtility.SBG_PERSON_NEWBIRTHDATE) {
            if (StringUtils.isEmpty(person.get_dateOfBirth())) {
                // generate plausierror
                person.get_plausierrors().add(createPlausierror(person.get_deliveryId(), person, null, XML_TAG_DATEOFBIRTH_NAME));
                verified = false;
            }
        } else {
            if (StringUtils.isEmpty(person.get_nDateOfBirth())) {
                // generate plausierror
                person.get_plausierrors().add(createPlausierror(person.get_deliveryId(), person, null, XML_TAG_NEWDATEOFBIRTH_NAME));
                verified = false;
            }
        }
        return verified;
    }

    protected boolean doVerify(EventBO event) {
        boolean verified = true;
        if (StringUtils.isEmpty(event.getContractNr())) {
            // generate plausierror
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_VERTNR_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(event.getProfessionCode())) {
            // generate plausierror
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_PROFID_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(event.getKeyAspect()) && isKeyAspectMandatory(event)) {
            // generate plausierror
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_KEYASPECT_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(event.getSbfiCode()) && event.getPerson().get_year() >= 2015) //added and mandatory since 2015
        {
            // generate plausierror
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_SBFICODE_NAME));
            verified = false;
        }
        if (StringUtils.isEmpty(event.getContractType())) {
            // generate plausierror
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_LEHRTYP_NAME));
            verified = false;
        }

        if (event instanceof ContractBO) {
            ContractBO contract = (ContractBO) event;
            if (StringUtils.isEmpty(contract.getContractDate())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_VERTDAT_NAME));
                verified = false;
            }
            if (StringUtils.isEmpty(contract.getEnterprise().get_name())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_UNTNAME_NAME));
                verified = false;
            }
            if (StringUtils.isEmpty(contract.getEnterprise().get_plz())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_PLZ_NAME));
                verified = false;
            }
            if (StringUtils.isEmpty(contract.getEnterprise().get_municipality())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_GEM_NAME));
                verified = false;
            }
        } else if (event instanceof ExamBO) {
            ExamBO exam = (ExamBO) event;
            if (StringUtils.isEmpty(exam.getExamType())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_EXTYP_NAME));
                verified = false;
            }
            if (StringUtils.isEmpty(exam.getExamNr())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_EXNR_NAME));
                verified = false;
            }
            if (StringUtils.isEmpty(exam.getRepetition())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_REP_NAME));
                verified = false;
            }
            if (StringUtils.isEmpty(exam.getResult())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_RES_NAME));
                verified = false;
            }
        } else if (event instanceof CancellationBO) {
            CancellationBO cancellation = (CancellationBO) event;
            if (StringUtils.isEmpty(cancellation.getCancelDate())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_ABDAT_NAME));
                verified = false;
            }
            if (StringUtils.isEmpty(cancellation.getCancelReason())) {
                // generate plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_ABTYP_NAME));
                verified = false;
            }
        }

        return verified;
    }

    private PlausierrorBO createPlausierror(Long deliveryId, PersonBO person, EventBO event, String xmlTagName) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        return new PlausierrorBO(deliveryId, person, event, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_MISSING, parameterList_de, parameterList_fr);
    }

    private boolean isKeyAspectMandatory(EventBO event) {//The KeyAspect is mandatory if a ProfessionCodes exists in the sql table SBG_KeyAspect (Mantis 2268)
        SbgEvent thisEvent = event.getThisEvent();
        if (thisEvent == null) {//paranoia check
            return false;
        }
        Long professionCode = thisEvent.getProfessionCode();
        if (professionCode == null) {//KeyAspect is not mandatory if no ProfessionCode exist.
            return false;
        }
        Set<KeyAspect> cachedKeyAspects = keyAspectManager.getCachedKeyAspect(professionCode);
        if (cachedKeyAspects == null) {//ProfessionCode not found
            return false;
        }
        if (cachedKeyAspects.size() > 0) {//ProfessionCode found
            return true;
        }
        return false;
    }
}
