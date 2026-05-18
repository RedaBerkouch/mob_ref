/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.sba.server.integration.dto.SbaConfigDelivery;
import ch.bfs.meb.sba.server.integration.repository.IConfigDeliveryRepository;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.integration.dto.ConfigDelivery;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.impl.IConfigDeliveryServiceProvider;
import ch.bfs.meb.util.SecurityConstants;

public class ConfigDeliveryServiceProvider implements IConfigDeliveryServiceProvider {
    private final String USER_TEXT_DELIMITER = ";";

    private IConfigDeliveryRepository _configDeliveryRepository;
    private IIdmUserService _idmService;

    public void setConfigDeliveryRepository(IConfigDeliveryRepository configDeliveryRepository) {
        _configDeliveryRepository = configDeliveryRepository;
    }

    public void setIdmService(IIdmUserService idmService) {
        _idmService = idmService;
    }

    @Override
    public ConfigDelivery getConfigDeliveryById(Long configDeliveryId) {
        return _configDeliveryRepository.getConfigDeliveryById(configDeliveryId);
    }

    @Override
    public ConfigDelivery getConfigDeliveryByCodeVersionAndCanton(String deliveryCode, Long version, Long canton) {
        return _configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(deliveryCode, version, canton);
    }

    @Override
    public List<ConfigDelivery> getConfigDeliveriesByCodeAndVersion(String deliveryCode, Long version) {
        return Collections.unmodifiableList(_configDeliveryRepository.getConfigDeliveriesByCodeAndVersion(deliveryCode, version));
    }

    protected void initConfigDeliveryUsers(SbaConfigDelivery delivery) {
        boolean first = true;
        String dlUsers = "";
        int anz = 0;
        for (String user : delivery.getDl_users().split(USER_TEXT_DELIMITER)) {
            if (_idmService.isUserInRole(user, SecurityConstants.ROLE_SBA_DL, SecurityConstants.SBA_ROLE_HIERARCHY)) {
                if (!first) {
                    dlUsers += USER_TEXT_DELIMITER;
                    ++anz;
                }
                first = false;
                dlUsers += user;
            }
        }
        while (anz++ < 3) {
            dlUsers += USER_TEXT_DELIMITER;
        }

        first = true;
        String roUsers = "";
        anz = 0;
        for (String user : delivery.getRo_users().split(USER_TEXT_DELIMITER)) {
            if (_idmService.isUserInRole(user, SecurityConstants.ROLE_SBA_RO, SecurityConstants.SBA_ROLE_HIERARCHY)) {
                if (!first) {
                    roUsers += USER_TEXT_DELIMITER;
                    ++anz;
                }
                first = false;
                roUsers += user;
            }
        }
        while (anz++ < 3) {
            roUsers += USER_TEXT_DELIMITER;
        }

        delivery.setDl_users(dlUsers);
        delivery.setRo_users(roUsers);
    }

    @Override
    public void copyConfigDeliveries(Long newVersion, Long version, Long canton) {
        List<SbaConfigDelivery> deliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton);
        for (SbaConfigDelivery delivery : deliveries) {
            SbaConfigDelivery newDelivery = new SbaConfigDelivery(delivery);
            newDelivery.setDeliveryId(null);
            newDelivery.setVersion(newVersion);
            initConfigDeliveryUsers(newDelivery);
            newDelivery.setCreation_date(new Date());
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            newDelivery.setCreation_user(user.getEmail());
            newDelivery.setUserText("");
            _configDeliveryRepository.insertConfigDelivery(newDelivery);
        }
    }

    @Override
    public List<ConfigDelivery> getConfigDeliveriesByVersionAndCanton(Long version, Long canton) {
        return Collections.unmodifiableList(_configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton));
    }

    @Override
    public List<ConfigDelivery> getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return Collections.unmodifiableList(_configDeliveryRepository.getConfigDeliveries(start, buffer, sortContext, filterContext, version, canton));
    }

    @Override
    public Long getMaxNrOfConfigDeliveries(FilterContext filterContext, Long version, Long canton) {
        return _configDeliveryRepository.getMaxNrOfConfigDeliveries(filterContext, version, canton);
    }

    @Override
    public List<ConfigDelivery> getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long version) {
        return Collections.unmodifiableList(_configDeliveryRepository.getConfigDeliveriesOwnedBySchools(schoolIds, sortContext, version));
    }

    @Override
    public ConfigDelivery insertConfigDelivery(ConfigDelivery configDelivery) {
        return _configDeliveryRepository.insertConfigDelivery(new SbaConfigDelivery(configDelivery));
    }

    @Override
    public ConfigDelivery updateConfigDelivery(ConfigDelivery configDelivery) {
        SbaConfigDelivery delivery = new SbaConfigDelivery(configDelivery);
        SbaConfigDelivery origDelivery = _configDeliveryRepository.getConfigDeliveryById(delivery.getDeliveryId());
        delivery.setBurSchools(origDelivery.getBurSchools());
        if (!delivery.getDeliveryCode().equals(origDelivery.getDeliveryCode())) {
            // update changed configDeliveryCode in delivery, person and activity
            _configDeliveryRepository.updateConfigDeliveryCodes(delivery, origDelivery.getDeliveryCode());
        }
        return _configDeliveryRepository.updateConfigDelivery(delivery);
    }

    @Override
    public void deleteConfigDelivery(ConfigDelivery configDelivery) {
        _configDeliveryRepository.deleteConfigDelivery(new SbaConfigDelivery(configDelivery));
    }
}
