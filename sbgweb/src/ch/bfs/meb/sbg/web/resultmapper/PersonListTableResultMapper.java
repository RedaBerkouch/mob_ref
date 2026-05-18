/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: ConfigDeliveryListTableResultMapper.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sbg.web.resultmapper;

import java.util.List;

import ch.bfs.meb.sbg.web.ws.sbgperson.Person;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonList;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.ListResultMapperBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Extracts a list of Events from EventList
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class PersonListTableResultMapper extends ListResultMapperBase {
    /**
     * @param result
     * @throws DhtmlxException
     */
    public PersonListTableResultMapper(PersonList result, IWebLocalizationManager languageManager, Long resultSize, Integer position) throws DhtmlxException {
        super(result, languageManager, resultSize != null ? resultSize.intValue() : null, position);
    }

    @Override
    public List<Person> getData() {
        return ((PersonList) getResult()).getPersons();
    }
}