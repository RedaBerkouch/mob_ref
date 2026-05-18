package ch.bfs.meb.ssp.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Plausi;
import ch.bfs.meb.server.commons.service.impl.IPlausiServiceProvider;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiRepository;
import ch.bfs.meb.util.CodegroupUtility;

public class PlausiServiceProvider implements IPlausiServiceProvider {
    private IPlausiRepository _plausiRepository;

    public void setPlausiRepository(IPlausiRepository plausiRepository) {
        _plausiRepository = plausiRepository;
    }

    private IPlausiErrorRepository _plausiErrorRepository;

    public void setPlausiErrorRepository(IPlausiErrorRepository plausiErrorRepository) {
        _plausiErrorRepository = plausiErrorRepository;
    }

    @Override
    public Plausi getPlausiById(Long plausiId) {
        return _plausiRepository.getPlausiById(plausiId);
    }

    @Override
    public List<Plausi> getPlausis() {
        return Collections.unmodifiableList(new ArrayList<Plausi>(_plausiRepository.getPlausis()));
    }

    @Override
    public Plausi insertPlausi(Plausi plausi) {
        return _plausiRepository.insertPlausi(new SspPlausi(plausi));
    }

    @Override
    public Plausi updatePlausi(Plausi plausi) {
        SspPlausi updatedPlausi = new SspPlausi(plausi);
        updatedPlausi.setSspParameters(_plausiRepository.getParameters(plausi.getPlausiId()));
        return _plausiRepository.updatePlausi(updatedPlausi);
    }

    @Override
    public boolean deletePlausi(Plausi plausi) {
        Long nofErrors = _plausiErrorRepository.getNofPlausiErrorsByPlausiId(plausi.getPlausiId());
        if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL) && nofErrors.equals(0L)) {
            _plausiRepository.deletePlausi(new SspPlausi(plausi));
            return true;
        }
        return false;
    }
}
