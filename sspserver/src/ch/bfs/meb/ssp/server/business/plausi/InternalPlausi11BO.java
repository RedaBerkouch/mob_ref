/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id$
 */
package ch.bfs.meb.ssp.server.business.plausi;

import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.ssp.server.business.DeliveryBO;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository;

/** 
 * Plausi 11 Keine doppelten Personen
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi11BO extends InternalPlausiBO {
    final IPlausiRepository _repository;

    public InternalPlausi11BO(SspPlausi plausi, IPlausiRepository repository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repository = repository;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;

        for (String personId : _repository.findDuplicatePersonPlausi11(delivery.getThisDelivery().getDeliveryId())) {
            // generate plausierror
            String[] parameterList = { personId };
            delivery.getPlausierrors().add(new PlausierrorBO(delivery, getThisPlausi(), PlausierrorBO.PLAUSIERROR_DUPLICATE_PERSON, parameterList,
                    personId.toString(), getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
