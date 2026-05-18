/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: IWizardService.java 1162 2010-03-26 12:39:56Z jfu $

 */
package ch.bfs.meb.ssp.web.service;

import java.util.List;

import ch.bfs.meb.ssp.web.ws.sspwizard.*;

/**
 * Interface for generic delivery services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 1162 $
 */
public interface IWizardService {
    public UserNameListResult getDlUserNames(Long version);

    public SspWizardSchoolListResult getSchools(String dlUser, Long version);

    //	public SspSchoolResult deleteSchool(SspSchool sspSchool);
    public SspDeliveryListResult deleteDeliveries(String dlUser, Long version);

    public SspPlausiErrorListResult getErrors(String dlUser, Long version);

    public FileResult getPlausireport(String dlUser, Long version, String locale);

    public SspPlausiErrorListResult confirmErrors(List<SspPlausiError> plausiErrors);

    public Boolean areDeliveriesValidated(String dlUser, Long version);

    public SspDeliveryListResult validateDeliveries(String dlUser, Long version, String locale);
}
