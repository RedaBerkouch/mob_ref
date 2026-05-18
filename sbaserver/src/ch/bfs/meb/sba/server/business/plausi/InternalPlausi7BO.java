/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi9BO.java 1525 2010-05-07 10:39:27Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.DeliveryBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 7 Mind. eine Person pro Lieferung
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1525 $ 
 */
public class InternalPlausi7BO extends InternalPlausiBO {
    final IDeliveryRepository _repository;

    public InternalPlausi7BO(SbaPlausi plausi, IDeliveryRepository repository, IServerLocalizationManager localizationManager) {
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
