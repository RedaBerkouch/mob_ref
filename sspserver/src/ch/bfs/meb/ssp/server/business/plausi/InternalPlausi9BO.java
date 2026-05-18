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
import ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository;

/** 
 * Plausi 9 Mind. eine Person pro Lieferung
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class InternalPlausi9BO extends InternalPlausiBO {
    final IDeliveryRepository _repository;

    public InternalPlausi9BO(SspPlausi plausi, IDeliveryRepository repository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repository = repository;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;

        if (!_repository.existsPerson(delivery.getThisDelivery().getDeliveryId())) {
            // generate plausierror
            delivery.getPlausierrors()
                    .add(new PlausierrorBO(delivery, getThisPlausi(), PlausierrorBO.PLAUSIERROR_NO_PERSON, null, null, getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
