/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.sdl.web.frontend.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.bfs.meb.security.MebUser;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Beispielcontroller fuer Testzwecke
 * 
 */
@Controller
@EnableWebMvc
public class PageController {

    @RequestMapping("/index.page")
    public void indexHandler() {}

    @RequestMapping("/init.page")
    public void initHandler() {}

    @RequestMapping("/maintain.page")
    public void maintainHandler() {}

    @RequestMapping("/admin.page")
    public void adminHandler() {}

    @RequestMapping("/upload.page")
    public void uploadHandler() {}

    @RequestMapping("/deliver.page")
    public void deliverHandler() {}

    @RequestMapping("/dlwizard.page")
    public ModelMap dlwizardHandler(@RequestParam("saveNr") Integer saveNr) {
        return new ModelMap("saveNr", saveNr);
    }

    @RequestMapping("/dlconfirm.page")
    public ModelMap dlconfirmHandler(@RequestParam("saveNr") Integer saveNr) {
        return new ModelMap("saveNr", saveNr);
    }

    @RequestMapping("/user.page")
    public ModelMap userHandler() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelMap values = new ModelMap("user", user.getEmail());
        values.addAttribute("canton", user.getCantonsAsString());
        return values;
    }
}
