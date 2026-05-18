/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.server.commons.integration.dto.PlausiListResult;
import ch.bfs.meb.server.commons.integration.dto.PlausiResult;

@Service
public class PlausiServiceImpl implements IPlausiService {
    protected static final String PLAUSI_DELETE_FAILURE_MESSAGE = "plausi.delete.failure.message";
    protected static final String PLAUSI_TYPE_EMPTY_MESSAGE = "plausi.typeempty.message";
    protected static final String PLAUSI_OBJECT_EMPTY_MESSAGE = "plausi.objectempty.message";
    protected static final String PLAUSI_SOURCE_EMPTY_MESSAGE = "plausi.sourceempty.message";

    IPlausiServiceProvider _plausiServiceProvider;

    public void setPlausiServiceProvider(IPlausiServiceProvider plausiServiceProvider) {
        _plausiServiceProvider = plausiServiceProvider;
    }

    @Transactional(readOnly = true)
    public PlausiListResult getPlausis() {
        return new PlausiListResult(_plausiServiceProvider.getPlausis());
    }

    @Transactional(readOnly = true)
    public PlausiResult getPlausiById(Long plausiId) {
        Plausi plausi = _plausiServiceProvider.getPlausiById(plausiId);
        if (plausi == null) {
            return new PlausiResult("Could not find plausi with id: " + plausiId);
        } else {
            return new PlausiResult(plausi);
        }
    }

    protected String checkPlausi(Plausi plausi) {
        if (plausi.getType() == null) {
            return PLAUSI_TYPE_EMPTY_MESSAGE;
        }
        if (plausi.getObjectLevel() == null) {
            return PLAUSI_OBJECT_EMPTY_MESSAGE;
        }
        if (plausi.getSource() == null || plausi.getSource().trim().equals("")) {
            return PLAUSI_SOURCE_EMPTY_MESSAGE;
        }
        return null;
    }

    @Transactional
    public PlausiResult insertPlausi(Plausi plausi) {
        String message = checkPlausi(plausi);
        if (message != null) {
            return new PlausiResult(message);
        }
        return new PlausiResult(_plausiServiceProvider.insertPlausi(plausi));
    }

    @Transactional
    public PlausiResult updatePlausi(Plausi plausi) {
        String message = checkPlausi(plausi);
        if (message != null) {
            return new PlausiResult(message);
        }
        return new PlausiResult(_plausiServiceProvider.updatePlausi(plausi));
    }

    @Transactional
    public PlausiResult deletePlausi(Plausi plausi) {
        if (!_plausiServiceProvider.deletePlausi(plausi)) {
            return new PlausiResult(PLAUSI_DELETE_FAILURE_MESSAGE);
        }
        return new PlausiResult();
    }
}
