package ch.bfs.meb.server.commons.service.impl;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.util.CodegroupUtility;

@Service
public class BurSchoolServiceImpl extends FilteredObjectsServiceBase implements IBurSchoolService {
    protected final String BUR_NOT_AVAILABLE_MESSAGE = "bur.notavailable.message";
    protected final String BUR_CANTON_REQUIRED_MESSAGE = "bur.cantonrequired.message";
    protected final String BUR_CONFIG_DELIVERY_NOT_FOUND_MESSAGE = "bur.configdeliverynotfound.message";
    protected final String BUR_CONFIG_DELIVERY_NOT_UNIQUE_MESSAGE = "bur.configdeliverynotunique.message";
    protected final String BUR_SCHOOL_NOT_VALID_MESSAGE = "bur.schoolnotvalid.message";
    protected final String BUR_DATA_EXISTS_FOR_SCHOOL_MESSAGE = "bur.dataexistsforschool.message";
    protected final String BUR_DATA_EXISTS_FOR_SCHOOL2_MESSAGE = "bur.dataexistsforschool2.message";
    protected final String BUR_CHANGE_DELIVERY_AND_IMPORT_MESSAGE = "bur.changedeliveryandimport.message";
    protected final String BUR_SCHOOLS_LOCKED_MESSAGE = "bur.schools.locked.message";

    IBurSchoolServiceProvider _burSchoolServiceProvider;
    IConfigDeliveryServiceProvider _configDeliveryServiceProvider;

    private static final Log LOGGER = LogFactory.getLog(BurSchoolServiceImpl.class);

    public void setBurSchoolServiceProvider(IBurSchoolServiceProvider burSchoolServiceProvider) {
        _burSchoolServiceProvider = burSchoolServiceProvider;
    }

    public void setConfigDeliveryServiceProvider(IConfigDeliveryServiceProvider configDeliveryServiceProvider) {
        _configDeliveryServiceProvider = configDeliveryServiceProvider;
    }

