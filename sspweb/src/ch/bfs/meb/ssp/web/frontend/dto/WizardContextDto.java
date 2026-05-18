package ch.bfs.meb.ssp.web.frontend.dto;

import java.util.List;

public class WizardContextDto {

    private String dlUser;
    private Long version;
    private boolean allSchoolsDelivered;
    private boolean deliveriesValidated;
    private boolean deliveriesValidatedConflict;

    private int nrOfConfirmableErrors;
    private int nrOfNonConfirmableErrors;
    private int nrOfConfirmedErrors;

    // ✅ Totaux legacy (WizardNrOfColumn + WS)
    private long totalPersons;
    private int totalQualifications;

    // ✅ DL user combo (SBA_DV)
    private List<String> availableDlUsers;

    public WizardContextDto() {
    }

    public WizardContextDto(String dlUser,
                            Long version,
                            boolean allSchoolsDelivered,
                            boolean deliveriesValidated,
                            boolean deliveriesValidatedConflict,
                            int nrOfConfirmableErrors,
                            int nrOfNonConfirmableErrors) {
        this.dlUser = dlUser;
        this.version = version;
        this.allSchoolsDelivered = allSchoolsDelivered;
        this.deliveriesValidated = deliveriesValidated;
        this.deliveriesValidatedConflict = deliveriesValidatedConflict;
        this.nrOfConfirmableErrors = nrOfConfirmableErrors;
        this.nrOfNonConfirmableErrors = nrOfNonConfirmableErrors;
    }

    /* ===============================
       GETTERS / SETTERS
       =============================== */

    public String getDlUser() {
        return dlUser;
    }

    public void setDlUser(String dlUser) {
        this.dlUser = dlUser;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isAllSchoolsDelivered() {
        return allSchoolsDelivered;
    }

    public void setAllSchoolsDelivered(boolean allSchoolsDelivered) {
        this.allSchoolsDelivered = allSchoolsDelivered;
    }

    public boolean isDeliveriesValidated() {
        return deliveriesValidated;
    }

    public void setDeliveriesValidated(boolean deliveriesValidated) {
        this.deliveriesValidated = deliveriesValidated;
    }

    public boolean isDeliveriesValidatedConflict() {
        return deliveriesValidatedConflict;
    }

    public void setDeliveriesValidatedConflict(boolean deliveriesValidatedConflict) {
        this.deliveriesValidatedConflict = deliveriesValidatedConflict;
    }

    public int getNrOfConfirmableErrors() {
        return nrOfConfirmableErrors;
    }

    public void setNrOfConfirmableErrors(int nrOfConfirmableErrors) {
        this.nrOfConfirmableErrors = nrOfConfirmableErrors;
    }

    public int getNrOfNonConfirmableErrors() {
        return nrOfNonConfirmableErrors;
    }

    public void setNrOfNonConfirmableErrors(int nrOfNonConfirmableErrors) {
        this.nrOfNonConfirmableErrors = nrOfNonConfirmableErrors;
    }

    public int getNrOfConfirmedErrors() {
        return nrOfConfirmedErrors;
    }

    public void setNrOfConfirmedErrors(int nrOfConfirmedErrors) {
        this.nrOfConfirmedErrors = nrOfConfirmedErrors;
    }

    /* ===============================
       TOTAL PERSONS / QUALIFICATIONS
       =============================== */

    public long getTotalPersons() {
        return totalPersons;
    }

    public void setTotalPersons(long totalPersons) {
        this.totalPersons = totalPersons;
    }

    public int getTotalQualifications() {
        return totalQualifications;
    }

    public void setTotalQualifications(int totalQualifications) {
        this.totalQualifications = totalQualifications;
    }

    /* ===============================
       DL USERS (combo)
       =============================== */

    public List<String> getAvailableDlUsers() {
        return availableDlUsers;
    }

    public void setAvailableDlUsers(List<String> availableDlUsers) {
        this.availableDlUsers = availableDlUsers;
    }
}
