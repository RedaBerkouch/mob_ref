/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2012

  Projekt: sbgserver

 */
package ch.bfs.meb.sbg.server.business;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.springframework.oxm.xmlbeans.XmlOptionsFactoryBean;

import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument;

/**
 * XmlBeans options for SbgHead.
 * 
 * @author $Author: msc $
 * @version $Revision: 2262 $
 */
public class HeadXmlBeansOptions extends XmlOptionsFactoryBean {
    public HeadXmlBeansOptions() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put(XmlOptions.DOCUMENT_TYPE, TableDocument.Table.Head.type);
        setOptions(options);
    }
}
