/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: DeliveryBO.java 644 2010-12-06 15:19:20Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import java.text.MessageFormat;
import java.util.*;

import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.admin.bfs.sbg.business.plausi.ExternalPlausiProcess;
import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausierrorBO;
import ch.admin.bfs.sbg.db.dao.DeliveryDAO;
import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PersonDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.psist.PersistAction;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.psist.PersistPerson;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Person;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.admin.bfs.sbg.transfer.SbgDelivery;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument.Table;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

/**
 * Business object for a delivery.
 *
 * @author $Author: lsc $
 * @version $Revision: 644 $
 */
public class DeliveryBO extends BOBase {
    private static final String VALIDATION_REPORT = "validation.Report";
    private static final String VALIDATION_UNDO_REPORT = "validation.undo.Report";

    /*
     * PERSONS = 0;
     * PERSON_ID = 1;
     * CONTRACTS = 1;
     * CONTRACT_ID = 1;
     * EDUCATIONS = 2;
     * EDUCATION_ID = 1;
     * EXAMS = 3;
     * EXAM_ID = 1;	
     * CANCELLATIONS = 4;
     * CANCELLATION_ID = 1;
     */

    private final Long _canton;
    private final Long _year;
    private final String _lidat;

    private List<PersonBO> _persons;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();
    private PersistDelivery _thisDelivery = null;
    private boolean _firstDelivery;

    /**
     * Constructs a delivery business object from XML part. Calls the construction of all
     * contained persons.
     *
     * @param canton           Delivery canton
     * @param year             Delivery year. Canton and year are the identifier of a delivery.
     * @param xmlPart          XML bean part for delivery
     * @param existingDelivery Already existing database object (in case of amend/replace)
     */
    public DeliveryBO(Long canton, Long year, TableDocument.Table xmlPart, PersistDelivery existingDelivery) {
        _canton = canton;
        _year = year;

        _lidat = xmlPart.getHead().getLidat();

        if (existingDelivery != null) {
            _thisDelivery = existingDelivery;
        }

        _persons = new ArrayList<PersonBO>();
        for (TableDocument.Table.Pers xmlPerson : xmlPart.getPersArray()) {
            _persons.add(new PersonBO(xmlPerson, this));
        }
    }

    /**
     * Constructs a delivery business object from XML part.
     *
     * @param xmlPart XML bean part for delivery
     * @return the constructed business object
     */
    public DeliveryBO(TableDocument.Table.Head xmlPart) {
        _year = new Long(xmlPart.getVid());
        _canton = new Long(xmlPart.getKtid());
        _lidat = xmlPart.getLidat();
    }

    public DeliveryBO(FieldSet fieldSet) {
        _year = verifyLong(fieldSet.readString("YEAR"));
        _canton = verifyLong(fieldSet.readString("CANTON"));
        _lidat = StringUtils.nullForEmpty(fieldSet.readString("LIDAT"));
    }

    /*
      Constructs a delivery business object from CSV part. Calls the construction of all
      contained persons.

      @param canton            Delivery canton
     * @param year                Delivery year. Canton and year are the identifier of a delivery.
     * @param lidat                Delivery date.
     * @param deliveryData        Hashmap with the data of a delivery
     * @param existingDelivery    Already existing database object (in case of amend/replace)
     */
    //	public DeliveryBO (Long canton, Long year, String lidat, HashMap<Long, PersonData> deliveryData, PersistDelivery existingDelivery)
    //	{
    //		_canton = canton;
    //		_year = year;
    //
    //		_lidat = lidat;
    //
    //		if (existingDelivery != null)
    //		{
    //			_thisDelivery = existingDelivery;
    //		}
    //
    //		_persons = new ArrayList<PersonBO> ();
    //		
    //		Collection<PersonData> persons = deliveryData.values();
    //		PersonData personData = null;
    //		
    //		Iterator<PersonData> personsIterator = persons.iterator();
    //		while (personsIterator.hasNext())
    //		{
    //			personData = personsIterator.next();
    //			
    //			List<String[]> persContracts = personData.getPersContracts();
    //			List<String[]> persEducations = personData.getPersEducations();
    //			List<String[]> persExams = personData.getPersExams();
    //			List<String[]> persCancellations = personData.getPersCancellations();
    //			
    //			List[] persData = {persContracts, persEducations, persExams, persCancellations};
    //			
    //			if (personData.getPersons() == null || personData.getPersons().size() == 0) {
    //				// No person type entry
    //				_persons.add(new PersonBO (personData.getPersId(), persData, this));
    //			}
    //			for (int i = 0; i < personData.getPersons().size(); i++) {
    //			    _persons.add(new PersonBO (personData.getPersons().get(i), persData, this));
    //			}
    //		}
    //	}

