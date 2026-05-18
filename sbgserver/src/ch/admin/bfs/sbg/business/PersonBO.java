/* ----------------------------------------------------------------------------
 *
 * SBG-Projekt
 *
 * Copyright (c) 2006 GLANCE AG, Switzerland
 *
 * $Id: PersonBO.java 644 2010-12-06 15:19:20Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.admin.bfs.sbg.business.plausi.PlausiBO;
import ch.admin.bfs.sbg.business.plausi.PlausierrorBO;
import ch.admin.bfs.sbg.db.dao.MacroDAO;
import ch.admin.bfs.sbg.db.dao.PersonDAO;
import ch.admin.bfs.sbg.db.dao.PlausierrorDAO;
import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.psist.PersistPerson;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.bfs.meb.sbg.server.business.delivery.csv.ItemBO;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.sbg.server.integration.repository.IEventRepository;
import ch.bfs.meb.sbg.server.service.xmlbeans.*;
import ch.bfs.meb.sbg.server.service.xmlbeans.TableDocument.Table;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Business object for a person.
 *
 * @author $Author: lsc $
 * @version $Revision: 644 $
 */
public class PersonBO extends BOBase {
    public static final long ID_TYPE_UNKNOWN = 0;
    public static final long ID_TYPE_AHV = 1;

    private final String XML_FRAGMENT_TAG = "xml-fragment";
    private final String PERSON_TAG = "pers";

    //	private static final int CONTRACTS_OF_PERSON = 0;
    //	private static final int TYPE = 0;
    //	private static final int EDUCATIONS_OF_PERSON = 1;
    //	private static final int EXAMS_OF_PERSON = 2;
    //	private static final int CANCELLATIONS_OF_PERSON = 3;
    //	private static final int PERSON_ID = 1;
    //	private static final int ID_TYPE = 2;
    //	private static final int SEX = 3;
    //	private static final int DATE_OF_BIRTH = 4;
    //	private static final int COMMENT = 5;

    private final String _idNr;
    private String _idType;
    private String _sex;
    private String _dateOfBirth;
    private String _nDateOfBirth;
    private String _comment;
    private String _deliveryText;

    private final Long _deliveryId;
    private final Long _canton;
    private final Long _year;
    //	private DeliveryBO _delivery;
    private final List<EventBO> _events;
    private final List<PlausierrorBO> _plausiErrors = new ArrayList<PlausierrorBO>();
    private PersistPerson _thisPerson;

    public PersonBO(String idNr, Long deliveryId, long year, long canton) {
        _events = new ArrayList<EventBO>();
        _idNr = idNr;
        _deliveryId = deliveryId;
        _year = year;
        _canton = canton;
    }

    /**
     * Constructs a person business object from XML part. Calls the construction of all
     * contained events.
     *
     * @param xmlPart  XML bean part for delivery
     * @param delivery A person belongs to exactly one delivery
     */
    public PersonBO(TableDocument.Table.Pers xmlPart, DeliveryBO delivery) {
        _deliveryId = delivery.get_thisDelivery().getDeliveryid();
        _canton = delivery.get_canton();
        _year = delivery.get_year();

        _deliveryText = xmlPart.xmlText();
        _deliveryText = _deliveryText.replace(XML_FRAGMENT_TAG, PERSON_TAG);

        _idNr = xmlPart.getId().getIdnr();
        _idType = xmlPart.getId().getIdtyp();
        _sex = xmlPart.getSex();
        _dateOfBirth = xmlPart.getDateofbirth();
        _nDateOfBirth = xmlPart.getNdateofbirth();
        _comment = xmlPart.getCom();

        _events = new ArrayList<EventBO>();

        for (Contract c : xmlPart.getVertArray()) {
            ContractBO contract = new ContractBO(c, this);
            _events.add(contract);
        }

        for (OngoingEducation e : xmlPart.getLaufArray()) {
            OngoingEducationBO education = new OngoingEducationBO(e, this);
            _events.add(education);
        }

        for (Exam e : xmlPart.getExamArray()) {
            ExamBO exam = new ExamBO(e, this);
            _events.add(exam);
        }

        for (Cancellation c : xmlPart.getAbArray()) {
            CancellationBO cancellation = new CancellationBO(c, this);
            _events.add(cancellation);
        }
    }

