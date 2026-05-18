package ch.bfs.meb.sbg.web.frontend.controller;
/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgweb

  $Id: PageController.java 630 2010-02-04 09:28:02Z jfu $
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Beispielcontroller fuer Testzwecke
 * 
 */
@Controller
@EnableWebMvc
public class PageController {
    @RequestMapping("/index.page")
    public void mainHandler() {}

    @RequestMapping("/deliver.page")
    public void deliverHandler() {}

    @RequestMapping("/track.page")
    public void trackHandler() {}

    @RequestMapping("/maintain.page")
    public void maintainHandler() {}

    @RequestMapping("/admin.page")
    public void adminHandler() {}

    //	@RequestMapping("/user.page")
    //	public ModelMap userHandler()
    //	{
    //		MebUser user = (MebUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //		ModelMap values = new ModelMap("user", user.getUsername());
    //		values.addAttribute("canton", user.getCantonsAsString());
    //		return values;
    //	}
}
