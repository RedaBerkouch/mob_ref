/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.bfs.meb.sba.server.business.plausi.PlausiBO;
import ch.bfs.meb.sba.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sba.server.integration.dto.SbaCanton;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.repository.ICantonRepository;
import ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Business object for handling Sba cantons.
 * 
 * @author $Author: dzw $
 * @version $Revision: 983 $
 */
public class CantonBO extends BOBase {
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private final SbaCanton _thisCanton;

    /**
     * Constructs a canton and the associated plausierror business objects from a database object.
     * 
     * @param canton	database object
     */
    public CantonBO(SbaCanton canton) {
        _thisCanton = canton;
        _confirmRules = canton.getConfirmRules();

        for (SbaPlausiError plausierror : canton.getPlausierrors()) {
            if (plausierror.getDeliveryId() == null) {
                _plausierrors.add(new PlausierrorBO(plausierror));
            }
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.business.BOBase#format()
     */
    @Override
    public void format() {}

    /**
     * Verifies this canton with all the plausis in the plausiList.
     * 
     * @param plausiList	list with all plausis
     */
    public void verifyCanton(List<PlausiBO> plausiList) {
        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Calculates the plausistatus of this class only based on the
     * plausierror business objects. No reload of database objects.
     * 
     * @return	plausistatus of the canton
     */
    private Long calculatePlausistatus() {
        Long newPlausistatus;
        if (_plausierrors.isEmpty()) {
            newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_VALID;
        } else {
            newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
            for (PlausierrorBO error : _plausierrors) {
                if (!error.getThisPlausierror().getIsConfirmed()) {
                    newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
                    break;
                }
            }
        }

        return newPlausistatus;
    }

    /**
     * Save all associated plausierrors.
     * 
     * @param repositories	Access to database repositories.
     */
    public void savePlausierrors(IPlausiErrorRepository peRepository, ICantonRepository cantonRepository, String userEmail) {
        Iterator<PlausierrorBO> iter = _plausierrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = iter.next();
            SbaPlausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(peRepository, userEmail);
                    _thisCanton.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    peRepository.deletePlausiError(pe.getThisPlausierror());
                    _thisCanton.getPlausierrors().remove(pe.getThisPlausierror());
                    iter.remove();
                }
            } else {
                // canton errors have been set to delete before recalculating
                // peRepository.updatePlausiError(pe.getThisPlausierror());
            }
        }
        Long newPlausistatus = calculatePlausistatus();
        if (!newPlausistatus.equals(_thisCanton.getPlausiStatus())) {
            _thisCanton.setPlausiStatus(newPlausistatus);
            _thisCanton.setModification_user(userEmail);
            _thisCanton.setModification_date(new Date());
            cantonRepository.updateCanton(_thisCanton);
        }
    }

    /**
     * @return the _plausierrors
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }

    /**
     * @return the _thisDelivery
     */
    public SbaCanton getThisCanton() {
        return _thisCanton;
    }
}
