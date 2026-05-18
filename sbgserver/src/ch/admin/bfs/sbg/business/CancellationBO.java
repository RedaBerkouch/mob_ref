/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: CancellationBO.java 644 2010-12-06 15:19:20Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import java.util.Date;

import ch.bfs.meb.sbg.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.service.xmlbeans.Cancellation;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Getter;

/**
 * Business object for cancellation of education. Derived from EventBO.
 *
 * @author $Author: lsc $
 * @version $Revision: 644 $
 */
@Getter
public class CancellationBO extends EventBO {
    //	private static final int CONTRACT_NR = 2;
    //	private static final int PROFESSION_CODE = 3;
    //	private static final int COMMENT = 7;
    //	private static final int CONTRACT_TYPE = 4;
    //	private static final int CANCEL_DATE = 5;
    //	private static final int CANCEL_REASON = 6;

    private String cancelDate;
    private String cancelReason;

    /**
     * Constructs a cancellation business object from XML part
     *
     * @param xmlPart XML bean part for cancellation
     * @param person  A cancellation belongs to exactly one person
     */
    public CancellationBO(Cancellation xmlPart, PersonBO person) {
        this.person = person;

        this.contractNr = xmlPart.getVertnr();
        this.professionCode = xmlPart.getProfid();
        this.keyAspect = xmlPart.getSchwerpunkt();
        this.educationYear = xmlPart.getAusbjahr();
        this.sbfiCode = xmlPart.getSbficode();
        this.contractType = xmlPart.getLehrtyp();
        this.comment = xmlPart.getCom();
        this.cancelDate = xmlPart.getAbdat();
        this.cancelReason = xmlPart.getAbtyp();
    }

    public CancellationBO(ItemBO item, PersonBO person) {
        this.person = person;

        this.contractNr = item.getCancellationContractNr();
        this.professionCode = item.getCancellationProfessionCode();
        this.keyAspect = item.getCancellationKeyAspect();
        this.educationYear = item.getCancellationEducationYear();
        this.sbfiCode = item.getCancellationSbfiCode();
        this.contractType = item.getCancellationContractType();
        this.comment = item.getCancellationComment();
        this.cancelDate = item.getCancellationCancelDate();
        this.cancelReason = item.getCancellationCancelReason();
    }

    /**
     * Constructs a cancellation business object from a database object
     *
     * @param persistEvent database object
     * @param person       A cancellation belongs to exactly one person
     */
    public CancellationBO(SbgEvent persistEvent, PersonBO person) {
        super(persistEvent, person);

        this.cancelDate = persistEvent.getCancelDate() != null ? dateToString(persistEvent.getCancelDate()) : null;
        this.cancelReason = persistEvent.getCancelReason() != null ? persistEvent.getCancelReason().toString() : null;
    }

    @Override
    public void addXml(Pers persXml) {
        Cancellation abXml = persXml.addNewAb();
        if (this.thisEvent.getContractNr() != null) {
            abXml.setVertnr(this.thisEvent.getContractNr().toString());
        }
        if (this.thisEvent.getProfessionCode() != null) {
            abXml.setProfid(this.thisEvent.getProfessionCode().toString());
        }
        if (this.thisEvent.getKeyAspect() != null) {
            abXml.setSchwerpunkt(this.thisEvent.getKeyAspect().toString());
        }
        if (this.thisEvent.getEducationYear() != null) {
            abXml.setAusbjahr(this.thisEvent.getEducationYear().toString());
        }
        if (this.thisEvent.getSbfiCode() != null) {
            abXml.setSbficode(this.thisEvent.getSbfiCode().toString());
        }
        if (this.thisEvent.getContractType() != null) {
            abXml.setLehrtyp(this.thisEvent.getContractType().toString());
        }
        if (this.thisEvent.getCancelDate() != null) {
            abXml.setAbdat(dateToString(this.thisEvent.getCancelDate()));
        }
        if (this.thisEvent.getCancelReason() != null) {
            abXml.setAbtyp(this.thisEvent.getCancelReason().toString());
        }
        if (this.thisEvent.getUserComment() != null) {
            abXml.setCom(this.thisEvent.getUserComment());
        }
    }

    @Override
    public void format() {
        if (this.thisEvent == null) {
            this.thisEvent = new SbgEvent();
        }
        this.thisEvent.setType(CodegroupUtility.SBG_EVENTTYPE_CANCELLATION);

        super.format();

        Date cancelDate = verifyDate(getCancelDate());
        this.thisEvent.setCancelDate(cancelDate);

        Long cancelReason = verifyLong(getCancelReason());
        this.thisEvent.setCancelReason(cancelReason);
    }
}
