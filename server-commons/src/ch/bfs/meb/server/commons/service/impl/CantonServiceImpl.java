/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: FilterServiceImpl.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.util.CodegroupUtility;

@Service
@Transactional
public class CantonServiceImpl implements ICantonService {
    protected final String INIT_BUR_NOT_SYNCHRON_MESSAGE = "init.burnotsynchron.message";
    protected final String INIT_ALREADY_DONE_1_MESSAGE = "init.alreadydone1.message";
    protected final String INIT_ALREADY_DONE_2_MESSAGE = "init.alreadydone2.message";
    protected final String INIT_DELIVERY_EXISTS_MESSAGE = "init.deliveryexists.message";
    protected final String DELETE_DELIVERY_EXISTS_MESSAGE = "delete.deliveryexists.message";
    protected final String CANTON_ALREADY_EXISTS_MESSAGE = "canton.alreadyexists.message";

    ICantonServiceProvider _cantonServiceProvider;
    IBurSchoolServiceProvider _burSchoolServiceProvider;
    IConfigDeliveryServiceProvider _configDeliveryServiceProvider;
    ICodegroupManager _codegroupManager;
    IBurSchoolService _burSchoolService;

    public void setCantonServiceProvider(ICantonServiceProvider cantonServiceProvider) {
        _cantonServiceProvider = cantonServiceProvider;
    }

    public void setBurSchoolServiceProvider(IBurSchoolServiceProvider burSchoolServiceProvider) {
        _burSchoolServiceProvider = burSchoolServiceProvider;
    }

    public void setConfigDeliveryServiceProvider(IConfigDeliveryServiceProvider configDeliveryServiceProvider) {
        _configDeliveryServiceProvider = configDeliveryServiceProvider;
    }

    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    public void setBurSchoolService(IBurSchoolService burSchoolService) {
        _burSchoolService = burSchoolService;
    }

    @Transactional(readOnly = true)
    public CantonListResult getCantons(Long version, Long canton) {
        return new CantonListResult(_cantonServiceProvider.getCantons(version, canton));
    }

    @Transactional(readOnly = true)
    public CantonResult getCantonById(Long cantonId) {
        Canton canton = _cantonServiceProvider.getCantonById(cantonId);
        if (canton == null) {
            return new CantonResult("Could not find canton with id: " + cantonId);
        } else {
            return new CantonResult(canton);
        }
    }

    public PlausiErrorListResult getPlausiErrorsForCanton(Long cantonId) {
        List<PlausiError> plausiErrors = _cantonServiceProvider.getPlausiErrorsForCanton(cantonId);
        if (plausiErrors == null) {
            return new PlausiErrorListResult("Could not find canton with id: " + cantonId);
        } else {
            return new PlausiErrorListResult(plausiErrors);
        }
    }

    protected boolean existsDeliveryForVersion(Long version, Long canton) {
        return _configDeliveryServiceProvider.getConfigDeliveriesByVersionAndCanton(version, canton).size() > 0;
    }

    protected boolean existsCantonWithConfigDeliveryAndSchoolForVersion(Long version, Long canton) {
        Canton c = _cantonServiceProvider.getCantonWithConfigDeliveryAndSchoolByMaxVersion(version, canton);
        return c != null && c.getVersion().equals(version);
    }

    protected String checkInitVersion(Long version, Long canton, String message) {
        if (existsCantonWithConfigDeliveryAndSchoolForVersion(version, canton)) {
            return message;
        }

        Canton cantonTemplate = _cantonServiceProvider.getCantonWithConfigDeliveryAndSchoolByMaxVersion(version - 1L, canton);
        if (cantonTemplate != null) {
            if (existsDeliveryForVersion(version, canton)) {
                return INIT_DELIVERY_EXISTS_MESSAGE;
            }
        }

        return null;
    }

    protected String initVersion(Long version, Long canton, List<BurSchool> burSchools, String message) {
        Canton newCanton;
        List<Canton> cantons = _cantonServiceProvider.getCantons(version, canton);
        if (cantons.size() == 0) {
            newCanton = new Canton();
            newCanton.setVersion(version);
            newCanton.setCanton(canton);
            newCanton.setCreation_date(new Date());
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            newCanton.setCreation_user(user.getEmail());
            newCanton = _cantonServiceProvider.insertCanton(newCanton);
        } else {
            // only one canton should exist
            newCanton = cantons.get(0);
        }

        Long versionTemplate = 0L;
        Canton cantonTemplate = _cantonServiceProvider.getCantonWithConfigDeliveryAndSchoolByMaxVersion(version - 1L, canton);
        if (cantonTemplate != null) {
            if (existsDeliveryForVersion(version, canton)) {
                return INIT_DELIVERY_EXISTS_MESSAGE;
            }
            versionTemplate = cantonTemplate.getVersion();
            _configDeliveryServiceProvider.copyConfigDeliveries(version, versionTemplate, canton);
        }

        for (BurSchool burSchool : burSchools) {
            burSchool.setVersion(version);
            if (_burSchoolServiceProvider.isActiveSchool(burSchool)) {
                _burSchoolServiceProvider.initBurSchool(burSchool, version, versionTemplate, canton);
            }
        }

        _cantonServiceProvider.initVersion(newCanton);

        return null;
    }

