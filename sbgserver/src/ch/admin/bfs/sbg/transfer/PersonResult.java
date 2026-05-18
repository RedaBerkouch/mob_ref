/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PersonResult.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

/** 
 * TODO Describe this class
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 36 $ 
 */
public class PersonResult extends ResultBase implements Serializable {

    private static final long serialVersionUID = -6513696769789555648L;

    Person _person;

    public PersonResult() {}

    public PersonResult(Person person) {

        _person = person;
        setState(OK);
    }

    public PersonResult(String message) {

        setPerson(new Person());
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the person.
     */
    public Person getPerson() {
        return _person;
    }

    /**
     * @param person The person to set.
     */
    public void setPerson(Person person) {
        this._person = person;
    }
}
