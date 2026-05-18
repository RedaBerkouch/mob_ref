/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: InstItemReader.java 530 2010-01-26 12:48:22Z dzw $

 */
package ch.bfs.meb.sbg.server.business;

import ch.admin.bfs.sbg.business.PersonBO;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.server.commons.business.MebStaxEventItemReader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Reader for the Inst part of an Ssp delivery file
 * 
 * @author $Author: jfu $
 * @version $Revision: 530 $
 */
public class PersItemReader extends MebStaxEventItemReader<PersonBO> {
    private Long _deliveryId;
    private Long _canton;
    private Long _year;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setCanton(Long canton) {
        _canton = canton;
    }

    public void setYear(Long year) {
        _year = year;
    }

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
            return new PersonBO(xmlPart, _deliveryId, _canton, _year);
        } else {
            return null;
        }
    }
}