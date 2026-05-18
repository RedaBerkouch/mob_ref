/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-common

  $Id: ApplicationContextProvider.java 980 2010-03-10 07:52:24Z dzw $

 */
package ch.bfs.meb.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Stellt den Spring application context zur Verfuegung
 * 
 * @author $Author: dzw $
 * @version $Revision: 980 $
 */
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // Assign the ApplicationContext into a static variable
        ApplicationContextProvider.applicationContext = applicationContext;
    }
}