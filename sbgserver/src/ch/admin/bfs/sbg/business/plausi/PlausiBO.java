/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PlausiBO.java 550 2008-10-02 17:14:06Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business.plausi;

import java.util.List;

import ch.admin.bfs.sbg.business.BOBase;
import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.admin.bfs.sbg.business.EventBO;
import ch.admin.bfs.sbg.business.PersonBO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;

/**
 * @author $Author: lsc $
 * @version $Revision: 550 $
 */
public abstract class PlausiBO {
    // Delivery: XML Tags
    protected final static String XML_TAG_LIDAT_NAME = "xml.tag.lidat.name";
    // Persons: XML Tags
    protected final static String XML_TAG_IDNR_NAME = "xml.tag.idnr.name";
    protected final static String XML_TAG_IDTYP_NAME = "xml.tag.idtyp.name";
    protected final static String XML_TAG_SEX_NAME = "xml.tag.sex.name";
    protected final static String XML_TAG_DATEOFBIRTH_NAME = "xml.tag.dateofbirth.name";
    protected final static String XML_TAG_NEWDATEOFBIRTH_NAME = "xml.tag.newdateofbirth.name";
    protected final static String XML_TAG_COM_NAME = "xml.tag.com.name";
    // Events: XML Tags
    protected final static String XML_TAG_SBFICODE_NAME = "xml.tag.sbficode.name";
    protected final static String XML_TAG_VERTNR_NAME = "xml.tag.vertnr.name";
    protected final static String XML_TAG_PROFID_NAME = "xml.tag.profid.name";
    protected final static String XML_TAG_KEYASPECT_NAME = "xml.tag.keyaspect.name";
    protected final static String XML_TAG_EDUCTAIONYEAR_NAME = "xml.tag.educationyear.name";
    protected final static String XML_TAG_LEHRTYP_NAME = "xml.tag.lehrtyp.name";
    protected final static String XML_TAG_VERTDAT_NAME = "xml.tag.vertdat.name";
    protected final static String XML_TAG_EXTYP_NAME = "xml.tag.extyp.name";
    protected final static String XML_TAG_EXNR_NAME = "xml.tag.exnr.name";
    protected final static String XML_TAG_REP_NAME = "xml.tag.rep.name";
    protected final static String XML_TAG_RES_NAME = "xml.tag.res.name";
    protected final static String XML_TAG_ABDAT_NAME = "xml.tag.abdat.name";
    protected final static String XML_TAG_ABTYP_NAME = "xml.tag.abtyp.name";
    protected final static String XML_TAG_BURNR_NAME = "xml.tag.burnr.name";
    protected final static String XML_TAG_KANTLBCODE_NAME = "xml.tag.kantlbcode.name";
    protected final static String XML_TAG_UNTNAME_NAME = "xml.tag.untname.name";
    protected final static String XML_TAG_STR_NAME = "xml.tag.str.name";
    protected final static String XML_TAG_STRNR_NAME = "xml.tag.strnr.name";
    protected final static String XML_TAG_PLZ_NAME = "xml.tag.plz.name";
    protected final static String XML_TAG_GEM_NAME = "xml.tag.gem.name";
    protected final static String XML_TAG_FLAGLBV_NAME = "xml.tag.flaglbv.name";

    protected final Macro _thisPlausi;

    public PlausiBO(Macro plausi) {
        _thisPlausi = plausi;
    }

    public boolean verify(DeliveryBO delivery) {
        boolean verified = doVerify(delivery);

        if (_thisPlausi.getIsconfirmable().equals(Macro.MACRO_IS_CONFIRMABLE)) {
            List<PlausierrorBO> errorList = delivery.get_plausierrors();
            updateConfirmedPlausierrors(errorList);
        }

        return verified;
    }

    public boolean verify(PersonBO person) {
        boolean verified = doVerify(person);

        if (_thisPlausi.getIsconfirmable().equals(Macro.MACRO_IS_CONFIRMABLE)) {
            List<PlausierrorBO> errorList = person.get_plausierrors();
            // Mantis 1783: set transient attributes of SspPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                Plausierror error = plausierrorBO.get_thisPlausierror();
                error.addPersonInfo(person.get_idType(), person.get_idNr());
            }
            updateConfirmedPlausierrors(errorList);
        }

        return verified;
    }

    public boolean verify(EventBO event) {
        boolean verified = doVerify(event);

        if (_thisPlausi.getIsconfirmable().equals(Macro.MACRO_IS_CONFIRMABLE)) {
            List<PlausierrorBO> errorList = event.getPlausiErrors();
            // Mantis 1783: set transient attributes of SspPlausiError for generation of logical key in replace/amend use case
            for (PlausierrorBO plausierrorBO : errorList) {
                Plausierror error = plausierrorBO.get_thisPlausierror();
                error.addPersonInfo(event.getPerson().get_idType(), event.getPerson().get_idNr());
                error.addEventInfo(event.getThisEvent().getType(), event.getContractNr());
            }
            updateConfirmedPlausierrors(errorList);
        }

        return verified;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        return true;
    }

    protected boolean doVerify(PersonBO person) {
        return true;
    }

    protected boolean doVerify(EventBO event) {
        return true;
    }

    protected void updateConfirmedPlausierrors(List<PlausierrorBO> errorList) {
        for (PlausierrorBO plausierrorBO : errorList) {
            Plausierror pe = plausierrorBO.get_thisPlausierror();
            if (pe.getPlausiId().equals(_thisPlausi.getMacroid()) && (pe.getErrorId() == null)) {
                for (PlausierrorBO plausierrorBO2 : errorList) {
                    Plausierror oldPE = plausierrorBO2.get_thisPlausierror();
                    if (oldPE.getPlausiId().equals(_thisPlausi.getMacroid()) && (oldPE.getErrorId() != null)
                            && (oldPE.getConfirmId() == null || oldPE.getConfirmId().equals(pe.getConfirmId()))) {
                        if (oldPE.getIsConfirmed() != pe.getIsConfirmed()) {
                            // take over confirmation information
                            pe.setIsConfirmed(oldPE.getIsConfirmed());
                            pe.setModification_user(oldPE.getModification_user());
                            pe.setModification_date(oldPE.getModification_date());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * @return Returns the _thisPlausi.
     */
    public Macro get_thisPlausi() {
        return _thisPlausi;
    }

    protected String getName_de(String key) {
        return BOBase.getResource_de().getString(key);
    }

    protected String getName_fr(String key) {
        return BOBase.getResource_fr().getString(key);
    }
}
