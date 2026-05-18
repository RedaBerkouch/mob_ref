/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

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
@Table(name = "SBA_CANTONS")
@GenericGenerator(name = "cantonseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SBASEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SbaCanton extends Canton {
    private Set<SbaPlausiError> _plausierrors = new LinkedHashSet<SbaPlausiError>();

    public SbaCanton() {
        super();
    }

    public SbaCanton(Canton dtoCanton, List<? extends PlausiError> plausiErrors) {
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
                _plausierrors.add(new SbaPlausiError((SbaPlausiError) plausiError));
            }
        }
    }

    @XmlTransient
    @OneToMany(mappedBy = "cantonId")
    @Where(clause = "deliveryId is null")
    @OrderBy("isConfirmed, plausi, errorId")
    public Set<SbaPlausiError> getPlausierrors() {
        return _plausierrors;
    }

    public void setPlausierrors(Set<SbaPlausiError> plausierrors) {
        _plausierrors = plausierrors;
    }
}