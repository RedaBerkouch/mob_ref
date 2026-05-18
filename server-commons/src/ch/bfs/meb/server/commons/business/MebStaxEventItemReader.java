/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.server.commons.business;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * TODO Document this class
 * 
 */
public class MebStaxEventItemReader<T> extends StaxEventItemReader<T> {

    private String fragmentRootElementName;

    /**
     * @param fragmentRootElementName
     *            name of the root element of the fragment TODO String can be
     *            ambiguous due to namespaces, use QName?
     */
    public void setFragmentRootElementName(String fragmentRootElementName) {
        this.fragmentRootElementName = fragmentRootElementName;
        super.setFragmentRootElementName(fragmentRootElementName);
    }

    /**
     * @see org.springframework.batch.item.xml.StaxEventItemReader#moveCursorToNextFragment(javax.xml.stream.XMLEventReader)
     */
    @Override
    protected boolean moveCursorToNextFragment(XMLEventReader reader) {
        try {
            while (true) {
                while (!reader.peek().isEndDocument() && !reader.peek().isStartElement()) {

                    reader.nextEvent();
                }
                if (reader.peek().isEndDocument()) {
                    return false;
                }
                QName startElementName = ((StartElement) reader.peek()).getName();
                if (startElementName.getLocalPart().equals(fragmentRootElementName)) {
                    return true;
                } else {
                    reader.nextEvent();
                }
            }
        } catch (XMLStreamException e) {
            throw new DataAccessResourceFailureException("Error while reading from event reader", e);
        }
    }

}
