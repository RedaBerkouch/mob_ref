package ch.bfs.meb.ssp.server.integration.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.util.SecurityFiltersBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

public class SecurityFilters extends SecurityFiltersBase {
    public final static String SCHOOLS_TABLE = "Schools";
    public final static String SSP_CONFIGDELIVERIES_TABLE = "Ssp_ConfigDeliveries";
    public final static String SSP_DELIVERIES_TABLE = "Ssp_Deliveries";
    public final static String SSP_PERSONS_TABLE = "Ssp_Persons";
    public final static String SSP_ACTIVITIES_TABLE = "Ssp_Activities";

    protected List<String> _roles = null;

    public SecurityFilters() {
        super();

        /*
          Schools filters
         */
        addCantonFilter(CodegroupUtility.SSP_OBJECTTYPE_BURSCHOOL, "select fltr_bs.* from Schools fltr_bs where fltr_bs.canton in (%1)",
                SecurityConstants.ROLE_SSP_DL, SCHOOLS_TABLE);

        /*
          SspDelivery filters
         */
        addCantonFilter(CodegroupUtility.SSP_OBJECTTYPE_DELIVERY, "select fltr_d.* from Ssp_Deliveries fltr_d where fltr_d.canton in (%1)",
                SecurityConstants.ROLE_SSP_DL, SSP_DELIVERIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SSP_OBJECTTYPE_DELIVERY,
                "select fltr_dcd.* from Ssp_Deliveries fltr_dcd, Ssp_ConfigDeliveries fltr_cdd where fltr_dcd.configDeliveryCode = fltr_cdd.deliveryCode and LOWER(fltr_cdd.ro_users) like '%%1%' and fltr_cdd.version=%2",
                SecurityConstants.ROLE_SSP_RO, SSP_DELIVERIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SSP_OBJECTTYPE_DELIVERY,
                "select distinct fltr_dcd.* from Ssp_Deliveries fltr_dcd left outer join Ssp_ConfigDeliveries fltr_cdd on fltr_dcd.configdeliverycode = fltr_cdd.deliveryCode where LOWER(fltr_dcd.creation_user) = '%3' or (fltr_dcd.configDeliveryCode = fltr_cdd.deliveryCode and LOWER(fltr_cdd.dl_users) like '%%1%' and (fltr_cdd.version is null or fltr_cdd.version=%2))",
                SecurityConstants.ROLE_SSP_DL, SSP_DELIVERIES_TABLE);

        /*
          SspPerson filters
         */
        addCantonFilter(CodegroupUtility.SSP_OBJECTTYPE_PERSON, "select fltr_p.* from Ssp_Persons fltr_p where fltr_p.canton in (%1)",
                SecurityConstants.ROLE_SSP_DL, SSP_PERSONS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SSP_OBJECTTYPE_PERSON,
                "select fltr_pcd.* from Ssp_Persons fltr_pcd, Ssp_ConfigDeliveries fltr_cdp where fltr_pcd.configDeliveryCode = fltr_cdp.deliveryCode and LOWER(fltr_cdp.ro_users) like '%%1%' and fltr_cdp.version=%2",
                SecurityConstants.ROLE_SSP_RO, SSP_PERSONS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SSP_OBJECTTYPE_PERSON,
                "select distinct fltr_pcd.* from Ssp_Persons fltr_pcd, Ssp_ConfigDeliveries fltr_cdp where LOWER(fltr_pcd.creation_user) = '%3' or (fltr_pcd.configDeliveryCode = fltr_cdp.deliveryCode and LOWER(fltr_cdp.dl_users) like '%%1%' and fltr_cdp.version=%2)",
                SecurityConstants.ROLE_SSP_DL, SSP_PERSONS_TABLE);

        /*
          SspActivity filters
         */
        addCantonFilter(CodegroupUtility.SSP_OBJECTTYPE_ACTIVITY, "select fltr_a.* from Ssp_Activities fltr_a where fltr_a.canton in (%1)",
                SecurityConstants.ROLE_SSP_DL, SSP_ACTIVITIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SSP_OBJECTTYPE_ACTIVITY,
                "select fltr_acd.* from Ssp_Activities fltr_acd, Ssp_ConfigDeliveries fltr_cda where fltr_acd.configDeliveryCode = fltr_cda.deliveryCode and LOWER(fltr_cda.ro_users) like '%%1%' and fltr_cda.version=%2",
                SecurityConstants.ROLE_SSP_RO, SSP_ACTIVITIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SSP_OBJECTTYPE_ACTIVITY,
                "select distinct fltr_acd.* from Ssp_Activities fltr_acd, Ssp_ConfigDeliveries fltr_cda where LOWER(fltr_acd.creation_user) = '%3' or (fltr_acd.configDeliveryCode = fltr_cda.deliveryCode and LOWER(fltr_cda.dl_users) like '%%1%' and fltr_cda.version=%2)",
                SecurityConstants.ROLE_SSP_DL, SSP_ACTIVITIES_TABLE);

        /*
          SspConfigDelivery filters
         */
        addCantonFilter(CodegroupUtility.SSP_OBJECTTYPE_CONFIGDELIVERY, "select fltr_cd.* from Ssp_ConfigDeliveries fltr_cd where fltr_cd.canton in (%1)",
                SecurityConstants.ROLE_SSP_DL, SSP_CONFIGDELIVERIES_TABLE);

    }

    protected synchronized List<String> getRoles() {
        if (_roles == null) {
            _roles = new ArrayList<String>();
            _roles.add(SecurityConstants.ROLE_SSP_RO);
            _roles.add(SecurityConstants.ROLE_SSP_DL);
            _roles.add(SecurityConstants.ROLE_SSP_DV);
            _roles.add(SecurityConstants.ROLE_SSP_EV);
            _roles.add(SecurityConstants.ROLE_SSP_EA);
        }
        return _roles;
    }

    protected boolean isInRole(String role) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.isInRole(role);
    }

    public List<Long> getFilterCantonsForActUser() {
        if (!isInRole(SecurityConstants.ROLE_SSP_EA) && !isInRole(SecurityConstants.ROLE_SSP_EV)) {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return user.getCantons();
        } else {
            return super.getFilterCantonsForActUser();
        }
    }

    protected List<String> getEvUserRoles() {
        List<String> evRoles = new ArrayList<String>();
        evRoles.add(SecurityConstants.ROLE_SSP_EV);
        evRoles.add(SecurityConstants.ROLE_SSP_EA);
        return evRoles;
    }
}
