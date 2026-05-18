/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.business;

import java.util.*;

import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.business.BOBase;
import ch.bfs.meb.ssp.server.business.plausi.ExternalPlausiProcess;
import ch.bfs.meb.ssp.server.business.plausi.PlausiBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.ssp.server.integration.repository.IActivityRepository;
import ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository;
import ch.bfs.meb.ssp.server.integration.repository.IPersonRepository;
import ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository;
import ch.bfs.meb.ssp.server.service.xmlbeans.TableDocument;
import ch.bfs.meb.ssp.server.service.xmlbeans.TableDocument.Table;
import ch.bfs.meb.ssp.server.service.xmlbeans.TableDocument.Table.Pers;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

/**
 * Business object for handling Ssp deliveries.
 * 
 * @author $Author: dzw $
 * @version $Revision: 954 $
 */
public class DeliveryBO extends BOBase {
    protected final Long _version;
    protected final Long _cantonId;
    protected String _dataDelivery;
    protected String _deliveryDate;
    protected SspDelivery _thisDelivery = null;

    private List<PersonBO> _persons;
    private final List<PlausierrorBO> _plausierrors = new ArrayList<PlausierrorBO>();

    protected boolean _firstDelivery;
    protected IDeliveryRepository _deliveryRepository;

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    /**
     * Constructs a delivery business object from XML part.
     * 
     * @param xmlPart	XML bean part for delivery
     * @return			the constructed business object
     */
    public DeliveryBO(TableDocument.Table.Head xmlPart) {
        _version = new Long(xmlPart.getVersion());
        _cantonId = new Long(xmlPart.getCantonId());
        _dataDelivery = xmlPart.getDataDelivery();
        _deliveryDate = xmlPart.getDeliveryDate();
        _confirmRules = xmlPart.getConfirmRules();
    }

    /**
     * Constructs a delivery business object from a database object.
     * 
     * @param sspDelivery	database object
     */
    public DeliveryBO(SspDelivery delivery) {
        this(delivery, false, null, null);
    }

    /**
     * Constructs a delivery business object from a database object.
     * 
     * @param sspDelivery	database object
     */
    public DeliveryBO(FieldSet fieldSet) {
        _version = verifyLong(fieldSet.readString("VERSION"));
        _cantonId = verifyLong(fieldSet.readString("CANTONID"));
        _dataDelivery = StringUtils.nullForEmpty(fieldSet.readString("DATADELIVERY"));
        _deliveryDate = StringUtils.nullForEmpty(fieldSet.readString("DELIVERYDATE"));
        _confirmRules = StringUtils.nullForEmpty(fieldSet.readString("CONFIRMDELIVERY"));
    }

    /**
     * Constructs a delivery business object from a database object.
     * 
     * @param sspDelivery	database object
     */
    public DeliveryBO(SspDelivery delivery, boolean loadAll, IPersonRepository personRepository, IActivityRepository activityRepository) {
        _thisDelivery = delivery;

        _cantonId = delivery.getCanton();
        _version = delivery.getVersion();
        _dataDelivery = delivery.getDeliveryCode();
        _confirmRules = delivery.getConfirmRules();

        _persons = new ArrayList<PersonBO>();
        if (loadAll) {
            Set<SspPerson> existingPersons = personRepository.loadWholeDelivery(_thisDelivery.getDeliveryId());
            for (SspPerson person : existingPersons) {
                _persons.add(new PersonBO(person, true, activityRepository));
            }
        }

        for (SspPlausiError plausierror : delivery.getPlausierrors()) {
            _plausierrors.add(new PlausierrorBO(plausierror));
        }
    }

