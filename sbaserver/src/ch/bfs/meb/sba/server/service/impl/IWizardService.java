/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: IWizardService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaDeliveryListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiErrorListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaWizardSchoolListResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.UserNameListResult;

/**
 * Interface for generic wizard services.
 * 
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IWizardService {
    public UserNameListResult getDlUserNames(Long version);

    public SbaWizardSchoolListResult getSchools(String dlUser, Long version);

    //	public BurSchoolResult deleteSchool(String dlUser, Long version, BurSchool burSchool);
    public SbaDeliveryListResult deleteDeliveries(String dlUser, Long version);

    public SbaPlausiErrorListResult getErrors(String dlUser, Long version);

    public FileResult getPlausireport(String dlUser, Long version, String locale);

    public SbaPlausiErrorListResult confirmErrors(List<SbaPlausiError> plausiErrors);

    public Boolean areDeliveriesValidated(String dlUser, Long version);

    public SbaDeliveryListResult validateDeliveries(String dlUser, Long version, String locale);
}
