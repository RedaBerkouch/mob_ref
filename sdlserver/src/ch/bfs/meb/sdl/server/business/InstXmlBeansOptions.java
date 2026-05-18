/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sdl.server.business;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.springframework.oxm.xmlbeans.XmlOptionsFactoryBean;

import ch.bfs.meb.sdl.server.service.xmlbeans.TableDocument;

/**
 * XmlBeans options for SdlInst.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class InstXmlBeansOptions extends XmlOptionsFactoryBean {

    public InstXmlBeansOptions() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put(XmlOptions.DOCUMENT_TYPE, TableDocument.Table.Inst.type);
        setOptions(options);
    }
}
