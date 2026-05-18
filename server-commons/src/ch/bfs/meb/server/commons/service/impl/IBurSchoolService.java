package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.*;

public interface IBurSchoolService {
    BurSchoolListResult getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch);

    BurSchoolListResult getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch);

    BurSchoolResult getBurSchoolById(Long burSchoolId, boolean showBurSynch, Long version);

    BurSchoolResult getBurSchoolByIdAndType(String schoolId, String schoolType, Long version);

    BurSchoolListResult synchronizeSchools();

    BurSchoolListResult importBurSchools(Long canton);

    BurSchoolResult importBurSchool(BurSchool burSchool);

    BurSchoolResult updateBurSchool(BurSchool burSchool, boolean showBurSynch);

    BurSchoolResult insertBurSchool(BurSchool burSchool);

    BurSchoolResult deleteBurSchool(BurSchool burSchool);
}