    public void initialize() {
        _thisDelivery = _deliveryRepository.getDeliveryByIdentification(_cantonId, _version, _dataDelivery);
        if (_thisDelivery == null) {
            // create new delivery
            _thisDelivery = new SspDelivery();
            _thisDelivery.setCanton(_cantonId);
            _thisDelivery.setVersion(_version);
            _thisDelivery.setDeliveryCode(_dataDelivery);
            _thisDelivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED);
            _thisDelivery.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
            _thisDelivery.setCreation_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
            _thisDelivery.setCreation_date(new Date());
            _thisDelivery.setIsLocked(SspDelivery.DELIVERY_NOT_LOCKED);
            _firstDelivery = true;
        } else if (_thisDelivery.getDeliveryStatus().equals(CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED)) {
            _firstDelivery = true;
        } else {
            _firstDelivery = false;
        }
        _thisDelivery.setConfirmRules(_confirmRules);
    }

    public void asXml(TableDocument xmlRoot) {
        Table table = xmlRoot.addNewTable();
        Table.Head head = table.addNewHead();
        head.setVersion(_thisDelivery.getVersion().intValue());
        head.setCantonId(_thisDelivery.getCanton().intValue());
        head.setDataDelivery(_thisDelivery.getDeliveryCode());
        head.setDeliveryDate(formatXmlDate(new Date()));
        if (_thisDelivery.getConfirmRules() != null) {
            head.setConfirmRules(_thisDelivery.getConfirmRules());
        }

        for (PersonBO person : _persons) {
            Pers pers = table.addNewPers();
            person.addXml(pers);
        }
    }

    /**
     * Verifies the delivery with the plausis in the plausiList.
     * 
     * @param plausiList	A list with plausis
     */
    public void verifyDelivery(List<PlausiBO> plausiList) {
        for (PlausiBO plausi : plausiList) {
            plausi.verify(this);
        }
    }

    /**
     * Verifies the delivery with the internal and external plausis.
     * 
     * @param internalPlausis		A list with internal plausis
     * @param externalPlausiProcess	A Process for external verification (SAS)
     */
    public void verifyDelivery(List<PlausiBO> internalPlausis, ExternalPlausiProcess externalPlausiProcess) {
        for (PlausiBO plausi : internalPlausis) {
            plausi.verify(this);
        }

        externalPlausiProcess.verify(this);
    }

    @Override
    public void format() {}

    public void save() {
        _thisDelivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED);
        _thisDelivery.setModification_user(((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        _thisDelivery.setModification_date(new Date());
        _thisDelivery = _deliveryRepository.updateDelivery(_thisDelivery);
    }

    public SspDelivery getThisDelivery() {
        return _thisDelivery;
    }

    public Long getVersion() {
        return _version;
    }

    public Long getCantonId() {
        return _cantonId;
    }

    public String getDataDelivery() {
        return _dataDelivery;
    }

    public void setDataDelivery(String dataDelivery) {
        _dataDelivery = dataDelivery;
    }

    public String getDeliveryDate() {
        return _deliveryDate;
    }

    public boolean isFirstDelivery() {
        return _firstDelivery;
    }

    /**
     * Calculates the plausistatus of this delivery only based on the
     * plausierror business objects. No reload of database objects.
     * 
     * @return	plausistatus of the delivery
     */
    private Long calculatePlausistatus() {
        Long newPlausistatus;
        if (_plausierrors.isEmpty()) {
            newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_VALID;
        } else {
            newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
            for (PlausierrorBO error : _plausierrors) {
                if (!error.getThisPlausierror().getIsConfirmed()) {
                    newPlausistatus = CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
                    break;
                }
            }
        }

        return newPlausistatus;
    }

    /**
     * Save all associated plausierrors.
     * 
     * @param repositories	Access to database repositories.
     */
    public void savePlausierrors(IPlausiErrorRepository peRepository, IDeliveryRepository deliveryRepository, String username) {
        Iterator<PlausierrorBO> iter = _plausierrors.iterator();
        while (iter.hasNext()) {
            PlausierrorBO pe = iter.next();
            SspPlausi plausi = pe.getThisPlausierror().getPlausi();
            if (plausi.getType().equals(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL)) {
                if (pe.getThisPlausierror().getErrorId() == null) {
                    pe.save(peRepository, username);
                    _thisDelivery.getPlausierrors().add(pe.getThisPlausierror());
                } else {
                    // delivery errors should have been set to delete before recalculating
                }
            }
        }
        Long newPlausistatus = calculatePlausistatus();
        if (!newPlausistatus.equals(_thisDelivery.getPlausiStatus())) {
            _thisDelivery.setPlausiStatus(newPlausistatus);
            _thisDelivery.setModification_user(username);
            _thisDelivery.setModification_date(new Date());
            deliveryRepository.updateDelivery(_thisDelivery);
        }
    }

    /**
     * @return the _plausierrors
     */
    public List<PlausierrorBO> getPlausierrors() {
        return _plausierrors;
    }
}