    /**
     * Constructs a school business object from XML part. Calls the construction of all
     * contained classes.
     *
     * @param xmlPart XML bean part for delivery
     * @return the constructed business object
     */
    public PersonBO(TableDocument.Table.Pers xmlPart, Long deliveryId, Long canton, Long year) {
        _deliveryId = deliveryId;
        _canton = canton;
        _year = year;

        _deliveryText = xmlPart.xmlText();
        _deliveryText = _deliveryText.replace(XML_FRAGMENT_TAG, PERSON_TAG);

        _idNr = xmlPart.getId().getIdnr();
        _idType = xmlPart.getId().getIdtyp();
        _sex = xmlPart.getSex();
        _dateOfBirth = xmlPart.getDateofbirth();
        _nDateOfBirth = xmlPart.getNdateofbirth();
        _comment = xmlPart.getCom();

        _events = new ArrayList<EventBO>();

        for (Contract c : xmlPart.getVertArray()) {
            ContractBO contract = new ContractBO(c, this);
            _events.add(contract);
        }

        for (OngoingEducation e : xmlPart.getLaufArray()) {
            OngoingEducationBO education = new OngoingEducationBO(e, this);
            _events.add(education);
        }

        for (Exam e : xmlPart.getExamArray()) {
            ExamBO exam = new ExamBO(e, this);
            _events.add(exam);
        }

        for (Cancellation c : xmlPart.getAbArray()) {
            CancellationBO cancellation = new CancellationBO(c, this);
            _events.add(cancellation);
        }
    }

    /**
     * Constructs a person business object from CSV part for a pers Id, where no person type entry is avvailable.
     * Calls the construction of all contained events.
     *
     * @param personId Person Id
     * @param persData Array with the data of all contained events
     * @param delivery A person belongs to exactly one delivery
     */
    //	public PersonBO (Long persId, List[] persData, DeliveryBO delivery)
    //	{
    //		_deliveryId = delivery.get_thisDelivery().getDeliveryid();
    //		_canton = delivery.get_canton();
    //		_year = delivery.get_year();
    //
    //		String[] personArray = new String[] {"","","","",""};
    //		personArray[PERSON_ID] = persId.toString();
    //		construct(personArray, persData);
    //	}
    public void setCsvData(ItemBO item) {
        _idType = item.getPersonIdType();
        _sex = item.getPersonSex();
        _dateOfBirth = item.getPersonYearOfBirth();
        _nDateOfBirth = item.getPersonYearOfBirth();
        _comment = item.getPersonComment();
    }

    public void addEvent(EventBO event) {
        _events.add(event);
    }