    /**
     * Constructs a delivery business object from a database object. Calls the construction of all
     * contained persons, if loadAll is true.
     *
     * @param persistDelivery database object
     * @param loadAll         if true, all contained persons, events and plausierrors are loaded
     */
    public DeliveryBO(PersonDAO personDAO, PersistDelivery persistDelivery, boolean loadAll) {
        _thisDelivery = persistDelivery;

        _canton = persistDelivery.getCanton();
        _year = persistDelivery.getVersion();

        _lidat = persistDelivery.getDeliverydate() != null ? dateToString(persistDelivery.getDeliverydate()) : null;

        _persons = new ArrayList<PersonBO>();
        if (loadAll) {
            Set<PersistPerson> existingPersons = personDAO.loadWholeDelivery(_thisDelivery.getDeliveryid());
            for (PersistPerson pers : existingPersons) {
                _persons.add(new PersonBO(pers, this, true));
            }
        }

        for (Plausierror plausierror : persistDelivery.getPlausiErrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    public void initialize(DeliveryDAO deliveryDAO, Long canton, Long year) {
        PersistDelivery newDelivery = new PersistDelivery(canton, year, null, null, null, null, null);
        List<PersistDelivery> existingDelivery = deliveryDAO.findByExample(newDelivery);
        if (!existingDelivery.isEmpty()) {
            // Only one delivery possible/allowed!!
            _thisDelivery = (PersistDelivery) existingDelivery.get(0);
        }

        if (_thisDelivery == null) {
            // create new delivery
            _thisDelivery = new PersistDelivery(canton, year, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail(),
                    null, CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED, CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED, SbgDelivery.DELIVERY_LOCKED);
            _thisDelivery.setIslocked(SbgDelivery.DELIVERY_NOT_LOCKED);
            _firstDelivery = true;
        } else if (_thisDelivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_EMPTY)) {
            _firstDelivery = true;
        } else {
            _firstDelivery = false;
        }
        //TODO: confirmation rules
        //		_thisDelivery.setConfirmRules (_confirmRules);
    }

    public boolean isFirstDelivery() {
        return _firstDelivery;
    }

    /**
     * Basic formatting of all objects of the delivery
     */
    public void formatDelivery() {
        format();

        Iterator<PersonBO> personIterator = _persons.iterator();
        while (personIterator.hasNext()) {
            personIterator.next().formatPersonAndEvents();
        }
    }

    /**
     * Check if at least one person of the imported XML exists already in the database
     * for this delivery.
     *
     * @return true if at least one imported person is already in the database
     */
    public boolean personsExist(PersonDAO personDAO) {
        if ((_thisDelivery == null) || (_thisDelivery.getDeliveryid() == null)) {
            return false;
        }

        List<PersistPerson> existingPersons = personDAO.findByProperty("deliveryId", _thisDelivery.getDeliveryid());
        return !existingPersons.isEmpty();
    }

    /**
     * Loads existing persons (including events and plausierrors) from the database into an
     * imported delivery. All existing plausierrors are marked as "isToDelete". All existing
     * persons that are in the new import are also marked as "isToDelete".
     * <p/>
     * At the end all the objects (persons, events, plausierrors) for this delivery are loaded
     * in the business object tree.
     */
    public void loadExistingPersons(PersonDAO personDAO, PlausierrorDAO plausierrorDAO) {
        if ((_thisDelivery == null) || (_thisDelivery.getDeliveryid() == null)) {
            return;
        }

        HashMap<PersId, Person> importedPersonIds = new HashMap<PersId, Person>();
        for (PersonBO personBO : _persons) {
            Person pers = personBO.get_thisPerson();
            if (pers.getIdType() != null && pers.getId() != null) {
                importedPersonIds.put(new PersId(pers.getIdType(), pers.getId()), pers);
            }
        }

        Set<PersistPerson> existingPersons = personDAO.loadWholeDelivery(_thisDelivery.getDeliveryid());
        for (PersistPerson pers : existingPersons) {
            // set existing plausierrors to delete
            for (Plausierror plausierror : pers.getPlausiErrors()) {
                plausierror.setIsToDelete(true);
                plausierrorDAO.merge(plausierror);
            }

            if (importedPersonIds.containsKey(new PersId(pers.getIdType(), pers.getId()))) {
                pers.setIsToDelete(true);
                personDAO.merge(pers);
            } else {
                _persons.add(new PersonBO(pers, this, true));
            }
        }
    }

    /**
     * Verifies all persons and events in the business object tree with all the plausis
     * in the plausiList.
     *
     * @param plausiList A list with plausis
     */
    public void verifyWholeDelivery(List<PlausiBO> plausiList) {
        for (PersonBO person : _persons) {
            person.verifyPersonAndEvents(plausiList);
        }

        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    public void verifyDelivery(List<PlausiBO> plausiList) {
        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Verifies the delivery with the internal and external plausis.
     *
     * @param internalPlausis       A list with internal plausis
     * @param externalPlausiProcess A Process for external verification (SAS)
     */
    public void verifyDelivery(List<PlausiBO> internalPlausis, ExternalPlausiProcess externalPlausiProcess) {
        for (PlausiBO plausi : internalPlausis) {
            plausi.verify(this);
        }

        externalPlausiProcess.verify(this);
    }

    /**
     * Save the data from the business object tree into the database.
     */
    public void saveImportedDelivery(DeliveryDAO deliveryDAO, PersonDAO personDAO, IEventRepository eventRepository, PlausierrorDAO plausierrorDAO) {
        saveImportedDelivery(deliveryDAO, personDAO, eventRepository, plausierrorDAO,
                ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
    }

    public void saveImportedDelivery(DeliveryDAO deliveryDAO, PersonDAO personDAO, IEventRepository eventRepository, PlausierrorDAO plausierrorDAO,
            String userEmail) {
        _thisDelivery.setCanton(_canton);
        _thisDelivery.setVersion(_year);
        _thisDelivery.setDeliveryuser(userEmail);
        _thisDelivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_IMPORTED);
        _thisDelivery.setIslocked(SbgDelivery.DELIVERY_LOCKED);

        _thisDelivery.setPlausistatus(calculatePlausistatus());

        // check if database entry already exists
        if (_thisDelivery.getDeliveryid() == null) {
            deliveryDAO.save(_thisDelivery);
        } else {
            deliveryDAO.merge(_thisDelivery);
        }

        for (PersonBO person : _persons) {
            person.savePersonAndEvents(_thisDelivery, personDAO, eventRepository, plausierrorDAO, userEmail);
        }

        for (PlausierrorBO pe : _plausierrors) {
            pe.save(plausierrorDAO, userEmail);
        }
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     */
    public void mergeSimplePlausierrors(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO) {
        // delete old, save new plausierrors
        Iterator<PlausierrorBO> iter = _plausierrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = iter.next();
            Macro plausi = (Macro) macroDAO.load(Macro.class, pe.get_thisPlausierror().getPlausiId());
            if (plausi.getType().equals(CodegroupUtility.SBG_MACROTYPE_SIMPLEPLAUSI)) {
                if (pe.get_thisPlausierror().getErrorId() == null) {
                    pe.save(plausierrorDAO, ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                } else {
                    plausierrorDAO.deletePlausiError(pe.get_thisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Save all associated plausierrors.
     */
    public void savePlausierrors(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO, DeliveryDAO deliveryDAO, String username) {
        Iterator<PlausierrorBO> iter = _plausierrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = iter.next();
            Macro plausi = (Macro) macroDAO.load(Macro.class, pe.get_thisPlausierror().getPlausiId());
            if (plausi.getType().equals(CodegroupUtility.SBG_MACROTYPE_SIMPLEPLAUSI)) {
                if (pe.get_thisPlausierror().getErrorId() == null) {
                    pe.save(plausierrorDAO, username);
                    _thisDelivery.getPlausiErrors().add(pe.get_thisPlausierror());
                } else {
                    // delivery errors should have been set to delete before recalculating
                }
            }
        }
        Long newPlausistatus = calculatePlausistatus();
        if (!newPlausistatus.equals(_thisDelivery.getPlausistatus())) {
            _thisDelivery.setPlausistatus(newPlausistatus);
            //			_thisDelivery.setModification_user (username);
            //			_thisDelivery.setModification_date (new Date ());
            deliveryDAO.merge(_thisDelivery);
        }
    }

    /**
     * Sets the plausistatus of all persons and events in the object tree. Plausistatus is
     * SBG_PLAUSISTATUS_VALID, if no plausierrors exist for an object, SBG_PLAUSISTATUS_NOTVALID
     * otherwise.
     *
     * @return true, if plausistatus of all objets is SBG_PLAUSISTATUS_VALID
     */
    public boolean setAllPlausistatus(DeliveryDAO deliveryDAO, PersonDAO personDAO, IEventRepository eventRepository, PlausierrorDAO plausierrorDAO) {
        boolean allOk = setPlausistatus(deliveryDAO);

        for (PersonBO person : _persons) {
            if (!person.setAllPlausistatus(personDAO, eventRepository, plausierrorDAO)) {
                allOk = false;
            }
        }
        return allOk;
    }

    /**
     * Calculates the plausistatus of this delivery only based on the
     * plausierror business objects. No reload of database objects.
     *
     * @return true, if plausistatus of this delivery is valid or confirmed
     */
    public boolean setPlausistatus(DeliveryDAO deliveryDAO) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisDelivery.getPlausistatus())) {
            _thisDelivery.setPlausistatus(newPlausistatus);
            deliveryDAO.merge(_thisDelivery);
        }

        return allOk;
    }

    /**
     * Calculates the plausistatus of this delivery only based on the
     * plausierror business objects. No reload of database objects.
     *
     * @return true, if plausistatus of this delivery is valid or confirmed
     */
    private Long calculatePlausistatus() {
        Long newPlausistatus;
        if (_plausierrors.isEmpty()) {
            newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_VALID;
        } else {
            newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED;
            for (PlausierrorBO error : _plausierrors) {
                if (!error.get_thisPlausierror().getIsConfirmed()) {
                    newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID;
                    break;
                }
            }
        }

        return newPlausistatus;
    }

    /**
     * Generates the validation report strings and saves them in the action
     *
     * @param action     Validation action object
     * @param nrValid    Number of valid persons in the delivery
     * @param nrNotValid Number of invalid persons in the delivery
     * @param undo       true if undo of validation
     */
    public static void generateValidationReport(PersistAction action, Long nrValid, Long nrNotValid, boolean undo) {
        String report_de;
        String report_fr;
        if (undo == true) {
            report_de = resource_de.getString(VALIDATION_UNDO_REPORT);
            report_fr = resource_fr.getString(VALIDATION_UNDO_REPORT);
        } else {
            report_de = resource_de.getString(VALIDATION_REPORT);
            report_fr = resource_fr.getString(VALIDATION_REPORT);
        }
        Object[] param = new Object[] { dateToString(new Date()), ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail(),
                nrValid, nrNotValid };
        report_de = MessageFormat.format(report_de, param);
        report_fr = MessageFormat.format(report_fr, param);
        action.setValidationreport_de(report_de);
        action.setValidationreport_fr(report_fr);
    }

    public void asXml(TableDocument xmlRoot) {
        Table table = xmlRoot.addNewTable();
        Table.Head head = table.addNewHead();
        head.setVid(_thisDelivery.getVersion().intValue());
        head.setKtid(_thisDelivery.getCanton().intValue());
        if (_thisDelivery.getDeliverydate() != null) {
            head.setLidat(dateToString(_thisDelivery.getDeliverydate()));
        }

        for (PersonBO person : _persons) {
            Table.Pers persXml = table.addNewPers();
            person.addXml(persXml);
        }
    }

    @Override
    public void format() {
        if (_thisDelivery == null) {
            _thisDelivery = new PersistDelivery();
        }

        Date liDat = verifyDate(get_lidat());
        _thisDelivery.setDeliverydate(liDat);
    }

    public int getNrPersons() {
        return _persons.size();
    }

    public int getNrEvents() {
        int result = 0;
        for (PersonBO person : _persons) {
            result += person.get_events().size();
        }
        return result;
    }

    /**
     * @return Returns the _thisDelivery.
     */
    public PersistDelivery get_thisDelivery() {
        return _thisDelivery;
    }

    /**
     * @param delivery The _thisDelivery to set.
     */
    public void set_thisDelivery(PersistDelivery delivery) {
        _thisDelivery = delivery;
    }

    /**
     * @return Returns the _canton.
     */
    public Long get_canton() {
        return _canton;
    }

    /**
     * @return Returns the _year.
     */
    public Long get_year() {
        return _year;
    }

    /**
     * @return Returns the _lidat.
     */
    public String get_lidat() {
        return _lidat;
    }

    /**
     * @return Returns the _persons.
     */
    public List<PersonBO> get_persons() {
        return _persons;
    }

    /**
     * @return Returns the _plausierrors.
     */
    public List<PlausierrorBO> get_plausierrors() {
        return _plausierrors;
    }
}