    @Transactional(timeout = 1800)
    public synchronized CantonListResult initVersion(Long version, Long canton, boolean noSync) {
        List<CodeGroup> cantons = canton >= 0L ? null : _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.CANTON, "de", true);

        if (canton >= 0L) {
            String message = checkInitVersion(version, canton, INIT_ALREADY_DONE_1_MESSAGE);
            if (message != null) {
                return new CantonListResult(message);
            }
        } else {
            for (CodeGroup cantonCodeGroup : cantons) {
                String message = checkInitVersion(version, cantonCodeGroup.getCode(), INIT_ALREADY_DONE_2_MESSAGE);
                if (message != null) {
                    return new CantonListResult(message);
                }
            }
        }

        if (!noSync) {
            // synchronize bur schools
            BurSchoolListResult burSchoolListResult = _burSchoolService.synchronizeSchools();
            if (burSchoolListResult.getMessage() != null && !burSchoolListResult.getMessage().equals("")) {
                return new CantonListResult(burSchoolListResult.getMessage());
            }
        }

        List<BurSchool> burSchools = new ArrayList<BurSchool>();
        if (canton >= 0L) {
            Canton cantonTemplate = _cantonServiceProvider.getCantonWithConfigDeliveryAndSchoolByMaxVersion(version - 1L, canton);
            if (cantonTemplate != null) {
                burSchools = _burSchoolServiceProvider.getBurSchoolsOfConfigDeliveries(cantonTemplate.getVersion(), canton);
            }
            for (BurSchool burSchool : _burSchoolServiceProvider.getBurSchools()) {
                if (!canton.equals(burSchool.getCanton())) {
                    continue;
                }
                if (!burSchools.contains(burSchool)) {
                    burSchools.add(burSchool);
                }
            }
        } else {
            burSchools = _burSchoolServiceProvider.getBurSchools();
        }

        if (!noSync) {
            // check, if some bur schools are out of synch
            for (BurSchool burSchool : burSchools) {
                _burSchoolServiceProvider.initSynchData(burSchool);
                if (burSchool.getSynchStatusBur() != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED
                        && _burSchoolServiceProvider.isVisibleSchool(burSchool, version)) {
                    throw new MebUncheckedNotMonitoredException(INIT_BUR_NOT_SYNCHRON_MESSAGE);
                }
            }
        }

        // do init version
        if (canton >= 0L) {
            String message = initVersion(version, canton, burSchools, INIT_ALREADY_DONE_1_MESSAGE);
            if (message != null) {
                throw new MebUncheckedNotMonitoredException(message);
            }
        } else {
            for (CodeGroup cantonCodeGroup : cantons) {
                String message = initVersion(version, cantonCodeGroup.getCode(), burSchools, INIT_ALREADY_DONE_2_MESSAGE);
                if (message != null) {
                    throw new MebUncheckedNotMonitoredException(message);
                }
            }
        }

        return getCantons(version, canton);
    }

    @Transactional(timeout = 600)
    public CantonResult validateCanton(Canton canton, boolean undo, String locale) {
        return _cantonServiceProvider.validateCanton(canton, undo, locale);
    }

    @Transactional(timeout = 600)
    public CantonResult finalizeCanton(Canton canton, boolean undo) {
        return _cantonServiceProvider.finalizeCanton(canton, undo);
    }

    @Transactional
    public CantonResult updateCanton(Canton canton, List<PlausiError> plausiErrors) {
        String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        canton.setModification_user(userEmail);
        canton.setModification_date(new Date());
        return new CantonResult(_cantonServiceProvider.updateCanton(canton, plausiErrors));
    }

    @Transactional
    public synchronized CantonResult insertCanton(Canton canton) {
        List<Canton> cantons = _cantonServiceProvider.getCantons(canton.getVersion(), canton.getCanton());
        if (cantons.size() > 0) {
            return new CantonResult(CANTON_ALREADY_EXISTS_MESSAGE);
        }
        String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        canton.setCreation_user(userEmail);
        canton.setCreation_date(new Date());
        canton.setModification_user(userEmail);
        canton.setModification_date(new Date());
        return new CantonResult(_cantonServiceProvider.insertCanton(canton));
    }

    @Transactional
    public CantonResult deleteCanton(Canton canton) {
        if (existsDeliveryForVersion(canton.getVersion(), canton.getCanton())) {
            return new CantonResult(DELETE_DELIVERY_EXISTS_MESSAGE);
        }

        _cantonServiceProvider.deleteCanton(canton);
        return new CantonResult();
    }

    @Transactional(readOnly = true)
    public Long getInitialVersion() {
        return _cantonServiceProvider.getInitialVersion();
    }

    @Transactional(readOnly = true)
    public List<Long> getFilterCantonsForActUser() {
        return _cantonServiceProvider.getFilterCantonsForActUser();
    }

    public CantonResult createPlausireport(Canton canton) {
        return _cantonServiceProvider.createPlausireport(canton);
    }

    public FileResult getLastPlausireport(Long cantonId, String locale) {
        return _cantonServiceProvider.getLastPlausireport(cantonId, locale);
    }
}