    //	private void construct(String[] personArray, List[] persData)
    //	{
    //		String persDataText = "";
    //
    //		// only build person part of orig text if person type is set
    //		if (!personArray[TYPE].isEmpty()) {
    //			for (int i = 0; i < personArray.length; i++) {
    //				persDataText = persDataText + personArray[i] + ";";
    //			}
    //		}
    //
    //		List persContracts = persData[CONTRACTS_OF_PERSON];
    //		for (int i = 0; i < persContracts.size(); i++) {
    //			String[] data = (String[])persContracts.get(i);
    //			for (int j = 0; j < data.length; j++) {
    //				persDataText = persDataText + data[j] + ";";
    //			}
    //			persDataText = persDataText + "\n";
    //		}
    //
    //		List persEducations = persData[EDUCATIONS_OF_PERSON];
    //		for (int i = 0; i < persEducations.size(); i++) {
    //			String[] data = (String[])persEducations.get(i);
    //			for (int j = 0; j < data.length; j++) {
    //				persDataText = persDataText + data[j] + ";";
    //			}
    //			persDataText = persDataText + "\n";
    //		}
    //
    //		List persExams = persData[EXAMS_OF_PERSON];
    //		for (int i = 0; i < persExams.size(); i++) {
    //			String[] data = (String[])persExams.get(i);
    //			for (int j = 0; j < data.length; j++) {
    //				persDataText = persDataText + data[j] + ";";
    //			}
    //			persDataText = persDataText + "\n";
    //		}
    //
    //		List persCancellations = persData[CANCELLATIONS_OF_PERSON];
    //		for (int i = 0; i < persCancellations.size(); i++) {
    //			String[] data = (String[])persCancellations.get(i);
    //			for (int j = 0; j < data.length; j++) {
    //				persDataText = persDataText + data[j] + ";";
    //			}
    //			persDataText = persDataText + "\n";
    //		}
    //
    //
    //		_deliveryText = persDataText;
    //
    //		_idNr = personArray[PERSON_ID].equals("") ? null : personArray[PERSON_ID];
    //		_idType = personArray[ID_TYPE].equals("") ? null : personArray[ID_TYPE];
    //		_sex = personArray[SEX].equals("") ? null : personArray[SEX];
    //		_dateOfBirth = personArray[DATE_OF_BIRTH].equals("") ? null : personArray[DATE_OF_BIRTH];
    //
    //		if(personArray.length == 6){
    //			_comment = personArray[COMMENT];
    //		}
    //
    //		_events = new ArrayList<EventBO> ();
    //
    //		//contracts
    //		List contracts = persData[CONTRACTS_OF_PERSON];
    //		if(contracts != null){
    //			for (int i = 0; i < contracts.size(); i++) {
    //				ContractBO contract = new ContractBO ((String[])contracts.get(i), this);
    //				_events.add(contract);
    //			}
    //		}
    //
    //		//ongoingEducations
    //		List ongoingEducations = persData[EDUCATIONS_OF_PERSON];
    //		if(ongoingEducations != null){
    //			for (int i = 0; i < ongoingEducations.size(); i++) {
    //				OngoingEducationBO education = new OngoingEducationBO ((String[])ongoingEducations.get(i), this);
    //				_events.add(education);
    //			}
    //		}
    //
    //		//exams
    //		List exams = persData[EXAMS_OF_PERSON];
    //		if(exams != null){
    //			for (int i = 0; i < exams.size(); i++) {
    //				ExamBO exam = new ExamBO ((String[])exams.get(i), this);
    //				_events.add(exam);
    //			}
    //		}
    //
    //		//cancellations
    //		List cancellations = persData[CANCELLATIONS_OF_PERSON];
    //		if(cancellations != null){
    //			for (int i = 0; i < cancellations.size(); i++) {
    //				CancellationBO cancellation = new CancellationBO ((String[])cancellations.get(i), this);
    //				_events.add(cancellation);
    //			}
    //		}
    //	}

