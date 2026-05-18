/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

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
@Table(name = "SSP_CANTONS")
@GenericGenerator(name = "cantonseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SSPSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SspCanton extends Canton {
    private Set<SspPlausiError> _plausierrors = new LinkedHashSet<SspPlausiError>();

    public SspCanton() {
        super();
    }

    public SspCanton(Canton dtoCanton, List<? extends PlausiError> plausiErrors) {
        setCantonId(dtoCanton.getCantonId());
        setCanton(dtoCanton.getCanton());
        setVersion(dtoCanton.getVersion());
        setDeliveryStatus(dtoCanton.getDeliveryStatus());
        setPlausiStatus(dtoCanton.getPlausiStatus());
        setPlausi_user(dtoCanton.getPlausi_user());
        setPlausi_date(dtoCanton.getPlausi_date());
        setCreation_user(dtoCanton.getCreation_user());
        setCreation_date(dtoCanton.getCreation_date());
        setModification_user(dtoCanton.getModification_user());
        setModification_date(dtoCanton.getModification_date());
        setValidation_user(dtoCanton.getValidation_user());
        setValidation_date(dtoCanton.getValidation_date());
        setFinalisation_user(dtoCanton.getFinalisation_user());
        setFinalisation_date(dtoCanton.getFinalisation_date());
        setUserText(dtoCanton.getUserText());

        if (plausiErrors != null) {
            for (PlausiError plausiError : plausiErrors) {
                _plausierrors.add(new SspPlausiError((SspPlausiError) plausiError));
            }
        }
    }

    @XmlTransient
    @OneToMany(mappedBy = "cantonId")
    @Where(clause = "deliveryId is null")
    @OrderBy("isConfirmed, plausi, errorId")
    public Set<SspPlausiError> getPlausierrors() {
        return _plausierrors;
    }

    public void setPlausierrors(Set<SspPlausiError> plausierrors) {
        _plausierrors = plausierrors;
    }
}
