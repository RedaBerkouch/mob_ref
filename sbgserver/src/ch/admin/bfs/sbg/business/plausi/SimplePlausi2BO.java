/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi2BO.java 621 2010-11-08 07:57:52Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.*;
import ch.admin.bfs.sbg.psist.PersistPerson;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

/**
 * Plausi 2 Gueltiges Format
 *
 * @author $Author: dzw $
 * @version $Revision: 621 $
 */
public class SimplePlausi2BO extends InternalPlausiBO {

    public SimplePlausi2BO(Macro plausi) {
        super(plausi);
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;
        // initialize transferDelivery and perform basic formatting
        delivery.format();
        SbgDelivery transferDelivery = delivery.get_thisDelivery();

        if (!StringUtils.isEmpty(delivery.get_lidat()) && (transferDelivery.getDeliverydate() == null)) {
            // generate date plausierror
            delivery.get_plausierrors().add(
                    createTypePlausierror(delivery.get_thisDelivery().getDeliveryid(), null, null, PlausierrorBO.PLAUSIERROR_NOT_A_DATE, XML_TAG_LIDAT_NAME));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        // initialize transferPerson and perform basic formatting
        person.format();
        PersistPerson persistPerson = person.get_thisPerson();

        if (!StringUtils.isEmpty(person.get_idNr()) && (persistPerson.getId() == null)) {
            // generate number plausierror
            person.get_plausierrors()
                    .add(createTypePlausierror(person.get_deliveryId(), person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_IDNR_NAME));
            verified = false;
        }
        if ((persistPerson.getId() != null) && (person.get_idNr().length() > 13)) {
            // generate length plausierror
            persistPerson.setId(null);
            person.get_plausierrors().add(createLengthPlausierror(person.get_deliveryId(), person, null, XML_TAG_IDNR_NAME, "13"));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.get_idNr()) && (persistPerson.getIdType() == null)) {
            // generate number plausierror
            person.get_plausierrors()
                    .add(createTypePlausierror(person.get_deliveryId(), person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_IDTYP_NAME));
            verified = false;
        }
        if ((persistPerson.getIdType() != null) && (person.get_idType().length() > 5)) {
            // generate length plausierror
            persistPerson.setIdType(null);
            person.get_plausierrors().add(createLengthPlausierror(person.get_deliveryId(), person, null, XML_TAG_IDTYP_NAME, "5"));
            verified = false;
        }

        if (!StringUtils.isEmpty(person.get_sex()) && (persistPerson.getSex() == null)) {
            // generate number plausierror
            person.get_plausierrors()
                    .add(createTypePlausierror(person.get_deliveryId(), person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_SEX_NAME));
            verified = false;
        }
        if ((persistPerson.getSex() != null) && (person.get_sex().length() != 1)) {
            // generate length plausierror
            persistPerson.setSex(null);
            person.get_plausierrors().add(createExactLengthPlausierror(person.get_deliveryId(), person, null, XML_TAG_SEX_NAME, "1"));
            verified = false;
        }

        if (person.get_year() < CodegroupUtility.SBG_PERSON_NEWBIRTHDATE) {
            if (!StringUtils.isEmpty(person.get_dateOfBirth()) && persistPerson.getBirthDate() == null) {
                // generate number plausierror
                person.get_plausierrors()
                        .add(createTypePlausierror(person.get_deliveryId(), person, null, PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_DATEOFBIRTH_NAME));
                verified = false;
            }
            if (persistPerson.getBirthDate() != null && person.get_dateOfBirth().length() != 4) {
                // generate length plausierror
                persistPerson.setBirthDate(null);
                person.get_plausierrors().add(createExactLengthPlausierror(person.get_deliveryId(), person, null, XML_TAG_DATEOFBIRTH_NAME, "4"));
                verified = false;
            }
        } else {
            if (!StringUtils.isEmpty(person.get_nDateOfBirth()) && persistPerson.getNewBirthDate() == null) {
                // generate date plausierror
                person.get_plausierrors()
                        .add(createTypePlausierror(person.get_deliveryId(), person, null, PlausierrorBO.PLAUSIERROR_NOT_A_DATE, XML_TAG_NEWDATEOFBIRTH_NAME));
                verified = false;
            }
        }

        if ((person.get_comment() != null) && (person.get_comment().length() > 1024)) {
            // generate length plausierror
            persistPerson.setUserComment(null);
            person.get_plausierrors().add(createLengthPlausierror(person.get_deliveryId(), person, null, XML_TAG_COM_NAME, "1024"));
            verified = false;
        }
        person.set_thisPerson(persistPerson);

        return verified;
    }

