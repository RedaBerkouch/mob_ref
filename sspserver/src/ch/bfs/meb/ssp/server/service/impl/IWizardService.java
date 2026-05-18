/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: IWizardService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspDeliveryListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiErrorListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspWizardSchoolListResult;

/**
 * Interface for generic wizard services.
 * 
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IWizardService {
    public UserNameListResult getDlUserNames(Long version);

    public SspWizardSchoolListResult getSchools(String dlUser, Long version);

    //	public BurSchoolResult deleteSchool(String dlUser, Long version, BurSchool burSchool);
    public SspDeliveryListResult deleteDeliveries(String dlUser, Long version);

    public SspPlausiErrorListResult getErrors(String dlUser, Long version);

    public FileResult getPlausireport(String dlUser, Long version, String locale);

    public SspPlausiErrorListResult confirmErrors(List<SspPlausiError> plausiErrors);

    public Boolean areDeliveriesValidated(String dlUser, Long version);

    public SspDeliveryListResult validateDeliveries(String dlUser, Long version, String locale);
}
