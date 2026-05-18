/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: PlausiFactory.java 892 2010-03-03 15:05:51Z dzw $
 */
package ch.bfs.meb.ssp.server.business.plausi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausi;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.ssp.server.integration.repository.*;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Factory for plausi rules.
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 892 $ 
 */
public class PlausiFactory {
    private IPlausiRepository _plausiRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IActivityRepository _activityRepository;
    private IPersonRepository _personRepository;
    private IDeliveryRepository _deliveryRepository;
    private IBurSchoolRepository _burSchoolRepository;
    private ICodegroupManager _codegroupManager;
    private IIdmUserService _idmService;
    private ISasService _sasService;
    private IServerLocalizationManager _localizationManager;

    public void setPlausiRepository(IPlausiRepository plausiRepository) {
        _plausiRepository = plausiRepository;
    }

    public void setPlausierrorRepository(IPlausiErrorRepository plausierrorRepository) {
        _plausierrorRepository = plausierrorRepository;
    }

    public void setActivityRepository(IActivityRepository activityRepository) {
        _activityRepository = activityRepository;
    }

    public void setPersonRepository(IPersonRepository personRepository) {
        _personRepository = personRepository;
    }

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setBurSchoolRepository(IBurSchoolRepository burSchoolRepository) {
        _burSchoolRepository = burSchoolRepository;
    }

    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    public void setIdmService(IIdmUserService idmService) {
        _idmService = idmService;
    }

    public void setSasService(ISasService sasService) {
        _sasService = sasService;
    }

    public void setLocalizationManager(IServerLocalizationManager localizationManager) {
        _localizationManager = localizationManager;
    }

    /**
     * Mantis 1783: Load old confirmed errors for taking over confirmation info in replace/amend use case
     * 
     * @param version
     * @param deliveryId
     * @param doLoadConfirmedErrors
     * @return
     */
    public List<PlausiBO> getInternalPlausis(Long version, Long deliveryId, boolean doLoadConfirmedErrors) {
        List<PlausiBO> internalPlausis = getInternalPlausis(version);
        if (!doLoadConfirmedErrors) {
            return internalPlausis;
        }
        // get all old internal confirmed errors for eventual taking over confirm information in replace/amend use case
        HashMap<String, SspPlausiError> confirmedInternalErrorMap = new HashMap<String, SspPlausiError>();
        List<SspPlausiError> confirmedInternalErrors = _plausierrorRepository.findConfirmedInternalErrors(deliveryId);
        for (SspPlausiError error : confirmedInternalErrors) {
            confirmedInternalErrorMap.put(error.getLogicalKey(), error);
        }
        // store confirmed errors map for confirmable internal plausis
        for (PlausiBO plausi : internalPlausis) {
            if (plausi instanceof InternalPlausiBO) {
                ((InternalPlausiBO) plausi).setConfirmablePlausiErrorMap(confirmedInternalErrorMap);
            }
        }
        return internalPlausis;
    }

