package ch.bfs.meb.ssp.web.frontend.dto;

public class WizardUserContext {

    private String dlUser;
    private Long version;

    public WizardUserContext(String dlUser, Long version) {
        this.dlUser = dlUser;
        this.version = version;
    }

    public String getDlUser() {
        return dlUser;
    }

    public Long getVersion() {
        return version;
    }
}

