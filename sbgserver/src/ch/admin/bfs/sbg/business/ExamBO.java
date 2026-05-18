/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ExamBO.java 644 2010-12-06 15:19:20Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import ch.bfs.meb.sbg.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.service.xmlbeans.Exam;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Getter;

/**
 * Business object for exam. Derived from EventBO.
 *
 * @author $Author: lsc $
 * @version $Revision: 644 $
 */
@Getter
public class ExamBO extends EventBO {
    //	private static final int CONTRACT_NR = 2;
    //	private static final int PORFESSION_CODE = 3;
    //	private static final int CONTRACT_TYPE = 4;
    //	private static final int COMMENT = 9;
    //	private static final int EXAM_TYPE = 5;
    //	private static final int EXAM_NR = 6;
    //	private static final int REPETITION = 7;
    //	private static final int RESULT = 8;

    private String examType;
    private String examNr;
    private String repetition;
    private String result;

    /**
     * Constructs an exam business object from XML part
     *
     * @param xmlPart XML bean part for exam
     * @param person  An exam belongs to exactly one person
     */
    public ExamBO(Exam xmlPart, PersonBO person) {
        this.person = person;

        this.contractNr = xmlPart.getVertnr();
        this.professionCode = xmlPart.getProfid();
        this.keyAspect = xmlPart.getSchwerpunkt();
        this.educationYear = xmlPart.getAusbjahr();
        this.sbfiCode = xmlPart.getSbficode();
        this.contractType = xmlPart.getLehrtyp();
        this.comment = xmlPart.getCom();
        this.examType = xmlPart.getExtyp();
        this.examNr = xmlPart.getExnr();
        this.repetition = xmlPart.getRep();
        this.result = xmlPart.getRes();
    }

    public ExamBO(ItemBO item, PersonBO person) {
        this.person = person;

        this.contractNr = item.getExamContractNr();
        this.professionCode = item.getExamProfessionCode();
        this.keyAspect = item.getExamKeyAspect();
        this.educationYear = item.getExamEducationYear();
        this.contractType = item.getExamContractType();
        this.comment = item.getExamComment();
        this.sbfiCode = item.getExamSbfiCode();
        this.examType = item.getExamType();
        this.examNr = item.getExamNr();
        this.repetition = item.getExamRepetition();
        this.result = item.getExamResult();
    }

    /**
     * Constructs an exam business object from a database object
     *
     * @param persistEvent database object
     * @param person       An exam belongs to exactly one person
     */
    public ExamBO(SbgEvent persistEvent, PersonBO person) {
        super(persistEvent, person);
        this.examType = persistEvent.getExamType() != null ? persistEvent.getExamType().toString() : null;
        this.examNr = persistEvent.getExamNr() != null ? persistEvent.getExamNr().toString() : null;
        this.repetition = persistEvent.getExamRepetition() != null ? persistEvent.getExamRepetition().toString() : null;
        this.result = persistEvent.getExamResult() != null ? persistEvent.getExamResult().toString() : null;
    }

    @Override
    public void addXml(Pers persXml) {
        Exam examXml = persXml.addNewExam();
        if (this.thisEvent.getContractNr() != null) {
            examXml.setVertnr(this.thisEvent.getContractNr().toString());
        }
        if (this.thisEvent.getProfessionCode() != null) {
            examXml.setProfid(this.thisEvent.getProfessionCode().toString());
        }
        if (this.thisEvent.getKeyAspect() != null) {
            examXml.setSchwerpunkt(this.thisEvent.getKeyAspect().toString());
        }
        if (this.thisEvent.getEducationYear() != null) {
            examXml.setAusbjahr(this.thisEvent.getEducationYear().toString());
        }
        if (this.thisEvent.getSbfiCode() != null) {
            examXml.setSbficode(this.thisEvent.getSbfiCode().toString());
        }
        if (this.thisEvent.getContractType() != null) {
            examXml.setLehrtyp(this.thisEvent.getContractType().toString());
        }
        if (this.thisEvent.getExamType() != null) {
            examXml.setExtyp(this.thisEvent.getExamType().toString());
        }
        if (this.thisEvent.getExamNr() != null) {
            examXml.setExnr(this.thisEvent.getExamNr().toString());
        }
        if (this.thisEvent.getExamRepetition() != null) {
            examXml.setRep(this.thisEvent.getExamRepetition().toString());
        }
        if (this.thisEvent.getExamResult() != null) {
            examXml.setRes(this.thisEvent.getExamResult().toString());
        }
        if (this.thisEvent.getUserComment() != null) {
            examXml.setCom(this.thisEvent.getUserComment());
        }
    }

    @Override
    public void format() {
        if (this.thisEvent == null) {
            this.thisEvent = new SbgEvent();
        }
        this.thisEvent.setType(CodegroupUtility.SBG_EVENTTYPE_EXAM);

        super.format();

        Long examType = verifyLong(getExamType());
        this.thisEvent.setExamType(examType);

        Long examNr = verifyLong(getExamNr());
        this.thisEvent.setExamNr(examNr);

        Long repetition = verifyLong(getRepetition());
        this.thisEvent.setExamRepetition(repetition);

        Long result = verifyLong(getResult());
        this.thisEvent.setExamResult(result);
    }
}