    public List<PlausiBO> getInternalPlausis(Long version) {
        List<SspPlausi> plausiList = _plausiRepository.getByType(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL);

        ArrayList<PlausiBO> internalPlausis = new ArrayList<PlausiBO>();
        for (SspPlausi plausi : plausiList) {
            if (plausi.getIsActive() && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)) {
                int plausiNr = Integer.valueOf(plausi.getSource().substring(0, 2).trim());
                switch (plausiNr) {
                case 1:
                    internalPlausis.add(new InternalPlausi1BO(plausi, _localizationManager));
                    break;
                case 2:
                    internalPlausis.add(new InternalPlausi2BO(plausi, _localizationManager));
                    break;
                case 3:
                    internalPlausis.add(new InternalPlausi3BO(plausi, _codegroupManager, _localizationManager));
                    break;
                case 4:
                    internalPlausis.add(new InternalPlausi4BO(plausi, _localizationManager));
                    break;
                case 5:
                    internalPlausis.add(new InternalPlausi5BO(plausi, _localizationManager));
                    break;
                case 6:
                    internalPlausis.add(new InternalPlausi6BO(plausi, _localizationManager));
                    break;
                case 7:
                    internalPlausis.add(new InternalPlausi7BO(plausi, _localizationManager));
                    break;
                case 9:
                    internalPlausis.add(new InternalPlausi9BO(plausi, _deliveryRepository, _localizationManager));
                    break;
                case 10:
                    internalPlausis.add(new InternalPlausi10BO(plausi, _localizationManager));
                    break;
                case 11:
                    internalPlausis.add(new InternalPlausi11BO(plausi, _plausiRepository, _localizationManager));
                    break;
                case 12:
                    internalPlausis.add(new InternalPlausi12BO(plausi, _localizationManager));
                    break;
                case 13:
                    internalPlausis.add(new InternalPlausi13BO(plausi, _localizationManager));
                    break;
                case 15:
                    internalPlausis.add(new InternalPlausi15BO(plausi, _plausiRepository, _personRepository, _localizationManager));
                    break;
                case 16:
                    internalPlausis.add(new InternalPlausi16BO(plausi, _personRepository, _localizationManager));
                    break;
                case 20:
                    internalPlausis.add(new InternalPlausi20BO(plausi, _activityRepository, _burSchoolRepository, _localizationManager));
                    break;
                case 21:
                    internalPlausis.add(new InternalPlausi21BO(plausi, _activityRepository, _burSchoolRepository, _idmService, _localizationManager));
                    break;
                default:
                    break;
                }
            }
        }

        return internalPlausis;
    }

    public List<PlausiBO> getFormatPlausis(Long version) {
        ArrayList<PlausiBO> formatPlausis = new ArrayList<PlausiBO>();
        for (SspPlausi plausi : _plausiRepository.getFormatPlausis()) {
            if (plausi.getIsActive() && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)) {
                formatPlausis.add(new InternalPlausi2BO(plausi, _localizationManager));
                break;
            }
        }

        return formatPlausis;
    }

    public List<PlausiBO> getExternalPlausis(Long version) {
        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IActivityRepository getActivityRepository() {
                return _activityRepository;
            }

            @Override
            public IPersonRepository getPersonRepository() {
                return _personRepository;
            }

            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }

            @Override
            public IBurSchoolRepository getBurSchoolRepository() {
                return _burSchoolRepository;
            }
        };

        List<SspPlausi> plausiList = _plausiRepository.getByType(CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
        ArrayList<PlausiBO> complexPlausis = new ArrayList<PlausiBO>();
        for (SspPlausi plausi : plausiList) {
            if (plausi.getIsActive() && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)) {
                complexPlausis.add(new ExternalPlausiBO(plausi, repositories, _sasService, _localizationManager));
            }
        }

        return complexPlausis;
    }

    public List<PlausiBO> getExternalPlausisFor(Long objectType, Long version) {
        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IActivityRepository getActivityRepository() {
                return _activityRepository;
            }

            @Override
            public IPersonRepository getPersonRepository() {
                return _personRepository;
            }

            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }

            @Override
            public IBurSchoolRepository getBurSchoolRepository() {
                return _burSchoolRepository;
            }
        };

        List<SspPlausi> plausiList = _plausiRepository.getByType(CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
        ArrayList<PlausiBO> complexPlausis = new ArrayList<PlausiBO>();
        for (SspPlausi plausi : plausiList) {
            if (plausi.getIsActive() && plausi.getObjectLevel().equals(objectType)
                    && (plausi.getValidFrom() == null || version.compareTo(plausi.getValidFrom()) >= 0)
                    && (plausi.getValidTo() == null || version.compareTo(plausi.getValidTo()) <= 0)) {
                complexPlausis.add(new ExternalPlausiBO(plausi, repositories, _sasService, _localizationManager));
            }
        }

        return complexPlausis;
    }

    public ExternalPlausiProcess createExternalPlausiProcess(Long objectType, Long version) {
        IRepositoryProvider repositories = new IRepositoryProvider() {
            @Override
            public IActivityRepository getActivityRepository() {
                return _activityRepository;
            }

            @Override
            public IPersonRepository getPersonRepository() {
                return _personRepository;
            }

            @Override
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }

            @Override
            public IBurSchoolRepository getBurSchoolRepository() {
                return _burSchoolRepository;
            }
        };
        return new ExternalPlausiProcess(repositories, getExternalPlausisFor(objectType, version));
    }
}
