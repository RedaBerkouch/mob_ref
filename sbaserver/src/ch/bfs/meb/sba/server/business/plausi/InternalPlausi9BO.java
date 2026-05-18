/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InternalPlausi11BO.java 1486 2010-05-05 14:29:23Z dzw $
 */
package ch.bfs.meb.sba.server.business.plausi;

import ch.bfs.meb.sba.server.business.DeliveryBO;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausi;
import ch.bfs.meb.sba.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;

/** 
 * Plausi 9 Keine doppelten Personen
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 1486 $ 
 */
public class InternalPlausi9BO extends InternalPlausiBO {
    final IPlausiRepository _repository;

    public InternalPlausi9BO(SbaPlausi plausi, IPlausiRepository repository, IServerLocalizationManager localizationManager) {
        super(plausi, localizationManager);
        _repository = repository;
    }

    protected boolean doVerify(DeliveryBO delivery) {
        boolean verified = true;

        for (String personId : _repository.findDuplicatePersonPlausi9(delivery.getThisDelivery().getDeliveryId())) {
            // generate plausierror
            String[] parameterList = { personId };
            delivery.getPlausierrors().add(new PlausierrorBO(delivery, getThisPlausi(), PlausierrorBO.PLAUSIERROR_DUPLICATE_PERSON, parameterList,
                    personId.toString(), getLocalizationManager()));
            verified = false;
        }

        return verified;
    }
}
