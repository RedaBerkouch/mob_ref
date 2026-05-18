/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: IDeliveryService.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */

package ch.bfs.meb.sbg.web.service;

import java.util.List;

import ch.bfs.meb.sbg.web.ws.sbgdelivery.DeliveryResult;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.PlausireportResult;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDelivery;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDeliveryListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Definition of the delivery interface.
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
 */
public interface IDeliveryService {
    /**
     * Amends the results of a file upload to an existing delivery. The upload
     * data is read from the action table.
     *
     * @param aDelivery Existing delivery
     * @param locale    language
     * @return ResultBase.OK if the data is validated and stored in the database
     */
    public DeliveryResult amendDelivery(SbgDelivery aDelivery, String locale);

    /**
     * Replaces the results of an existing delivery with the file upload data.
     * The upload data is read from the action table.
     *
     * @param aDelivery Existing delivery
     * @param locale    language
     * @return ResultBase.OK if the data is validated and stored in the database
     */
    public DeliveryResult replaceDelivery(SbgDelivery aDelivery, String locale);

    /**
     * Confirms the pending delivery. The state of the delivery is set to
     * delivered.
     *
     * @param aDelivery Pending delivery
     * @param locale    language
     * @return ResultBase.OK if the data is validated and stored in the database
     */
    public DeliveryResult confirmDelivery(SbgDelivery aDelivery, String locale);

    /**
     * Cancels the pending delivery. The state of the delivery is recalculated.
     *
     * @param aDelivery Pending delivery
     * @return ResultBase.OK if the data is validated and stored in the database
     */
    public DeliveryResult cancelDelivery(SbgDelivery aDelivery);

    /**
     * Validates possible persons and the delivery. Generates validation report.
     *
     * @param aDelivery Delivery
     * @return ResultBase.OK if no error occured during validation
     */
    public DeliveryResult validateDelivery(SbgDelivery aDelivery);

    /**
     * UnValidates possible persons and the delivery. Generates validation report.
     *
     * @param aDelivery Delivery
     * @return ResultBase.OK if no error occured during validation
     */
    public DeliveryResult unvalidateDelivery(SbgDelivery aDelivery);

    /**
     * Finalizes the delivery. If undo is true, finalization is undone.
     *
     * @param aDelivery Delivery
     * @param undo      If true, finalization is undone
     * @return ResultBase.OK if no error occured during finalization
     */
    public DeliveryResult finalizeDelivery(SbgDelivery aDelivery, boolean undo);

    /**
     * Update the delivery.
     *
     * @param aDelivery Delivery
     * @return ResultBase.OK if no error occured during update
     */
    public DeliveryResult updateDelivery(SbgDelivery aDelivery);

    /**
     * Delete the delivery.
     *
     * @param aDelivery Delivery
     * @return ResultBase.OK if no error occured during deletion
     */
    public DeliveryResult deleteDelivery(SbgDelivery aDelivery);

    /**
     * @return List of all the deliveries from the database. The delivery
     * objects also contain cumulated information about the validation
     * state of associated persons and events.
     */
    public SbgDeliveryListResult getDeliveries();

    /**
     * @param filterContext Context with filters.
     * @return List of deliveries from the database, with filters from
     * filterContext applied. The delivery objects also contain
     * cumulated information about the validation state of associated
     * persons and events.
     */
    public SbgDeliveryListResult getFilteredDeliveries(WebFilterContext filterContext);

    /**
     * Gets the last plausi report for the given delivery.
     *
     * @param aDeliveryId delivery to find plausi report for
     * @return ResultBase.OK if the service terminated without error.
     */
    public PlausireportResult getLastPlausiReport(Long aDeliveryId, String locale);

    /**
     * Executes plausis and creates plausi report.
     *
     * @param aDelivery delivery to create plausi report for
     * @return ResultBase.OK if the service terminated without error.
     */
    public DeliveryResult createPlausiReport(SbgDelivery aDelivery);

    /**
     * Evaluate Canton(s) for actual user
     *
     * @return canotn(s) for actual user
     */
    public List<Long> getFilterCantonsForActUser(IWebLocalizationManager localizationManager);

    public DeliveryResult refreshStatus(SbgDelivery delivery);

    public DeliveryResult getDeliveryByCantonByVersion(Long canton, Long version);

    public SbgDelivery saveDelivry(SbgDelivery deliveryResult);
}