    protected boolean doVerify(EventBO event) {
        boolean verified = true;
        // initialize transferEvent and perform basic formatting
        event.format();
        SbgEvent persistEvent = event.getThisEvent();

        if (!StringUtils.isEmpty(event.getSbfiCode()) && (persistEvent.getSbfiCode() == null)) {
            // generate number plausierror
            event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_SBFICODE_NAME));
            verified = false;
        }
        if ((persistEvent.getSbfiCode() != null) && (event.getSbfiCode().length() > 5)) {
            // generate length plausierror
            persistEvent.setSbfiCode(null);
            event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_SBFICODE_NAME, "5"));
            verified = false;
        }

        if (!StringUtils.isEmpty(event.getContractNr()) && (persistEvent.getContractNr() == null)) {
            // generate number plausierror
            event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_VERTNR_NAME));
            verified = false;
        }

        if (!StringUtils.isEmpty(event.getProfessionCode()) && (persistEvent.getProfessionCode() == null)) {
            // generate number plausierror
            event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_PROFID_NAME));
            verified = false;
        }
        if ((persistEvent.getProfessionCode() != null) && (event.getProfessionCode().length() > 8)) {
            // generate length plausierror
            persistEvent.setProfessionCode(null);
            event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_PROFID_NAME, "8"));
            verified = false;
        }

        if (!StringUtils.isEmpty(event.getKeyAspect()) && (persistEvent.getKeyAspect() == null)) {
            // generate number plausierror
            event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_KEYASPECT_NAME));
            verified = false;
        }
        if ((persistEvent.getKeyAspect() != null) && (event.getKeyAspect().length() > 3)) {
            // generate length plausierror
            persistEvent.setKeyAspect(null);
            event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_KEYASPECT_NAME, "3"));
            verified = false;
        }

        if (!StringUtils.isEmpty(event.getEducationYear()) && (persistEvent.getEducationYear() == null)) {
            // generate number plausierror
            event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_EDUCTAIONYEAR_NAME));
            verified = false;
        }
        if ((persistEvent.getEducationYear() != null) && (event.getEducationYear().length() > 2)) {
            // generate length plausierror
            persistEvent.setEducationYear(null);
            event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_EDUCTAIONYEAR_NAME, "2"));
            verified = false;
        }

        if (!StringUtils.isEmpty(event.getContractType()) && (persistEvent.getContractType() == null)) {
            // generate number plausierror
            event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                    PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_LEHRTYP_NAME));
            verified = false;
        }
        if ((persistEvent.getContractType() != null) && (event.getContractType().length() > 3)) {
            // generate length plausierror
            persistEvent.setContractType(null);
            event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_LEHRTYP_NAME, "3"));
            verified = false;
        }

        if ((event.getComment() != null) && (event.getComment().length() > 1024)) {
            // generate length plausierror
            persistEvent.setUserComment(null);
            event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_COM_NAME, "1024"));
            verified = false;
        }

        if (event instanceof ContractBO) {
            ContractBO contract = (ContractBO) event;

            if (!StringUtils.isEmpty(contract.getContractDate()) && (persistEvent.getContractDate() == null)) {
                // generate date plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_DATE, XML_TAG_VERTDAT_NAME));
                verified = false;
            }

            EnterpriseBO enterprise = contract.getEnterprise();
            if (!StringUtils.isEmpty(enterprise.get_burNr()) && (persistEvent.getBurnr() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_BURNR_NAME));
                verified = false;
            }
            if ((persistEvent.getBurnr() != null) && (enterprise.get_burNr().length() > 8)) {
                // generate length plausierror
                persistEvent.setBurnr(null);
                event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_BURNR_NAME, "8"));
                verified = false;
            }

            if ((persistEvent.getKantLbCode() != null) && (enterprise.get_kantLbCode().length() > 15)) {
                // generate length plausierror
                persistEvent.setKantLbCode(null);
                event.getPlausiErrors()
                        .add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_KANTLBCODE_NAME, "15"));
                verified = false;
            }

            if ((enterprise.get_name() != null) && (enterprise.get_name().length() > 252)) {
                // generate length plausierror
                persistEvent.setFirmName(null);
                event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_UNTNAME_NAME, "252"));
                verified = false;
            }

            if ((enterprise.get_street() != null) && (enterprise.get_street().length() > 252)) {
                // generate length plausierror
                persistEvent.setFirmStreet(null);
                event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_STR_NAME, "252"));
                verified = false;
            }

            if ((enterprise.get_streetNr() != null) && (enterprise.get_streetNr().length() > 10)) {
                // generate length plausierror
                persistEvent.setFirmStreetNr(null);
                event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_STRNR_NAME, "10"));
                verified = false;
            }

            if (!StringUtils.isEmpty(enterprise.get_plz()) && (persistEvent.getFirmPlz() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_PLZ_NAME));
                verified = false;
            }
            if ((persistEvent.getFirmPlz() != null) && (enterprise.get_plz().length() != 4)) {
                // generate length plausierror
                persistEvent.setFirmPlz(null);
                event.getPlausiErrors().add(createExactLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_PLZ_NAME, "4"));
                verified = false;
            }

            if ((enterprise.get_municipality() != null) && (enterprise.get_municipality().length() > 252)) {
                // generate length plausierror
                persistEvent.setFirmMunicipality(null);
                event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_GEM_NAME, "252"));
                verified = false;
            }

            Boolean flagLbv = event.verifyBoolean(enterprise.get_flagLbv());
            if (!StringUtils.isEmpty(enterprise.get_flagLbv()) && (flagLbv == null)) {
                // generate boolean plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_BOOLEAN, XML_TAG_FLAGLBV_NAME));
                verified = false;
            }
        } else if (event instanceof ExamBO) {
            ExamBO exam = (ExamBO) event;

            if (!StringUtils.isEmpty(exam.getExamType()) && (persistEvent.getExamType() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_EXTYP_NAME));
                verified = false;
            }
            if ((persistEvent.getExamType() != null) && (exam.getExamType().length() > 3)) {
                // generate length plausierror
                persistEvent.setExamType(null);
                event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_EXTYP_NAME, "3"));
                verified = false;
            }

            if (!StringUtils.isEmpty(exam.getExamNr()) && (persistEvent.getExamNr() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_EXNR_NAME));
                verified = false;
            }

            if (!StringUtils.isEmpty(exam.getRepetition()) && (persistEvent.getExamRepetition() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_REP_NAME));
                verified = false;
            }

            if (!StringUtils.isEmpty(exam.getResult()) && (persistEvent.getExamResult() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_RES_NAME));
                verified = false;
            }
        } else if (event instanceof CancellationBO) {
            CancellationBO cancellation = (CancellationBO) event;

            if (!StringUtils.isEmpty(cancellation.getContractType()) && (persistEvent.getContractType() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_LEHRTYP_NAME));
                verified = false;
            }
            if ((persistEvent.getContractType() != null) && (cancellation.getContractType().length() > 3)) {
                // generate length plausierror
                persistEvent.setContractType(null);
                event.getPlausiErrors().add(createLengthPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, XML_TAG_LEHRTYP_NAME, "3"));
                verified = false;
            }

            if (!StringUtils.isEmpty(cancellation.getCancelDate()) && (persistEvent.getCancelDate() == null)) {
                // generate date plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_DATE, XML_TAG_ABDAT_NAME));
                verified = false;
            }

            if (!StringUtils.isEmpty(cancellation.getCancelReason()) && (persistEvent.getCancelReason() == null)) {
                // generate number plausierror
                event.getPlausiErrors().add(createTypePlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event,
                        PlausierrorBO.PLAUSIERROR_NOT_A_NUMBER, XML_TAG_ABTYP_NAME));
                verified = false;
            }
        }
        event.setThisEvent(persistEvent);

        return verified;
    }

    private PlausierrorBO createLengthPlausierror(Long deliveryId, PersonBO person, EventBO event, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        return new PlausierrorBO(deliveryId, person, event, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_TOO_LONG, parameterList_de, parameterList_fr);
    }

    private PlausierrorBO createExactLengthPlausierror(Long deliveryId, PersonBO person, EventBO event, String xmlTagName, String length) {
        String[] parameterList_de = { getName_de(xmlTagName), length };
        String[] parameterList_fr = { getName_fr(xmlTagName), length };
        return new PlausierrorBO(deliveryId, person, event, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_WRONG_LENGTH, parameterList_de, parameterList_fr);
    }

    private PlausierrorBO createTypePlausierror(Long deliveryId, PersonBO person, EventBO event, String xmlTagName, String type) {
        String[] parameterList_de = { getName_de(xmlTagName) };
        String[] parameterList_fr = { getName_fr(xmlTagName) };
        return new PlausierrorBO(deliveryId, person, event, get_thisPlausi(), type, parameterList_de, parameterList_fr);
    }
}
