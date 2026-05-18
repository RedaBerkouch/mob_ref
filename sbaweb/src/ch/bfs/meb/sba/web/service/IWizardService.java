/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: IWizardService.java 1162 2010-03-26 12:39:56Z jfu $

 */
package ch.bfs.meb.sba.web.service;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbawizard.*;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 1162 $
 */
public interface IWizardService {
    public UserNameListResult getDlUserNames(Long version);

    public SbaWizardSchoolListResult getSchools(String dlUser, Long version);

    //	public SbaSchoolResult deleteSchool(SbaSchool sbaSchool);
    public SbaDeliveryListResult deleteDeliveries(String dlUser, Long version);

    public SbaPlausiErrorListResult getErrors(String dlUser, Long version);

    public FileResult getPlausireport(String dlUser, Long version, String locale);

    public SbaPlausiErrorListResult confirmErrors(List<SbaPlausiError> plausiErrors);

    public Boolean areDeliveriesValidated(String dlUser, Long version);

    public SbaDeliveryListResult validateDeliveries(String dlUser, Long version, String locale);
}
