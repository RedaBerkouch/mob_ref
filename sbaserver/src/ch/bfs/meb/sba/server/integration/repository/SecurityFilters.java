package ch.bfs.meb.sba.server.integration.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.util.SecurityFiltersBase;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityFilters extends SecurityFiltersBase {
    public final static String SCHOOLS_TABLE = "Schools";
    public final static String SBA_CONFIGDELIVERIES_TABLE = "Sba_ConfigDeliveries";
    public final static String SBA_DELIVERIES_TABLE = "Sba_Deliveries";
    public final static String SBA_PERSONS_TABLE = "Sba_Persons";
    public final static String SBA_QUALIFICATIONS_TABLE = "Sba_Qualifications";

    protected List<String> _roles = null;

    public SecurityFilters() {
        super();

        /*
          Schools filters
         */
        addCantonFilter(CodegroupUtility.SBA_OBJECTTYPE_BURSCHOOL, "select fltr_bs.* from Schools fltr_bs where fltr_bs.canton in (%1)",
                SecurityConstants.ROLE_SBA_DL, SCHOOLS_TABLE);

        /*
          SbaDelivery filters
         */
        addCantonFilter(CodegroupUtility.SBA_OBJECTTYPE_DELIVERY, "select fltr_d.* from Sba_Deliveries fltr_d where fltr_d.canton in (%1)",
                SecurityConstants.ROLE_SBA_DL, SBA_DELIVERIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SBA_OBJECTTYPE_DELIVERY,
                "select fltr_dcd.* from Sba_Deliveries fltr_dcd, Sba_ConfigDeliveries fltr_cdd where fltr_dcd.configDeliveryCode = fltr_cdd.deliveryCode and LOWER(fltr_cdd.ro_users) like '%%1%' and fltr_cdd.version=%2",
                SecurityConstants.ROLE_SBA_RO, SBA_DELIVERIES_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SBA_OBJECTTYPE_DELIVERY,
                "select distinct fltr_dcd.* from Sba_Deliveries fltr_dcd left outer join Sba_ConfigDeliveries fltr_cdd on fltr_dcd.configdeliverycode = fltr_cdd.deliveryCode where LOWER(fltr_dcd.creation_user) = '%3' or (fltr_dcd.configDeliveryCode = fltr_cdd.deliveryCode and LOWER(fltr_cdd.dl_users) like '%%1%' and (fltr_cdd.version is null or fltr_cdd.version=%2))",
                SecurityConstants.ROLE_SBA_DL, SBA_DELIVERIES_TABLE);

        /*
          SbaPerson filters
         */
        addCantonFilter(CodegroupUtility.SBA_OBJECTTYPE_PERSON, "select fltr_p.* from Sba_Persons fltr_p where fltr_p.canton in (%1)",
                SecurityConstants.ROLE_SBA_DL, SBA_PERSONS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SBA_OBJECTTYPE_PERSON,
                "select fltr_pcd.* from Sba_Persons fltr_pcd, Sba_ConfigDeliveries fltr_cdp where fltr_pcd.configDeliveryCode = fltr_cdp.deliveryCode and LOWER(fltr_cdp.ro_users) like '%%1%' and fltr_cdp.version=%2",
                SecurityConstants.ROLE_SBA_RO, SBA_PERSONS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SBA_OBJECTTYPE_PERSON,
                "select distinct fltr_pcd.* from Sba_Persons fltr_pcd, Sba_ConfigDeliveries fltr_cdp where LOWER(fltr_pcd.creation_user) = '%3' or (fltr_pcd.configDeliveryCode = fltr_cdp.deliveryCode and LOWER(fltr_cdp.dl_users) like '%%1%' and fltr_cdp.version=%2)",
                SecurityConstants.ROLE_SBA_DL, SBA_PERSONS_TABLE);

        /*
          SbaQualification filters
         */
        addCantonFilter(CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION, "select fltr_q.* from Sba_Qualifications fltr_q where fltr_q.canton in (%1)",
                SecurityConstants.ROLE_SBA_DL, SBA_QUALIFICATIONS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION,
                "select fltr_qcd.* from Sba_Qualifications fltr_qcd, Sba_ConfigDeliveries fltr_cdq where fltr_qcd.configDeliveryCode = fltr_cdq.deliveryCode and LOWER(fltr_cdq.ro_users) like '%%1%' and fltr_cdq.version=%2",
                SecurityConstants.ROLE_SBA_RO, SBA_QUALIFICATIONS_TABLE);
        addConfigDeliveryFilter(CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION,
                "select distinct fltr_qcd.* from Sba_Qualifications fltr_qcd, Sba_ConfigDeliveries fltr_cdq where LOWER(fltr_qcd.creation_user) = '%3' or (fltr_qcd.configDeliveryCode = fltr_cdq.deliveryCode and LOWER(fltr_cdq.dl_users) like '%%1%' and fltr_cdq.version=%2)",
                SecurityConstants.ROLE_SBA_DL, SBA_QUALIFICATIONS_TABLE);

        /*
          SbaConfigDelivery filters
         */
        addCantonFilter(CodegroupUtility.SBA_OBJECTTYPE_CONFIGDELIVERY, "select fltr_cd.* from Sba_ConfigDeliveries fltr_cd where fltr_cd.canton in (%1)",
                SecurityConstants.ROLE_SBA_DL, SBA_CONFIGDELIVERIES_TABLE);

    }

    protected synchronized List<String> getRoles() {
        if (_roles == null) {
            _roles = new ArrayList<String>();
            _roles.add(SecurityConstants.ROLE_SBA_RO);
            _roles.add(SecurityConstants.ROLE_SBA_DL);
            _roles.add(SecurityConstants.ROLE_SBA_DV);
            _roles.add(SecurityConstants.ROLE_SBA_EV);
            _roles.add(SecurityConstants.ROLE_SBA_EA);
        }
        return _roles;
    }

    protected boolean isInRole(String role) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.isInRole(role);
    }

    public List<Long> getFilterCantonsForActUser() {
        List<Long> cantons;
        if (!isInRole(SecurityConstants.ROLE_SBA_EA) && !isInRole(SecurityConstants.ROLE_SBA_EV)) {
            log.debug("getFilterCantonsForActUser 1");
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            cantons = user.getCantons();
        } else {
            log.debug("getFilterCantonsForActUser 2");
            cantons = super.getFilterCantonsForActUser();
        }
        cantons.forEach(canton -> log.debug("canton: {}", canton));
        return cantons;
    }

    protected List<String> getEvUserRoles() {
        List<String> evRoles = new ArrayList<String>();
        evRoles.add(SecurityConstants.ROLE_SBA_EV);
        evRoles.add(SecurityConstants.ROLE_SBA_EA);
        return evRoles;
    }
}
