/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.server.business.plausi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlPlausi;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.repository.*;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import ch.bfs.meb.server.commons.integration.sas.ISasService;
import ch.bfs.meb.util.CodegroupUtility;

/** 
 * Factory for plausi rules.
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public class PlausiFactory {
    private IPlausiRepository _plausiRepository;
    private IPlausiErrorRepository _plausierrorRepository;
    private IDeliveryRepository _deliveryRepository;
    private ISchoolRepository _schoolRepository;
    private IClassRepository _classRepository;
    private ILearnerRepository _learnerRepository;
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

    public void setDeliveryRepository(IDeliveryRepository deliveryRepository) {
        _deliveryRepository = deliveryRepository;
    }

    public void setSchoolRepository(ISchoolRepository schoolRepository) {
        _schoolRepository = schoolRepository;
    }

    public void setClassRepository(IClassRepository classRepository) {
        _classRepository = classRepository;
    }

    public void setLearnerRepository(ILearnerRepository learnerRepository) {
        _learnerRepository = learnerRepository;
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
        HashMap<String, SdlPlausiError> confirmedInternalErrorMap = new HashMap<String, SdlPlausiError>();
        List<SdlPlausiError> confirmedInternalErrors = _plausierrorRepository.findConfirmedInternalErrors(deliveryId);
        for (SdlPlausiError error : confirmedInternalErrors) {
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
        List<SdlPlausi> plausiList = _plausiRepository.getByType(CodegroupUtility.MEB_PLAUSITYPE_INTERNAL);

        ArrayList<PlausiBO> internalPlausis = new ArrayList<PlausiBO>();
        for (SdlPlausi plausi : plausiList) {
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
                case 7:
                    internalPlausis.add(new InternalPlausi7BO(plausi, _localizationManager));
                    break;
                case 8:
                    internalPlausis.add(new InternalPlausi8BO(plausi, _localizationManager));
                    break;
                case 9:
                    internalPlausis.add(new InternalPlausi9BO(plausi, _localizationManager));
                    break;
                case 10:
                    internalPlausis.add(new InternalPlausi10BO(plausi, _plausiRepository, _localizationManager));
                    break;
                case 13:
                    internalPlausis.add(new InternalPlausi13BO(plausi, _localizationManager));
                    break;
                case 14:
                    internalPlausis.add(new InternalPlausi14BO(plausi, _plausiRepository, _localizationManager));
                    break;
                case 20:
                    internalPlausis.add(new InternalPlausi20BO(plausi, _schoolRepository, _burSchoolRepository, _localizationManager));
                    break;
                case 21:
                    internalPlausis.add(new InternalPlausi21BO(plausi, _schoolRepository, _burSchoolRepository, _idmService, _localizationManager));
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
        for (SdlPlausi plausi : _plausiRepository.getFormatPlausis()) {
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
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public ISchoolRepository getSchoolRepository() {
                return _schoolRepository;
            }

            @Override
            public IClassRepository getClassRepository() {
                return _classRepository;
            }

            @Override
            public ILearnerRepository getLearnerRepository() {
                return _learnerRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }
        };

        List<SdlPlausi> plausiList = _plausiRepository.getByType(CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
        ArrayList<PlausiBO> complexPlausis = new ArrayList<PlausiBO>();
        for (SdlPlausi plausi : plausiList) {
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
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public ISchoolRepository getSchoolRepository() {
                return _schoolRepository;
            }

            @Override
            public IClassRepository getClassRepository() {
                return _classRepository;
            }

            @Override
            public ILearnerRepository getLearnerRepository() {
                return _learnerRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }
        };

        List<SdlPlausi> plausiList = _plausiRepository.getByType(CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
        ArrayList<PlausiBO> complexPlausis = new ArrayList<PlausiBO>();
        for (SdlPlausi plausi : plausiList) {
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
            public IDeliveryRepository getDeliveryRepository() {
                return _deliveryRepository;
            }

            @Override
            public ISchoolRepository getSchoolRepository() {
                return _schoolRepository;
            }

            @Override
            public IClassRepository getClassRepository() {
                return _classRepository;
            }

            @Override
            public ILearnerRepository getLearnerRepository() {
                return _learnerRepository;
            }

            @Override
            public IPlausiErrorRepository getPlausierrorRepository() {
                return _plausierrorRepository;
            }
        };
        return new ExternalPlausiProcess(repositories, getExternalPlausisFor(objectType, version));
    }
}
