/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.server.commons.business;

/** 
 * Unique Identification of a person. Consists of idType and id.
 * Objects of this class are used as unique identifiers in maps.
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class PersId {
    private final String _idType;
    private final String _id;

    /**
     * Constructs a unique identifier for a person.
     * 
     * @param idType	Type of identifier
     * @param id		Id
     */
    public PersId(String idType, String id) {
        _idType = idType;
        _id = id;
    }

    /**
     * @return the idType
     */
    public String getIdType() {
        return _idType;
    }

    /**
     * @return the id
     */
    public String getId() {
        return _id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PersId) {
            return _idType.equals(((PersId) obj)._idType) && _id.equals(((PersId) obj)._id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (_idType == null ? 0 : _idType.hashCode()) + (_id == null ? 0 : _id.hashCode());
    }
}
