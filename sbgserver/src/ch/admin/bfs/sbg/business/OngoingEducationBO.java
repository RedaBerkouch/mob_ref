/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: OngoingEducationBO.java 644 2010-12-06 15:19:20Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import ch.bfs.meb.sbg.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.service.xmlbeans.OngoingEducation;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Getter;

/**
 * Business object for ongoing education. Derived from EventBO.
 *
 * @author $Author: lsc $
 * @version $Revision: 644 $
 */
@Getter
public class OngoingEducationBO extends EventBO {
    //	private static final int CONTRACT_NR = 2;
    //	private static final int PROFESSION_CODE = 3;
    //	private static final int COMMENT = 5;
    //	private static final int CONTRACT_TYPE = 4;

    /**
     * Constructs an ongoing education business object from XML part
     *
     * @param xmlPart XML bean part for ongoing education
     * @param person  An ongoing education belongs to exactly one person
     */
    public OngoingEducationBO(OngoingEducation xmlPart, PersonBO person) {
        this.person = person;

        this.contractNr = xmlPart.getVertnr();
        this.professionCode = xmlPart.getProfid();
        this.keyAspect = xmlPart.getSchwerpunkt();
        this.educationYear = xmlPart.getAusbjahr();
        this.sbfiCode = xmlPart.getSbficode();
        this.contractType = xmlPart.getLehrtyp();
        this.comment = xmlPart.getCom();
    }

    public OngoingEducationBO(ItemBO item, PersonBO person) {
        this.person = person;

        this.contractNr = item.getEducationContractNr();
        this.professionCode = item.getEducationProfessionCode();
        this.keyAspect = item.getEducationKeyAspect();
        this.educationYear = item.getEducationYear();
        this.sbfiCode = item.getEducationSbfiCode();
        this.contractType = item.getEducationContractType();
        this.comment = item.getEducationComment();
    }

    /**
     * Constructs an ongoing education business object from a database object
     *
     * @param persistEvent database object
     * @param person       An ongoing education belongs to exactly one person
     */
    public OngoingEducationBO(SbgEvent persistEvent, PersonBO person) {
        super(persistEvent, person);
    }

    @Override
    public void addXml(Pers persXml) {
        OngoingEducation laufXml = persXml.addNewLauf();
        if (this.thisEvent.getContractNr() != null) {
            laufXml.setVertnr(this.thisEvent.getContractNr().toString());
        }
        if (this.thisEvent.getProfessionCode() != null) {
            laufXml.setProfid(this.thisEvent.getProfessionCode().toString());
        }
        if (this.thisEvent.getKeyAspect() != null) {
            laufXml.setSchwerpunkt(this.thisEvent.getKeyAspect().toString());
        }
        if (this.thisEvent.getEducationYear() != null) {
            laufXml.setAusbjahr(this.thisEvent.getEducationYear().toString());
        }
        if (this.thisEvent.getSbfiCode() != null) {
            laufXml.setSbficode(this.thisEvent.getSbfiCode().toString());
        }
        if (this.thisEvent.getContractType() != null) {
            laufXml.setLehrtyp(this.thisEvent.getContractType().toString());
        }
        if (this.thisEvent.getUserComment() != null) {
            laufXml.setCom(this.thisEvent.getUserComment());
        }
    }

    @Override
    public void format() {
        if (this.thisEvent == null) {
            this.thisEvent = new SbgEvent();
        }
        this.thisEvent.setType(CodegroupUtility.SBG_EVENTTYPE_ONGOINGEDUCATION);

        super.format();
    }
}
