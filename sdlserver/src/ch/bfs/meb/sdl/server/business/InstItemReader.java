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
 * Reader for the Inst part of an Sdl delivery file
 * 
 * @author $Author$
 * @version $Revision$
 */
public class InstItemReader extends MebStaxEventItemReader<SchoolBO> {
    /* (non-Javadoc)
     * @see org.springframework.batch.item.xml.StaxEventItemReader#doRead()
     */
    @Override
    protected SchoolBO doRead() {
        TableDocument.Table.Inst xmlPart = null;
        try {
            xmlPart = (TableDocument.Table.Inst) super.doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        if (xmlPart != null) {
            xmlPart = ((TableDocument.Table) xmlPart.changeType(TableDocument.Table.type)).getInstArray(0);
            return new SchoolBO(xmlPart);
        } else {
            return null;
        }
    }
}
