/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PersId.java 213 2007-07-05 14:08:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

/** 
 * Unique Identification of a person. Consists of idType and idNr.
 * Objects of this class are used as unique identifiers in maps.
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 213 $ 
 */
public class PersId {
    private final Long _idType;
    private final Long _idNr;

    /**
     * Constructs a unique identifier for a person.
     * 
     * @param idType	Type of identifier
     * @param idNr		Id number
     */
    public PersId(Long idType, Long idNr) {
        _idType = idType;
        _idNr = idNr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PersId) {
            return _idType.equals(((PersId) obj)._idType) && _idNr.equals(((PersId) obj)._idNr);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (_idType == null ? 0 : _idType.hashCode()) + (_idNr == null ? 0 : _idNr.hashCode());
    }
}
