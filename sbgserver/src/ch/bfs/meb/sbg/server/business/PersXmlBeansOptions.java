/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

 */
package ch.bfs.meb.sbg.server.business;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.springframework.oxm.xmlbeans.XmlOptionsFactoryBean;

import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument;

/**
 * XmlBeans options for SbgPers.
 * 
 * @author $Author: jfu $
 * @version $Revision: 530 $
 */
public class PersXmlBeansOptions extends XmlOptionsFactoryBean {

    public PersXmlBeansOptions() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put(XmlOptions.DOCUMENT_TYPE, TableDocument.Table.Pers.type);
        setOptions(options);
    }
}
