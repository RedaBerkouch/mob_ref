/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EnterpriseBO.java 570 2009-01-08 15:47:32Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import ch.bfs.meb.sbg.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.service.xmlbeans.Contract;

/**
 * Business object for an enterprise. An enterprise is part of a contract.
 *
 * @author $Author: lsc $
 * @version $Revision: 570 $
 */
public class EnterpriseBO {
    private static final int BUR_NR = 6;
    private static final int KANTLBCODE = 7;
    private static final int NAME = 8;
    private static final int STREET = 9;
    private static final int STREET_NR = 10;
    private static final int PLZ = 11;
    private static final int MUNICIPALITY = 12;
    private static final int FLAG_LBV = 13;

    private String _burNr;
    private String _kantLbCode;
    private String _name;
    private String _street;
    private String _streetNr;
    private String _plz;
    private String _municipality;
    private String _flagLbv;

    /**
     * Constructs an enterprise business object from XML part
     *
     * @param xmlPart XML bean part for enterprise
     */
    public EnterpriseBO(Contract.Unt xmlPart) {
        if (xmlPart != null) {
            _burNr = xmlPart.getBurnr();
            if (xmlPart.getNobur() != null) {
                _kantLbCode = xmlPart.getNobur().getKntLbCode();
                _name = xmlPart.getNobur().getUntname();
                _street = xmlPart.getNobur().getStr();
                _streetNr = xmlPart.getNobur().getStrnr();
                _plz = xmlPart.getNobur().getPlz();
                _municipality = xmlPart.getNobur().getGem();
                _flagLbv = xmlPart.getNobur().getFlaglbv();
            }
        }
    }

    public EnterpriseBO(ItemBO item) {
        _burNr = item.getEnterpriseBurNr();
        //		if (_burNr == null)
        //		{
        _kantLbCode = item.getEnterpriseKantLbCode();
        _name = item.getEnterpriseName();
        _street = item.getEnterpriseStreet();
        _streetNr = item.getEnterpriseStreetNr();
        _plz = item.getEnterprisePlz();
        _municipality = item.getEnterpriseMunicipality();
        _flagLbv = item.getEnterpriseFlagLbv();
        //		}
    }

    /**
     * Constructs an enterprise business object from CSV part
     *
     * @param contractData CSV bean part for enterprise
     */
    public EnterpriseBO(String[] contractData) {
        _burNr = contractData[BUR_NR].equals("") ? null : contractData[BUR_NR];
        if (contractData[BUR_NR] == null || contractData[BUR_NR].trim().equals("")) {
            _kantLbCode = contractData[KANTLBCODE].equals("") ? null : contractData[KANTLBCODE];
            _name = contractData[NAME].equals("") ? null : contractData[NAME];
            _street = contractData[STREET].equals("") ? null : contractData[STREET];
            _streetNr = contractData[STREET_NR].equals("") ? null : contractData[STREET_NR];
            _plz = contractData[PLZ].equals("") ? null : contractData[PLZ];
            _municipality = contractData[MUNICIPALITY].equals("") ? null : contractData[MUNICIPALITY];
            _flagLbv = contractData[FLAG_LBV].equals("") ? null : contractData[FLAG_LBV];
        }
    }

    /**
     * Constructs an enterprise business object from a database object
     *
     * @param persistentEvent database object
     */
    public EnterpriseBO(SbgEvent persistentEvent) {
        _burNr = persistentEvent.getBurnr() != null ? persistentEvent.getBurnr().toString() : null;
        _kantLbCode = persistentEvent.getKantLbCode() != null ? persistentEvent.getKantLbCode() : null;
        _name = persistentEvent.getFirmName();
        _street = persistentEvent.getFirmStreet();
        _streetNr = persistentEvent.getFirmStreetNr();
        _plz = persistentEvent.getFirmPlz() != null ? persistentEvent.getFirmPlz().toString() : null;
        _municipality = persistentEvent.getFirmMunicipality();
        _flagLbv = persistentEvent.getFlagLbv() ? "1" : "0";
    }

    /**
     * @return Returns the _burNr.
     */
    public String get_burNr() {
        return _burNr;
    }

    /**
     * @return Returns the _kantLbCode.
     */
    public String get_kantLbCode() {
        return _kantLbCode;
    }

    /**
     * @return Returns the _flagLbv.
     */
    public String get_flagLbv() {
        return _flagLbv;
    }

    /**
     * @return Returns the _municipality.
     */
    public String get_municipality() {
        return _municipality;
    }

    /**
     * @return Returns the _name.
     */
    public String get_name() {
        return _name;
    }

    /**
     * @return Returns the _plz.
     */
    public String get_plz() {
        return _plz;
    }

    /**
     * @return Returns the _street.
     */
    public String get_street() {
        return _street;
    }

    /**
     * @return Returns the _streetNr.
     */
    public String get_streetNr() {
        return _streetNr;
    }
}
