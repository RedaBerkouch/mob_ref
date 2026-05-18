package ch.bfs.meb.sdl.server.integration.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.util.SecurityFiltersBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

public class SecurityFilters extends SecurityFiltersBase {
    public final static String SCHOOLS_TABLE = "Schools";
    public final static String SDL_CONFIGDELIVERIES_TABLE = "Sdl_ConfigDeliveries";
    public final static String SDL_DELIVERIES_TABLE = "Sdl_Deliveries";
    public final static String SDL_SCHOOLS_TABLE = "Sdl_Schools";
    public final static String SDL_CLASSES_TABLE = "Sdl_Classes";
    public final static String SDL_LEARNERS_TABLE = "Sdl_Learners";

    protected List<String> _roles = null;

    public SecurityFilters() {
        super();

        /*
          Schools filters
         */
        addCantonFilter(CodegroupUtility.SDL_OBJECTTYPE_BURSCHOOL, "select fltr_bs.* from Schools fltr_bs where fltr_bs.canton in (%1)",
                SecurityConstants.ROLE_SDL_DL, SCHOOLS_TABLE);

        /*
          SdlDelivery filters
         */
        addCantonFilter(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY, "select fltr_d.* from Sdl_Deliveries fltr_d where fltr_d.canton in (%1)",
                SecurityConstants.ROLE_SDL_DL, SDL_DELIVERIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY,
                "select fltr_dcd.* from Sdl_Deliveries fltr_dcd, Sdl_ConfigDeliveries fltr_cdd where fltr_dcd.configDeliveryCode = fltr_cdd.deliveryCode and LOWER(fltr_cdd.ro_users) like '%%1%' and fltr_cdd.version=%2",
                SecurityConstants.ROLE_SDL_RO, SDL_DELIVERIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY,
                "select distinct fltr_dcd.* from Sdl_Deliveries fltr_dcd left outer join Sdl_ConfigDeliveries fltr_cdd on fltr_dcd.configdeliverycode = fltr_cdd.deliveryCode where LOWER(fltr_dcd.creation_user) = '%3' or (fltr_dcd.configDeliveryCode = fltr_cdd.deliveryCode and LOWER(fltr_cdd.dl_users) like '%%1%' and (fltr_cdd.version is null or fltr_cdd.version=%2))",
                SecurityConstants.ROLE_SDL_DL, SDL_DELIVERIES_TABLE);

        /*
          SdlSchool filters
         */
        addCantonFilter(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL, "select fltr_s.* from Sdl_Schools fltr_s where fltr_s.canton in (%1)",
                SecurityConstants.ROLE_SDL_DL, SDL_SCHOOLS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL,
                "select fltr_scd.* from Sdl_Schools fltr_scd, Sdl_ConfigDeliveries fltr_cds where fltr_scd.configDeliveryCode = fltr_cds.deliveryCode and LOWER(fltr_cds.ro_users) like '%%1%' and fltr_cds.version=%2",
                SecurityConstants.ROLE_SDL_RO, SDL_SCHOOLS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_SCHOOL,
                "select distinct fltr_scd.* from Sdl_Schools fltr_scd, Sdl_ConfigDeliveries fltr_cds where LOWER(fltr_scd.creation_user) = '%3' or (fltr_scd.configDeliveryCode = fltr_cds.deliveryCode and LOWER(fltr_cds.dl_users) like '%%1%' and fltr_cds.version=%2)",
                SecurityConstants.ROLE_SDL_DL, SDL_SCHOOLS_TABLE);

        /*
          SdlClass filters
         */
        addCantonFilter(CodegroupUtility.SDL_OBJECTTYPE_CLASS, "select fltr_c.* from Sdl_Classes fltr_c where fltr_c.canton in (%1)",
                SecurityConstants.ROLE_SDL_DL, SDL_CLASSES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_CLASS,
                "select fltr_ccd.* from Sdl_Classes fltr_ccd, Sdl_ConfigDeliveries fltr_cdc where fltr_ccd.configDeliveryCode = fltr_cdc.deliveryCode and LOWER(fltr_cdc.ro_users) like '%%1%' and fltr_cdc.version=%2",
                SecurityConstants.ROLE_SDL_RO, SDL_CLASSES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_CLASS,
                "select distinct fltr_ccd.* from Sdl_Classes fltr_ccd, Sdl_ConfigDeliveries fltr_cdc where LOWER(fltr_ccd.creation_user) = '%3' or (fltr_ccd.configDeliveryCode = fltr_cdc.deliveryCode and LOWER(fltr_cdc.dl_users) like '%%1%' and fltr_cdc.version=%2)",
                SecurityConstants.ROLE_SDL_DL, SDL_CLASSES_TABLE);

        /*
          SdlLearner filters
         */
        addCantonFilter(CodegroupUtility.SDL_OBJECTTYPE_LEARNER, "select fltr_l.* from Sdl_Learners fltr_l where fltr_l.canton in (%1)",
                SecurityConstants.ROLE_SDL_DL, SDL_LEARNERS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_LEARNER,
                "select fltr_lcd.* from Sdl_Learners fltr_lcd, Sdl_ConfigDeliveries fltr_cdl where fltr_lcd.configDeliveryCode = fltr_cdl.deliveryCode and LOWER(fltr_cdl.ro_users) like '%%1%' and fltr_cdl.version=%2",
                SecurityConstants.ROLE_SDL_RO, SDL_LEARNERS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SDL_OBJECTTYPE_LEARNER,
                "select distinct fltr_lcd.* from Sdl_Learners fltr_lcd, Sdl_ConfigDeliveries fltr_cdl where LOWER(fltr_lcd.creation_user) = '%3' or (fltr_lcd.configDeliveryCode = fltr_cdl.deliveryCode and LOWER(fltr_cdl.dl_users) like '%%1%' and fltr_cdl.version=%2)",
                SecurityConstants.ROLE_SDL_DL, SDL_LEARNERS_TABLE);

        /*
          SdlConfigDelivery filters
         */
        addCantonFilter(CodegroupUtility.SDL_OBJECTTYPE_CONFIGDELIVERY, "select fltr_cd.* from Sdl_ConfigDeliveries fltr_cd where fltr_cd.canton in (%1)",
                SecurityConstants.ROLE_SDL_DL, SDL_CONFIGDELIVERIES_TABLE);

    }

    protected synchronized List<String> getRoles() {
        if (_roles == null) {
            _roles = new ArrayList<String>();
            _roles.add(SecurityConstants.ROLE_SDL_RO);
            _roles.add(SecurityConstants.ROLE_SDL_DL);
            _roles.add(SecurityConstants.ROLE_SDL_DV);
            _roles.add(SecurityConstants.ROLE_SDL_EV);
            _roles.add(SecurityConstants.ROLE_SDL_EA);
        }
        return _roles;
    }

    protected boolean isInRole(String role) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.isInRole(role);
    }

    public List<Long> getFilterCantonsForActUser() {
        if (!isInRole(SecurityConstants.ROLE_SDL_EA) && !isInRole(SecurityConstants.ROLE_SDL_EV)) {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return user.getCantons();
        } else {
            return super.getFilterCantonsForActUser();
        }
    }

    protected List<String> getEvUserRoles() {
        List<String> evRoles = new ArrayList<String>();
        evRoles.add(SecurityConstants.ROLE_SDL_EV);
        evRoles.add(SecurityConstants.ROLE_SDL_EA);
        return evRoles;
    }
}
