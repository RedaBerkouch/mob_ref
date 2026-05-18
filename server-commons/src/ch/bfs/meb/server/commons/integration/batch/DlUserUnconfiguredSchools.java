/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: DlUserUnconfiguredSchools.java 531 2010-01-26 12:59:30Z msc $

 */
package ch.bfs.meb.server.commons.integration.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TODO Document this class
 * 
 */
public class DlUserUnconfiguredSchools {
    protected final HashMap<Long, List<String>> _unconfiguredSchools;
    protected final HashMap<Long, List<String>> _unconfiguredSchoolTypes;
    protected boolean _oneCantonPerDeliveryError;

    public DlUserUnconfiguredSchools() {
        _unconfiguredSchools = new HashMap<Long, List<String>>();
        _unconfiguredSchoolTypes = new HashMap<Long, List<String>>();
        _oneCantonPerDeliveryError = false;
    }

    public void addUnconfiguredSchool(Long deliveryId, String schoolId, String schoolType) {
        if (!_unconfiguredSchools.containsKey(deliveryId)) {
            _unconfiguredSchools.put(deliveryId, new ArrayList<String>());
            _unconfiguredSchoolTypes.put(deliveryId, new ArrayList<String>());
        }
        for (int i = 0; i < _unconfiguredSchools.get(deliveryId).size(); ++i) {
            if (_unconfiguredSchools.get(deliveryId).get(i).equals(schoolId) && _unconfiguredSchoolTypes.get(deliveryId).get(i).equals(schoolType)) {
                return;
            }
        }
        _unconfiguredSchools.get(deliveryId).add(schoolId);
        _unconfiguredSchoolTypes.get(deliveryId).add(schoolType);
    }

    public void clear(Long deliveryId) {
        _unconfiguredSchools.remove(deliveryId);
        _unconfiguredSchoolTypes.remove(deliveryId);
        _oneCantonPerDeliveryError = false;
    }

    public List<String> getUnconfiguredSchools(Long deliveryId) {
        List<String> unconfiguredSchools = _unconfiguredSchools.remove(deliveryId);
        return unconfiguredSchools == null ? new ArrayList<String>() : unconfiguredSchools;
    }

    public List<String> getUnconfiguredSchoolTypes(Long deliveryId) {
        List<String> unconfiguredSchoolCategories = _unconfiguredSchoolTypes.remove(deliveryId);
        return unconfiguredSchoolCategories == null ? new ArrayList<String>() : unconfiguredSchoolCategories;
    }

    public void setOneCantonPerDeliveryError(boolean oneCantonPerDeliveryError) {
        _oneCantonPerDeliveryError = oneCantonPerDeliveryError;
    }

    public boolean getOneCantonPerDeliveryError() {
        boolean oneCantonPerDeliveryError = _oneCantonPerDeliveryError;
        _oneCantonPerDeliveryError = false;
        return oneCantonPerDeliveryError;
    }
}