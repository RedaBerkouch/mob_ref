/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;

import ch.bfs.meb.sdl.server.integration.dto.SdlBurSchool;
import ch.bfs.meb.sdl.server.integration.dto.SdlConfigDelivery;
import ch.bfs.meb.sdl.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.sdl.server.integration.repository.IConfigDeliveryRepository;
import ch.bfs.meb.sdl.server.integration.repository.IFilterRepository;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.impl.BurSchoolServiceHelper;
import ch.bfs.meb.server.commons.service.impl.IBurSchoolServiceProvider;
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

    protected SdlBurSchool initDto(SdlBurSchool school, Long version) {
        if (school != null) {
            for (SdlConfigDelivery configDelivery : school.getConfigDeliveries()) {
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

    protected SdlBurSchool initDto(SdlBurSchool school, List<Long> configDeliveryIds) {
        if (school != null) {
            for (SdlConfigDelivery configDelivery : school.getConfigDeliveries()) {
                for (Long deliveryId : configDeliveryIds) {
                    if (configDelivery.getDeliveryId().equals(deliveryId)) {
                        school.setDeliveryId(configDelivery.getDeliveryId());
                        school.setDeliveryCode(configDelivery.getDeliveryCode());
                        school.setVersion(configDelivery.getVersion());
                        return school;
                    }
                }
            }
        }
        return school;
    }

    protected List<SdlBurSchool> initDto(List<SdlBurSchool> schools, Long version) {
        for (SdlBurSchool school : schools) {
            initDto(school, version);
        }
        return schools;
    }

    protected List<SdlBurSchool> initDto(List<SdlBurSchool> schools, List<Long> configDeliveryIds) {
        for (SdlBurSchool school : schools) {
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
        List<SdlConfigDelivery> configDeliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton);
        for (SdlConfigDelivery cd : configDeliveries) {
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
        if (burSchool instanceof SdlBurSchool) {
            // create new config deliveries for all config deliveries in templateVersion
            List<SdlConfigDelivery> newDeliveries = new ArrayList<SdlConfigDelivery>();
            SdlBurSchool school = (SdlBurSchool) burSchool;
            for (SdlConfigDelivery delivery : school.getConfigDeliveries()) {
                // there is a config delivery in templateVersion
                if (delivery.getVersion().equals(versionTemplate) && delivery.getCanton().equals(canton)) {
                    SdlConfigDelivery newDelivery = null;
                    for (SdlConfigDelivery d : school.getConfigDeliveries()) {
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
                // Performance: Not needed because configDeliveryCode never changed during init
                //				for (SdlConfigDelivery newDelivery : newDeliveries)
                //				{
                //					_burSchoolRepository.updateAddedConfigDeliveryCodes (school, newDelivery);
                //				}

                _burSchoolRepository.updateBurSchool(school);
            }
        }
    }

    @Override
    public BurSchool insertBurSchool(BurSchool burSchool) {
        return _burSchoolRepository.insertBurSchool(new SdlBurSchool(burSchool));
    }

    protected void addConfigDelivery(SdlBurSchool burSchool, ConfigDelivery configDelivery) {
        SdlConfigDelivery sdlConfigDelivery;
        if (configDelivery instanceof SdlConfigDelivery) {
            sdlConfigDelivery = (SdlConfigDelivery) configDelivery;
        } else {
            // should not happen
            sdlConfigDelivery = new SdlConfigDelivery(configDelivery);
        }
        burSchool.getConfigDeliveries().add(sdlConfigDelivery);
        _burSchoolRepository.updateAddedConfigDeliveryCodes(burSchool, sdlConfigDelivery);
    }

    @Override
    public BurSchool updateSynchBurSchool(BurSchool burSchool) {
        return _burSchoolRepository.updateBurSchool((SdlBurSchool) burSchool);
    }

    @Override
    public BurSchool updateBurSchool(BurSchool burSchool, ConfigDelivery configDelivery) {
        SdlBurSchool origBurSchool = _burSchoolRepository.getBurSchoolById(burSchool.getSchoolId());
        SdlConfigDelivery origConfigDelivery = null;

        for (SdlConfigDelivery cd : origBurSchool.getConfigDeliveries()) {
            if (cd.getVersion().equals(burSchool.getVersion())) {
                origConfigDelivery = cd;
                break;
            }
        }

        SdlBurSchool newBurSchool = new SdlBurSchool(burSchool);
        newBurSchool.setConfigDeliveries(new ArrayList<SdlConfigDelivery>(origBurSchool.getConfigDeliveries()));

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
                _burSchoolRepository.updateRemovedConfigDeliveryCodes(newBurSchool, origConfigDelivery);
            }
        }

        Long actVersion = _filterRepository.getActVersion();
        newBurSchool.setVersion(actVersion);
        if (!isActiveSchool(newBurSchool)) {
            for (int i = newBurSchool.getConfigDeliveries().size() - 1; i >= 0; --i) {
                SdlConfigDelivery cd = newBurSchool.getConfigDeliveries().get(i);
                newBurSchool.setVersion(cd.getVersion());
                if (cd.getVersion() < actVersion || !isActiveSchool(newBurSchool)) {
                    newBurSchool.getConfigDeliveries().remove(i);
                    _burSchoolRepository.updateRemovedConfigDeliveryCodes(newBurSchool, cd);
                }
            }
        }

        newBurSchool = _burSchoolRepository.updateBurSchool(newBurSchool);
        initDto(newBurSchool, burSchool.getVersion());
        return newBurSchool;
    }

    @Override
    public void deleteBurSchool(BurSchool burSchool) {
        _burSchoolRepository.deleteBurSchool(new SdlBurSchool(burSchool));
    }

    @Override
    public boolean isActiveSchool(BurSchool burSchool) {
        return burSchool.is_sdl()
                && (burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                && (burSchool.getValidFrom_sdl_ssp() == null || burSchool.getValidFrom_sdl_ssp() <= burSchool.getVersion())
                && (burSchool.getValidTo_sdl_ssp() == null || burSchool.getValidTo_sdl_ssp() >= burSchool.getVersion());
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.service.impl.IBurSchoolServiceProvider#isVisibleSchool(ch.bfs.meb.server.commons.integration.dto.BurSchool)
     */
    @Override
    public boolean isVisibleSchool(BurSchool burSchool, Long version) {
        return (burSchool.is_sdl()
                && (burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                && (burSchool.getValidFrom_sdl_ssp() == null || burSchool.getValidFrom_sdl_ssp() <= version)
                && (burSchool.getValidTo_sdl_ssp() == null || burSchool.getValidTo_sdl_ssp() >= version))
                || (burSchool.isBur_is_sdl()
                        && (burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                                || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                                || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                        && (burSchool.getBur_validFrom_sdl_ssp() == null || burSchool.getBur_validFrom_sdl_ssp() <= version)
                        && (burSchool.getBur_validTo_sdl_ssp() == null || burSchool.getBur_validTo_sdl_ssp() >= version));
    }

    protected boolean importBurSchool(BurSchool burSchool, SdlConfigDelivery defaultConfigDelivery) {
        if (burSchool.getSynchStatus_sdl() != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED) {
            Calendar cal = new GregorianCalendar();
            long now = cal.get(Calendar.YEAR);

            // TODO: hack, remember original values for rollback case (see next "else if")
            Long origVersion = burSchool.getVersion();
            Long origCanton = burSchool.getCanton();
            Long origMunicipality = burSchool.getMunicipality();
            Long origActivityStatus = burSchool.getActivityStatus();
            String origCantonalCode_sdl = burSchool.getCantonalCode_sdl();
            boolean origIs_sdl = burSchool.is_sdl();
            Long origValidFrom_sdl_ssp = burSchool.getValidFrom_sdl_ssp();
            Long origValidTo_sdl_ssp = burSchool.getValidTo_sdl_ssp();
            Long origChar_publ_flg = burSchool.getChar_publ_flg();
            Long origChar_priv_sub_flg = burSchool.getChar_priv_sub_flg();
            Long origChar_priv_no_sub_flg = burSchool.getChar_priv_no_sub_flg();
            Boolean origIsSpecialSchool = burSchool.getIsSpecialSchool();

            burSchool.setVersion(now);
            burSchool.setCanton(burSchool.getBur_canton());
            burSchool.setLabel(burSchool.getBur_label());
            burSchool.setMunicipality(burSchool.getBur_municipality());
            burSchool.setActivityStatus(burSchool.getBur_activityStatus());
            burSchool.setCantonalCode_sdl(burSchool.getBur_cantonalCode_sdl());
            burSchool.set_sdl(burSchool.isBur_is_sdl());
            burSchool.setValidFrom_sdl_ssp(burSchool.getBur_validFrom_sdl_ssp());
            burSchool.setValidTo_sdl_ssp(burSchool.getBur_validTo_sdl_ssp());
            burSchool.setChar_publ_flg(burSchool.getBur_char_publ_flg());
            burSchool.setChar_priv_sub_flg(burSchool.getBur_char_priv_sub_flg());
            burSchool.setChar_priv_no_sub_flg(burSchool.getBur_char_priv_no_sub_flg());
            burSchool.setIsSpecialSchool(burSchool.getIsSpecialSchoolBur());
            if (burSchool.getSynchStatus_sdl() == CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
                // create eventual default config delivery
                if (defaultConfigDelivery != null) {
                    if (burSchool.getDeliveryCode() == null || burSchool.getDeliveryCode().equals("")) {
                        burSchool.setDeliveryCode(defaultConfigDelivery.getDeliveryCode());
                    }
                    if (((SdlBurSchool) burSchool).getConfigDeliveries() == null) {
                        ((SdlBurSchool) burSchool).setConfigDeliveries(new ArrayList<SdlConfigDelivery>());
                    }
                    ((SdlBurSchool) burSchool).getConfigDeliveries().add(defaultConfigDelivery);
                    _burSchoolRepository.updateAddedConfigDeliveryCodes((SdlBurSchool) burSchool, defaultConfigDelivery);
                }
            } else if (!isActiveSchool(burSchool)) {
                if (_burSchoolRepository.existsSchoolForBurSchool((SdlBurSchool) burSchool)) {
                    // TODO: hack, reset original values in dto, as hibernate does not a rollback
                    // but a commit of values in dto
                    burSchool.setVersion(origVersion);
                    burSchool.setCanton(origCanton);
                    burSchool.setMunicipality(origMunicipality);
                    burSchool.setActivityStatus(origActivityStatus);
                    burSchool.setCantonalCode_sdl(origCantonalCode_sdl);
                    burSchool.set_sdl(origIs_sdl);
                    burSchool.setValidFrom_sdl_ssp(origValidFrom_sdl_ssp);
                    burSchool.setValidTo_sdl_ssp(origValidTo_sdl_ssp);
                    burSchool.setChar_publ_flg(origChar_publ_flg);
                    burSchool.setChar_priv_sub_flg(origChar_priv_sub_flg);
                    burSchool.setChar_priv_no_sub_flg(origChar_priv_no_sub_flg);
                    burSchool.setIsSpecialSchoolBur(origIsSpecialSchool);

                    return false;
                }
                burSchool.setDeliveryCode(null);
            }
            burSchool.setSynchStatus_sdl(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
        }
        return true;
    }

    protected SdlConfigDelivery getActDefaultConfigDelivery(Long canton) {
        Calendar cal = new GregorianCalendar();
        long now = cal.get(Calendar.YEAR);

        List<SdlConfigDelivery> deliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(now, canton);
        for (SdlConfigDelivery delivery : deliveries) {
            if (delivery.getIsDefault()) {
                return delivery;
            }
        }
        return null;
    }

    @Override
    public boolean importBurSchool(BurSchool burSchool) {
        SdlConfigDelivery defaultConfigDelivery = null;
        if (burSchool.getSynchStatus_sdl() == CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
            defaultConfigDelivery = getActDefaultConfigDelivery(burSchool.getBur_canton());
        }
        return importBurSchool(burSchool, defaultConfigDelivery);
    }

    @Override
    public boolean importBurSchools(Long canton) {
        SdlConfigDelivery defaultConfigDelivery = getActDefaultConfigDelivery(canton);

        List<SdlBurSchool> schools = _burSchoolRepository.getBurSchools();
        if (canton > 0) {
            schools = schools.stream()
                    .filter(s -> canton.equals(s.getCanton()) || canton.equals(s.getBur_canton()))
                    .collect(Collectors.toList());
        }

        for (SdlBurSchool school : schools) {
            if (canton > 0L && !canton.equals(school.getCanton()) && !canton.equals(school.getBur_canton())) {
                continue;
            }

            if (school.getSynchStatus_sdl() != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED) {
                if (!importBurSchool(school, defaultConfigDelivery)) {
                    return false;
                } else {
                    SdlConfigDelivery configDelivery = null;

                    if (isActiveSchool(school)) {
                        // Search for original configDelivery (Changes of deliveryCode not propagated to server!)
                        // Eventual default configdelivery for new schools is already set and added in method importBurSchool
                        for (SdlConfigDelivery cd : school.getConfigDeliveries()) {
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
        burSchool.setSynchStatusBur(burSchool.getSynchStatus_sdl());
        burSchool.setNameBur(burSchool.getBur_label());
        burSchool.setCantonBur(burSchool.getBur_canton());
        burSchool.setMunicipalityBur(burSchool.getBur_municipality());
        burSchool.setValidFromBur(burSchool.getBur_validFrom_sdl_ssp());
        burSchool.setValidToBur(burSchool.getBur_validTo_sdl_ssp());
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
            burSchool.setBur_cantonalCode_sdl(burSchoolExt.getSdl_cantonal_id());
            burSchool.setBur_is_sdl(burSchoolExt.getStat_act_sdl_flg() != null && burSchoolExt.getStat_act_sdl_flg() == 1L);
            burSchool.setBur_validFrom_sdl_ssp(burSchoolExt.getStat_act_sdl_ssp_from());
            burSchool.setBur_validTo_sdl_ssp(burSchoolExt.getStat_act_sdl_ssp_to());
            // burSchool.setBur_cantonalCode_ssp(burSchoolExt.getSsp_cantonal_id());
            // burSchool.setBur_is_ssp(burSchoolExt.getStat_act_ssp_flag() != null
            // && burSchoolExt.getStat_act_ssp_flag() == 1L);
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
            burSchool.setBur_is_sdl(false);
        }

        if (burSchool.getSynchStatus_sdl() != CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
            Calendar cal = new GregorianCalendar();
            long now = cal.get(Calendar.YEAR);
            boolean isSdl = burSchool.is_sdl();
            boolean isBurSdl = burSchool.isBur_is_sdl();
            boolean isValid = (burSchool.getValidFrom_sdl_ssp() == null || burSchool.getValidFrom_sdl_ssp() <= now)
                    && (burSchool.getValidTo_sdl_ssp() == null || burSchool.getValidTo_sdl_ssp() >= now);
            boolean isBurValid = (burSchool.getBur_validFrom_sdl_ssp() == null || burSchool.getBur_validFrom_sdl_ssp() <= now)
                    && (burSchool.getBur_validTo_sdl_ssp() == null || burSchool.getBur_validTo_sdl_ssp() >= now);
            boolean isActive = burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                    || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                    || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN;
            boolean isBurActive = burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                    || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                    || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN;

            boolean isVisible = isSdl && isValid && isActive;
            boolean isBurVisible = isBurSdl && isBurValid && isBurActive;

            if (isVisible && !isBurVisible) {
                burSchool.setSynchStatus_sdl(CodegroupUtility.MEB_SYNCHSTATUS_INACTIVATED);
            } else if (!isVisible && isBurVisible) {
                burSchool.setSynchStatus_sdl(CodegroupUtility.MEB_SYNCHSTATUS_CHANGED);
            } else if (!isVisible && !isBurVisible) {
                burSchool.setSynchStatus_sdl(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
            } else {
                if (!BurSchoolServiceHelper.equalObjects(burSchool.getLabel(), burSchool.getBur_label())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getCanton(), burSchool.getBur_canton())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getCantonalCode_sdl(), burSchool.getBur_cantonalCode_sdl())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getMunicipality(), burSchool.getBur_municipality())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getValidFrom_sdl_ssp(), burSchool.getBur_validFrom_sdl_ssp())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getValidTo_sdl_ssp(), burSchool.getBur_validTo_sdl_ssp())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getActivityStatus(), burSchool.getBur_activityStatus())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_publ_flg(), burSchool.getBur_char_publ_flg())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_priv_sub_flg(), burSchool.getBur_char_priv_sub_flg())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_priv_no_sub_flg(), burSchool.getBur_char_priv_no_sub_flg())
                        || !BurSchoolServiceHelper.equalBooleans(burSchool.getIsSpecialSchool(), burSchool.getIsSpecialSchoolBur())) {
                    burSchool.setSynchStatus_sdl(CodegroupUtility.MEB_SYNCHSTATUS_CHANGED);
                } else {
                    burSchool.setSynchStatus_sdl(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
                }
            }
        }

        return burSchool.getSynchStatus_sdl();
    }

    @Override
    public void lockBurSchools() throws HibernateException {
        _burSchoolRepository.lockBurSchools();
    }

}
