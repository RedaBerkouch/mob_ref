package ch.bfs.meb.server.commons.integration.dto;

import javax.persistence.*;

import lombok.Data;

/** Entity for the external school data view. */
@Data
@Entity
@Table(name = "V_SCHUL")
public class BurSchoolExt {
    @Id
    @Column(name = "LOCAL_ID")
    private Long local_id;
    @Column
    private String canton_cd;
    @Column
    private String name_tx;
    @Column
    private Long municipality_cd;
    @Column
    private Long unit_status_cd;
    @Column
    private String sdl_cantonal_id;
    @Column
    private String ssp_cantonal_id;
    @Column
    private String matu_cantonal_id;
    @Column
    private Long stat_act_sdl_flg;
    @Column
    private Long stat_act_ssp_flg;
    @Column
    private Long stat_act_matu_flg;
    @Column
    private Long stat_act_sdl_ssp_from;
    @Column
    private Long stat_act_sdl_ssp_to;
    @Column
    private Long stat_act_ssp_from;
    @Column
    private Long stat_act_ssp_to;
    @Column
    private Long stat_act_matu_from;
    @Column
    private Long stat_act_matu_to;
    @Column
    private Long char_publ_flg;
    @Column
    private Long char_priv_sub_flg;
    @Column
    private Long char_priv_no_sub_flg;
    @Column(name = "inst_typ_blp_flg")
    private Boolean isSpecialSchool;

    /** Used to map canton cd to number TODO skaufmann: remove this field since the V_SCHUL can directly access the cantonCode as number */
    @Transient
    private Long cantonCode;
}
