/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: DeliveryWebServiceFacade.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.*;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
 */
@Service("deliveryService")
public class DeliveryService implements IDeliveryService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Autowired
    private DozerBeanMapper _dozerBeanMapper;

    protected List<Long> _allCantons = null;

    protected FilterContext convertToFilterContext(WebFilterContext filterContext) {
        if (filterContext == null) {
            return null;
        }

        return _dozerBeanMapper.map(filterContext, FilterContext.class);
    }

    public DeliveryResult amendDelivery(SbgDelivery aDelivery, String locale) {
        return _webServiceClientFactory.getDeliveryWebService().amendDelivery(aDelivery, locale);
    }

    public DeliveryResult replaceDelivery(SbgDelivery aDelivery, String locale) {
        return _webServiceClientFactory.getDeliveryWebService().replaceDelivery(aDelivery, locale);
    }

    public DeliveryResult confirmDelivery(SbgDelivery aDelivery, String locale) {
        return _webServiceClientFactory.getDeliveryWebService().confirmDelivery(aDelivery, locale);
    }

    public DeliveryResult cancelDelivery(SbgDelivery aDelivery) {
        return _webServiceClientFactory.getDeliveryWebService().cancelDelivery(aDelivery);
    }

    public DeliveryResult validateDelivery(SbgDelivery aDelivery) {
        return _webServiceClientFactory.getDeliveryWebService().validateDelivery(aDelivery);
    }

    @Override
    public DeliveryResult unvalidateDelivery(SbgDelivery aDelivery) {
        return _webServiceClientFactory.getDeliveryWebService().unvalidateDelivery(aDelivery);
    }

    public DeliveryResult finalizeDelivery(SbgDelivery aDelivery, boolean undo) {
        return _webServiceClientFactory.getDeliveryWebService().finalizeDelivery(aDelivery, undo);
    }

    public DeliveryResult updateDelivery(SbgDelivery aDelivery) {
        return _webServiceClientFactory.getDeliveryWebService().updateDelivery(aDelivery);
    }

    public DeliveryResult deleteDelivery(SbgDelivery aDelivery) {
        return _webServiceClientFactory.getDeliveryWebService().deleteDelivery(aDelivery);
    }

    public SbgDeliveryListResult getDeliveries() {
        return _webServiceClientFactory.getDeliveryWebService().getDeliveries();
    }

    public SbgDeliveryListResult getFilteredDeliveries(WebFilterContext filterContext) {
        return _webServiceClientFactory.getDeliveryWebService().getFilteredDeliveries(convertToFilterContext(filterContext));
    }

    public PlausireportResult getLastPlausiReport(Long aDeliveryId, String locale) {
        return _webServiceClientFactory.getDeliveryWebService().getLastPlausiReport(aDeliveryId, locale);
    }

    public DeliveryResult createPlausiReport(SbgDelivery aDelivery) {
        return _webServiceClientFactory.getDeliveryWebService().createPlausiReport(aDelivery);
    }

    public List<Long> getFilterCantonsForActUser(IWebLocalizationManager localizationManager) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_EA) && !user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
            return user.getCantons();
        } else {
            synchronized (this) {
                if (_allCantons == null) {
                    if (!localizationManager.isCodegroupServiceInitialized()) {
                        List<Long> allCantons = new ArrayList<Long>();
                        allCantons.add(new Long(-1));
                        return allCantons;
                    }

                    _allCantons = new ArrayList<Long>();
                    _allCantons.add(new Long(-1));
                    for (ILocalizedCode code : localizationManager.getCodeGroupAllValues(CodegroupUtility.CANTON, true)) {
                        _allCantons.add(code.getKey());
                    }
                }
            }
            return _allCantons;
        }
    }

    public DeliveryResult refreshStatus(SbgDelivery delivery) {
        return _webServiceClientFactory.getDeliveryWebService().refreshStatus(delivery);
    }

    @Override
    public DeliveryResult getDeliveryByCantonByVersion(Long canton, Long version) {
        return _webServiceClientFactory.getDeliveryWebService().getDeliveryByCantonByVersion(canton, version);
    }

    @Override
    public SbgDelivery saveDelivry(SbgDelivery deliveryResult) {
        return _webServiceClientFactory.getDeliveryWebService().saveNewDelivry(deliveryResult);
    }
}
