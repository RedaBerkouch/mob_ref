/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EventBO.java 629 2010-11-17 13:50:50Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausierrorBO;
import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument.Table;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Getter;
import lombok.Setter;

/**
 * Base business object for all type of events.
 *
 * @author $Author: dzw $
 * @version $Revision: 629 $
 */
@Getter
public abstract class EventBO extends BOBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventBO.class);

    protected String sbfiCode;
    protected String contractNr;
    protected String professionCode;
    protected String keyAspect;
    protected String educationYear;
    protected String contractType;
    protected String comment;

    protected PersonBO person;
    private List<PlausierrorBO> plausiErrors = new ArrayList<PlausierrorBO>();
    @Setter
    protected SbgEvent thisEvent;

    /**
     * Default constructor
     */
    public EventBO() {}

    /**
     * Initializes the common attributes for all events from a database object. Calls the construction
     * of all associated plausierror objects.
     *
     * @param persistentEvent database object
     * @param person          An event belongs to exactly one person
     */
    public EventBO(SbgEvent persistentEvent, PersonBO person) {
        this.person = person;
        this.thisEvent = persistentEvent;

        this.sbfiCode = persistentEvent.getSbfiCode() != null ? persistentEvent.getSbfiCode().toString() : null;
        this.contractNr = persistentEvent.getContractNr() != null ? persistentEvent.getContractNr().toString() : null;
        this.professionCode = persistentEvent.getProfessionCode() != null ? persistentEvent.getProfessionCode().toString() : null;
        this.keyAspect = persistentEvent.getKeyAspect() != null ? persistentEvent.getKeyAspect().toString() : null;
        this.educationYear = persistentEvent.getEducationYear() != null ? persistentEvent.getEducationYear().toString() : null;
        this.contractType = persistentEvent.getContractType() != null ? persistentEvent.getContractType().toString() : null;
        this.comment = persistentEvent.getUserComment();

        for (Plausierror plausierror : persistentEvent.getPlausierrors()) {
            this.plausiErrors.add(new PlausierrorBO(plausierror));
        }
    }

    /**
     * Verifies this event with all the plausis in the plausiList.
     *
     * @param plausiList list with all plausis
     */
    public void verifyEvent(List<PlausiBO> plausiList) {
        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Sets the plausistatus of the event
     * No reload of database objects.
     *
     * @param eventRepository Event repository
     * @param plausistatus
     */
    public void setPlausistatus(IEventRepository eventRepository, long plausistatus) {
        this.thisEvent.setPlausiStatus(plausistatus);
        eventRepository.updateEvent(this.thisEvent);
    }

    /**
     * Set default values for new event and insert it into database. Save all associated plausierrors.
     *
     * @param personId Foreign key to person for the new event.
     */
    public void saveEvent(IEventRepository eventRepository, PlausierrorDAO plausierrorDAO, Long personId, String username) {
        // save event, if not loaded from database
        if (this.thisEvent.getEventid() == null) {
            this.thisEvent.setPid(personId);
            this.thisEvent.setCanton(this.person.get_thisPerson().getCanton());
            this.thisEvent.setVersion(this.person.get_thisPerson().getVersion());
            this.thisEvent.setModUser(username);
            this.thisEvent.setModDate(new Date());
            this.thisEvent.setIsValidated(false);

            this.thisEvent.setPlausiStatus(calculatePlausistatus());

            eventRepository.insertEvent(this.thisEvent);
        }

        for (PlausierrorBO pe : this.plausiErrors) {
            pe.save(plausierrorDAO, username);
        }
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     */
    public void mergeSimplePlausierrors(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO, IEventRepository eventRepository, String username,
            List<PlausiBO> simplePlausis) {
        // delete old, save new plausierrors
        Iterator<PlausierrorBO> iter = this.plausiErrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = (PlausierrorBO) iter.next();
            Macro plausi = null;
            for (PlausiBO plausiBO : simplePlausis) {
                if (pe.get_thisPlausierror().getPlausiId().equals(plausiBO.get_thisPlausi().getMacroid())) {
                    plausi = plausiBO.get_thisPlausi();
                    break;
                }
            }
            if (plausi != null && plausi.getType().equals(CodegroupUtility.SBG_MACROTYPE_SIMPLEPLAUSI)) {
                if (pe.get_thisPlausierror().getErrorId() == null) {
                    pe.save(plausierrorDAO, username);
                } else {
                    plausierrorDAO.deletePlausiError(pe.get_thisPlausierror());
                    iter.remove();
                }
            }
        }
        Long newPlausistatus = calculatePlausistatus();
        if (!newPlausistatus.equals(this.thisEvent.getPlausiStatus())) {
            this.thisEvent.setPlausiStatus(newPlausistatus);
            this.thisEvent.setModUser(username);
            this.thisEvent.setModDate(new Date());
            eventRepository.updateEvent(this.thisEvent);
        }
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     */
    public void mergeSimplePlausierrors(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO) {
        // delete old, save new plausierrors
        Iterator<PlausierrorBO> iter = this.plausiErrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = iter.next();
            Macro plausi = macroDAO.findById(pe.get_thisPlausierror().getPlausiId());
            if (plausi.getType().equals(CodegroupUtility.SBG_MACROTYPE_SIMPLEPLAUSI)) {
                if (pe.get_thisPlausierror().getErrorId() == null) {
                    pe.save(plausierrorDAO, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                    this.thisEvent.getPlausierrors().add(pe.get_thisPlausierror());
                } else {
                    plausierrorDAO.deletePlausiError(pe.get_thisPlausierror());
                    this.thisEvent.getPlausierrors().remove(pe.get_thisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the event based on the plausierror business objects.
     * No reload of database objects.
     *
     * @return true, if plausistatus of event is valid or confirmed
     */
    public boolean setAllPlausistatus(IEventRepository eventRepository, PlausierrorDAO plausierrorDAO) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(this.thisEvent.getPlausiStatus())) {
            this.thisEvent.setPlausiStatus(newPlausistatus);
            eventRepository.updateEvent(this.thisEvent);
        }
        return allOk;
    }

    /**
     * Calculates the plausistatus of this event only based on the
     * plausierror business objects. No reload of database objects.
     *
     * @return plausistatus of this event
     */
    private Long calculatePlausistatus() {
        Long newPlausistatus;
        if (this.plausiErrors.isEmpty()) {
            newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_VALID;
        } else {
            newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED;
            for (PlausierrorBO error : this.plausiErrors) {
                if (!error.get_thisPlausierror().getIsConfirmed()) {
                    newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID;
                    break;
                }
            }
        }

        return newPlausistatus;
    }

    public abstract void addXml(Table.Pers persXml);

    @Override
    public void format() {
        Long contractNr = verifyLong(getContractNr());
        this.thisEvent.setContractNr(contractNr);

        Long profId = verifyLong(getProfessionCode());
        this.thisEvent.setProfessionCode(profId);

        Long keyAspect = verifyLong(getKeyAspect());
        this.thisEvent.setKeyAspect(keyAspect);

        Long educationYear = verifyLong(getEducationYear());
        this.thisEvent.setEducationYear(educationYear);

        Long sbfiCode = verifyLong(getSbfiCode());
        this.thisEvent.setSbfiCode(sbfiCode);

        Long contractType = verifyLong(getContractType());
        this.thisEvent.setContractType(contractType);

        this.thisEvent.setUserComment(getComment());
    }
}
