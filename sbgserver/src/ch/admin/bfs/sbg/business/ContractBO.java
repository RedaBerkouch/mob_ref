/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ContractBO.java 644 2010-12-06 15:19:20Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import java.util.Date;

import ch.bfs.meb.sbg.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.service.xmlbeans.Contract;
import ch.bfs.meb.sbg.server.service.xmlbeans.NoBurGroup;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Getter;

/**
 * Business object for a contract. Derived from EventBO.
 *
 * @author $Author: lsc $
 * @version $Revision: 644 $
 */
@Getter
public class ContractBO extends EventBO {
    //	private static final int CONTRACT_NR = 2;
    //	private static final int PROFESSION_CODE = 3;
    //	private static final int CONTRACT_TYPE = 4;
    //	private static final int CONTRACT_DATE = 5;
    //	private static final int COMMENT = 14;

    private String contractDate;

    private EnterpriseBO enterprise;

    /**
     * Constructs a contract business object from XML part
     *
     * @param xmlPart XML bean part for contract
     * @param person  A contract belongs to exactly one person
     */
    public ContractBO(Contract xmlPart, PersonBO person) {
        this.person = person;

        this.contractNr = xmlPart.getVertnr();
        this.professionCode = xmlPart.getProfid();
        this.keyAspect = xmlPart.getSchwerpunkt();
        this.educationYear = xmlPart.getAusbjahr();
        this.sbfiCode = xmlPart.getSbficode();
        this.contractType = xmlPart.getLehrtyp();
        this.comment = xmlPart.getCom();
        this.contractDate = xmlPart.getVertdat();

        Contract.Unt enterprise = xmlPart.getUnt();
        this.enterprise = new EnterpriseBO(enterprise);
    }

    public ContractBO(ItemBO item, PersonBO person) {
        this.person = person;

        this.contractNr = item.getContractNr();
        this.professionCode = item.getContractProfessionCode();
        this.keyAspect = item.getContractKeyAspect();
        this.educationYear = item.getContractEducationYear();
        this.sbfiCode = item.getContractSbfiCode();
        this.contractType = item.getContractType();
        this.comment = item.getContractComment();
        this.contractDate = item.getContractDate();

        this.enterprise = new EnterpriseBO(item);
    }

    /**
     * Constructs a contract business object from a database object
     *
     * @param persistEvent database object
     * @param person       A contract belongs to exactly one person
     */
    public ContractBO(SbgEvent persistEvent, PersonBO person) {
        super(persistEvent, person);

        this.contractDate = persistEvent.getContractDate() != null ? dateToString(persistEvent.getContractDate()) : null;

        this.enterprise = new EnterpriseBO(persistEvent);
    }

    @Override
    public void addXml(Pers persXml) {
        Contract vertXml = persXml.addNewVert();
        if (this.thisEvent.getContractNr() != null) {
            vertXml.setVertnr(this.thisEvent.getContractNr().toString());
        }
        if (this.thisEvent.getProfessionCode() != null) {
            vertXml.setProfid(this.thisEvent.getProfessionCode().toString());
        }
        if (this.thisEvent.getKeyAspect() != null) {
            vertXml.setSchwerpunkt(this.thisEvent.getKeyAspect().toString());
        }
        if (this.thisEvent.getEducationYear() != null) {
            vertXml.setAusbjahr(this.thisEvent.getEducationYear().toString());
        }
        if (this.thisEvent.getSbfiCode() != null) {
            vertXml.setSbficode(this.thisEvent.getSbfiCode().toString());
        }
        if (this.thisEvent.getContractType() != null) {
            vertXml.setLehrtyp(this.thisEvent.getContractType().toString());
        }
        if (this.thisEvent.getContractDate() != null) {
            vertXml.setVertdat(dateToString(this.thisEvent.getContractDate()));
        }

        Contract.Unt untXml = vertXml.addNewUnt();
        if (this.thisEvent.getBurnr() != null) {
            untXml.setBurnr(this.thisEvent.getBurnr().toString());
        }

        NoBurGroup noBurXml = untXml.addNewNobur();
        if (this.thisEvent.getKantLbCode() != null) {
            noBurXml.setKntLbCode(this.thisEvent.getKantLbCode());
        }
        if (this.thisEvent.getFirmName() != null) {
            noBurXml.setUntname(this.thisEvent.getFirmName());
        }
        if (this.thisEvent.getFirmStreet() != null) {
            noBurXml.setStr(this.thisEvent.getFirmStreet());
        }
        if (this.thisEvent.getFirmStreetNr() != null) {
            noBurXml.setStrnr(this.thisEvent.getFirmStreetNr());
        }
        if (this.thisEvent.getFirmPlz() != null) {
            noBurXml.setPlz(this.thisEvent.getFirmPlz().toString());
        }
        if (this.thisEvent.getFirmMunicipality() != null) {
            noBurXml.setGem(this.thisEvent.getFirmMunicipality());
        }
        noBurXml.setFlaglbv(this.thisEvent.getFlagLbv() ? "1" : "0");

        if (this.thisEvent.getUserComment() != null) {
            vertXml.setCom(this.thisEvent.getUserComment());
        }
    }

    @Override
    public void format() {
        if (this.thisEvent == null) {
            this.thisEvent = new SbgEvent();
        }
        this.thisEvent.setType(CodegroupUtility.SBG_EVENTTYPE_CONTRACT);

        super.format();

        Date contractDate = verifyDate(getContractDate());
        this.thisEvent.setContractDate(contractDate);

        EnterpriseBO enterprise = getEnterprise();
        Long burNr = verifyLong(enterprise.get_burNr());
        this.thisEvent.setBurnr(burNr);

        this.thisEvent.setKantLbCode(enterprise.get_kantLbCode());
        this.thisEvent.setFirmName(enterprise.get_name());
        this.thisEvent.setFirmStreet(enterprise.get_street());
        this.thisEvent.setFirmStreetNr(enterprise.get_streetNr());

        Long plz = verifyLong(enterprise.get_plz());
        this.thisEvent.setFirmPlz(plz);

        this.thisEvent.setFirmMunicipality(enterprise.get_municipality());

        Boolean flagLbv = verifyBoolean(enterprise.get_flagLbv());
        if (flagLbv == null) {
            this.thisEvent.setFlagLbv(false);
        } else {
            this.thisEvent.setFlagLbv(flagLbv);
        }
    }
}
