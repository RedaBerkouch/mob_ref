package ch.bfs.meb.server.commons.integration.dto;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "CODEGROUPS")
@GenericGenerator(name = "codegroupseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "MEBSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class CodeGroup {
    private Long _id;
    private String _codeGroupId;
    private Long _code;
    private String _language;
    private String _codeTextAbbr;
    private String _codeText;
    private Integer _validFromYear;
    private Integer _validToYear;
    private Long _canton;

    /*
     * Getter and setter
     */

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "codegroupseqgen")
    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    @Column
    public String getCodeGroupId() {
        return _codeGroupId;
    }

    public void setCodeGroupId(String codeGroupId) {
        _codeGroupId = codeGroupId;
    }

    @Column
    public Long getCode() {
        return _code;
    }

    public void setCode(Long code) {
        _code = code;
    }

    @Column
    public String getLanguage() {
        return _language;
    }

    public void setLanguage(String language) {
        _language = language;
    }

    @Column
    public String getCodeTextAbbr() {
        return _codeTextAbbr;
    }

    public void setCodeTextAbbr(String codeTextAbbr) {
        _codeTextAbbr = codeTextAbbr;
    }

    @Column
    public String getCodeText() {
        return _codeText;
    }

    public void setCodeText(String codeText) {
        _codeText = codeText;
    }

    @Column
    public Integer getValidFromYear() {
        return _validFromYear;
    }

    public void setValidFromYear(Integer validFromYear) {
        _validFromYear = validFromYear;
    }

    @Column
    public Integer getValidToYear() {
        return _validToYear;
    }

    public void setValidToYear(Integer validToYear) {
        _validToYear = validToYear;
    }

    @Column
    public Long getCanton() {
        return _canton;
    }

    public void setCanton(Long canton) {
        _canton = canton;
    }
}