    /**
     * Constructs a person and the associated plausierror business objects from a database object.
     * Calls the construction of all contained events, if loadAll is true.
     *
     * @param persistPerson database object
     * @param delivery      A person belongs to exactly one delivery
     * @param loadAll       if true, all contained events and plausierrors are loaded
     */
    public PersonBO(PersistPerson persistPerson, DeliveryBO delivery, boolean loadAll) {
        _deliveryId = delivery.get_thisDelivery().getDeliveryid();
        _canton = delivery.get_canton();
        _year = delivery.get_year();

        _thisPerson = persistPerson;

        _idNr = persistPerson.getId() != null ? persistPerson.getId().toString() : null;
        _idType = persistPerson.getIdType() != null ? persistPerson.getIdType().toString() : null;
        _sex = persistPerson.getSex() != null ? persistPerson.getSex().toString() : null;
        _dateOfBirth = persistPerson.getBirthDate() != null ? persistPerson.getBirthDate().toString() : null;
        _nDateOfBirth = persistPerson.getNewBirthDate() != null ? dateToString(persistPerson.getNewBirthDate()) : null;
        _comment = persistPerson.getUserComment();

        _events = new ArrayList<EventBO>();

        if (loadAll) {
            for (SbgEvent event : persistPerson.getEvents()) {
                if (event.getType() == CodegroupUtility.SBG_EVENTTYPE_CONTRACT) {
                    ContractBO contract = new ContractBO(event, this);
                    _events.add(contract);
                } else if (event.getType() == CodegroupUtility.SBG_EVENTTYPE_ONGOINGEDUCATION) {
                    OngoingEducationBO education = new OngoingEducationBO(event, this);
                    _events.add(education);
                } else if (event.getType() == CodegroupUtility.SBG_EVENTTYPE_EXAM) {
                    ExamBO exam = new ExamBO(event, this);
                    _events.add(exam);
                } else if (event.getType() == CodegroupUtility.SBG_EVENTTYPE_CANCELLATION) {
                    CancellationBO cancellation = new CancellationBO(event, this);
                    _events.add(cancellation);
                }
            }
        }

        for (Plausierror plausierror : persistPerson.getPlausiErrors()) {
            if (plausierror.getEventId() == null) {
                _plausiErrors.add(new PlausierrorBO(plausierror));
            }
        }
    }

    /**
     * Basic formatting of this person and its associated events
     */
    public void formatPersonAndEvents() {
        format();

        for (EventBO event : _events) {
            event.format();
        }
    }

