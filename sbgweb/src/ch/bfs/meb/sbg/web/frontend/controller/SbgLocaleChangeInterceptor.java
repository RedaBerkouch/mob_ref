/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgweb

  $Id: SbgLocaleChangeInterceptor.java  15.11.2012 15:04:23 Administrator $

 */
package ch.bfs.meb.sbg.web.frontend.controller;

import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

public class SbgLocaleChangeInterceptor extends LocaleChangeInterceptor {
    public SbgLocaleChangeInterceptor() {}

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
        String newLocale = request.getParameter(getParamName());

        if (newLocale != null && (newLocale.toLowerCase().startsWith("de") || newLocale.toLowerCase().startsWith("fr"))) {
            return super.preHandle(request, response, handler);
        } else {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver != null) {
                String lang = localeResolver.resolveLocale(request).getLanguage().toLowerCase();
                if (!lang.startsWith("de") && !lang.startsWith("fr")) {
                    LocaleEditor localeEditor = new LocaleEditor();
                    localeEditor.setAsText("de");
                    localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
                }
            }
        }

        return true;
    }
}
