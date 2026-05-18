/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: FilterServiceImpl.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.util.CodegroupUtility;

@Service
public class ConfigDeliveryServiceImpl extends FilteredObjectsServiceBase implements IConfigDeliveryService {
    protected static final String CONFIG_DELIVERY_ONLY_ONE_DEFAULT_MESSAGE = "configdelivery.onlyonedefault.message";
    protected static final String CONFIG_DELIVERY_UNIQUE_CODE_MESSAGE = "configdelivery.uniquecode.message";
    protected static final String CONFIG_DELIVERY_CANTON_EMPTY_MESSAGE = "configdelivery.cantonempty.message";
    protected static final String CONFIG_DELIVERY_ID_EMPTY_MESSAGE = "configdelivery.idempty.message";

    protected IConfigDeliveryServiceProvider _configDeliveryServiceProvider;

    public void setConfigDeliveryServiceProvider(IConfigDeliveryServiceProvider configDeliveryServiceProvider) {
        _configDeliveryServiceProvider = configDeliveryServiceProvider;
    }

    @Transactional(readOnly = true)
    public ConfigDeliveryListResult getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version,
            Long canton) {
        if (filterContext == null) {
            filterContext = new FilterContext();
            filterContext.setLocale(sortContext.getLocale());
            List<Filter> activeFilters = _filterServiceProvider.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_CONFIGDELIVERY);
            for (Filter filter : activeFilters) {
                if (filter.getIsDefault()) {
                    filterContext.getFilter().add(filter);
                }
            }
        }

        completeFilterParams(filterContext);

        return new ConfigDeliveryListResult(_configDeliveryServiceProvider.getConfigDeliveries(start, buffer, sortContext, filterContext, version, canton),
                _configDeliveryServiceProvider.getMaxNrOfConfigDeliveries(filterContext, version, canton));
    }

    @Transactional(readOnly = true)
    public ConfigDeliveryListResult getConfigDeliveriesOwnedBySchools(List<Long> selectedSchoolIds, SortContext sortContext, Long version) {
        List<ConfigDelivery> configDeliveries = _configDeliveryServiceProvider.getConfigDeliveriesOwnedBySchools(selectedSchoolIds, sortContext, version);
        return new ConfigDeliveryListResult(configDeliveries, new Long(configDeliveries.size()));
    }

    @Transactional(readOnly = true)
    public ConfigDeliveryResult getConfigDeliveryById(Long configDeliveryId) {
        ConfigDelivery configDelivery = _configDeliveryServiceProvider.getConfigDeliveryById(configDeliveryId);
        if (configDelivery == null) {
            return new ConfigDeliveryResult("Could not find config delivery with id: " + configDeliveryId);
        } else {
            return new ConfigDeliveryResult(configDelivery);
        }
    }

    protected String checkConfigDelivery(ConfigDelivery configDelivery) {
        if (configDelivery.getCanton() == null || configDelivery.getCanton() <= 0L) {
            return CONFIG_DELIVERY_CANTON_EMPTY_MESSAGE;
        }
        if (configDelivery.getDeliveryCode() == null || configDelivery.getDeliveryCode().trim().equals("")) {
            return CONFIG_DELIVERY_ID_EMPTY_MESSAGE;
        }

        List<ConfigDelivery> deliveries = _configDeliveryServiceProvider.getConfigDeliveriesByVersionAndCanton(configDelivery.getVersion(),
                configDelivery.getCanton());
        for (ConfigDelivery delivery : deliveries) {
            if (configDelivery.getDeliveryId() == null || !configDelivery.getDeliveryId().equals(delivery.getDeliveryId())) {
                if (configDelivery.getIsDefault() && delivery.getIsDefault()) {
                    return CONFIG_DELIVERY_ONLY_ONE_DEFAULT_MESSAGE;
                }
                if (configDelivery.getDeliveryCode().equals(delivery.getDeliveryCode())) {
                    return CONFIG_DELIVERY_UNIQUE_CODE_MESSAGE;
                }
            }
        }
        return null;
    }

    @Transactional
    public ConfigDeliveryResult updateConfigDelivery(ConfigDelivery configDelivery) {
        String message = checkConfigDelivery(configDelivery);
        if (message != null) {
            return new ConfigDeliveryResult(message);
        }

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        configDelivery.setModification_user(user.getEmail());
        configDelivery.setModification_date(new Date());

        return new ConfigDeliveryResult(_configDeliveryServiceProvider.updateConfigDelivery(configDelivery));
    }

    @Transactional
    public ConfigDeliveryResult insertConfigDelivery(ConfigDelivery configDelivery) {
        String message = checkConfigDelivery(configDelivery);
        if (message != null) {
            return new ConfigDeliveryResult(message);
        }

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        configDelivery.setCreation_user(user.getEmail());
        configDelivery.setCreation_date(new Date());
        configDelivery.setModification_user(user.getEmail());
        configDelivery.setModification_date(new Date());

        return new ConfigDeliveryResult(_configDeliveryServiceProvider.insertConfigDelivery(configDelivery));
    }

    @Transactional
    public ConfigDeliveryResult deleteConfigDelivery(ConfigDelivery configDelivery) {
        _configDeliveryServiceProvider.deleteConfigDelivery(configDelivery);
        return new ConfigDeliveryResult();
    }
}
