/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.business;

import ch.bfs.meb.sdl.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.server.commons.business.MebStaxEventItemReader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Reader for the Head part of an Sdl delivery file
 * 
 * @author $Author$
 * @version $Revision$
 */
public class HeadItemReader extends MebStaxEventItemReader<DeliveryBO> {
    /* (non-Javadoc)
     * @see org.springframework.batch.item.xml.StaxEventItemReader#doRead()
     */
    @Override
    protected DeliveryBO doRead()  {
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
