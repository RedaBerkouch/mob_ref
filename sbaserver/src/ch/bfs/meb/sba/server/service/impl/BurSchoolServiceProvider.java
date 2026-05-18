/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;

import ch.bfs.meb.sba.server.integration.dto.SbaBurSchool;
import ch.bfs.meb.sba.server.integration.dto.SbaConfigDelivery;
import ch.bfs.meb.sba.server.integration.repository.IBurSchoolRepository;
import ch.bfs.meb.sba.server.integration.repository.IConfigDeliveryRepository;
import ch.bfs.meb.sba.server.integration.repository.IFilterRepository;
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

    protected SbaBurSchool initDto(SbaBurSchool school, Long version) {
        if (school != null) {
            for (SbaConfigDelivery configDelivery : school.getConfigDeliveries()) {
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

    protected SbaBurSchool initDto(SbaBurSchool school, List<Long> configDeliveryIds) {
        for (SbaConfigDelivery configDelivery : school.getConfigDeliveries()) {
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

    protected List<SbaBurSchool> initDto(List<SbaBurSchool> schools, Long version) {
        for (SbaBurSchool school : schools) {
            initDto(school, version);
        }
        return schools;
    }

    protected List<SbaBurSchool> initDto(List<SbaBurSchool> schools, List<Long> configDeliveryIds) {
        for (SbaBurSchool school : schools) {
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
        List<SbaConfigDelivery> configDeliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(version, canton);
        for (SbaConfigDelivery cd : configDeliveries) {
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
        if (burSchool instanceof SbaBurSchool) {
            // create new config deliveries for all config deliveries in templateVersion
            List<SbaConfigDelivery> newDeliveries = new ArrayList<SbaConfigDelivery>();
            SbaBurSchool school = (SbaBurSchool) burSchool;
            for (SbaConfigDelivery delivery : school.getConfigDeliveries()) {
                // there is a config delivery in templateVersion
                if (delivery.getVersion().equals(versionTemplate) && delivery.getCanton().equals(canton)) {
                    SbaConfigDelivery newDelivery = null;
                    for (SbaConfigDelivery d : school.getConfigDeliveries()) {
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
        return _burSchoolRepository.insertBurSchool(new SbaBurSchool(burSchool));
    }

    protected void addConfigDelivery(SbaBurSchool burSchool, ConfigDelivery configDelivery) {
        if (configDelivery instanceof SbaConfigDelivery) {
            burSchool.getConfigDeliveries().add((SbaConfigDelivery) configDelivery);
        } else {
            // should not happen
            burSchool.getConfigDeliveries().add(new SbaConfigDelivery(configDelivery));
        }
    }

    @Override
    public BurSchool updateSynchBurSchool(BurSchool burSchool) {
        return _burSchoolRepository.updateBurSchool((SbaBurSchool) burSchool);
    }

    @Override
    public BurSchool updateBurSchool(BurSchool burSchool, ConfigDelivery configDelivery) {
        SbaBurSchool origBurSchool = _burSchoolRepository.getBurSchoolById(burSchool.getSchoolId());
        SbaConfigDelivery origConfigDelivery = null;

        for (SbaConfigDelivery cd : origBurSchool.getConfigDeliveries()) {
            if (cd.getVersion().equals(burSchool.getVersion())) {
                origConfigDelivery = cd;
                break;
            }
        }

        SbaBurSchool newBurSchool = new SbaBurSchool(burSchool);
        newBurSchool.setConfigDeliveries(new ArrayList<SbaConfigDelivery>(origBurSchool.getConfigDeliveries()));

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
                SbaConfigDelivery cd = newBurSchool.getConfigDeliveries().get(i);
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
        _burSchoolRepository.deleteBurSchool(new SbaBurSchool(burSchool));
    }

    @Override
    public boolean isActiveSchool(BurSchool burSchool) {
        return burSchool.is_sba()
                && (burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                && (burSchool.getValidFrom_sba() == null || burSchool.getValidFrom_sba() <= burSchool.getVersion())
                && (burSchool.getValidTo_sba() == null || burSchool.getValidTo_sba() >= burSchool.getVersion());
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.service.impl.IBurSchoolServiceProvider#isVisibleSchool(ch.bfs.meb.server.commons.integration.dto.BurSchool)
     */
    @Override
    public boolean isVisibleSchool(BurSchool burSchool, Long version) {
        return (burSchool.is_sba()
                && (burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                        || burSchool.getActivityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                && (burSchool.getValidFrom_sba() == null || burSchool.getValidFrom_sba() <= version)
                && (burSchool.getValidTo_sba() == null || burSchool.getValidTo_sba() >= version))
                || (burSchool.isBur_is_sba()
                        && (burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE
                                || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                                || burSchool.getBur_activityStatus() == CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN)
                        && (burSchool.getBur_validFrom_sba() == null || burSchool.getBur_validFrom_sba() <= version)
                        && (burSchool.getBur_validTo_sba() == null || burSchool.getBur_validTo_sba() >= version));
    }

    protected boolean importBurSchool(BurSchool burSchool, SbaConfigDelivery defaultConfigDelivery) {
        if (burSchool.getSynchStatus_sba() != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED) {
            Calendar cal = new GregorianCalendar();
            long now = cal.get(Calendar.YEAR);

            // TODO: hack, remember original values for rollback case (see next "else if")
            Long origVersion = burSchool.getVersion();
            Long origCanton = burSchool.getCanton();
            Long origMunicipality = burSchool.getMunicipality();
            Long origActivityStatus = burSchool.getActivityStatus();
            String origCantonalCode_sba = burSchool.getCantonalCode_sba();
            boolean origIs_sba = burSchool.is_sba();
            Long origValidFrom_sba = burSchool.getValidFrom_sba();
            Long origValidTo_sba = burSchool.getValidTo_sba();
            Long origChar_publ_flg = burSchool.getChar_publ_flg();
            Long origChar_priv_sub_flg = burSchool.getChar_priv_sub_flg();
            Long origChar_priv_no_sub_flg = burSchool.getChar_priv_no_sub_flg();
            Boolean origIsSpecialSchool = burSchool.getIsSpecialSchool();

            burSchool.setVersion(now);
            burSchool.setCanton(burSchool.getBur_canton());
            burSchool.setLabel(burSchool.getBur_label());
            burSchool.setMunicipality(burSchool.getBur_municipality());
            burSchool.setActivityStatus(burSchool.getBur_activityStatus());
            burSchool.setCantonalCode_sba(burSchool.getBur_cantonalCode_sba());
            burSchool.set_sba(burSchool.isBur_is_sba());
            burSchool.setValidFrom_sba(burSchool.getBur_validFrom_sba());
            burSchool.setValidTo_sba(burSchool.getBur_validTo_sba());
            burSchool.setChar_publ_flg(burSchool.getBur_char_publ_flg());
            burSchool.setChar_priv_sub_flg(burSchool.getBur_char_priv_sub_flg());
            burSchool.setChar_priv_no_sub_flg(burSchool.getBur_char_priv_no_sub_flg());
            burSchool.setIsSpecialSchool(burSchool.getIsSpecialSchoolBur());
            if (burSchool.getSynchStatus_sba() == CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
                // create eventual default config delivery
                if (defaultConfigDelivery != null) {
                    if (burSchool.getDeliveryCode() == null || burSchool.getDeliveryCode().equals("")) {
                        burSchool.setDeliveryCode(defaultConfigDelivery.getDeliveryCode());
                    }
                    if (((SbaBurSchool) burSchool).getConfigDeliveries() == null) {
                        ((SbaBurSchool) burSchool).setConfigDeliveries(new ArrayList<SbaConfigDelivery>());
                    }
                    ((SbaBurSchool) burSchool).getConfigDeliveries().add(defaultConfigDelivery);
                }
            } else if (!isActiveSchool(burSchool)) {
                if (_burSchoolRepository.existsQualificationForBurSchool((SbaBurSchool) burSchool)) {
                    // TODO: hack, reset original values in dto, as hibernate does not a rollback
                    // but a commit of values in dto
                    burSchool.setVersion(origVersion);
                    burSchool.setCanton(origCanton);
                    burSchool.setMunicipality(origMunicipality);
                    burSchool.setActivityStatus(origActivityStatus);
                    burSchool.setCantonalCode_sba(origCantonalCode_sba);
                    burSchool.set_sba(origIs_sba);
                    burSchool.setValidFrom_sba(origValidFrom_sba);
                    burSchool.setValidTo_sba(origValidTo_sba);
                    burSchool.setChar_publ_flg(origChar_publ_flg);
                    burSchool.setChar_priv_sub_flg(origChar_priv_sub_flg);
                    burSchool.setChar_priv_no_sub_flg(origChar_priv_no_sub_flg);
                    burSchool.setIsSpecialSchoolBur(origIsSpecialSchool);

                    return false;
                }
                burSchool.setDeliveryCode(null);
            }
            burSchool.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
        }
        return true;
    }

    protected SbaConfigDelivery getActDefaultConfigDelivery(Long canton) {
        Calendar cal = new GregorianCalendar();
        long now = cal.get(Calendar.YEAR);

        List<SbaConfigDelivery> deliveries = _configDeliveryRepository.getConfigDeliveriesByVersionAndCanton(now, canton);
        for (SbaConfigDelivery delivery : deliveries) {
            if (delivery.getIsDefault()) {
                return delivery;
            }
        }
        return null;
    }

    @Override
    public boolean importBurSchool(BurSchool burSchool) {
        SbaConfigDelivery defaultConfigDelivery = null;
        if (burSchool.getSynchStatus_sba() == CodegroupUtility.MEB_SYNCHSTATUS_NEW) {
            defaultConfigDelivery = getActDefaultConfigDelivery(burSchool.getBur_canton());
        }
        return importBurSchool(burSchool, defaultConfigDelivery);
    }

    @Override
    public boolean importBurSchools(Long canton) {
        SbaConfigDelivery defaultConfigDelivery = getActDefaultConfigDelivery(canton);

        List<SbaBurSchool> schools = _burSchoolRepository.getBurSchools();
        if (canton > 0) {
            schools = schools.stream()
                    .filter(s -> canton.equals(s.getCanton()) || canton.equals(s.getBur_canton()))
                    .collect(Collectors.toList());
        }
        for (SbaBurSchool school : schools) {
            if (canton > 0L && !canton.equals(school.getCanton()) && !canton.equals(school.getBur_canton())) {
                continue;
            }

            if (school.getSynchStatus_sba() != CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED) {
                if (!importBurSchool(school, defaultConfigDelivery)) {
                    return false;
                } else {
                    SbaConfigDelivery configDelivery = null;

                    if (isActiveSchool(school)) {
                        // Search for original configDelivery (Changes of deliveryCode not propagated to server!)
                        // Eventual default configdelivery for new schools is already set and added in method importBurSchool
                        for (SbaConfigDelivery cd : school.getConfigDeliveries()) {
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
        burSchool.setSynchStatusBur(burSchool.getSynchStatus_sba());
        burSchool.setNameBur(burSchool.getBur_label());
        burSchool.setCantonBur(burSchool.getBur_canton());
        burSchool.setMunicipalityBur(burSchool.getBur_municipality());
        burSchool.setValidFromBur(burSchool.getBur_validFrom_sba());
        burSchool.setValidToBur(burSchool.getBur_validTo_sba());
    }

    @Override
    public void initSynchData(List<BurSchool> burSchools) {
        for (BurSchool burSchool : burSchools) {
            initSynchData(burSchool);
        }
    }

    @Override
    public long calculateSynchStatus(BurSchool burSchool, BurSchoolExt burSchoolExt) {

        if (burSchool == null) {
            throw new IllegalArgumentException("burSchool must not be null");
        }

        //  Fix NPE: synchStatus_sba peut être NULL en DB / DTO
        if (burSchool.getSynchStatus_sba() == null) {
            burSchool.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
        }

        if (burSchoolExt != null) {
            burSchool.setBur_canton(burSchoolExt.getCantonCode());
            burSchool.setBur_label(burSchoolExt.getName_tx());
            burSchool.setBur_municipality(burSchoolExt.getMunicipality_cd());
            burSchool.setBur_activityStatus(burSchoolExt.getUnit_status_cd());

            burSchool.setBur_cantonalCode_sba(burSchoolExt.getMatu_cantonal_id());
            burSchool.setBur_is_sba(burSchoolExt.getStat_act_matu_flg() != null && burSchoolExt.getStat_act_matu_flg() == 1L);
            burSchool.setBur_validFrom_sba(burSchoolExt.getStat_act_matu_from());
            burSchool.setBur_validTo_sba(burSchoolExt.getStat_act_matu_to());
            burSchool.setBur_char_publ_flg(BurSchoolServiceHelper.zeroForNull(burSchoolExt.getChar_publ_flg()));
            burSchool.setBur_char_priv_sub_flg(BurSchoolServiceHelper.zeroForNull(burSchoolExt.getChar_priv_sub_flg()));
            burSchool.setBur_char_priv_no_sub_flg(BurSchoolServiceHelper.zeroForNull(burSchoolExt.getChar_priv_no_sub_flg()));
            burSchool.setIsSpecialSchoolBur(burSchoolExt.getIsSpecialSchool());
        } else {
            burSchool.setBur_is_sba(false);
        }

        //  Fix NPE: éviter "Long != long" (déboxing). Comparaison null-safe
        if (!Objects.equals(burSchool.getSynchStatus_sba(), CodegroupUtility.MEB_SYNCHSTATUS_NEW)) {

            Calendar cal = new GregorianCalendar();
            long now = cal.get(Calendar.YEAR);

            boolean isSba = burSchool.is_sba();
            boolean isBurSba = burSchool.isBur_is_sba();

            boolean isValid = (burSchool.getValidFrom_sba() == null || burSchool.getValidFrom_sba() <= now)
                    && (burSchool.getValidTo_sba() == null || burSchool.getValidTo_sba() >= now);

            boolean isBurValid = (burSchool.getBur_validFrom_sba() == null || burSchool.getBur_validFrom_sba() <= now)
                    && (burSchool.getBur_validTo_sba() == null || burSchool.getBur_validTo_sba() >= now);

            Long activityStatus = burSchool.getActivityStatus();
            boolean isActive =
                    Objects.equals(activityStatus, CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE)
                            || Objects.equals(activityStatus, CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL)
                            || Objects.equals(activityStatus, CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN);

            Long burActivityStatus = burSchool.getBur_activityStatus();
            boolean isBurActive =
                    Objects.equals(burActivityStatus, CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE)
                            || Objects.equals(burActivityStatus, CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL)
                            || Objects.equals(burActivityStatus, CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN);

            boolean isVisible = isSba && isValid && isActive;
            boolean isBurVisible = isBurSba && isBurValid && isBurActive;

            if (isVisible && !isBurVisible) {
                burSchool.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_INACTIVATED);
            } else if (!isVisible && isBurVisible) {
                burSchool.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_CHANGED);
            } else if (!isVisible && !isBurVisible) {
                burSchool.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
            } else {
                if (!BurSchoolServiceHelper.equalObjects(burSchool.getLabel(), burSchool.getBur_label())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getCanton(), burSchool.getBur_canton())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getCantonalCode_sba(), burSchool.getBur_cantonalCode_sba())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getMunicipality(), burSchool.getBur_municipality())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getValidFrom_sba(), burSchool.getBur_validFrom_sba())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getValidTo_sba(), burSchool.getBur_validTo_sba())
                        || !BurSchoolServiceHelper.equalObjects(burSchool.getActivityStatus(), burSchool.getBur_activityStatus())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_publ_flg(), burSchool.getBur_char_publ_flg())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_priv_sub_flg(), burSchool.getBur_char_priv_sub_flg())
                        || !BurSchoolServiceHelper.equalLongs(burSchool.getChar_priv_no_sub_flg(), burSchool.getBur_char_priv_no_sub_flg())
                        || !BurSchoolServiceHelper.equalBooleans(burSchool.getIsSpecialSchool(), burSchool.getIsSpecialSchoolBur())) {
                    burSchool.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_CHANGED);
                } else {
                    burSchool.setSynchStatus_sba(CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED);
                }
            }
        }

        //  Fix NPE return: si jamais quelqu’un remet à null, on protège
        Long status = burSchool.getSynchStatus_sba();
        return status != null ? status : CodegroupUtility.MEB_SYNCHSTATUS_UNCHANGED;
    }

    @Override
    public void lockBurSchools() throws HibernateException {
        _burSchoolRepository.lockBurSchools();
    }
}
