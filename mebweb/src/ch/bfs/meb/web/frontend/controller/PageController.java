/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

  $Id: PageController.java 630 2010-02-04 09:28:02Z jfu $
 */
package ch.bfs.meb.web.frontend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.bfs.meb.web.service.IMonitoringService;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Beispielcontroller fuer Testzwecke
 * 
 */
@Controller
@EnableWebMvc
public class PageController {
    @Autowired
    IMonitoringService monitoringService;

    @RequestMapping("/index.page")
    public void indexHandler() {}

    @RequestMapping("/monitoring.page")
    public ModelMap monitoringHandler() {
        ModelMap values = new ModelMap();

        // IDM
        long start = System.currentTimeMillis();
        values.addAttribute("IDM", monitoringService.checkIdmService());
        long stop = System.currentTimeMillis();
        values.addAttribute("IDM_DURATION", stop - start);

        // SAS
        start = System.currentTimeMillis();
        values.addAttribute("SAS", monitoringService.checkSasService());
        stop = System.currentTimeMillis();
        values.addAttribute("SAS_DURATION", stop - start);

        // METASTAT
        start = System.currentTimeMillis();
        values.addAttribute("METASTAT", monitoringService.checkMetastatService());
        stop = System.currentTimeMillis();
        values.addAttribute("METASTAT_DURATION", stop - start);

        // BUR
        start = System.currentTimeMillis();
        values.addAttribute("BUR", monitoringService.checkBurService());
        stop = System.currentTimeMillis();
        values.addAttribute("BUR_DURATION", stop - start);

        // DB
        start = System.currentTimeMillis();
        values.addAttribute("DB", monitoringService.checkDatabase());
        stop = System.currentTimeMillis();
        values.addAttribute("DB_DURATION", stop - start);

        return values;
    }
}