package ch.bfs.meb.web.commons.security;

import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import ch.bfs.meb.security.MebAccessDeniedException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

public class AccessDeniedAdvice implements ThrowsAdvice {
    @Autowired
    private IWebLocalizationManager _localizationManager;

    public void afterThrowing(AccessDeniedException e) {
        throw new MebAccessDeniedException(_localizationManager.getMessage("no.authorization.message"));
    }
}