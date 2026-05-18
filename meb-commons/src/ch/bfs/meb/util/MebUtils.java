/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.util;

/**
 * Different helpers.
 * 
 * @author  $Author$ 
 * @version $Revision$ 
 */
public final class MebUtils {
    /**
     * Returns whether two objects are equal or not considering also null values.
     *
     * @param o1 first object
     * @param o2 second object
     * @return true if objects are equal, else false
     */
    public static boolean areEqual(Object o1, Object o2) {
        return ((o1 == null && o2 == null) || (o1 != null && o1.equals(o2)));
    }

    /**
     * Indicates if given configure string contains the given user email.
     *
     * @param configureString colon-separated string containing email addresses
     * @param userEmail user email to check
     * @return <code>true</code> if the string contains the email, <code>false</code> otherwise
     */
    public static boolean isUserEmailConfigured(String configureString, String userEmail) {
        userEmail = userEmail.toLowerCase();
        for (String user : configureString.toLowerCase().split(";")) {
            if (userEmail.equals(user)) {
                return true;
            }
        }
        return false;
    }

    public static String getMebSupportMailAddress() {
        return "meb-support@bfs.admin.ch";
    }

    public static String getDeliveryToBeLoadedMessage() {
        return "delivery.tobeloaded";
    }

    public static String getDeliveryQueuedMessage() {
        return "delivery.queued";
    }
}
