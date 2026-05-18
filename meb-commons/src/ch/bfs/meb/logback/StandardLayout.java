/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: StandardLayout.java  01.03.2010 15:21:26 jfu $

 */
package ch.bfs.meb.logback;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class StandardLayout extends PatternLayout {
    @Override
    public String doLayout(ILoggingEvent event) {
        String username = "SYSTEM";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof MebUser) {
                MebUser details = (MebUser) authentication.getPrincipal();
                if (details != null) {
                    username = details.getEmail() + " (" + details.getUsername() + ")";
                }
            }
        }

        getContext().putProperty("username", username);

        return super.doLayout(event);
    }
}
