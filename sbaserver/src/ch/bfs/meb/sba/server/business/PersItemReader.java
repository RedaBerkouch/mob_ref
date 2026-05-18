/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: InstItemReader.java 530 2010-01-26 12:48:22Z dzw $

 */
package ch.bfs.meb.sba.server.business;

import ch.bfs.meb.sba.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.server.commons.business.MebStaxEventItemReader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Reader for the Inst part of an Sba delivery file
 * 
 * @author $Author: jfu $
 * @version $Revision: 530 $
 */
public class PersItemReader extends MebStaxEventItemReader<PersonBO> {
    /* (non-Javadoc)
     * @see org.springframework.batch.item.xml.StaxEventItemReader#doRead()
     */
    @Override
    protected PersonBO doRead() {
        TableDocument.Table.Pers xmlPart = null;
        try {
            xmlPart = (TableDocument.Table.Pers) super.doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        if (xmlPart != null) {
            xmlPart = ((TableDocument.Table) xmlPart.changeType(TableDocument.Table.type)).getPersArray(0);
            return new PersonBO(xmlPart);
        } else {
            return null;
        }
    }
}