    /**
     * Verifies this person and its associated events with all the plausis
     * in the plausiList.
     *
     * @param plausiList A list with plausis
     */
    public void verifyPersonAndEvents(List<PlausiBO> plausiList) {
        for (EventBO event : _events) {
            event.verifyEvent(plausiList);
        }

        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Verifies only this person with the plausis in the plausiList.
     *
     * @param plausiList A list with plausis
     */
    public void verifyPerson(List<PlausiBO> plausiList) {
        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Set default values for new person and insert it into database. Save all associated
     * events and plausierrors.
     *
     * @param delivery delivery for the new event.
     */
    public void savePersonAndEvents(PersistDelivery delivery, PersonDAO personDAO, IEventRepository eventRepository, PlausierrorDAO plausierrorDAO,
            String username) {
        // save person, if not loaded from database
        if (_thisPerson.getPid() == null) {
            _thisPerson.setDeliveryId(delivery.getDeliveryid());
            _thisPerson.setCanton(delivery.getCanton());
            _thisPerson.setVersion(delivery.getVersion());
            _thisPerson.setDeliveryText(_deliveryText);
            _thisPerson.setModUser(username);
            _thisPerson.setModDate(new Date());
            _thisPerson.setValidationUser(null);
            _thisPerson.setValidationDate(null);
            _thisPerson.setIsToDelete(false);
            _thisPerson.setStatus(CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);

            _thisPerson.setPlausiStatus(calculatePlausistatus());

            personDAO.save(_thisPerson);
        }

        for (EventBO event : _events) {
            event.saveEvent(eventRepository, plausierrorDAO, _thisPerson.getPid(), username);
        }

        for (PlausierrorBO pe : _plausiErrors) {
            pe.save(plausierrorDAO, username);
        }
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     */
    public void saveErrorsForReport(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO, PersonDAO personDAO, IEventRepository eventRepository, String username,
            List<PlausiBO> simplePlausis) {
        // delete old, save new plausierrors
        Iterator<PlausierrorBO> iter = _plausiErrors.iterator();
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
        if (!newPlausistatus.equals(_thisPerson.getPlausiStatus())) {
            _thisPerson.setPlausiStatus(newPlausistatus);
            _thisPerson.setModUser(username);
            _thisPerson.setModDate(new Date());
            personDAO.merge(_thisPerson);
        }

        for (EventBO event : _events) {
            event.mergeSimplePlausierrors(macroDAO, plausierrorDAO, eventRepository, username, simplePlausis);
        }
    }

    /**
     * Delete old plausierrors from database, insert new ones.
     */
    public void mergeSimplePlausierrors(MacroDAO macroDAO, PlausierrorDAO plausierrorDAO, PersonDAO personDAO, String userEmail) {
        // delete old, save new plausierrors
        Iterator<PlausierrorBO> iter = _plausiErrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = iter.next();
            Macro plausi = (Macro) macroDAO.load(Macro.class, pe.get_thisPlausierror().getPlausiId());
            if (plausi.getType().equals(CodegroupUtility.SBG_MACROTYPE_SIMPLEPLAUSI)) {
                if (pe.get_thisPlausierror().getErrorId() == null) {
                    pe.save(plausierrorDAO, userEmail);
                    _thisPerson.getPlausiErrors().add(pe.get_thisPlausierror());
                } else {
                    plausierrorDAO.deletePlausiError(pe.get_thisPlausierror());
                    _thisPerson.getPlausiErrors().remove(pe.get_thisPlausierror());
                    iter.remove();
                }
            }
        }
    }

    /**
     * Calculates the plausistatus of the person and all associated events based on the
     * plausierror business objects. No reload of database objects.
     *
     * @return true, if plausistatus of person and all events is valid or confirmed
     */
    public boolean setAllPlausistatus(PersonDAO personDAO, IEventRepository eventRepository, PlausierrorDAO plausierrorDAO) {
        boolean allOk = setPlausistatus(personDAO);

        for (EventBO event : _events) {
            if (!event.setAllPlausistatus(eventRepository, plausierrorDAO)) {
                allOk = false;
            }
        }
        return allOk;
    }

    /**
     * Calculates the plausistatus of this person only based on the
     * plausierror business objects. No reload of database objects.
     *
     * @return true, if plausistatus of this person is valid or confirmed
     */
    public boolean setPlausistatus(PersonDAO personDAO) {
        Long newPlausistatus = calculatePlausistatus();
        boolean allOk = !newPlausistatus.equals(CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID);

        if (!newPlausistatus.equals(_thisPerson.getPlausiStatus())) {
            _thisPerson.setPlausiStatus(newPlausistatus);
            personDAO.merge(_thisPerson);
        }

        return allOk;
    }

    /**
     * Sets the plausistatus of the person.
     * No reload of database objects.
     *
     * @param personDAO    Person repository
     * @param plausistatus
     */
    public void setPlausistatus(PersonDAO personDAO, long plausistatus) {
        _thisPerson.setPlausiStatus(plausistatus);
        personDAO.merge(_thisPerson);
    }

    /**
     * Calculates the plausistatus of this person only based on the
     * plausierror business objects. No reload of database objects.
     *
     * @return plausistatus of this person
     */
    private Long calculatePlausistatus() {
        Long newPlausistatus;
        if (_plausiErrors.isEmpty()) {
            newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_VALID;
        } else {
            newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED;
            for (PlausierrorBO error : _plausiErrors) {
                if (!error.get_thisPlausierror().getIsConfirmed()) {
                    newPlausistatus = CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID;
                    break;
                }
            }
        }

        return newPlausistatus;
    }

    /**
     * A person can be validated, if person and all
     * events have SBG_PLAUSISTATUS_VALID or SBG_PLAUSISTATUS_CONFIRMED.
     *
     * @return true, if person is valid
     */
    public boolean validate(PersonDAO personDAO, IEventRepository eventRepository) {
        boolean isValid = _thisPerson.getPlausiStatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_VALID)
                || _thisPerson.getPlausiStatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED);
        if (isValid) {
            for (EventBO event : _events) {
                if (event.getThisEvent().getPlausiStatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID)
                        || event.getThisEvent().getPlausiStatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED)) {
                    isValid = false;
                    break;
                }
            }
        }
        if (_thisPerson.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_DELIVERED) && isValid) {
            _thisPerson.setStatus(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
            _thisPerson.setValidationDate(new Date());
            _thisPerson.setValidationUser(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            personDAO.merge(_thisPerson);

            // setting isValidated also on all events
            for (EventBO eventBo : _events) {
                SbgEvent persistEvent = eventBo.getThisEvent();
                persistEvent.setIsValidated(true);
                eventRepository.updateEvent(persistEvent);
            }
        }

        return _thisPerson.getStatus().equals(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
    }

    public void addXml(Table.Pers persXml) {
        Table.Pers.Id id = persXml.addNewId();
        if (_thisPerson.getId() != null) {
            id.setIdnr(_thisPerson.getId().toString());
        }
        if (_thisPerson.getIdType() != null) {
            id.setIdtyp(_thisPerson.getIdType().toString());
        }

        if (_thisPerson.getSex() != null) {
            persXml.setSex(_thisPerson.getSex().toString());
        }
        if (_thisPerson.getBirthDate() != null && _thisPerson.getNewBirthDate() != null) {
            if (_thisPerson.getVersion() < CodegroupUtility.SBG_PERSON_NEWBIRTHDATE) {
                persXml.setDateofbirth(_thisPerson.getBirthDate().toString());
            } else {
                persXml.setNdateofbirth(dateToString(_thisPerson.getNewBirthDate()));
            }
        } else {
            if (_thisPerson.getBirthDate() != null) {
                persXml.setDateofbirth(_thisPerson.getBirthDate().toString());
            }
            if (_thisPerson.getNewBirthDate() != null) {
                persXml.setNdateofbirth(dateToString(_thisPerson.getNewBirthDate()));
            }
        }

        if (_thisPerson.getUserComment() != null) {
            persXml.setCom(_thisPerson.getUserComment());
        }

        for (EventBO event : _events) {
            event.addXml(persXml);
        }
    }

    @Override
    public void format() {
        if (_thisPerson == null) {
            _thisPerson = new PersistPerson();
        }

        Long idNr = verifyLong(get_idNr());
        _thisPerson.setId(idNr);

        Long idType = verifyLong(get_idType());
        _thisPerson.setIdType(idType);

        Long sex = verifyLong(get_sex());
        _thisPerson.setSex(sex);

        Long yearOfBirth = verifyLong(get_dateOfBirth());
        _thisPerson.setBirthDate(yearOfBirth);

        Date dateOfBirth = verifyDate(get_nDateOfBirth());
        _thisPerson.setNewBirthDate(dateOfBirth);

        _thisPerson.setUserComment(get_comment());
    }

    /**
     * @return Returns the _thisPerson.
     */
    public PersistPerson get_thisPerson() {
        return _thisPerson;
    }

    /**
     * @param person The _thisPerson to set.
     */
    public void set_thisPerson(PersistPerson person) {
        _thisPerson = person;
    }

    /**
     * @return Returns the _dateOfBirth.
     */
    public String get_dateOfBirth() {
        return _dateOfBirth;
    }

    /**
     * @return Returns the _nDateOfBirth.
     */
    public String get_nDateOfBirth() {
        return _nDateOfBirth;
    }

    /**
     * @return Returns the _idNr.
     */
    public String get_idNr() {
        return _idNr;
    }

    /**
     * @return Returns the _idType.
     */
    public String get_idType() {
        return _idType;
    }

    /**
     * @return Returns the _sex.
     */
    public String get_sex() {
        return _sex;
    }

    /**
     * @return Returns the _comment.
     */
    public String get_comment() {
        return _comment;
    }

    /**
     * @return Returns the _delivery.
     */
    //	public DeliveryBO get_delivery() {
    //		return _delivery;
    //	}
    public Long get_deliveryId() {
        return _deliveryId;
    }

    public Long get_canton() {
        return _canton;
    }

    public Long get_year() {
        return _year;
    }

    /**
     * @return Returns the _events.
     */
    public List<EventBO> get_events() {
        return _events;
    }

    /**
     * @return Returns the _plausiErrors.
     */
    public List<PlausierrorBO> get_plausierrors() {
        return _plausiErrors;
    }
}
