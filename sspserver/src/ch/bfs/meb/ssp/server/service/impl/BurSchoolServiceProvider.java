/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;

import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.BurSchoolServiceHelper;
import ch.bfs.meb.server.commons.service.impl.IBurSchoolServiceProvider;
import ch.bfs.meb.ssp.server.integration.dto.SspBurSchool;
import ch.bfs.meb.ssp.server.integration.dto.SspConfigDelivery;
import ch.bfs.meb.ssp.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.ssp.server.integration.repository.IConfigDeliveryRepository;
import ch.bfs.meb.ssp.server.integration.repository.IFilterRepository;
import ch.bfs.meb.util.CodegroupUtility;

public class BurSchoolServiceProvider implements IBurSchoolServiceProvider {
    private IBurSchoolRepository _burSchoolRepository;
    private IConfigDeliveryRepository _configDeliveryRepository;
    private IFilterRepository _filterRepository;

    public void setBurSchoolRepository(IBurSchoolRepository burSchoolRepository) {
        _burSchoolRepository = burSchoolRepository;
    }

    public void setConfigDeliveryRepository(IConfigDeliveryRepository configDeliveryRepository) {
        _configDeliveryRepository = configDeliveryRepository;
    }

    public void setFilterRepository(IFilterRepository filterRepository) {
        _filterRepository = filterRepository;
    }

    protected SspBurSchool initDto(SspBurSchool school, Long version) {
        if (school != null) {
            for (SspConfigDelivery configDelivery : school.getConfigDeliveries()) {
                if (configDelivery.getVersion().equals(version)) {
                    school.setDeliveryId(configDelivery.getDeliveryId());
                    school.setDeliveryCode(configDelivery.getDeliveryCode());
                    school.setVersion(configDelivery.getVersion());
                    return school;
                }
            }
            // empty config delivery attributes if no configDelivery is assigned for version (might contain an older version)
            school.setDeliveryId(null);
            school.setDeliveryCode(null);
            school.setVersion(version);
        }

        return school;
    }

    protected SspBurSchool initDto(SspBurSchool school, List<Long> configDeliveryIds) {
        for (SspConfigDelivery configDelivery : school.getConfigDeliveries()) {
            for (Long deliveryId : configDeliveryIds) {
                if (configDelivery.getDeliveryId().equals(deliveryId)) {
                    school.setDeliveryId(configDelivery.getDeliveryId());
                    school.setDeliveryCode(configDelivery.getDeliveryCode());
                    school.setVersion(configDelivery.getVersion());
                    return school;
                }
            }
        }
        return school;
    }

    protected List<SspBurSchool> initDto(List<SspBurSchool> schools, Long version) {
        for (SspBurSchool school : schools) {
            initDto(school, version);
        }
        return schools;
    }

    protected List<SspBurSchool> initDto(List<SspBurSchool> schools, List<Long> configDeliveryIds) {
        for (SspBurSchool school : schools) {
            initDto(school, configDeliveryIds);
        }
        return schools;
    }

    @Override
    public BurSchool getBurSchoolById(Long burSchoolId, Long version) {
        return initDto(_burSchoolRepository.getBurSchoolById(burSchoolId), version);
    }

    @Override
    public BurSchool getBurSchoolByIdAndType(String schoolId, String schoolType, Long version) {
        return initDto(_burSchoolRepository.getBurSchoolByIdAndType(schoolId, schoolType), version);
    }

    @Override
    public List<BurSchool> getBurSchools() {
        return Collections.unmodifiableList(new ArrayList<BurSchool>(_burSchoolRepository.getBurSchools()));
    }

