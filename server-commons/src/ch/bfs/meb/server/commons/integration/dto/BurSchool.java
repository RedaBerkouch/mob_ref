package ch.bfs.meb.server.commons.integration.dto;

import javax.persistence.*;

import lombok.Data;
import lombok.ToString;

/** Data Transfer Object for the school data table. */
@Data
@MappedSuperclass
@ToString(of = { "schoolId", "burNr", "canton", "version", "bur_canton", "cantonBur" })
public class BurSchool {
    @Id
    @Column(name = "SCHOOLID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schoolseqgen")
    private Long schoolId;
    @Column
    private Long burNr;
    @Column
    private Long canton;
    @Column
    private Long bur_canton;
    @Column
    private String label;
    @Column
    private String bur_label;
    @Column
    private Long municipality;
    @Column
    private Long bur_municipality;
    @Column
    private Long activityStatus;
    @Column
    private Long bur_activityStatus;
    @Column
    private Long synchStatus_sdl;
    @Column
    private String cantonalCode_sdl;
    @Column
    private String bur_cantonalCode_sdl;
    @Column
    private boolean is_sdl;
    @Column
    private boolean bur_is_sdl;
    @Column
    private Long validFrom_sdl_ssp;
    @Column
    private Long bur_validFrom_sdl_ssp;
    @Column
    private Long validTo_sdl_ssp;
    @Column
    private Long bur_validTo_sdl_ssp;
    @Column
    private Long validFrom_ssp;
    @Column
    private Long bur_validFrom_ssp;
    @Column
    private Long validTo_ssp;
    @Column
    private Long bur_validTo_ssp;
    @Column
    private Long synchStatus_ssp;
    @Column
    private String cantonalCode_ssp;
    @Column
    private String bur_cantonalCode_ssp;
    @Column
    private boolean is_ssp;
    @Column
    private boolean bur_is_ssp;
    @Column
    private Long synchStatus_sba;
    @Column
    private String cantonalCode_sba;
    @Column
    private String bur_cantonalCode_sba;
    @Column
    private boolean is_sba;
    @Column
    private boolean bur_is_sba;
    @Column
    private Long validFrom_sba;
    @Column
    private Long bur_validFrom_sba;
    @Column
    private Long validTo_sba;
    @Column
    private Long bur_validTo_sba;
    @Column
    private Long char_publ_flg;
    @Column
    private Long bur_char_publ_flg;
    @Column
    private Long char_priv_sub_flg;
    @Column
    private Long bur_char_priv_sub_flg;
    @Column
    private Long char_priv_no_sub_flg;
    @Column
    private Long bur_char_priv_no_sub_flg;
    @Column(name = "INST_TYP_BLP_FLG")
    private Boolean isSpecialSchool;
    @Column(name = "BUR_INST_TYP_BLP_FLG")
    private Boolean isSpecialSchoolBur;
    @Column
    private String userText;

    @Transient
    private String deliveryCode;
    @Transient
    private Long deliveryId;
    @Transient
    private Long version;

    @Transient
    private Long synchStatusBur;
    @Transient
    private String nameBur;
    @Transient
    private Long cantonBur;
    @Transient
    private Long municipalityBur;
    @Transient
    private Long validFromBur;
    @Transient
    private Long validToBur;
}
