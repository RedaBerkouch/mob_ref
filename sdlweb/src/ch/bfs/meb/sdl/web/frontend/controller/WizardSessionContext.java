package ch.bfs.meb.sdl.web.frontend.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WizardSessionContext {

    private String selectedDlUser;

    public String getSelectedDlUser() {
        return selectedDlUser;
    }

    public void setSelectedDlUser(String selectedDlUser) {
        this.selectedDlUser = selectedDlUser;
    }

    public void clear() {
        this.selectedDlUser = null;
    }
}