    @Override
    public List<BurSchool> getBurSchoolsOfConfigDeliveries(Long version, Long canton) {
        List<BurSchool> burSchools = new ArrayList<BurSchool>();
        List<SspConfigDelivery> configDeliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton);
        for (SspConfigDelivery cd : configDeliveries) {
            burSchools.addAll(cd.getBurSchools());
        }
        return burSchools;
    }

    @Override
    public List<BurSchool> getBurSchoolsForCsvExport() {
        return Collections.unmodifiableList(new ArrayList<BurSchool>(_burSchoolRepository.getBurSchoolsForCsvExport()));
    }

    @Override
    public List<BurSchool> getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch) {
        return Collections.unmodifiableList(new ArrayList<BurSchool>(
                initDto(_burSchoolRepository.getBurSchools(start, buffer, sortContext, filterContext, version, canton, showBurSynch), version)));
    }

    @Override
    public Long getMaxNrOfBurSchools(FilterContext filterContext, Long version, Long canton, boolean showBurSynch) {
        return _burSchoolRepository.getMaxNrOfBurSchools(filterContext, version, canton, showBurSynch);
    }

    @Override
    public List<BurSchool> getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch) {
        return Collections.unmodifiableList(new ArrayList<BurSchool>(
                initDto(_burSchoolRepository.getBurSchoolsOwnedByConfigDeliveries(configDeliveryIds, sortContext, showBurSynch), configDeliveryIds)));
    }

    @Override
    public List<BurSchoolExt> getExternalBurSchools() {
        return Collections.unmodifiableList(_burSchoolRepository.getExternalBurSchools());
    }

    @Override
    public void initBurSchool(BurSchool burSchool, Long version, Long versionTemplate, Long canton) {
        if (burSchool instanceof SspBurSchool) {
            // create new config deliveries for all config deliveries in templateVersion
            List<SspConfigDelivery> newDeliveries = new ArrayList<SspConfigDelivery>();
            SspBurSchool school = (SspBurSchool) burSchool;
            for (SspConfigDelivery delivery : school.getConfigDeliveries()) {
                // there is a config delivery in templateVersion
                if (delivery.getVersion().equals(versionTemplate) && delivery.getCanton().equals(canton)) {
                    SspConfigDelivery newDelivery = null;
                    for (SspConfigDelivery d : school.getConfigDeliveries()) {
                        if (d.getVersion().equals(version) && d.getCanton().equals(canton) && d.getDeliveryCode().equals(delivery.getDeliveryCode())) {
                            // config delivery already exists in target, should not happen
                            newDelivery = d;
                            break;
                        }
                    }

                    if (newDelivery == null) {
                        newDelivery = _configDeliveryRepository.getConfigDeliveryByCodeVersionAndCanton(delivery.getDeliveryCode(), version, canton);
                        newDeliveries.add(newDelivery);
                    }
                }
            }

            if (newDeliveries.size() > 0) {
                school.getConfigDeliveries().addAll(newDeliveries);

                _burSchoolRepository.updateBurSchool(school);
            }
        }
    }

    @Override
    public BurSchool insertBurSchool(BurSchool burSchool) {
        return _burSchoolRepository.insertBurSchool(new SspBurSchool(burSchool));
    }

    protected void addConfigDelivery(SspBurSchool burSchool, ConfigDelivery configDelivery) {
        if (configDelivery instanceof SspConfigDelivery) {
            burSchool.getConfigDeliveries().add((SspConfigDelivery) configDelivery);
        } else {
            // should not happen
            burSchool.getConfigDeliveries().add(new SspConfigDelivery(configDelivery));
        }
    }

    @Override
    public BurSchool updateSynchBurSchool(BurSchool burSchool) {
        return _burSchoolRepository.updateBurSchool((SspBurSchool) burSchool);
    }

    @Override
    public BurSchool updateBurSchool(BurSchool burSchool, ConfigDelivery configDelivery) {
        SspBurSchool origBurSchool = _burSchoolRepository.getBurSchoolById(burSchool.getSchoolId());
        SspConfigDelivery origConfigDelivery = null;

        for (SspConfigDelivery cd : origBurSchool.getConfigDeliveries()) {
            if (cd.getVersion().equals(burSchool.getVersion())) {
                origConfigDelivery = cd;
                break;
            }
        }

        SspBurSchool newBurSchool = new SspBurSchool(burSchool);
        newBurSchool.setConfigDeliveries(new ArrayList<SspConfigDelivery>(origBurSchool.getConfigDeliveries()));

        if (configDelivery != null || origConfigDelivery != null) {
            if (configDelivery != null && origConfigDelivery != null) {
                if (!configDelivery.getDeliveryId().equals(origConfigDelivery.getDeliveryId())) {
                    newBurSchool.getConfigDeliveries().remove(origConfigDelivery);
                    addConfigDelivery(newBurSchool, configDelivery);
                }
            } else if (configDelivery != null) {
                addConfigDelivery(newBurSchool, configDelivery);
            } else // origConfigDelivery != null
            {
                newBurSchool.getConfigDeliveries().remove(origConfigDelivery);
            }
        }

        Long actVersion = _filterRepository.getActVersion();
        newBurSchool.setVersion(actVersion);
        if (!isActiveSchool(newBurSchool)) {
            for (int i = newBurSchool.getConfigDeliveries().size() - 1; i >= 0; --i) {
                SspConfigDelivery cd = newBurSchool.getConfigDeliveries().get(i);
                newBurSchool.setVersion(cd.getVersion());
                if (cd.getVersion() < actVersion || !isActiveSchool(newBurSchool)) {
                    newBurSchool.getConfigDeliveries().remove(i);
                }
            }
        }

        newBurSchool = _burSchoolRepository.updateBurSchool(newBurSchool);
        initDto(newBurSchool, burSchool.getVersion());
        return newBurSchool;
    }

    @Override
    public void deleteBurSchool(BurSchool burSchool) {
        _burSchoolRepository.deleteBurSchool(new SspBurSchool(burSchool));
    }

    @Override
    public boolean isActiveSchool(BurSchool burSchool) {
        return burSchool.is_ssp()
                && (burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                && (burSchool.getValidFrom_ssp() == null || burSchool.getValidFrom_ssp() <= burSchool.getVersion())
                && (burSchool.getValidTo_ssp() == null || burSchool.getValidTo_ssp() >= burSchool.getVersion());
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.service.impl.IBurSchoolServiceProvider#isVisibleSchool(ch.bfs.meb.server.commons.integration.dto.BurSchool)
     */
    @Override
    public boolean isVisibleSchool(BurSchool burSchool, Long version) {
        return (burSchool.is_ssp()
                && (burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                && (burSchool.getValidFrom_ssp() == null || burSchool.getValidFrom_ssp() <= version)
                && (burSchool.getValidTo_ssp() == null || burSchool.getValidTo_ssp() >= version))
                || (burSchool.isBur_is_ssp()
                        && (burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                                || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                                || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                        && (burSchool.getBur_validFrom_ssp() == null || burSchool.getBur_validFrom_ssp() <= version)
                        && (burSchool.getBur_validTo_ssp() == null || burSchool.getBur_validTo_ssp() >= version));
    }

    protected boolean importBurSchool(BurSchool burSchool, SspConfigDelivery defaultConfigDelivery) {
        if (burSchool.getSynchStatus_ssp() != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED) {
            Calendar cal = new GregorianCalendar();
            long now = cal.get(Calendar.YEAR);

            // TODO: hack, remember original values for rollback case (see next "else if")
            Long origVersion = burSchool.getVersion();
            Long origCanton = burSchool.getCanton();
            Long origMunicipality = burSchool.getMunicipality();
            Long origActivityStatus = burSchool.getActivityStatus();
            String origCantonalCode_ssp = burSchool.getCantonalCode_ssp();
            boolean origIs_ssp = burSchool.is_ssp();
            Long origValidFrom_ssp = burSchool.getValidFrom_ssp();
            Long origValidTo_ssp = burSchool.getValidTo_ssp();
            Long origChar_publ_flg = burSchool.getChar_publ_flg();
            Long origChar_priv_sub_flg = burSchool.getChar_priv_sub_flg();
            Long origChar_priv_no_sub_flg = burSchool.getChar_priv_no_sub_flg();
            Boolean origIsSpecialSchool = burSchool.getIsSpecialSchool();

            burSchool.setVersion(now);
            burSchool.setCanton(burSchool.getBur_canton());
            burSchool.setLabel(burSchool.getBur_label());
            burSchool.setMunicipality(burSchool.getBur_municipality());
            burSchool.setActivityStatus(burSchool.getBur_activityStatus());
            burSchool.setCantonalCode_ssp(burSchool.getBur_cantonalCode_ssp());
            burSchool.set_ssp(burSchool.isBur_is_ssp());
            burSchool.setValidFrom_ssp(burSchool.getBur_validFrom_ssp());
            burSchool.setValidTo_ssp(burSchool.getBur_validTo_ssp());
            burSchool.setChar_publ_flg(burSchool.getBur_char_publ_flg());
            burSchool.setChar_priv_sub_flg(burSchool.getBur_char_priv_sub_flg());
            burSchool.setChar_priv_no_sub_flg(burSchool.getBur_char_priv_no_sub_flg());
            burSchool.setIsSpecialSchool(burSchool.getIsSpecialSchoolBur());
            if (burSchool.getSynchStatus_ssp() == CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
                // create eventual default config delivery
                if (defaultConfigDelivery != null) {
                    if (burSchool.getDeliveryCode() == null || burSchool.getDeliveryCode().equals("")) {
                        burSchool.setDeliveryCode(defaultConfigDelivery.getDeliveryCode());
                    }
                    if (((SspBurSchool) burSchool).getConfigDeliveries() == null) {
                        ((SspBurSchool) burSchool).setConfigDeliveries(new ArrayList<SspConfigDelivery>());
                    }
                    ((SspBurSchool) burSchool).getConfigDeliveries().add(defaultConfigDelivery);
                }
            } else if (!isActiveSchool(burSchool)) {
                if (_burSchoolRepository.existsActivityForBurSchool((SspBurSchool) burSchool)) {
                    // TODO: hack, reset original values in dto, as hibernate does not a rollback
                    // but a commit of values in dto
                    burSchool.setVersion(origVersion);
                    burSchool.setCanton(origCanton);
                    burSchool.setMunicipality(origMunicipality);
                    burSchool.setActivityStatus(origActivityStatus);
                    burSchool.setCantonalCode_ssp(origCantonalCode_ssp);
                    burSchool.set_ssp(origIs_ssp);
                    burSchool.setValidFrom_ssp(origValidFrom_ssp);
                    burSchool.setValidTo_ssp(origValidTo_ssp);
                    burSchool.setChar_publ_flg(origChar_publ_flg);
                    burSchool.setChar_priv_sub_flg(origChar_priv_sub_flg);
                    burSchool.setChar_priv_no_sub_flg(origChar_priv_no_sub_flg);
                    burSchool.setIsSpecialSchoolBur(origIsSpecialSchool);

                    return false;
                }
                burSchool.setDeliveryCode(null);
            }
            burSchool.setSynchStatus_ssp(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
        }
        return true;
    }

    protected SspConfigDelivery getActDefaultConfigDelivery(Long canton) {
        Calendar cal = new GregorianCalendar();
        long now = cal.get(Calendar.YEAR);

        List<SspConfigDelivery> deliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(now, canton);
        for (SspConfigDelivery delivery : deliveries) {
            if (delivery.getIsDefault()) {
                return delivery;
            }
        }
        return null;
    }

    @Override
    public boolean importBurSchool(BurSchool burSchool) {
        SspConfigDelivery defaultConfigDelivery = null;
        if (burSchool.getSynchStatus_ssp() == CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
            defaultConfigDelivery = getActDefaultConfigDelivery(burSchool.getBur_canton());
        }
        return importBurSchool(burSchool, defaultConfigDelivery);
    }

    @Override
    public boolean importBurSchools(Long canton) {
        SspConfigDelivery defaultConfigDelivery = getActDefaultConfigDelivery(canton);

        List<SspBurSchool> schools = _burSchoolRepository.getBurSchools();
        if (canton > 0) {
            schools = schools.stream()
                    .filter(s -> canton.equals(s.getCanton()) || canton.equals(s.getBur_canton()))
                    .collect(Collectors.toList());
        }
        for (SspBurSchool school : schools) {
            if (canton > 0L && !canton.equals(school.getCanton()) && !canton.equals(school.getBur_canton())) {
                continue;
            }

            if (school.getSynchStatus_ssp() != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED) {
                if (!importBurSchool(school, defaultConfigDelivery)) {
                    return false;
                } else {
                    SspConfigDelivery configDelivery = null;

                    if (isActiveSchool(school)) {
                        // Search for original configDelivery (Changes of deliveryCode not propagated to server!)
                        // Eventual default configdelivery for new schools is already set and added in method importBurSchool
                        for (SspConfigDelivery cd : school.getConfigDeliveries()) {
                            if (cd.getVersion().equals(school.getVersion())) {
                                configDelivery = cd;
                                break;
                            }
                        }
                    }

                    updateBurSchool(school, configDelivery);
                }
            }
        }
        return true;
    }

    @Override
    public void initSynchData(BurSchool burSchool) {
        burSchool.setSynchStatusBur(burSchool.getSynchStatus_ssp());
        burSchool.setNameBur(burSchool.getBur_label());
        burSchool.setCantonBur(burSchool.getBur_canton());
        burSchool.setMunicipalityBur(burSchool.getBur_municipality());
        burSchool.setValidFromBur(burSchool.getBur_validFrom_ssp());
        burSchool.setValidToBur(burSchool.getBur_validTo_ssp());
    }

    @Override
    public void initSynchData(List<BurSchool> burSchools) {
        for (BurSchool burSchool : burSchools) {
            initSynchData(burSchool);
        }
    }

    @Override
    public long calculateSynchStatus(BurSchool burSchool, BurSchoolExt burSchoolExt) {
        if (burSchoolExt != null) {
            burSchool.setBur_canton(burSchoolExt.getCantonCode());
            burSchool.setBur_label(burSchoolExt.getName_tx());
            burSchool.setBur_municipality(burSchoolExt.getMunicipality_cd());
            burSchool.setBur_activityStatus(burSchoolExt.getUnit_status_cd());
            // burSchool.setBur_cantonalCode_sdl(burSchoolExt.getSdl_cantonal_id());
            // burSchool.setBur_is_sdl(burSchoolExt.getStat_act_sdl_flg() != null && burSchoolExt.getStat_act_sdl_flg() == 1L);
            burSchool.setBur_validFrom_ssp(burSchoolExt.getStat_act_ssp_from());
            burSchool.setBur_validTo_ssp(burSchoolExt.getStat_act_ssp_to());
            burSchool.setBur_cantonalCode_ssp(burSchoolExt.getSsp_cantonal_id());
            burSchool.setBur_is_ssp(burSchoolExt.getStat_act_ssp_flg() != null && burSchoolExt.getStat_act_ssp_flg() == 1L);
            // burSchool.setBur_cantonalCode_sba(burSchoolExt.getMatu_cantonal_id());
            // burSchool.setBur_is_sba(burSchoolExt.getStat_act_matu_flag() != null
            // && burSchoolExt.getStat_act_matu_flag() == 1L);
            // burSchool.setBur_validFrom_sba(burSchoolExt.getStat_act_matu_from());
            // burSchool.setBur_validTo_sba(burSchoolExt.getStat_act_matu_to());
            burSchool.setBur_char_publ_flg(BurSchoolServiceHelper.zeroForNull(burSchoolExt.getChar_publ_flg()));
            burSchool.setBur_char_priv_sub_flg(BurSchoolServiceHelper.zeroForNull(burSchoolExt.getChar_priv_sub_flg()));
            burSchool.setBur_char_priv_no_sub_flg(BurSchoolServiceHelper.zeroForNull(burSchoolExt.getChar_priv_no_sub_flg()));
            burSchool.setIsSpecialSchoolBur(BurSchoolServiceHelper.zeroForNull(burSchoolExt.getIsSpecialSchool()));
        } else {
            burSchool.setBur_is_ssp(false);
        }

        if (burSchool.getSynchStatus_ssp() != CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
            Calendar cal = new GregorianCalendar();
            long now = cal.get(Calendar.YEAR);
            boolean isSsp = burSchool.is_ssp();
            boolean isBurSsp = burSchool.isBur_is_ssp();
            boolean isValid = (burSchool.getValidFrom_ssp() == null || burSchool.getValidFrom_ssp() <= now)
                    && (burSchool.getValidTo_ssp() == null || burSchool.getValidTo_ssp() >= now);
            boolean isBurValid = (burSchool.getBur_validFrom_ssp() == null || burSchool.getBur_validFrom_ssp() <= now)
                    && (burSchool.getBur_validTo_ssp() == null || burSchool.getBur_validTo_ssp() >= now);
            boolean isActive = burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                    || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                    || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN;
            boolean isBurActive = burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                    || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                    || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN;

            boolean isVisible = isSsp && isValid && isActive;
            boolean isBurVisible = isBurSsp && isBurValid && isBurActive;

            if (isVisible && !isBurVisible) {
                burSchool.setSynchStatus_ssp(CodegroupUtility.MEB_SYNCHSTATUS_INACTIVATED);
            } else if (!isVisible && isBurVisible) {
                burSchool.setSynchStatus_ssp(CodegroupUtility.MEB_SYNCHSTATUS_CHANGED);
            } else if (!isVisible && !isBurVisible) {
                burSchool.setSynchStatus_ssp(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
            } else {
                if (!BurSchoolServiceHelper.equalObjects(burSchool.getLabel(), burSchool.getBur_label())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getCanton(), burSchool.getBur_canton())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getCantonalCode_ssp(), burSchool.getBur_cantonalCode_ssp())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getMunicipality(), burSchool.getBur_municipality())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getValidFrom_ssp(), burSchool.getBur_validFrom_ssp())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getValidTo_ssp(), burSchool.getBur_validTo_ssp())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getActivityStatus(), burSchool.getBur_activityStatus())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_publ_flg(), burSchool.getBur_char_publ_flg())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_priv_sub_flg(), burSchool.getBur_char_priv_sub_flg())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_priv_no_sub_flg(), burSchool.getBur_char_priv_no_sub_flg())
                        || !BurSchoolServiceHelper.equalBooleans(burSchool.getIsSpecialSchool(), burSchool.getIsSpecialSchoolBur())) {
                    burSchool.setSynchStatus_ssp(CodegroupUtility.MEB_SYNCHSTATUS_CHANGED);
                } else {
                    burSchool.setSynchStatus_ssp(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
                }
            }
        }

        return burSchool.getSynchStatus_ssp();
    }

    @Override
    public void lockBurSchools() throws HibernateException {
        _burSchoolRepository.lockBurSchools();
    }
}
