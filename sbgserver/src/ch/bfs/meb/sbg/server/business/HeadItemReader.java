/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: HeadItemReader.java 2262 2011-04-21 09:27:01Z msc $

 */
package ch.bfs.meb.sbg.server.business;

import ch.admin.bfs.sbg.business.DeliveryBO;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.server.commons.business.MebStaxEventItemReader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Reader for the Head part of an Sbg delivery file
 * 
 * @author $Author: msc $
 * @version $Revision: 2262 $
 */
public class HeadItemReader extends MebStaxEventItemReader<DeliveryBO> {
    /* (non-Javadoc)
     * @see org.springframework.batch.item.xml.StaxEventItemReader#doRead()
     */
    @Override
    protected DeliveryBO doRead() {
        TableDocument.Table.Head xmlPart = null;
        try {
            xmlPart = (TableDocument.Table.Head) super.doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        if (xmlPart != null) {
            xmlPart = ((TableDocument.Table) xmlPart.changeType(TableDocument.Table.type)).getHead();
            return new DeliveryBO(xmlPart);
        } else {
            return null;
        }
    }
}
