/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: SimplePlausi3BO.java 505 2008-02-25 22:57:45Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import ch.admin.bfs.sbg.business.*;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Person;
import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * @author $Author: lsc $
 * @version $Revision: 505 $
 */
public class SimplePlausi3BO extends InternalPlausiBO {
    private static final String CODEGROUP_CANTON_NAME = "codegroup.canton.name";
    private static final String CODEGROUP_SEX_NAME = "codegroup.sex.name";
    private static final String CODEGROUP_PROFESSIONCODE_NAME = "codegroup.professioncode.name";
    private static final String CODEGROUP_SBFICODE_NAME = "codegroup.sbficode.name";
    private static final String CODEGROUP_CONTRACTTYPE_NAME = "codegroup.contracttype.name";
    private static final String CODEGROUP_EXAMTYPE_NAME = "codegroup.examtype.name";
    private static final String CODEGROUP_EXAMRESULT_NAME = "codegroup.examresult.name";
    private static final String CODEGROUP_CANCELREASON_NAME = "codegroup.cancelreason.name";

    private final ICodegroupManager _codegroupManager;

    public SimplePlausi3BO(Macro plausi, ICodegroupManager codegroupManager) {
        super(plausi);
        _codegroupManager = codegroupManager;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;
        SbgDelivery transferDelivery = delivery.get_thisDelivery();
        Long version = delivery.get_year();

        if ((transferDelivery.getCanton() != null) && !_codegroupManager.contains(CodegroupUtility.CANTON, transferDelivery.getCanton(), null, version, true)) {
            // generate codegroup plausierror
            delivery.get_plausierrors().add(
                    createPlausierror(delivery.get_thisDelivery().getDeliveryid(), null, null, CODEGROUP_CANTON_NAME, transferDelivery.getCanton().toString()));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(PersonBO person) {
        boolean verified = true;
        Person transferPerson = person.get_thisPerson();
        Long version = person.get_year();

        if ((transferPerson.getSex() != null) && !_codegroupManager.contains(CodegroupUtility.SEX, transferPerson.getSex(), null, version, true)) {
            // generate codegroup plausierror
            person.get_plausierrors().add(createPlausierror(person.get_deliveryId(), person, null, CODEGROUP_SEX_NAME, transferPerson.getSex().toString()));
            verified = false;
        }

        return verified;
    }

    protected boolean doVerify(EventBO event) {
        SbgEvent transferEvent = event.getThisEvent();
        Long version = event.getPerson().get_year();

        boolean verified = generateCodegroupPlausierrorIfNeeded(event, transferEvent.getSbfiCode(), version);

        if ((transferEvent.getProfessionCode() != null)
                && !_codegroupManager.contains(CodegroupUtility.PROFESSIONCODE, transferEvent.getProfessionCode(), null, version, true)) {
            // generate codegroup plausierror
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, CODEGROUP_PROFESSIONCODE_NAME,
                    transferEvent.getProfessionCode().toString()));
            verified = false;
        }

        if ((transferEvent.getContractType() != null)
                && !_codegroupManager.contains(CodegroupUtility.CONTRACTTYPE, transferEvent.getContractType(), null, version, true)) {
            // generate codegroup plausierror
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, CODEGROUP_CONTRACTTYPE_NAME,
                    transferEvent.getContractType().toString()));
            verified = false;
        }


        if (event instanceof ExamBO) {
            if ((transferEvent.getExamType() != null)
                    && !_codegroupManager.contains(CodegroupUtility.EXAMTYPE, transferEvent.getExamType(), null, version, true)) {
                // generate codegroup plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, CODEGROUP_EXAMTYPE_NAME,
                        transferEvent.getExamType().toString()));
                verified = false;
            }

            if ((transferEvent.getExamResult() != null)
                    && !_codegroupManager.contains(CodegroupUtility.EXAMRESULT, transferEvent.getExamResult(), null, version, true)) {
                // generate codegroup plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, CODEGROUP_EXAMRESULT_NAME,
                        transferEvent.getExamResult().toString()));
                verified = false;
            }
        } else if (event instanceof CancellationBO) {
            if ((transferEvent.getCancelReason() != null)
                    && !_codegroupManager.contains(CodegroupUtility.CANCELREASON, transferEvent.getCancelReason(), null, version, true)) {
                // generate codegroup plausierror
                event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, CODEGROUP_CANCELREASON_NAME,
                        transferEvent.getCancelReason().toString()));
                verified = false;
            }
        }

        return verified;
    }

    private boolean generateCodegroupPlausierrorIfNeeded(EventBO event, Long code, Long version) {
        if (code != null && !_codegroupManager.contains(CodegroupUtility.SBG_SBFICODE, code, null, version, true)) {
            event.getPlausiErrors().add(createPlausierror(event.getPerson().get_deliveryId(), event.getPerson(), event, CODEGROUP_SBFICODE_NAME, code.toString()));
           return false;
        }
        return true;
    }

    private PlausierrorBO createPlausierror(Long deliveryId, PersonBO person, EventBO event, String xmlTagName, String value) {
        String[] parameterList_de = { getName_de(xmlTagName), value };
        String[] parameterList_fr = { getName_fr(xmlTagName), value };
        return new PlausierrorBO(deliveryId, person, event, get_thisPlausi(), PlausierrorBO.PLAUSIERROR_CODE_NOT_IN_CODEGROUP, parameterList_de,
                parameterList_fr);
    }
}
