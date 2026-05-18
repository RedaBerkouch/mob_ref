/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Where;

import ch.bfs.meb.server.commons.integration.dto.Canton;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;

/**
 * Persistence Object for the canton data table
 */
@Entity
@Table(name = "SDL_CANTONS")
@GenericGenerator(name = "cantonseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SDLSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlCanton extends Canton {
    private Set<SdlPlausiError> _plausierrors = new LinkedHashSet<SdlPlausiError>();

    public SdlCanton() {
        super();
    }

    public SdlCanton(Canton canton, List<? extends PlausiError> plausiErrors) {
        setCantonId(canton.getCantonId());
        setCanton(canton.getCanton());
        setVersion(canton.getVersion());
        setDeliveryStatus(canton.getDeliveryStatus());
        setPlausiStatus(canton.getPlausiStatus());
        setPlausi_user(canton.getPlausi_user());
        setPlausi_date(canton.getPlausi_date());
        setCreation_user(canton.getCreation_user());
        setCreation_date(canton.getCreation_date());
        setModification_user(canton.getModification_user());
        setModification_date(canton.getModification_date());
        setValidation_user(canton.getValidation_user());
        setValidation_date(canton.getValidation_date());
        setFinalisation_user(canton.getFinalisation_user());
        setFinalisation_date(canton.getFinalisation_date());
        setUserText(canton.getUserText());

        if (plausiErrors != null) {
            for (PlausiError plausiError : plausiErrors) {
                _plausierrors.add(new SdlPlausiError((SdlPlausiError) plausiError));
            }
        }
    }

    @XmlTransient
    @OneToMany(mappedBy = "cantonId")
    @Where(clause = "deliveryId is null")
    @OrderBy("isConfirmed, plausi, errorId")
    public Set<SdlPlausiError> getPlausierrors() {
        return _plausierrors;
    }

    public void setPlausierrors(Set<SdlPlausiError> plausierrors) {
        _plausierrors = plausierrors;
    }
}
