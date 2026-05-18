/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PersonList.java 345 2007-09-14 11:32:17Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

/**
 * TODO Describe this class
 * 
 * @author $Author: dzw $
 * @version $Revision: 345 $
 */
public class PersonList extends ResultBase {
    private static final long serialVersionUID = -3644758743001279597L;

    private Person[] _persons;
    private Long _resultSize;

    public PersonList() {
        _resultSize = 0L;
    }

    public PersonList(Person[] persons) {
        _persons = persons;
        _resultSize = 0L;
    }

    public Person[] getPersons() {
        return _persons;
    }

    public void setPersons(Person[] persons) {
        _persons = persons;
    }

    public Long getResultSize() {
        return _resultSize;
    }

    public void setResultSize(Long resultSize) {
        _resultSize = resultSize;
    }
}
