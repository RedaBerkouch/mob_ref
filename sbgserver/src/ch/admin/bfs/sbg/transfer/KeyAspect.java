package ch.admin.bfs.sbg.transfer;

public class KeyAspect implements java.io.Serializable {
    private static final long serialVersionUID = -1694910889565402983L;

    // Fields
    private Long keyAspectId;
    private Long keyAspectCode;
    private Long sbfiCode;
    private String text_de;
    private String text_fr;
    private Integer validFromYear;
    private Integer validToYear;

    // Constructors

    /** default constructor */
    public KeyAspect() {}

    /** full constructor */
    public KeyAspect(Long keyAspectCode, Long sbfiCode, String text_de, String text_fr, Integer validFromYear, Integer validToYear) {
        this.keyAspectCode = keyAspectCode;
        this.sbfiCode = sbfiCode;
        this.text_de = text_de;
        this.text_fr = text_fr;
        this.validFromYear = validFromYear;
        this.validToYear = validToYear;
    }

    // Property accessors
    public Long getKeyAspectId() {
        return this.keyAspectId;
    }

    public void setKeyAspectId(Long keyAspectId) {
        this.keyAspectId = keyAspectId;
    }

    public Long getKeyAspectCode() {
        return this.keyAspectCode;
    }

    public void setKeyAspectCode(Long keyAspect) {
        this.keyAspectCode = keyAspect;
    }

    public Long getSbfiCode() {
        return this.sbfiCode;
    }

    public void setSbfiCode(Long sbfiCode) {
        this.sbfiCode = sbfiCode;
    }

    public String getText_de() {
        return this.text_de;
    }

    public void setText_de(String text_de) {
        this.text_de = text_de;
    }

    public String getText_fr() {
        return this.text_fr;
    }

    public void setText_fr(String text_fr) {
        this.text_fr = text_fr;
    }

    public Integer getValidFromYear() {
        return validFromYear;
    }

    public void setValidFromYear(Integer validFromYear) {
        this.validFromYear = validFromYear;
    }

    public Integer getValidToYear() {
        return validToYear;
    }

    public void setValidToYear(Integer validToYear) {
        this.validToYear = validToYear;
    }

}