    @Transactional(readOnly = true)
    public BurSchoolListResult getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
                                             boolean showBurSynch) {
        completeFilterParams(filterContext);

        List<BurSchool> burSchools = _burSchoolServiceProvider.getBurSchools(start, buffer, sortContext, filterContext, version, canton, showBurSynch);
        if (showBurSynch) {
            _burSchoolServiceProvider.initSynchData(burSchools);
        }
        return new BurSchoolListResult(burSchools, _burSchoolServiceProvider.getMaxNrOfBurSchools(filterContext, version, canton, showBurSynch));
    }

    @Transactional(readOnly = true)
    public BurSchoolListResult getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch) {
        List<BurSchool> burSchools = _burSchoolServiceProvider.getBurSchoolsOwnedByConfigDeliveries(configDeliveryIds, sortContext, showBurSynch);
        if (showBurSynch) {
            _burSchoolServiceProvider.initSynchData(burSchools);
        }
        return new BurSchoolListResult(burSchools, new Long(burSchools.size()));
    }

    @Transactional(readOnly = true)
    public BurSchoolResult getBurSchoolById(Long burSchoolId, boolean showBurSynch, Long version) {
        BurSchool burSchool = _burSchoolServiceProvider.getBurSchoolById(burSchoolId, version);
        if (burSchool == null) {
            return new BurSchoolResult("Could not find school with id: " + burSchoolId);
        } else {
            if (showBurSynch) {
                _burSchoolServiceProvider.initSynchData(burSchool);
            }
            return new BurSchoolResult(burSchool);
        }
    }

    @Transactional(readOnly = true)
    public BurSchoolResult getBurSchoolByIdAndType(String schoolId, String schoolType, Long version) {
        BurSchool burSchool = _burSchoolServiceProvider.getBurSchoolByIdAndType(schoolId, schoolType, version);
        if (burSchool == null) {
            return new BurSchoolResult("Could not find school with id " + schoolId + " and type " + schoolType + " for version " + version);
        } else {
            return new BurSchoolResult(burSchool);
        }
    }

    @Transactional(timeout = 1800)
    public synchronized BurSchoolListResult synchronizeSchools() {
        LOGGER.info("Début de synchronizeSchools()");

        try {
            LOGGER.debug("Tentative de verrouillage des BurSchools pour éviter la synchronisation concurrente...");
            // E05 2012 Mantis 1421: éviter synchronisation simultanée d’un canton
            _burSchoolServiceProvider.lockBurSchools();
            LOGGER.debug("Verrouillage réussi.");
        } catch (HibernateException e) {
            LOGGER.error("Erreur lors du verrouillage des BurSchools : " + BUR_SCHOOLS_LOCKED_MESSAGE, e);
            return new BurSchoolListResult(BUR_SCHOOLS_LOCKED_MESSAGE);
        }

        List<BurSchoolExt> extSchools;
        try {
            LOGGER.debug("Récupération des BurSchools externes...");
            extSchools = _burSchoolServiceProvider.getExternalBurSchools();
            LOGGER.info("Nombre d'écoles externes récupérées : " + (extSchools != null ? extSchools.size() : 0));
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la récupération des écoles externes : " + BUR_NOT_AVAILABLE_MESSAGE, e);
            return new BurSchoolListResult(BUR_NOT_AVAILABLE_MESSAGE);
        }

        LOGGER.debug("Récupération des BurSchools internes...");
        List<BurSchool> schoolList = _burSchoolServiceProvider.getBurSchools();
        LOGGER.info("Nombre d'écoles internes récupérées : " + (schoolList != null ? schoolList.size() : 0));

        Map<Long, BurSchool> schools = new HashMap<Long, BurSchool>();
        for (BurSchool s : schoolList) {
            schools.put(s.getBurNr(), s);
        }

        LOGGER.debug("Synchronisation des écoles externes avec la base interne...");
        for (BurSchoolExt extSchool : extSchools) {
            BurSchool school = schools.remove(extSchool.getLocal_id());

            if (school == null) {
                LOGGER.debug("Nouvelle école détectée : " + extSchool.getLocal_id());
                school = new BurSchool();
                school.setBurNr(extSchool.getLocal_id());
                school.setSynchStatus_sdl(CodegroupUtility.MEB_SYNCHSTATUS_NEW);
                school.setSynchStatus_ssp(CodegroupUtility.MEB_SYNCHSTATUS_NEW);
                school.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_NEW);
            }

            _burSchoolServiceProvider.calculateSynchStatus(school, extSchool);

            if (school.getSchoolId() == null) {
                LOGGER.debug("Insertion de l'école : " + school.getBurNr());
                insertBurSchool(school);
            } else {
                LOGGER.debug("Mise à jour de l'école : " + school.getBurNr());
                _burSchoolServiceProvider.updateSynchBurSchool(school);
            }
        }

        LOGGER.debug("Vérification des écoles internes restantes pour suppression ou mise à jour...");
        for (BurSchool school : schools.values()) {
            if (_burSchoolServiceProvider.calculateSynchStatus(school, null) != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED) {
                LOGGER.debug("Mise à jour de l'école restante : " + school.getBurNr());
                _burSchoolServiceProvider.updateSynchBurSchool(school);
            }
        }

        LOGGER.info("Synchronisation des BurSchools terminée avec succès.");
        return new BurSchoolListResult();
    }


    @Transactional(timeout = 1800)
    public BurSchoolListResult importBurSchools(Long canton) {
        LOGGER.info("Début de importBurSchools avec canton=" + canton);

        try {
            boolean imported = _burSchoolServiceProvider.importBurSchools(canton);
            LOGGER.debug("Résultat de l'appel à _burSchoolServiceProvider.importBurSchools : " + imported);

            if (!imported) {
                LOGGER.warn("Import impossible : " + BUR_DATA_EXISTS_FOR_SCHOOL2_MESSAGE);
                throw new MebUncheckedNotMonitoredException(BUR_DATA_EXISTS_FOR_SCHOOL2_MESSAGE);
            }

            LOGGER.info("Import des BurSchools terminé avec succès pour canton=" + canton);
            return new BurSchoolListResult();

        } catch (MebUncheckedNotMonitoredException e) {
            LOGGER.error("Erreur fonctionnelle lors de l'import des BurSchools pour canton="
                    + canton + " : " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Erreur technique inattendue lors de l'import des BurSchools pour canton="
                    + canton, e);
            throw e;
        }
    }

    protected boolean equalDeliveryCode(BurSchool school1, BurSchool school2) {
        String deliveryCode1 = school1.getDeliveryCode() == null ? "" : school1.getDeliveryCode();
        String deliveryCode2 = school2.getDeliveryCode() == null ? "" : school2.getDeliveryCode();
        return deliveryCode1.equals(deliveryCode2);
    }

    @Transactional
    public BurSchoolResult importBurSchool(BurSchool burSchool) {
        Long burSchoolId = burSchool.getSchoolId();
        Long version = burSchool.getVersion();
        Long now = new Long(new GregorianCalendar().get(Calendar.YEAR));

        // change of configDelivery and import of bur school in one step
        // is only possible, if version is current year
        if (!now.equals(version)) {
            BurSchoolResult res = getBurSchoolById(burSchoolId, true, version);
            if (!equalDeliveryCode(burSchool, res.getSchool())) {
                throw new MebUncheckedNotMonitoredException(BUR_CHANGE_DELIVERY_AND_IMPORT_MESSAGE);
            }
        }

        BurSchoolResult res = getBurSchoolById(burSchoolId, true, now);
        BurSchool origSchool = res.getSchool();
        // only set deliveryCode if current version (see check above) - otherwise old deliveryCode of version is set for current year!
        if (now.equals(version)) {
            origSchool.setDeliveryCode(burSchool.getDeliveryCode());
        }

        if (!_burSchoolServiceProvider.importBurSchool(origSchool)) {
            throw new MebUncheckedNotMonitoredException(BUR_DATA_EXISTS_FOR_SCHOOL_MESSAGE);
        }
        updateBurSchool(origSchool, true);

        // return the bur school with the original version
        return getBurSchoolById(burSchoolId, true, version);
    }

    @Transactional
    public BurSchoolResult updateBurSchool(BurSchool burSchool, boolean showBurSynch) {
        ConfigDelivery configDelivery = null;

        if (burSchool.getDeliveryCode() != null && !burSchool.getDeliveryCode().trim().equals("")) {
            if (burSchool.getCanton() == null || burSchool.getCanton() == 0L) {
                throw new MebUncheckedNotMonitoredException(BUR_CANTON_REQUIRED_MESSAGE);
            }
            configDelivery = _configDeliveryServiceProvider.getConfigDeliveryByCodeVersionAndCanton(burSchool.getDeliveryCode().trim(), burSchool.getVersion(),
                    burSchool.getCanton());
            if (configDelivery == null) { // Search for config Delivery of other canton!
                List<ConfigDelivery> configDeliveries = _configDeliveryServiceProvider.getConfigDeliveriesByCodeAndVersion(burSchool.getDeliveryCode().trim(),
                        burSchool.getVersion());
                if (configDeliveries.size() == 1) {
                    configDelivery = configDeliveries.get(0);
                } else if (configDeliveries.size() > 1) {
                    throw new MebUncheckedNotMonitoredException(BUR_CONFIG_DELIVERY_NOT_UNIQUE_MESSAGE);
                }
            }
            if (configDelivery == null) {
                throw new MebUncheckedNotMonitoredException(BUR_CONFIG_DELIVERY_NOT_FOUND_MESSAGE);
            }
            if (!_burSchoolServiceProvider.isActiveSchool(burSchool)) {
                throw new MebUncheckedNotMonitoredException(BUR_SCHOOL_NOT_VALID_MESSAGE);
            }
        }

        burSchool = _burSchoolServiceProvider.updateBurSchool(burSchool, configDelivery);

        if (showBurSynch) {
            _burSchoolServiceProvider.initSynchData(burSchool);
        }

        return new BurSchoolResult(burSchool);
    }

    @Transactional
    public BurSchoolResult insertBurSchool(BurSchool burSchool) {
        return new BurSchoolResult(_burSchoolServiceProvider.insertBurSchool(burSchool));
    }

    @Transactional
    public BurSchoolResult deleteBurSchool(BurSchool burSchool) {
        _burSchoolServiceProvider.deleteBurSchool(burSchool);
        return new BurSchoolResult();
    }
}
