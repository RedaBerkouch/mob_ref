/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.ToString;

@ToString
public class ExportUser {
    private User user = null;
    private Long canton = null;
    private long role;
    private String deliveries = "";
    private Long minDeliveryStatus = CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED;

    public ExportUser(User user, Long canton, long role) {
        this(user, role);
        this.canton = canton;

    }

    public ExportUser(User user, long role) {
        this.user = user;
        this.role = role;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the canton
     */
    public Long getCanton() {
        return canton;
    }

    /**
     * @return the role
     */
    public long getRole() {
        return role;
    }

    /**
     * @return the deliveries
     */
    public String getDeliveries() {
        return deliveries;
    }

    /**
     * @param deliveries the deliveries to set
     */
    public void setDeliveries(String deliveries) {
        this.deliveries = deliveries;
    }

    /**
     * @param deliveryCodes the delivery codes to set
     */
    public void setDeliveries(List<String> deliveryCodes) {
        String deliveries = "";
        for (String code : deliveryCodes) {
            if (deliveries.length() > 0) {
                deliveries += ",";
            }
            deliveries += code;
        }
        this.deliveries = deliveries;
    }

    /**
     * @return the minDeliveryStatus (null if no Delivery in MEB)
     */
    public Long getMinDeliveryStatus() {
        return minDeliveryStatus;
    }

    /**
     * @param minDeliveryStatus the minDeliveryStatus to set
     */
    public void setMinDeliveryStatus(Long minDeliveryStatus) {
        this.minDeliveryStatus = minDeliveryStatus;
    }

}
